package com.partypeteai.ai;

public final class AiRequest
{
	private final String apiKey;
	private final String model;
	private final String prompt;
	private final int maxOutputTokens;
	private final double temperature;
	private final int timeoutSeconds;

	public AiRequest(String apiKey, String model, String prompt, int maxOutputTokens, double temperature, int timeoutSeconds)
	{
		this.apiKey = apiKey; this.model = model; this.prompt = prompt; this.maxOutputTokens = maxOutputTokens;
		this.temperature = temperature; this.timeoutSeconds = timeoutSeconds;
	}
	public String getApiKey() { return apiKey; }
	public String getModel() { return model; }
	public String getPrompt() { return prompt; }
	public int getMaxOutputTokens() { return maxOutputTokens; }
	public double getTemperature() { return temperature; }
	public int getTimeoutSeconds() { return timeoutSeconds; }
}

