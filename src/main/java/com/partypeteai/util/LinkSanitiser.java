package com.partypeteai.util;

import java.net.URI;

public final class LinkSanitiser
{
	private LinkSanitiser() {}
	public static boolean isSafe(String url)
	{
		try
		{
			URI uri = URI.create(url);
			return "https".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null && uri.getUserInfo() == null;
		}
		catch (RuntimeException e) { return false; }
	}
}
