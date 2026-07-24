package com.partypeteai.ai;

import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;

public final class ProviderErrorMapper
{
	private ProviderErrorMapper() {}
	public static ProviderException fromStatus(int status, String safeMessage)
	{
		if (status == 401 || status == 403) return new ProviderException(ProviderException.Kind.AUTHENTICATION, safeMessage, status);
		if (status == 429) return new ProviderException(ProviderException.Kind.RATE_LIMIT, safeMessage, status);
		if (status == 400 || status == 404) return new ProviderException(ProviderException.Kind.INVALID_MODEL, safeMessage, status);
		if (status >= 500) return new ProviderException(ProviderException.Kind.UNAVAILABLE, safeMessage, status);
		return new ProviderException(ProviderException.Kind.OTHER, safeMessage, status);
	}
	public static ProviderException fromIo(Throwable error, boolean cancelled)
	{
		if (cancelled) return new ProviderException(ProviderException.Kind.CANCELLED, "Request cancelled");
		if (error instanceof SocketTimeoutException || error instanceof InterruptedIOException) return new ProviderException(ProviderException.Kind.TIMEOUT, "Request timed out");
		return new ProviderException(ProviderException.Kind.UNAVAILABLE, "Provider connection failed");
	}
	public static String userMessage(ProviderException error)
	{
		switch (error.getKind())
		{
			case MISSING_KEY: return "Party Pete needs an API key before the party can begin. Open the plugin settings and add a key for your selected provider.";
			case AUTHENTICATION: return "That API key was rejected by the provider. Check that it was copied correctly and is still active.";
			case RATE_LIMIT: return "The provider says we’ve partied a little too hard. Please wait briefly and try again.";
			case INVALID_MODEL: return "The selected model is not available for this account. Choose another model in the plugin settings.";
			case TIMEOUT: return "Party Pete took too long to answer. Check your connection or increase the request timeout.";
			case CANCELLED: return "The request was cancelled.";
			case SAFETY_REFUSAL: return "The provider declined to answer that request.";
			case MALFORMED_RESPONSE:
			case EMPTY_RESPONSE: return "Party Pete received an unreadable response. Please try again.";
			default: return "The AI provider is currently unavailable. Try again shortly or select another provider.";
		}
	}
}

