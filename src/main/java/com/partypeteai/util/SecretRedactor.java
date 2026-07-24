package com.partypeteai.util;

public final class SecretRedactor
{
	private SecretRedactor() {}
	public static String redact(String text, String secret)
	{
		if (text == null || secret == null || secret.isEmpty()) return text;
		return text.replace(secret, "[REDACTED]");
	}
}

