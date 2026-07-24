package com.partypeteai.ai;

import com.google.gson.Gson;
import com.partypeteai.config.ProviderType;
import okhttp3.OkHttpClient;

public class AiProviderFactory
{
	private final OkHttpClient http;
	private final Gson gson;
	public AiProviderFactory(OkHttpClient http, Gson gson) { this.http = http; this.gson = gson; }
	public AiProvider create(ProviderType type)
	{
		switch (type)
		{
			case GEMINI: return new GeminiProvider(http, gson);
			case OPENAI: return new OpenAiProvider(http, gson);
			case DEEPSEEK: return new DeepSeekProvider(http, gson);
			case OPENROUTER: return new OpenRouterProvider(http, gson);
			default: throw new IllegalArgumentException("Unsupported provider");
		}
	}
}

