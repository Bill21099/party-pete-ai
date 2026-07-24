package com.partypeteai;

import com.google.gson.Gson;
import com.google.inject.Provides;
import com.partypeteai.ai.AiProvider;
import com.partypeteai.ai.AiProviderFactory;
import com.partypeteai.ai.AiRequest;
import com.partypeteai.ai.AiResponse;
import com.partypeteai.ai.ProviderErrorMapper;
import com.partypeteai.ai.ProviderException;
import com.partypeteai.chat.ChatMessage;
import com.partypeteai.chat.ChatRole;
import com.partypeteai.chat.ConversationService;
import com.partypeteai.chat.OsrsScopeResult;
import com.partypeteai.chat.PromptBuilder;
import com.partypeteai.chat.ResponseParser;
import com.partypeteai.chat.SafetyPolicy;
import com.partypeteai.config.ModelCatalog;
import com.partypeteai.context.PlayerContextService;
import com.partypeteai.knowledge.GrandExchangePriceService;
import com.partypeteai.knowledge.OsrsWikiKnowledgeSource;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import okhttp3.OkHttpClient;

@Slf4j
@PluginDescriptor(
	name = "Party Pete AI",
	description = "Ask Party Pete questions about Old School RuneScape",
	tags = {"ai", "assistant", "chat", "guide", "party pete", "osrs", "help"}
)
public class PartyPeteAiPlugin extends Plugin
{
	@Inject private Client client;
	@Inject private ClientThread clientThread;
	@Inject private ClientToolbar clientToolbar;
	@Inject private PartyPeteAiConfig config;
	@Inject private ConfigManager configManager;
	@Inject private OkHttpClient http;
	@Inject private Gson gson;

	private final ConversationService conversation = new ConversationService();
	private final AtomicBoolean requestRunning = new AtomicBoolean();
	private PartyPeteAiPanel panel;
	private NavigationButton navigation;
	private AiProvider activeProvider;
	private PlayerContextService playerContext;
	private OsrsWikiKnowledgeSource wikiKnowledge;
	private GrandExchangePriceService gePrices;

