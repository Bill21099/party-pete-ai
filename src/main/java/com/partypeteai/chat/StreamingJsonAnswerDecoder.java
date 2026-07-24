package com.partypeteai.chat;

import java.util.regex.Pattern;

public class StreamingJsonAnswerDecoder
{
	private static final Pattern RELATED = Pattern.compile("\"isOsrsRelated\"\\s*:\\s*true");
	private static final Pattern UNRELATED = Pattern.compile("\"isOsrsRelated\"\\s*:\\s*false");
	private final StringBuilder raw = new StringBuilder();
	private int emitted;
	private String answer = "";

	public synchronized String accept(String delta)
	{
		raw.append(delta);
		String value = raw.toString();
		if (UNRELATED.matcher(value).find() || !RELATED.matcher(value).find()) return "";
		int key = value.indexOf("\"answer\"");
		if (key < 0) return "";
		int colon = value.indexOf(':', key + 8);
		int quote = colon < 0 ? -1 : value.indexOf('"', colon + 1);
		if (quote < 0) return "";
		String decoded = decodeAvailable(value, quote + 1);
		answer = decoded;
		if (decoded.length() <= emitted) return "";
		String fresh = decoded.substring(emitted);
		emitted = decoded.length();
		return fresh;
	}

	public synchronized boolean hasAnswer()
	{
		return !answer.trim().isEmpty();
	}

	public synchronized String getAnswer()
	{
		return answer.trim();
	}

	private static String decodeAvailable(String value, int start)
	{
		StringBuilder out = new StringBuilder();
		for (int i = start; i < value.length(); i++)
		{
			char c = value.charAt(i);
			if (c == '"') break;
			if (c != '\\') { out.append(c); continue; }
			if (++i >= value.length()) break;
			char escaped = value.charAt(i);
			switch (escaped)
			{
				case '"': out.append('"'); break;
				case '\\': out.append('\\'); break;
				case '/': out.append('/'); break;
				case 'b': out.append('\b'); break;
				case 'f': out.append('\f'); break;
				case 'n': out.append('\n'); break;
				case 'r': out.append('\r'); break;
				case 't': out.append('\t'); break;
				case 'u':
					if (i + 4 >= value.length()) return out.toString();
					try { out.append((char) Integer.parseInt(value.substring(i + 1, i + 5), 16)); i += 4; }
					catch (NumberFormatException ignored) { return out.toString(); }
					break;
				default: return out.toString();
			}
		}
		return out.toString();
	}
}
