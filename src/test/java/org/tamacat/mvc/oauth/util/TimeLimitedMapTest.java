/*
 * Copyright (c) 2018 tamacat.org
 * All rights reserved.
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.tamacat.mvc.oauth.util;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Original source is SelfExpiringHashMapTests.java
 * Copyright (c) 2017 Pierantonio Cangianiello
 * Released under the MIT license
 * http://opensource.org/licenses/mit-license.php
 * @see https://gist.github.com/pcan/16faf4e59942678377e0#file-selfexpiringhashmaptests-java
 */
public class TimeLimitedMapTest {

	private final static int SLEEP_MULTIPLIER = 50;

	@Test
	public void basicGetTest() throws InterruptedException {
		TimeLimitedMap<String, String> map = new TimeLimitedMap<String, String>();
		map.put("a", "b", 2 * SLEEP_MULTIPLIER);
		Thread.sleep(1 * SLEEP_MULTIPLIER);
		assertEquals(map.get("a"), "b");
	}

	@Test
	public void basicExpireTest() throws InterruptedException {
		TimeLimitedMap<String, String> map = new TimeLimitedMap<String, String>();
		map.put("a", "b", 2 * SLEEP_MULTIPLIER);
		Thread.sleep(3 * SLEEP_MULTIPLIER);
		assertNull(map.get("a"));
	}

	@Test
	public void basicRenewTest() throws InterruptedException {
		TimeLimitedMap<String, String> map = new TimeLimitedMap<String, String>();
		map.put("a", "b", 3 * SLEEP_MULTIPLIER);
		Thread.sleep(2 * SLEEP_MULTIPLIER);
		map.renewKey("a");
		Thread.sleep(2 * SLEEP_MULTIPLIER);
		assertEquals(map.get("a"), "b");
	}

	@Test
	public void getRenewTest() throws InterruptedException {
		TimeLimitedMap<String, String> map = new TimeLimitedMap<String, String>();
		map.put("a", "b", 3 * SLEEP_MULTIPLIER);
		Thread.sleep(2 * SLEEP_MULTIPLIER);
		assertEquals(map.get("a"), "b");
		Thread.sleep(2 * SLEEP_MULTIPLIER);
		assertEquals(map.get("a"), "b");
	}

	@Test
	public void multiplePutThenRemoveTest() throws InterruptedException {
		TimeLimitedMap<String, String> map = new TimeLimitedMap<String, String>();
		map.put("a", "b", 2 * SLEEP_MULTIPLIER);
		Thread.sleep(1 * SLEEP_MULTIPLIER);
		map.put("a", "c", 2 * SLEEP_MULTIPLIER);
		Thread.sleep(1 * SLEEP_MULTIPLIER);
		map.put("a", "d", 400 * SLEEP_MULTIPLIER);
		Thread.sleep(2 * SLEEP_MULTIPLIER);
		assertEquals(map.remove("a"), "d");
	}

	@Test
	public void multiplePutThenGetTest() throws InterruptedException {
		TimeLimitedMap<String, String> map = new TimeLimitedMap<String, String>();
		map.put("a", "b", 2 * SLEEP_MULTIPLIER);
		Thread.sleep(1 * SLEEP_MULTIPLIER);
		map.put("a", "c", 2 * SLEEP_MULTIPLIER);
		Thread.sleep(1 * SLEEP_MULTIPLIER);
		map.put("a", "d", 400 * SLEEP_MULTIPLIER);
		Thread.sleep(2 * SLEEP_MULTIPLIER);
		assertEquals(map.get("a"), "d");
	}
}
