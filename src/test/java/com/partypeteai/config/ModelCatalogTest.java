package com.partypeteai.config;

import org.junit.Test;
import static org.junit.Assert.*;

public class ModelCatalogTest
{
	@Test public void resolvesGeminiFlashLitePreset()
	{
		assertEquals("gemini-2.5-flash-lite",
			ModelCatalog.resolve(ProviderType.GEMINI, ModelPreset.GEMINI_25_FLASH_LITE, ""));
	}
}
