package com.partypeteai.util;

import com.partypeteai.config.ProviderType;
import java.net.URI;
import java.util.EnumMap;
import java.util.Map;

public final class ProviderHosts
{
	private static final Map<ProviderType, String> HOSTS = new EnumMap<>(ProviderType.class);
	static
	{
		HOSTS.put(ProviderType.OPENAI, "api.openai.com");
		HOSTS.put(ProviderType.GEMINI, "generativelanguage.googleapis.com");
		HOSTS.put(ProviderType.DEEPSEEK, "api.deepseek.com");
		HOSTS.put(ProviderType.OPENROUTER, "openrouter.ai");
	}
	private ProviderHosts() {}
	public static String requireAllowed(ProviderType provider, String url)
	{
		URI uri = URI.create(url);
		if (!"https".equalsIgnoreCase(uri.getScheme()) || !HOSTS.get(provider).equalsIgnoreCase(uri.getHost()))
			throw new IllegalArgumentException("Provider endpoint is not allowlisted");
		return url;
	}
}
