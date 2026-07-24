package com.partypeteai.config;

public enum CreativityLevel
{
	PRECISE(0.15), BALANCED(0.45), CREATIVE(0.75);
	private final double temperature;
	CreativityLevel(double temperature) { this.temperature = temperature; }
	public double getTemperature() { return temperature; }
}

