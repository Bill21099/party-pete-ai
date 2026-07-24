package com.partypeteai.chat;

import org.junit.Test;
import static org.junit.Assert.*;

public class StreamingJsonAnswerDecoderTest
{
	@Test public void streamsOnlyAnswerValueAfterScopeIsKnown()
	{
		StreamingJsonAnswerDecoder decoder = new StreamingJsonAnswerDecoder();
		assertEquals("", decoder.accept("{\"isOsrs"));
		assertEquals("Use ", decoder.accept("Related\":true,\"answer\":\"Use "));
		assertEquals("ranged.\n", decoder.accept("ranged.\\n"));
		assertEquals("", decoder.accept("\"}"));
		assertTrue(decoder.hasAnswer());
		assertEquals("Use ranged.", decoder.getAnswer());
	}

	@Test public void neverStreamsUnrelatedGeneratedAnswer()
	{
		StreamingJsonAnswerDecoder decoder = new StreamingJsonAnswerDecoder();
		assertEquals("", decoder.accept("{\"isOsrsRelated\":false,\"answer\":\"private generated answer\"}"));
		assertFalse(decoder.hasAnswer());
	}

	@Test public void preservesDecodedAnswerWhenEnvelopeIsTruncated()
	{
		StreamingJsonAnswerDecoder decoder = new StreamingJsonAnswerDecoder();
		assertEquals("A useful answer.", decoder.accept("{\"isOsrsRelated\":true,\"answer\":\"A useful answer."));
		assertTrue(decoder.hasAnswer());
		assertEquals("A useful answer.", decoder.getAnswer());
	}
}