	@Override
	protected void startUp()
	{
		playerContext = new PlayerContextService(client, clientThread);
		wikiKnowledge = new OsrsWikiKnowledgeSource(http, gson);
		gePrices = new GrandExchangePriceService(http, gson);
		panel = new PartyPeteAiPanel(this, config);
		BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/icon.png");
		navigation = NavigationButton.builder()
			.tooltip("Party Pete AI")
			.icon(icon)
			.priority(6)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navigation);
		log.debug("Party Pete AI started");
	}

	@Override
	protected void shutDown()
	{
		cancelRequest();
		if (panel != null) panel.shutdown();
		if (navigation != null) clientToolbar.removeNavigation(navigation);
		conversation.clear();
		activeProvider = null;
		navigation = null;
		panel = null;
		playerContext = null;
		wikiKnowledge = null;
		gePrices = null;
		log.debug("Party Pete AI stopped");
	}

	void submitQuestion(String question)
	{
		if (requestRunning.get() || question == null || question.trim().isEmpty()) return;
		String clean = question.trim();
		panel.addUser(clean);
		if (SafetyPolicy.isCheatingRequest(clean))
		{
			conversation.add(new ChatMessage(ChatRole.USER, clean));
			conversation.add(new ChatMessage(ChatRole.ASSISTANT, SafetyPolicy.CHEATING));
			panel.addAssistant(SafetyPolicy.CHEATING);
			return;
		}
		if (config.apiKey() == null || config.apiKey().trim().isEmpty())
		{
			panel.addAssistant(ProviderErrorMapper.userMessage(new ProviderException(ProviderException.Kind.MISSING_KEY, "Missing key")));
			return;
		}

		List<ChatMessage> history = conversation.recent(config.memory().getMessages());
		conversation.add(new ChatMessage(ChatRole.USER, clean));
		requestRunning.set(true);
		panel.setBusy(true);
		activeProvider = new AiProviderFactory(http, gson).create(config.provider());
		final AiProvider provider = activeProvider;
		final String apiKey = config.apiKey();
		final String model = ModelCatalog.resolve(config.provider(), config.modelPreset(), config.customModel());
		java.util.concurrent.CompletableFuture<String> wikiFuture = config.wikiGrounding()
			? wikiKnowledge.search(clean).thenApply(OsrsWikiKnowledgeSource::format).exceptionally(error -> "")
			: java.util.concurrent.CompletableFuture.completedFuture("");
		java.util.concurrent.CompletableFuture<String> priceFuture = config.liveGePrices()
			? gePrices.findPrices(clean).exceptionally(error -> "")
			: java.util.concurrent.CompletableFuture.completedFuture("");
		playerContext.capture(config.includeAccountContext())
			.thenCombine(wikiFuture, ContextAndKnowledge::new)
			.thenCombine(priceFuture, (bundle, prices) -> bundle.withPrices(prices))
			.thenCompose(bundle ->
			{
				String prompt = new PromptBuilder().build(history, clean, bundle.context, bundle.knowledge, config.responseLength(), config.showPersonality());
				AiRequest request = new AiRequest(apiKey, model, prompt, config.responseLength().getTokens(), config.creativity().getTemperature(), config.requestTimeout());
				return provider.send(request);
			})
			.whenComplete((response, error) -> completeRequest(provider, response, error));
	}

	private void completeRequest(AiProvider provider, AiResponse response, Throwable error)
	{
		if (activeProvider != provider) return;
		if (error != null)
		{
			Throwable cause = unwrap(error);
			ProviderException providerError = cause instanceof ProviderException
				? (ProviderException) cause
				: new ProviderException(ProviderException.Kind.UNAVAILABLE, "Provider failure");
			log.debug("Provider {} failed with kind {}, status {}, cause {}", provider.getType(), providerError.getKind(),
				providerError.getStatusCode(), cause.getClass().getName());
			finishWithMessage(ProviderErrorMapper.userMessage(providerError), providerError.getKind() != ProviderException.Kind.CANCELLED);
			return;
		}
		try
		{
			OsrsScopeResult parsed = new ResponseParser(gson).parse(response.getContent());
			String answer = parsed.isOsrsRelated() ? parsed.getAnswer() : SafetyPolicy.OUT_OF_SCOPE;
			if (SafetyPolicy.isCheatingRequest(answer)) answer = SafetyPolicy.CHEATING;
			log.debug("Provider {} completed request id {}", provider.getType(), response.getRequestId());
			finishWithMessage(answer, false);
		}
		catch (ProviderException e)
		{
			log.debug("Provider {} returned invalid structured output ({}, {} characters)", provider.getType(), e.getKind(),
				response.getContent() == null ? 0 : response.getContent().length());
			finishWithMessage(ProviderErrorMapper.userMessage(e), true);
		}
	}

	private void finishWithMessage(String message, boolean error)
	{
		conversation.add(new ChatMessage(ChatRole.ASSISTANT, message));
		requestRunning.set(false);
		activeProvider = null;
		SwingUtilities.invokeLater(() ->
		{
			if (panel == null) return;
			panel.setBusy(false);
			panel.finishStream(message);
			if (error) panel.showProviderError();
		});
	}

	void cancelRequest()
	{
		AiProvider provider = activeProvider;
		if (provider != null) provider.cancelActiveRequest();
	}

	void clearConversation()
	{
		cancelRequest();
		requestRunning.set(false);
		activeProvider = null;
		conversation.clear();
		if (panel != null) panel.clear();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!PartyPeteAiConfig.GROUP.equals(event.getGroup())) return;
		if ("clearConversation".equals(event.getKey()) && config.clearConversation())
		{
			clearConversation();
			configManager.setConfiguration(PartyPeteAiConfig.GROUP, "clearConversation", false);
		}
		else if ("testConnection".equals(event.getKey()) && config.testConnection())
		{
			submitQuestion("Confirm that you can answer a simple question about Old School RuneScape in one short sentence.");
			configManager.setConfiguration(PartyPeteAiConfig.GROUP, "testConnection", false);
		}
		else
		{
			cancelRequest();
			if (panel != null) panel.refreshConfiguration();
		}
	}

	private static Throwable unwrap(Throwable error)
	{
		Throwable value = error;
		while ((value instanceof CompletionException || value instanceof java.util.concurrent.ExecutionException) && value.getCause() != null) value = value.getCause();
		return value;
	}

	private static final class ContextAndKnowledge
	{
		private final String context;
		private final String knowledge;
		private ContextAndKnowledge(String context, String knowledge) { this.context = context; this.knowledge = knowledge; }
		private ContextAndKnowledge withPrices(String prices)
		{
			String combined = knowledge == null || knowledge.isEmpty() ? prices :
				prices == null || prices.isEmpty() ? knowledge : knowledge + "\n\n" + prices;
			return new ContextAndKnowledge(context, combined == null ? "" : combined);
		}
	}

	@Provides
	PartyPeteAiConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PartyPeteAiConfig.class);
	}
}
