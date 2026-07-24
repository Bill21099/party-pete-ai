package com.partypeteai.ui;

import com.partypeteai.util.LinkSanitiser;
import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import net.runelite.client.util.LinkBrowser;

public final class SafeTextRenderer
{
	private static final Pattern LINK = Pattern.compile("\\[([^\\]]{1,200})\\]\\((https://[^\\s)]+)\\)");
	private SafeTextRenderer() {}

	public static JEditorPane create(String text, boolean allowLinks, Color background)
	{
		JEditorPane pane = new JEditorPane("text/html", html(text, allowLinks));
		pane.setEditable(false);
		pane.setOpaque(true);
		pane.setBackground(background);
		pane.setForeground(Color.WHITE);
		pane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		pane.addHyperlinkListener(event ->
		{
			if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED && LinkSanitiser.isSafe(event.getURL().toString()))
				LinkBrowser.browse(event.getURL().toString());
		});
		return pane;
	}

	static String html(String source, boolean allowLinks)
	{
		String escaped = escape(source == null ? "" : source);
		escaped = escaped.replaceAll("\\*\\*([^*\\n]+)\\*\\*", "<b>$1</b>");
		escaped = escaped.replaceAll("`([^`\\n]+)`", "<code>$1</code>");
		if (allowLinks)
		{
			Matcher matcher = LINK.matcher(escaped);
			StringBuffer output = new StringBuffer();
			while (matcher.find())
			{
				String url = matcher.group(2);
				String replacement = LinkSanitiser.isSafe(url) ? "<a href=\"" + url + "\">" + matcher.group(1) + "</a>" : matcher.group(1);
				matcher.appendReplacement(output, Matcher.quoteReplacement(replacement));
			}
			matcher.appendTail(output);
			escaped = output.toString();
		}
		else escaped = LINK.matcher(escaped).replaceAll("$1");
		StringBuilder body = new StringBuilder();
		for (String line : escaped.split("\\r?\\n", -1))
		{
			if (line.matches("^[-*] .+")) body.append("&#8226; ").append(line.substring(2));
			else body.append(line);
			body.append("<br>");
		}
		return "<html><head><style>body{font-family:sans-serif;color:#eee;margin:7px;word-wrap:break-word}a{color:#6fb7ff}code{background:#222;padding:2px}</style></head><body>" + body + "</body></html>";
	}

	private static String escape(String value)
	{
		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
	}
}

