package com.partypeteai.knowledge;

public final class KnowledgeResult
{
	private final String title;
	private final String excerpt;
	private final String httpsUrl;
	public KnowledgeResult(String title, String excerpt, String httpsUrl)
	{
		this.title = title; this.excerpt = excerpt; this.httpsUrl = httpsUrl;
	}
	public String getTitle() { return title; }
	public String getExcerpt() { return excerpt; }
	public String getHttpsUrl() { return httpsUrl; }
}

