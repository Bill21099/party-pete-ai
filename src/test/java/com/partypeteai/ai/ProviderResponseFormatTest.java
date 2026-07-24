package com.partypeteai.ai;

import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import org.junit.Test;
import static org.junit.Assert.*;

public class ProviderResponseFormatTest
{
	private final Gson gson = new Gson();
	private final OkHttpClient http = new OkHttpClient();
	private final String structured = "{\"isOsrsRelated\":true,\"answer\":\"ok\"}";

	@Test public void parsesOpenAi() throws Exception
	{
		String json = "{\"output\":[{\"type\":\"message\",\"content\":[{\"type\":\"output_text\",\"text\":" + gson.toJson(structured) + "}]}]}";
		assertEquals(structured, new OpenAiProvider(http, gson).responseText(json));
	}
	@Test public void parsesGemini() throws Exception
	{
		String json = "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":" + gson.toJson(structured) + "}]},\"finishReason\":\"STOP\"}]}";
		assertEquals(structured, new GeminiProvider(http, gson).responseText(json));
	}
	@Test public void ignoresGeminiMetadataOnlyStreamEvent() throws Exception
	{
		assertEquals("", new GeminiProvider(http, gson).streamDelta("{\"usageMetadata\":{\"totalTokenCount\":42}}"));
		assertEquals("", new GeminiProvider(http, gson).streamDelta("{\"candidates\":[{\"finishReason\":\"STOP\"}]}"));
	}
	@Test public void parsesDeepSeek() throws Exception
	{
		String json = "{\"choices\":[{\"message\":{\"content\":" + gson.toJson(structured) + "},\"finish_reason\":\"stop\"}]}";
		assertEquals(structured, new DeepSeekProvider(http, gson).responseText(json));
	}
	@Test public void parsesOpenRouter() throws Exception
	{
		String json = "{\"choices\":[{\"message\":{\"content\":" + gson.toJson(structured) + "}}]}";
		assertEquals(structured, new OpenRouterProvider(http, gson).responseText(json));
	}
}
