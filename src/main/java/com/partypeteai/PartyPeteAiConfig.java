package com.partypeteai;

import com.partypeteai.config.CreativityLevel;
import com.partypeteai.config.MemoryLength;
import com.partypeteai.config.ModelPreset;
import com.partypeteai.config.ProviderType;
import com.partypeteai.config.ResponseLength;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup(PartyPeteAiConfig.GROUP)
public interface PartyPeteAiConfig extends Config
{
	String GROUP = "party-pete-ai";

	@ConfigItem(keyName = "provider", name = "Provider", description = "AI provider that receives your requests", position = 0)
	default ProviderType provider() { return ProviderType.OPENROUTER; }

	@ConfigItem(keyName = "apiKey", name = "API key", description = "Your own provider API key. You supply, manage, and may be charged for this key. RuneLite config storage is not claimed to be encrypted.", secret = true, position = 1)
	default String apiKey() { return ""; }

	@ConfigItem(keyName = "modelPreset", name = "Model", description = "Choose a provider default, preset, or custom model ID", position = 2)
	default ModelPreset modelPreset() { return ModelPreset.AUTO; }

	@ConfigItem(keyName = "customModel", name = "Custom model ID", description = "Used only when Model is set to Custom model", position = 3)
	default String customModel() { return ""; }

	@ConfigItem(keyName = "responseLength", name = "Response length", description = "Maximum answer detail", position = 4)
	default ResponseLength responseLength() { return ResponseLength.NORMAL; }

	@ConfigItem(keyName = "creativity", name = "Creativity", description = "Lower values favour factual consistency", position = 5)
	default CreativityLevel creativity() { return CreativityLevel.PRECISE; }

	@ConfigItem(keyName = "memory", name = "Conversation memory", description = "Messages included in each request; Full session keeps the complete in-memory chat", position = 6)
	default MemoryLength memory() { return MemoryLength.LAST_6; }

	@ConfigItem(keyName = "includeAccountContext", name = "Include account context", description = "Opt in to sending display name, skill levels, total level, login state and region ID to the selected provider", warning = "This feature submits your IP address to a 3rd-party server not controlled or verified by RuneLite developers", position = 7)
	default boolean includeAccountContext() { return false; }

	@ConfigItem(keyName = "wikiGrounding", name = "Live OSRS Wiki grounding", description = "Search the OSRS Wiki for relevant current information before answering", warning = "This feature submits your IP address to a 3rd-party server not controlled or verified by RuneLite developers", position = 8)
	default boolean wikiGrounding() { return false; }

	@ConfigItem(keyName = "liveGePrices", name = "Live Grand Exchange prices", description = "Look up matching item prices through the OSRS Wiki price API", warning = "This feature submits your IP address to a 3rd-party server not controlled or verified by RuneLite developers", position = 9)
	default boolean liveGePrices() { return false; }

	@ConfigItem(keyName = "showStarterQuestions", name = "Show starter questions", description = "Show suggestions in an empty chat", position = 10)
	default boolean showStarterQuestions() { return true; }

	@ConfigItem(keyName = "showPersonality", name = "Party Pete personality", description = "Allow a small amount of festive character voice", position = 11)
	default boolean showPersonality() { return true; }

	@ConfigItem(keyName = "allowSafeLinks", name = "Allow safe links", description = "Display clickable HTTPS links from replies", position = 12)
	default boolean allowSafeLinks() { return true; }

	@Range(min = 10, max = 120)
	@ConfigItem(keyName = "requestTimeout", name = "Request timeout", description = "Timeout in seconds", position = 13)
	default int requestTimeout() { return 45; }

	@ConfigItem(keyName = "clearConversation", name = "Clear conversation", description = "Clear in-memory chat history", position = 14)
	default boolean clearConversation() { return false; }

	@ConfigItem(keyName = "testConnection", name = "Test provider", description = "Send a small OSRS-only test request", position = 15)
	default boolean testConnection() { return false; }
}
