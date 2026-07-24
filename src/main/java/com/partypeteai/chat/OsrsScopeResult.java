package com.partypeteai.chat;

public final class OsrsScopeResult
{
	private final boolean osrsRelated;
	private final String answer;
	public OsrsScopeResult(boolean osrsRelated, String answer) { this.osrsRelated = osrsRelated; this.answer = answer; }
	public boolean isOsrsRelated() { return osrsRelated; }
	public String getAnswer() { return answer; }
}

