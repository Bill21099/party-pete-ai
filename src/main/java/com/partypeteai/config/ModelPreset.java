package com.partypeteai.config;

public enum ModelPreset
{
	AUTO("Provider default"),
	OPENAI_SOL("OpenAI: GPT-5.6 Sol"),
	OPENAI_TERRA("OpenAI: GPT-5.6 Terra"),
	OPENAI_LUNA("OpenAI: GPT-5.6 Luna"),
	GEMINI_25_FLASH_LITE("Gemini 2.5 Flash Lite"),
	GEMINI_FLASH("Gemini 3.5 Flash"),
	GEMINI_PRO("Gemini 3.5 Pro"),
	DEEPSEEK_FLASH("DeepSeek V4 Flash"),
	DEEPSEEK_PRO("DeepSeek V4 Pro"),
	OPENROUTER_AUTO("OpenRouter Auto"),
	CUSTOM("Custom model");

	private final String label;
	ModelPreset(String label) { this.label = label; }
	@Override public String toString() { return label; }
}
