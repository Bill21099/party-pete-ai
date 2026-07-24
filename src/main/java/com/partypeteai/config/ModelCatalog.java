package com.partypeteai.config;

import java.util.EnumMap;
import java.util.Map;

public final class ModelCatalog
{
	private static final Map<ProviderType, String> DEFAULTS = new EnumMap<>(ProviderType.class);
	static
	{
		DEFAULTS.put(ProviderType.OPENAI, "gpt-5.6-terra");
		DEFAULTS.put(ProviderType.GEMINI, "gemini-3.5-flash");
		DEFAULTS.put(ProviderType.DEEPSEEK, "deepseek-v4-flash");
		DEFAULTS.put(ProviderType.OPENROUTER, "openrouter/auto");
	}

	private ModelCatalog() {}

	public static String resolve(ProviderType provider, ModelPreset preset, String custom)
	{
		if (preset == ModelPreset.CUSTOM)
		{
			return custom == null ? "" : custom.trim();
		}
		switch (preset)
		{
			case OPENAI_SOL: return "gpt-5.6-sol";
			case OPENAI_TERRA: return "gpt-5.6-terra";
			case OPENAI_LUNA: return "gpt-5.6-luna";
			case GEMINI_25_FLASH_LITE: return "gemini-2.5-flash-lite";
			case GEMINI_FLASH: return "gemini-3.5-flash";
			case GEMINI_PRO: return "gemini-3.5-pro";
			case DEEPSEEK_FLASH: return "deepseek-v4-flash";
			case DEEPSEEK_PRO: return "deepseek-v4-pro";
			case OPENROUTER_AUTO: return "openrouter/auto";
			default: return DEFAULTS.get(provider);
		}
	}
}
