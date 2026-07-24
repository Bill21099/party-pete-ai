package com.partypeteai.chat;

import java.util.Locale;
import java.util.regex.Pattern;

public final class SafetyPolicy
{
	public static final String OUT_OF_SCOPE = "Sorry, adventurer! I’m Party Pete, and I can only answer questions about Old School RuneScape.";
	public static final String CHEATING = "I can help with legitimate OSRS strategies and RuneLite features, but I can’t help create or hide bots, macros, cheats or rule-breaking automation.";
	private static final Pattern CHEAT = Pattern.compile("\\b(bot(?:ting)?|macro(?:ing)?|auto[ -]?click(?:er|ing)?|automate (?:gameplay|clicking|inputs?)|script(?:ed)? click(?:ing)?|ban[ -]?evad|undetectable automation|detection (?:avoidance|bypass)|packet manipulation|real[ -]?world trad(?:e|ing)|rwt|phish(?:ing)?|steal (?:an )?account|credential theft|exploit (?:a )?vulnerabilit)\\b", Pattern.CASE_INSENSITIVE);
	private SafetyPolicy() {}
	public static boolean isCheatingRequest(String text) { return text != null && CHEAT.matcher(text.toLowerCase(Locale.ROOT)).find(); }
}
