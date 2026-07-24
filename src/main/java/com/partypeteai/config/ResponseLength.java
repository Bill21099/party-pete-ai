package com.partypeteai.config;

public enum ResponseLength
{
	BRIEF(250, "Be concise."),
	NORMAL(500, "Give a practical, moderately detailed answer."),
	DETAILED(750, "Give a thorough answer with useful steps.");

	private final int tokens;
	private final String instruction;
	ResponseLength(int tokens, String instruction) { this.tokens = tokens; this.instruction = instruction; }
	public int getTokens() { return tokens; }
	public String getInstruction() { return instruction; }
}
