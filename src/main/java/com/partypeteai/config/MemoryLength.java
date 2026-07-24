package com.partypeteai.config;

public enum MemoryLength
{
	LAST_2(2), LAST_6(6), LAST_10(10), FULL_SESSION(Integer.MAX_VALUE);
	private final int messages;
	MemoryLength(int messages) { this.messages = messages; }
	public int getMessages() { return messages; }
}
