/*
 * Copyright (c) 2018 tamacat.org
 * All rights reserved.
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.tamacat.mvc.oauth.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * A thread-safe implementation of a HashMap which entries expires after the specified life time.
 * The life-time can be defined on a per-key basis, or using a default one, that is passed to the 
 * constructor.
 * 
 * @param <K> the Key type
 * @param <V> the Value type
 * 
 * Original source is SelfExpiringHashMap.java
 * Copyright (c) 2017 Pierantonio Cangianiello
 * Released under the MIT license
 * http://opensource.org/licenses/mit-license.php
 * @see https://gist.github.com/pcan/16faf4e59942678377e0
 */
public class TimeLimitedMap<K, V> implements Map<K, V> {

    final Map<K, V> internalMap;
    final Map<K, ExpiringKey<K>> expiringKeys;

    /**
     * Holds the map keys using the given life time for expiration.
     */
    final DelayQueue<ExpiringKey<K>> delayQueue = new DelayQueue<>();

    /**
     * The default max life time in milliseconds.
     */
    final long maxLifeTimeMillis;

    public TimeLimitedMap() {
        this(new ConcurrentHashMap<K, V>(), Long.MAX_VALUE);
    }

    public TimeLimitedMap(long defaultMaxLifeTimeMillis) {
        this(new ConcurrentHashMap<K, V>(), defaultMaxLifeTimeMillis);
    }
    
    public TimeLimitedMap(Map<K, V> internalMap, long defaultMaxLifeTimeMillis) {
    	this.internalMap = internalMap;
        expiringKeys = new WeakHashMap<K, ExpiringKey<K>>();
        this.maxLifeTimeMillis = defaultMaxLifeTimeMillis;
    }

    public TimeLimitedMap(long defaultMaxLifeTimeMillis, int initialCapacity) {
        internalMap = new ConcurrentHashMap<K, V>(initialCapacity);
        expiringKeys = new WeakHashMap<K, ExpiringKey<K>>(initialCapacity);
        this.maxLifeTimeMillis = defaultMaxLifeTimeMillis;
    }
    
    @Override
    public int size() {
        cleanup();
        return internalMap.size();
    }
    
    @Override
    public boolean isEmpty() {
        cleanup();
        return internalMap.isEmpty();
    }
    
    @SuppressWarnings("unchecked")
	@Override
    public boolean containsKey(Object key) {
        cleanup();
        return internalMap.containsKey((K) key);
    }
    
    @SuppressWarnings("unchecked")
	@Override
    public boolean containsValue(Object value) {
        cleanup();
        return internalMap.containsValue((V) value);
    }

    @SuppressWarnings("unchecked")
	@Override
    public V get(Object key) {
        cleanup();
        renewKey((K) key);
        return internalMap.get((K) key);
    }

    @Override
    public V put(K key, V value) {
        return this.put(key, value, maxLifeTimeMillis);
    }
    
    /**
     * Associates the given key to the given value in this map, with the specified life
     * times in milliseconds.
     *
     * @param key
     * @param value
     * @param lifeTimeMillis
     * @return a previously associated object for the given key (if exists).
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public V put(K key, V value, long lifeTimeMillis) {
        cleanup();
		ExpiringKey<K> delayedKey = new ExpiringKey(key, lifeTimeMillis);
        ExpiringKey<K> oldKey = expiringKeys.put((K) key, delayedKey);
        if(oldKey != null) {
            expireKey(oldKey);
            expiringKeys.put((K) key, delayedKey);
        }
        delayQueue.offer(delayedKey);
        return internalMap.put(key, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public V remove(Object key) {
    	V k = internalMap.remove((K) key);
        expireKey(expiringKeys.remove((K) key));
        return k;
    }

    /**
     * Not supported.
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    /**
     * Renews the specified key, setting the life time to the initial value.
     *
     * @param key
     * @return true if the key is found, false otherwise
     */
    public boolean renewKey(K key) {
        ExpiringKey<K> delayedKey = expiringKeys.get((K) key);
        if (delayedKey != null) {
            delayedKey.renew();
            return true;
        }
        return false;
    }

    void expireKey(ExpiringKey<K> delayedKey) {
        if (delayedKey != null) {
            delayedKey.expire();
            cleanup();
        }
    }

    @Override
    public void clear() {
        delayQueue.clear();
        expiringKeys.clear();
        internalMap.clear();
    }

    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported.
     */
    @Override
    public Collection<V> values() {
        throw new UnsupportedOperationException();
    }

    /**
     * Not supported.
     */
    @Override
    public Set<Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }

    private void cleanup() {
        ExpiringKey<K> delayedKey = delayQueue.poll();
        while (delayedKey != null) {
            internalMap.remove(delayedKey.getKey());
            expiringKeys.remove(delayedKey.getKey());
            delayedKey = delayQueue.poll();
        }
    }

    @SuppressWarnings("hiding")
	class ExpiringKey<K> implements Delayed {

        long startTime = System.nanoTime();
        final long maxLifeTimeMillis;
        final K key;

        public ExpiringKey(K key, long maxLifeTimeMillis) {
            this.maxLifeTimeMillis = maxLifeTimeMillis*1000*1000;
            this.key = key;
        }

        public K getKey() {
            return key;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ExpiringKey<K> other = (ExpiringKey<K>) obj;
            if (this.key != other.key && (this.key == null || !this.key.equals(other.key))) {
                return false;
            }
            return true;
        }
        
        @Override
        public int hashCode() {
            int hash = 7;
            hash = 31 * hash + (this.key != null ? this.key.hashCode() : 0);
            return hash;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(getDelayMillis(), TimeUnit.NANOSECONDS);
        }

        long getDelayMillis() {
            return (startTime + maxLifeTimeMillis) - System.nanoTime();
        }

        public void renew() {
            startTime = System.nanoTime();
        }

        public void expire() {
            startTime =  System.nanoTime() - maxLifeTimeMillis - 1;
        }

		@SuppressWarnings("unchecked")
		@Override
        public int compareTo(Delayed that) {
            return Long.compare(this.getDelayMillis(), ((ExpiringKey<K>) that).getDelayMillis());
        }
    }
}