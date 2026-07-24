package com.partypeteai.ai;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

abstract class AbstractHttpProvider implements AiProvider
{
	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	protected final OkHttpClient http;
	protected final Gson gson;
	private volatile Call activeCall;
	private volatile boolean cancelRequested;

	AbstractHttpProvider(OkHttpClient http, Gson gson) { this.http = http; this.gson = gson; }
	protected abstract String endpoint(AiRequest request);
	protected abstract String requestJson(AiRequest request);
	protected abstract String responseText(String json) throws ProviderException;
	protected abstract void addAuthentication(Request.Builder builder, AiRequest request);
	protected String streamEndpoint(AiRequest request) { return endpoint(request); }
	protected String streamRequestJson(AiRequest request)
	{
		com.google.gson.JsonObject json = gson.fromJson(requestJson(request), com.google.gson.JsonObject.class);
		json.addProperty("stream", true);
		return gson.toJson(json);
	}
	protected abstract String streamDelta(String eventJson) throws ProviderException;

	@Override
	public CompletableFuture<AiResponse> send(AiRequest request)
	{
		CompletableFuture<AiResponse> future = new CompletableFuture<>();
		if (request.getApiKey() == null || request.getApiKey().trim().isEmpty())
		{
			future.completeExceptionally(new ProviderException(ProviderException.Kind.MISSING_KEY, "Missing key"));
			return future;
		}
		OkHttpClient timed = http.newBuilder()
			.callTimeout(request.getTimeoutSeconds(), TimeUnit.SECONDS)
			.connectTimeout(request.getTimeoutSeconds(), TimeUnit.SECONDS)
			.readTimeout(request.getTimeoutSeconds(), TimeUnit.SECONDS)
			.writeTimeout(request.getTimeoutSeconds(), TimeUnit.SECONDS)
			.build();
		Request.Builder builder = new Request.Builder()
			.url(endpoint(request))
			.post(RequestBody.create(JSON, requestJson(request)));
		addAuthentication(builder, request);
		Call call = timed.newCall(builder.build());
		cancelRequested = false;
		activeCall = call;
		call.enqueue(new Callback()
		{
			@Override public void onFailure(Call call, IOException e)
			{
				clear(call);
				future.completeExceptionally(ProviderErrorMapper.fromIo(e, cancelRequested && call.isCanceled()));
			}

			@Override public void onResponse(Call call, Response response)
			{
				try (Response closable = response)
				{
					String requestId = response.header("x-request-id", response.header("request-id", ""));
					if (!response.isSuccessful())
					{
						future.completeExceptionally(ProviderErrorMapper.fromStatus(response.code(), "Provider HTTP " + response.code()));
						return;
					}
					String body = response.body() == null ? "" : response.body().string();
					future.complete(new AiResponse(responseText(body), requestId));
				}
				catch (ProviderException e) { future.completeExceptionally(e); }
				catch (IOException e) { future.completeExceptionally(ProviderErrorMapper.fromIo(e, cancelRequested && call.isCanceled())); }
				catch (Exception e) { future.completeExceptionally(new ProviderException(ProviderException.Kind.MALFORMED_RESPONSE, "Malformed provider response")); }
				finally { clear(call); }
			}
		});
		return future;
	}

	@Override
	public CompletableFuture<AiResponse> sendStreaming(AiRequest request, Consumer<String> onTextDelta)
	{
		CompletableFuture<AiResponse> future = new CompletableFuture<>();
		if (request.getApiKey() == null || request.getApiKey().trim().isEmpty())
		{
			future.completeExceptionally(new ProviderException(ProviderException.Kind.MISSING_KEY, "Missing key"));
			return future;
		}
		OkHttpClient timed = http.newBuilder()
			.callTimeout(request.getTimeoutSeconds(), TimeUnit.SECONDS)
			.connectTimeout(request.getTimeoutSeconds(), TimeUnit.SECONDS)
			.readTimeout(request.getTimeoutSeconds(), TimeUnit.SECONDS)
			.writeTimeout(request.getTimeoutSeconds(), TimeUnit.SECONDS)
			.build();
		Request.Builder builder = new Request.Builder()
			.url(streamEndpoint(request))
			.header("Accept", "text/event-stream")
			.post(RequestBody.create(JSON, streamRequestJson(request)));
		addAuthentication(builder, request);
		Call call = timed.newCall(builder.build());
		cancelRequested = false;
		activeCall = call;
		call.enqueue(new Callback()
		{
			@Override public void onFailure(Call call, IOException e)
			{
				clear(call);
				future.completeExceptionally(ProviderErrorMapper.fromIo(e, cancelRequested && call.isCanceled()));
			}
			@Override public void onResponse(Call call, Response response)
			{
				try (Response ignored = response)
				{
					String requestId = response.header("x-request-id", response.header("request-id", ""));
					if (!response.isSuccessful() || response.body() == null)
					{
						future.completeExceptionally(ProviderErrorMapper.fromStatus(response.code(), "Provider HTTP " + response.code()));
						return;
					}
					StringBuilder fullText = new StringBuilder();
					String line;
					while ((line = response.body().source().readUtf8Line()) != null)
					{
						if (!line.startsWith("data:")) continue;
						String data = line.substring(5).trim();
						if (data.isEmpty() || "[DONE]".equals(data)) continue;
						String delta = streamDelta(data);
						if (delta != null && !delta.isEmpty())
						{
							fullText.append(delta);
							onTextDelta.accept(delta);
						}
					}
					if (fullText.length() == 0) throw new ProviderException(ProviderException.Kind.EMPTY_RESPONSE, "Empty response");
					future.complete(new AiResponse(fullText.toString(), requestId));
				}
				catch (ProviderException e) { future.completeExceptionally(e); }
				catch (IOException e) { future.completeExceptionally(ProviderErrorMapper.fromIo(e, cancelRequested && call.isCanceled())); }
				catch (RuntimeException e) { future.completeExceptionally(new ProviderException(ProviderException.Kind.MALFORMED_RESPONSE, "Malformed provider stream")); }
				finally { clear(call); }
			}
		});
		return future;
	}

	private void clear(Call call) { if (activeCall == call) { activeCall = null; } }
	@Override public void cancelActiveRequest()
	{
		Call call = activeCall;
		if (call != null)
		{
			cancelRequested = true;
			call.cancel();
		}
	}
}
