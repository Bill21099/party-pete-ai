package com.partypeteai.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class SecurityUtilitiesTest
{
	@Test public void redactsApiKey() { assertEquals("key=[REDACTED]", SecretRedactor.redact("key=secret123", "secret123")); }
	@Test public void acceptsHttps() { assertTrue(LinkSanitiser.isSafe("https://oldschool.runescape.wiki/w/Barrows")); }
	@Test public void rejectsHttpAndScript() { assertFalse(LinkSanitiser.isSafe("http://example.com")); assertFalse(LinkSanitiser.isSafe("javascript:alert(1)")); }
}

