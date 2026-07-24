package com.partypeteai.ai;

public class ProviderException extends Exception
{
	public enum Kind { MISSING_KEY, AUTHENTICATION, RATE_LIMIT, INVALID_MODEL, TIMEOUT, CANCELLED, UNAVAILABLE, SAFETY_REFUSAL, MALFORMED_RESPONSE, EMPTY_RESPONSE, OTHER }
	private final Kind kind;
	private final int statusCode;
	public ProviderException(Kind kind, String message) { this(kind, message, -1); }
	public ProviderException(Kind kind, String message, int statusCode) { super(message); this.kind = kind; this.statusCode = statusCode; }
	public Kind getKind() { return kind; }
	public int getStatusCode() { return statusCode; }
}

