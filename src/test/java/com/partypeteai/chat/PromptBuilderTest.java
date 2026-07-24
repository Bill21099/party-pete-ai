package com.partypeteai.chat;

import com.partypeteai.config.ResponseLength;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;

public class PromptBuilderTest
{
	@Test public void omitsDisabledContext()
	{
		String prompt = new PromptBuilder().build(Collections.emptyList(), "Fire Cape?", "", ResponseLength.NORMAL, true);
		assertFalse(prompt.contains("Player-provided read-only context"));
	}
	@Test public void includesEnabledContext()
	{
		String prompt = new PromptBuilder().build(Collections.emptyList(), "What next?", "Total level: 1500", ResponseLength.NORMAL, true);
		assertTrue(prompt.contains("Total level: 1500"));
	}
}

