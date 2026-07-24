package com.partypeteai.chat;

import com.google.gson.Gson;
import com.partypeteai.ai.ProviderException;
import org.junit.Test;
import static org.junit.Assert.*;

public class ResponseParserTest
{
	private final ResponseParser parser = new ResponseParser(new Gson());

	@Test public void parsesOsrsAnswer() throws Exception
	{
		OsrsScopeResult result = parser.parse("{\"isOsrsRelated\":true,\"answer\":\"Use ranged.\"}");
		assertTrue(result.isOsrsRelated()); assertEquals("Use ranged.", result.getAnswer());
	}
	@Test public void parsesUnrelatedAnswer() throws Exception
	{
		assertFalse(parser.parse("{\"isOsrsRelated\":false,\"answer\":\"discard me\"}").isOsrsRelated());
	}
	@Test public void parsesMarkdownFence() throws Exception
	{
		assertEquals("Barrows", parser.parse("```json\n{\"isOsrsRelated\":true,\"answer\":\"Barrows\"}\n```").getAnswer());
	}
	@Test public void extractsJsonSurroundedByProviderText() throws Exception
	{
		assertEquals("Barrows", parser.parse("Result follows: {\"isOsrsRelated\":true,\"answer\":\"Barrows\"} done").getAnswer());
	}
	@Test public void recoversAnswerFromTruncatedJsonEnvelope() throws Exception
	{
		assertEquals("Use ranged.", parser.parse("{\"isOsrsRelated\":true,\"answer\":\"Use ranged.").getAnswer());
	}
	@Test(expected = ProviderException.class) public void rejectsMalformed() throws Exception { parser.parse("not json"); }
	@Test(expected = ProviderException.class) public void rejectsEmptyAnswer() throws Exception { parser.parse("{\"isOsrsRelated\":true,\"answer\":\"\"}"); }
}
