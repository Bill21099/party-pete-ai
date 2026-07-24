package com.partypeteai.knowledge;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GrandExchangePriceService
{
	private static final String BASE = "https://prices.runescape.wiki/api/v1/osrs/";
	private static final String USER_AGENT = "PartyPeteAI/1.0 RuneLite plugin";
	private final OkHttpClient http;
	private final Gson gson;
	private volatile CompletableFuture<List<Item>> mapping;

	public GrandExchangePriceService(OkHttpClient http, Gson gson) { this.http = http; this.gson = gson; }

	public CompletableFuture<String> findPrices(String question)
	{
		return mapping().thenCompose(items ->
		{
			List<Item> matches = match(items, question);
			if (matches.isEmpty()) return CompletableFuture.completedFuture("");
			return getJson(BASE + "latest").thenApply(root -> format(matches, root));
		});
	}

	private CompletableFuture<List<Item>> mapping()
	{
		CompletableFuture<List<Item>> current = mapping;
		if (current != null) return current;
		synchronized (this)
		{
			if (mapping == null)
			{
				mapping = getJson(BASE + "mapping").thenApply(root ->
				{
					List<Item> items = new ArrayList<>();
					JsonArray array = root.getAsJsonArray("_array");
					if (array != null) for (JsonElement element : array)
					{
						JsonObject value = element.getAsJsonObject();
						if (value.has("id") && value.has("name")) items.add(new Item(value.get("id").getAsInt(), value.get("name").getAsString()));
					}
					return items;
				});
			}
			return mapping;
		}
	}

	private CompletableFuture<JsonObject> getJson(String url)
	{
		CompletableFuture<JsonObject> future = new CompletableFuture<>();
		http.newCall(new Request.Builder().url(url).header("User-Agent", USER_AGENT).build()).enqueue(new Callback()
		{
			@Override public void onFailure(Call call, IOException e) { future.completeExceptionally(e); }
			@Override public void onResponse(Call call, Response response)
			{
				try (Response ignored = response)
				{
					if (!response.isSuccessful() || response.body() == null) { future.completeExceptionally(new IOException("Price API unavailable")); return; }
					JsonElement parsed = gson.fromJson(response.body().charStream(), JsonElement.class);
					if (parsed.isJsonArray())
					{
						JsonObject wrapper = new JsonObject(); wrapper.add("_array", parsed); future.complete(wrapper);
					}
					else future.complete(parsed.getAsJsonObject());
				}
				catch (RuntimeException e) { future.completeExceptionally(e); }
			}
		});
		return future;
	}

	private static List<Item> match(List<Item> items, String question)
	{
		String normalQuestion = normalize(question);
		List<Item> found = new ArrayList<>();
		for (Item item : items)
		{
			String name = normalize(item.name);
			String plural = " " + name.trim() + "s ";
			if (normalQuestion.contains(name) || normalQuestion.contains(plural)) found.add(item);
		}
		found.sort(Comparator.comparingInt((Item item) -> item.name.length()).reversed());
		return found.subList(0, Math.min(5, found.size()));
	}

	private static String format(List<Item> matches, JsonObject root)
	{
		JsonObject data = root.getAsJsonObject("data");
		if (data == null) return "";
		StringBuilder out = new StringBuilder("Live Grand Exchange guide prices (OSRS Wiki price API):\n");
		for (Item item : matches)
		{
			JsonObject price = data.getAsJsonObject(Integer.toString(item.id));
			if (price == null) continue;
			out.append("- ").append(item.name).append(": latest high ")
				.append(number(price, "high")).append(" gp, latest low ")
				.append(number(price, "low")).append(" gp");
			if (price.has("highTime")) out.append(" (Unix time ").append(price.get("highTime").getAsLong()).append(")");
			out.append("\n");
		}
		if (out.toString().endsWith(":\n")) return "";
		out.append("Source: https://prices.runescape.wiki/");
		return out.toString();
	}

	private static String number(JsonObject object, String key)
	{
		return !object.has(key) || object.get(key).isJsonNull() ? "unavailable" : String.format(Locale.US, "%,d", object.get(key).getAsLong());
	}

	private static String normalize(String value)
	{
		return " " + value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim() + " ";
	}

	private static final class Item
	{
		private final int id;
		private final String name;
		private Item(int id, String name) { this.id = id; this.name = name; }
	}
}
