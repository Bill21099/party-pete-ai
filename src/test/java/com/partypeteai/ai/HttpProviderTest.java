package com.partypeteai.ai;

import com.google.gson.Gson;
import com.partypeteai.config.ProviderType;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class HttpProviderTest
{
	private MockWebServer server;
	private TestProvider provider;
	@Before public void setUp() throws Exception
	{
		server = new MockWebServer(); server.start();
		provider = new TestProvider(new OkHttpClient(), new Gson(), server.url("/v1/chat").toString());
	}
	@After public void tearDown() throws Exception { server.shutdown(); }

	@Test public void missingApiKey() throws Exception
	{
		assertKind(ProviderException.Kind.MISSING_KEY, provider.send(request("", 2)));
	}
	@Test public void handlesMockResponse() throws Exception
	{
		server.enqueue(new MockResponse().setResponseCode(200).setBody("{\"answer\":\"ok\"}"));
		assertEquals("ok", provider.send(request("test-key", 2)).get(3, TimeUnit.SECONDS).getContent());
	}
	@Test public void handlesAuthenticationFailure() throws Exception
	{
		server.enqueue(new MockResponse().setResponseCode(401));
		assertKind(ProviderException.Kind.AUTHENTICATION, provider.send(request("test-key", 2)));
	}
	@Test public void handlesRateLimit() throws Exception
	{
		server.enqueue(new MockResponse().setResponseCode(429));
		assertKind(ProviderException.Kind.RATE_LIMIT, provider.send(request("test-key", 2)));
	}
	@Test public void handlesServerFailure() throws Exception
	{
		server.enqueue(new MockResponse().setResponseCode(500));
		assertKind(ProviderException.Kind.UNAVAILABLE, provider.send(request("test-key", 2)));
	}
	@Test public void handlesRequestTimeout() throws Exception
	{
		server.enqueue(new MockResponse().setBodyDelay(3, TimeUnit.SECONDS).setBody("{\"answer\":\"late\"}"));
		assertKind(ProviderException.Kind.TIMEOUT, provider.send(request("test-key", 1)));
	}
	@Test public void cancellation() throws Exception
	{
		server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));
		java.util.concurrent.CompletableFuture<AiResponse> future = provider.send(request("test-key", 10));
		server.takeRequest(2, TimeUnit.SECONDS);
		provider.cancelActiveRequest();
		assertKind(ProviderException.Kind.CANCELLED, future);
	}
	@Test public void consumesSseStream() throws Exception
	{
		server.enqueue(new MockResponse().setHeader("Content-Type", "text/event-stream")
			.setBody("data: {\"delta\":\"hello \"}\n\ndata: {\"delta\":\"world\"}\n\ndata: [DONE]\n\n"));
		StringBuilder deltas = new StringBuilder();
		AiResponse response = provider.sendStreaming(request("test-key", 2), deltas::append).get(3, TimeUnit.SECONDS);
		assertEquals("hello world", response.getContent());
		assertEquals("hello world", deltas.toString());
	}

	private static AiRequest request(String key, int timeout) { return new AiRequest(key, "test-model", "test", 10, 0.1, timeout); }
	private static void assertKind(ProviderException.Kind expected, java.util.concurrent.CompletableFuture<?> future) throws Exception
	{
		try { future.get(4, TimeUnit.SECONDS); fail("Expected failure"); }
		catch (ExecutionException e) { assertEquals(expected, ((ProviderException) e.getCause()).getKind()); }
	}

	private static class TestProvider extends AbstractHttpProvider
	{
		private final String url;
		TestProvider(OkHttpClient http, Gson gson, String url) { super(http, gson); this.url = url; }
		@Override protected String endpoint(AiRequest request) { return url; }
		@Override protected String requestJson(AiRequest request) { return "{}"; }
		@Override protected String responseText(String json) { return gson.fromJson(json, com.google.gson.JsonObject.class).get("answer").getAsString(); }
		@Override protected String streamDelta(String json) { return gson.fromJson(json, com.google.gson.JsonObject.class).get("delta").getAsString(); }
		@Override protected void addAuthentication(Request.Builder builder, AiRequest request) { builder.header("Authorization", "Bearer " + request.getApiKey()); }
		@Override public ProviderType getType() { return ProviderType.OPENAI; }
	}
}
