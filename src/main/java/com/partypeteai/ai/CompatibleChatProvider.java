package com.partypeteai.ai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;

abstract class CompatibleChatProvider extends AbstractHttpProvider
{
	CompatibleChatProvider(OkHttpClient http, Gson gson) { super(http, gson); }
	@Override protected void addAuthentication(Request.Builder builder, AiRequest request)
	{
		builder.header("Authorization", "Bearer " + request.getApiKey());
	}
	@Override protected String requestJson(AiRequest request)
	{
		JsonObject root = new JsonObject();
		root.addProperty("model", request.getModel());
		root.addProperty("max_tokens", request.getMaxOutputTokens());
		root.addProperty("temperature", request.getTemperature());
		JsonObject responseFormat = new JsonObject(); responseFormat.addProperty("type", "json_object"); root.add("response_format", responseFormat);
		JsonArray messages = new JsonArray();
		JsonObject message = new JsonObject(); message.addProperty("role", "user"); message.addProperty("content", request.getPrompt()); messages.add(message);
		root.add("messages", messages);
		return gson.toJson(root);
	}
	@Override protected String responseText(String json) throws ProviderException
	{
		JsonObject root = gson.fromJson(json, JsonObject.class);
		JsonArray choices = root.getAsJsonArray("choices");
		if (choices == null || choices.size() == 0) throw new ProviderException(ProviderException.Kind.EMPTY_RESPONSE, "Empty response");
		JsonObject choice = choices.get(0).getAsJsonObject();
		if ("content_filter".equals(get(choice, "finish_reason"))) throw new ProviderException(ProviderException.Kind.SAFETY_REFUSAL, "Provider refusal");
		JsonObject message = choice.getAsJsonObject("message");
		if (message == null || !message.has("content") || message.get("content").isJsonNull()) throw new ProviderException(ProviderException.Kind.EMPTY_RESPONSE, "Empty response");
		return message.get("content").getAsString();
	}
	@Override protected String streamDelta(String eventJson)
	{
		JsonObject root = gson.fromJson(eventJson, JsonObject.class);
		JsonArray choices = root.getAsJsonArray("choices");
		if (choices == null || choices.size() == 0) return "";
		JsonObject choice = choices.get(0).getAsJsonObject();
		if ("content_filter".equals(get(choice, "finish_reason"))) return "";
		JsonObject delta = choice.getAsJsonObject("delta");
		return delta != null && delta.has("content") && !delta.get("content").isJsonNull() ? delta.get("content").getAsString() : "";
	}
	private static String get(JsonObject object, String key) { return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsString() : ""; }
}
