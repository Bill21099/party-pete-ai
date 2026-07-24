package com.partypeteai.chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConversationService
{
	private final List<ChatMessage> messages = new ArrayList<>();

	public synchronized void add(ChatMessage message)
	{
		messages.add(message);
	}

	public synchronized List<ChatMessage> recent(int count)
	{
		int from = Math.max(0, messages.size() - Math.min(count, messages.size()));
		return Collections.unmodifiableList(new ArrayList<>(messages.subList(from, messages.size())));
	}

	public synchronized List<ChatMessage> all() { return recent(Integer.MAX_VALUE); }
	public synchronized void clear() { messages.clear(); }
	public synchronized int size() { return messages.size(); }
}
