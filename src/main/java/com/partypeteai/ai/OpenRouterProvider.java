package com.partypeteai.ai;

import com.google.gson.Gson;
import com.partypeteai.config.ProviderType;
import com.partypeteai.util.ProviderHosts;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class OpenRouterProvider extends CompatibleChatProvider
{
	public OpenRouterProvider(OkHttpClient http, Gson gson) { super(http, gson); }
	@Override public ProviderType getType() { return ProviderType.OPENROUTER; }
	@Override protected String endpoint(AiRequest request) { return ProviderHosts.requireAllowed(getType(), "https://openrouter.ai/api/v1/chat/completions"); }
	@Override protected void addAuthentication(Request.Builder builder, AiRequest request)
	{
		super.addAuthentication(builder, request);
		builder.header("X-Title", "Party Pete AI RuneLite Plugin");
	}
}
