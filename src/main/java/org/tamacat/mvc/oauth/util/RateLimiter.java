/*
 * Copyright (c) 2018 tamacat.org
 * All rights reserved.
 */
package org.tamacat.mvc.oauth.util;

import java.util.HashMap;
import java.util.Map;

public class RateLimiter {

	static final Map<String, TimeLimitedMap<Long, String>> COUNTER = new HashMap<>();
	int maxLimit;
	
	public RateLimiter(int maxLimit) {
		this.maxLimit = maxLimit;
	}

	public boolean addAndCheck(String key, long maxLifeTimeMillis) {
		TimeLimitedMap<Long, String> log = COUNTER.get(key);
		if (log == null) {
			log = new TimeLimitedMap<>(maxLifeTimeMillis);
			COUNTER.put(key, log);
		}
		if (log.size() < maxLimit) {
			log.put(System.nanoTime(), key, maxLifeTimeMillis);
			return true;
		} else {
			return false;
		}
	}

	public int count(String key) {
		TimeLimitedMap<Long, String> log = COUNTER.get(key);
		if (log != null) {
			return log.size();
		}
		return 0;
	}
}
