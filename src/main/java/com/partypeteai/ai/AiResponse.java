package com.partypeteai.ai;

public final class AiResponse
{
	private final String content;
	private final String requestId;
	public AiResponse(String content, String requestId) { this.content = content; this.requestId = requestId; }
	public String getContent() { return content; }
	public String getRequestId() { return requestId; }
}

