package com.partypeteai.config;

public enum ProviderType
{
	GEMINI("Google Gemini"),
	OPENAI("OpenAI"),
	DEEPSEEK("DeepSeek"),
	OPENROUTER("OpenRouter");

	private final String label;
	ProviderType(String label) { this.label = label; }
	@Override public String toString() { return label; }
}

