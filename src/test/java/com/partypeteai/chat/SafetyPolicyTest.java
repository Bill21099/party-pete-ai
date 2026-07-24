package com.partypeteai.chat;

import org.junit.Test;
import static org.junit.Assert.*;

public class SafetyPolicyTest
{
	@Test public void catchesBottingRequest() { assertTrue(SafetyPolicy.isCheatingRequest("Make me an undetectable bot for Zulrah")); }
	@Test public void allowsLegitimateRequest() { assertFalse(SafetyPolicy.isCheatingRequest("How should I fight Zulrah legitimately?")); }
}

