package com.partypeteai.ai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.partypeteai.config.ProviderType;
import com.partypeteai.util.ProviderHosts;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class OpenAiProvider extends AbstractHttpProvider
{
	private static final String ENDPOINT = "https://api.openai.com/v1/responses";
	public OpenAiProvider(OkHttpClient http, Gson gson) { super(http, gson); }
	@Override public ProviderType getType() { return ProviderType.OPENAI; }
	@Override protected String endpoint(AiRequest request) { return ProviderHosts.requireAllowed(getType(), ENDPOINT); }
	@Override protected void addAuthentication(Request.Builder builder, AiRequest request) { builder.header("Authorization", "Bearer " + request.getApiKey()); }

	@Override protected String requestJson(AiRequest request)
	{
		JsonObject root = new JsonObject();
		root.addProperty("model", request.getModel());
		root.addProperty("input", request.getPrompt());
		root.addProperty("max_output_tokens", request.getMaxOutputTokens());
		root.addProperty("store", false);
		JsonObject schema = new JsonObject();
		schema.addProperty("type", "object");
		JsonObject props = new JsonObject();
		JsonObject related = new JsonObject(); related.addProperty("type", "boolean");
		JsonObject answer = new JsonObject(); answer.addProperty("type", "string");
		props.add("isOsrsRelated", related); props.add("answer", answer);
		schema.add("properties", props);
		JsonArray required = new JsonArray(); required.add("isOsrsRelated"); required.add("answer");
		schema.add("required", required); schema.addProperty("additionalProperties", false);
		JsonObject format = new JsonObject();
		format.addProperty("type", "json_schema"); format.addProperty("name", "osrs_answer"); format.addProperty("strict", true); format.add("schema", schema);
		JsonObject text = new JsonObject(); text.add("format", format); root.add("text", text);
		return gson.toJson(root);
	}

	@Override protected String responseText(String json) throws ProviderException
	{
		JsonObject root = gson.fromJson(json, JsonObject.class);
		if (root.has("output_text")) { return root.get("output_text").getAsString(); }
		JsonArray output = root.getAsJsonArray("output");
		if (output != null) for (int i = 0; i < output.size(); i++)
		{
			JsonObject item = output.get(i).getAsJsonObject();
			JsonArray content = item.getAsJsonArray("content");
			if (content != null) for (int j = 0; j < content.size(); j++)
			{
				JsonObject part = content.get(j).getAsJsonObject();
				if (part.has("text")) return part.get("text").getAsString();
				if ("refusal".equals(string(part, "type"))) throw new ProviderException(ProviderException.Kind.SAFETY_REFUSAL, "Provider refusal");
			}
		}
		throw new ProviderException(ProviderException.Kind.EMPTY_RESPONSE, "Empty response");
	}
	@Override protected String streamDelta(String eventJson)
	{
		JsonObject event = gson.fromJson(eventJson, JsonObject.class);
		return "response.output_text.delta".equals(string(event, "type")) && event.has("delta")
			? event.get("delta").getAsString() : "";
	}
	private static String string(JsonObject value, String key) { return value.has(key) ? value.get(key).getAsString() : ""; }
}
