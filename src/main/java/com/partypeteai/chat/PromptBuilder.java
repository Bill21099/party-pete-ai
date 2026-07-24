package com.partypeteai.chat;

import com.partypeteai.config.ResponseLength;
import java.util.List;

public class PromptBuilder
{
	public String build(List<ChatMessage> history, String question, String accountContext, ResponseLength length, boolean personality)
	{
		return build(history, question, accountContext, "", length, personality);
	}

	public String build(List<ChatMessage> history, String question, String accountContext, String liveKnowledge, ResponseLength length, boolean personality)
	{
		StringBuilder out = new StringBuilder(SystemPrompt.TEXT);
		out.append("\n\n").append(length.getInstruction());
		out.append(personality ? "\nUse a small, non-intrusive amount of Party Pete personality." : "\nUse a neutral guide voice.");
		if (accountContext != null && !accountContext.isEmpty()) { out.append("\n\nPlayer-provided read-only context:\n").append(accountContext); }
		if (liveKnowledge != null && !liveKnowledge.isEmpty())
		{
			out.append("\n\nLive OSRS data supplied for this request. Prefer it for changing facts and cite its HTTPS source links:\n").append(liveKnowledge);
		}
		if (history != null && !history.isEmpty())
		{
			out.append("\n\nConversation history:");
			for (ChatMessage message : history)
			{
				out.append("\n").append(message.getRole() == ChatRole.USER ? "User: " : "Assistant: ").append(message.getContent());
			}
		}
		out.append("\n\nNewest user question:\n").append(question);
		return out.toString();
	}
}
