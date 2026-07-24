package com.partypeteai.chat;

import org.junit.Test;
import static org.junit.Assert.*;

public class ConversationServiceTest
{
	@Test public void truncatesRequestedHistory()
	{
		ConversationService service = new ConversationService();
		for (int i = 0; i < 12; i++) service.add(new ChatMessage(ChatRole.USER, Integer.toString(i)));
		assertEquals(6, service.recent(6).size());
		assertEquals("6", service.recent(6).get(0).getContent());
	}
	@Test public void fullSessionIsNotCapped()
	{
		ConversationService service = new ConversationService();
		for (int i = 0; i < 100; i++) service.add(new ChatMessage(ChatRole.USER, "x"));
		assertEquals(100, service.all().size());
	}
}
