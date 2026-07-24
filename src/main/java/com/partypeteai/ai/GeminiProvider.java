package com.partypeteai.ai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.partypeteai.config.ProviderType;
import com.partypeteai.util.ProviderHosts;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class GeminiProvider extends AbstractHttpProvider
{
	public GeminiProvider(OkHttpClient http, Gson gson) { super(http, gson); }
	@Override public ProviderType getType() { return ProviderType.GEMINI; }
	@Override protected String endpoint(AiRequest request)
	{
		String model = URLEncoder.encode(request.getModel(), StandardCharsets.UTF_8);
		return ProviderHosts.requireAllowed(getType(), "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent");
	}
	@Override protected void addAuthentication(Request.Builder builder, AiRequest request) { builder.header("x-goog-api-key", request.getApiKey()); }
	@Override protected String streamEndpoint(AiRequest request)
	{
		return endpoint(request).replace(":generateContent", ":streamGenerateContent") + "?alt=sse";
	}
	@Override protected String streamRequestJson(AiRequest request) { return requestJson(request); }
	@Override protected String requestJson(AiRequest request)
	{
		JsonObject root = new JsonObject();
		JsonObject part = new JsonObject(); part.addProperty("text", request.getPrompt());
		JsonArray parts = new JsonArray(); parts.add(part);
		JsonObject content = new JsonObject(); content.addProperty("role", "user"); content.add("parts", parts);
		JsonArray contents = new JsonArray(); contents.add(content); root.add("contents", contents);
		JsonObject generation = new JsonObject();
		generation.addProperty("maxOutputTokens", request.getMaxOutputTokens());
		generation.addProperty("temperature", request.getTemperature());
		generation.addProperty("responseMimeType", "application/json");
		JsonObject thinking = new JsonObject();
		thinking.addProperty("thinkingBudget", 0);
		generation.add("thinkingConfig", thinking);
		JsonObject schema = new JsonObject();
		schema.addProperty("type", "OBJECT");
		JsonObject properties = new JsonObject();
		JsonObject related = new JsonObject(); related.addProperty("type", "BOOLEAN");
		JsonObject answer = new JsonObject(); answer.addProperty("type", "STRING");
		properties.add("isOsrsRelated", related);
		properties.add("answer", answer);
		schema.add("properties", properties);
		JsonArray required = new JsonArray(); required.add("isOsrsRelated"); required.add("answer");
		schema.add("required", required);
		generation.add("responseSchema", schema);
		root.add("generationConfig", generation);
		return gson.toJson(root);
	}
	@Override protected String responseText(String json) throws ProviderException
	{
		JsonObject root = gson.fromJson(json, JsonObject.class);
		JsonArray candidates = root.getAsJsonArray("candidates");
		if (candidates == null || candidates.size() == 0)
		{
			if (root.has("promptFeedback")) throw new ProviderException(ProviderException.Kind.SAFETY_REFUSAL, "Provider refusal");
			throw new ProviderException(ProviderException.Kind.EMPTY_RESPONSE, "Empty response");
		}
		JsonObject candidate = candidates.get(0).getAsJsonObject();
		if ("SAFETY".equals(candidate.has("finishReason") ? candidate.get("finishReason").getAsString() : ""))
			throw new ProviderException(ProviderException.Kind.SAFETY_REFUSAL, "Provider refusal");
		JsonObject content = candidate.getAsJsonObject("content");
		JsonArray parts = content == null ? null : content.getAsJsonArray("parts");
		if (parts == null || parts.size() == 0 || !parts.get(0).getAsJsonObject().has("text"))
			throw new ProviderException(ProviderException.Kind.EMPTY_RESPONSE, "Empty response");
		return parts.get(0).getAsJsonObject().get("text").getAsString();
	}
	@Override protected String streamDelta(String eventJson) throws ProviderException
	{
		JsonObject root = gson.fromJson(eventJson, JsonObject.class);
		JsonArray candidates = root.getAsJsonArray("candidates");
		if (candidates == null || candidates.size() == 0) return "";
		JsonObject candidate = candidates.get(0).getAsJsonObject();
		if ("SAFETY".equals(candidate.has("finishReason") ? candidate.get("finishReason").getAsString() : ""))
			throw new ProviderException(ProviderException.Kind.SAFETY_REFUSAL, "Provider refusal");
		JsonObject content = candidate.getAsJsonObject("content");
		JsonArray parts = content == null ? null : content.getAsJsonArray("parts");
		if (parts == null) return "";
		StringBuilder text = new StringBuilder();
		for (int i = 0; i < parts.size(); i++)
		{
			JsonObject part = parts.get(i).getAsJsonObject();
			if (part.has("text") && !part.get("text").isJsonNull()) text.append(part.get("text").getAsString());
		}
		return text.toString();
	}
}
