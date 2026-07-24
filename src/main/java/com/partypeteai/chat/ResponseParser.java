package com.partypeteai.chat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.partypeteai.ai.ProviderException;

public class ResponseParser
{
	private final Gson gson;
	public ResponseParser(Gson gson) { this.gson = gson; }

	public OsrsScopeResult parse(String raw) throws ProviderException
	{
		if (raw == null || raw.trim().isEmpty()) { throw new ProviderException(ProviderException.Kind.EMPTY_RESPONSE, "Empty provider response"); }
		String cleaned = stripFence(raw.trim());
		int objectStart = cleaned.indexOf('{');
		int objectEnd = cleaned.lastIndexOf('}');
		if (objectStart >= 0 && objectEnd > objectStart)
		{
			cleaned = cleaned.substring(objectStart, objectEnd + 1);
		}
		try
		{
			JsonObject json = gson.fromJson(cleaned, JsonObject.class);
			if (json == null || !json.has("isOsrsRelated")) { throw new JsonParseException("Missing scope"); }
			boolean related = json.get("isOsrsRelated").getAsBoolean();
			String answer = json.has("answer") && !json.get("answer").isJsonNull() ? json.get("answer").getAsString().trim() : "";
			if (related && answer.isEmpty()) { throw new ProviderException(ProviderException.Kind.EMPTY_RESPONSE, "Empty answer"); }
			return new OsrsScopeResult(related, answer);
		}
		catch (ProviderException e) { throw e; }
		catch (RuntimeException e)
		{
			StreamingJsonAnswerDecoder decoder = new StreamingJsonAnswerDecoder();
			decoder.accept(cleaned);
			if (decoder.hasAnswer()) return new OsrsScopeResult(true, decoder.getAnswer());
			throw new ProviderException(ProviderException.Kind.MALFORMED_RESPONSE, "Malformed provider response");
		}
	}

	static String stripFence(String value)
	{
		if (!value.startsWith("```")) { return value; }
		int firstNewline = value.indexOf('\n');
		int lastFence = value.lastIndexOf("```");
		return firstNewline >= 0 && lastFence > firstNewline ? value.substring(firstNewline + 1, lastFence).trim() : value;
	}
}
