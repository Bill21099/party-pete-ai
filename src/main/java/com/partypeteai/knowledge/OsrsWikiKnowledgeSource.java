package com.partypeteai.knowledge;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OsrsWikiKnowledgeSource implements OsrsKnowledgeSource
{
	private static final String API = "https://oldschool.runescape.wiki/api.php";
	private static final String USER_AGENT = "PartyPeteAI/1.0 RuneLite plugin";
	private final OkHttpClient http;
	private final Gson gson;

	public OsrsWikiKnowledgeSource(OkHttpClient http, Gson gson) { this.http = http; this.gson = gson; }

	@Override
	public CompletableFuture<List<KnowledgeResult>> search(String query)
	{
		HttpUrl url = HttpUrl.parse(API).newBuilder()
			.addQueryParameter("action", "query")
			.addQueryParameter("generator", "search")
			.addQueryParameter("gsrsearch", query)
			.addQueryParameter("gsrnamespace", "0")
			.addQueryParameter("gsrlimit", "3")
			.addQueryParameter("prop", "extracts|info")
			.addQueryParameter("exintro", "1")
			.addQueryParameter("explaintext", "1")
			.addQueryParameter("exchars", "700")
			.addQueryParameter("inprop", "url")
			.addQueryParameter("format", "json")
			.addQueryParameter("formatversion", "2")
			.build();
		CompletableFuture<List<KnowledgeResult>> future = new CompletableFuture<>();
		http.newCall(new Request.Builder().url(url).header("User-Agent", USER_AGENT).build()).enqueue(new Callback()
		{
			@Override public void onFailure(Call call, IOException e) { future.completeExceptionally(e); }
			@Override public void onResponse(Call call, Response response)
			{
				try (Response ignored = response)
				{
					if (!response.isSuccessful() || response.body() == null) { future.complete(Collections.emptyList()); return; }
					JsonObject root = gson.fromJson(response.body().charStream(), JsonObject.class);
					List<KnowledgeResult> results = new ArrayList<>();
					JsonObject queryObject = root == null ? null : root.getAsJsonObject("query");
					if (queryObject != null && queryObject.has("pages"))
					{
						for (JsonElement element : queryObject.getAsJsonArray("pages"))
						{
							JsonObject page = element.getAsJsonObject();
							results.add(new KnowledgeResult(text(page, "title"), text(page, "extract"), text(page, "fullurl")));
						}
					}
					future.complete(results);
				}
				catch (RuntimeException e) { future.completeExceptionally(e); }
			}
		});
		return future;
	}

	public static String format(List<KnowledgeResult> results)
	{
		StringBuilder out = new StringBuilder();
		for (KnowledgeResult result : results)
		{
			out.append("Wiki: ").append(result.getTitle()).append("\n")
				.append(result.getExcerpt()).append("\nSource: ").append(result.getHttpsUrl()).append("\n");
		}
		return out.toString().trim();
	}

	private static String text(JsonObject object, String key)
	{
		return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsString() : "";
	}
}

