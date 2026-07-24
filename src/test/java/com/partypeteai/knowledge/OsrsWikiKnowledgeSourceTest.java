package com.partypeteai.knowledge;

import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;

public class OsrsWikiKnowledgeSourceTest
{
	@Test public void formatsGroundingWithSource()
	{
		String value = OsrsWikiKnowledgeSource.format(Collections.singletonList(
			new KnowledgeResult("Vorkath", "A draconic boss.", "https://oldschool.runescape.wiki/w/Vorkath")));
		assertTrue(value.contains("A draconic boss."));
		assertTrue(value.contains("https://oldschool.runescape.wiki/w/Vorkath"));
	}
}
