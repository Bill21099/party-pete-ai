package com.partypeteai.chat;

import java.util.Objects;

public final class ChatMessage
{
	private final ChatRole role;
	private final String content;

	public ChatMessage(ChatRole role, String content)
	{
		this.role = Objects.requireNonNull(role);
		this.content = Objects.requireNonNull(content);
	}
	public ChatRole getRole() { return role; }
	public String getContent() { return content; }
}

