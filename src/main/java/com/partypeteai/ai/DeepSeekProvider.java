package com.partypeteai.ai;

import com.google.gson.Gson;
import com.partypeteai.config.ProviderType;
import com.partypeteai.util.ProviderHosts;
import okhttp3.OkHttpClient;

public class DeepSeekProvider extends CompatibleChatProvider
{
	public DeepSeekProvider(OkHttpClient http, Gson gson) { super(http, gson); }
	@Override public ProviderType getType() { return ProviderType.DEEPSEEK; }
	@Override protected String endpoint(AiRequest request) { return ProviderHosts.requireAllowed(getType(), "https://api.deepseek.com/chat/completions"); }
}
