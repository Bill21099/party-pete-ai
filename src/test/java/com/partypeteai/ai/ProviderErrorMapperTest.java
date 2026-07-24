package com.partypeteai.ai;

import java.net.SocketTimeoutException;
import org.junit.Test;
import static org.junit.Assert.*;

public class ProviderErrorMapperTest
{
	@Test public void mapsAuthentication() { assertEquals(ProviderException.Kind.AUTHENTICATION, ProviderErrorMapper.fromStatus(401, "safe").getKind()); }
	@Test public void mapsRateLimit() { assertEquals(ProviderException.Kind.RATE_LIMIT, ProviderErrorMapper.fromStatus(429, "safe").getKind()); }
	@Test public void mapsServerFailure() { assertEquals(ProviderException.Kind.UNAVAILABLE, ProviderErrorMapper.fromStatus(500, "safe").getKind()); }
	@Test public void mapsTimeout() { assertEquals(ProviderException.Kind.TIMEOUT, ProviderErrorMapper.fromIo(new SocketTimeoutException(), false).getKind()); }
}

