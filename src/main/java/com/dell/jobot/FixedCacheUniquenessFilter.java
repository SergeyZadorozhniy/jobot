package com.dell.jobot;

import com.dell.jobot.monitor.PerformanceMonitor;
import com.google.common.cache.CacheBuilder;
import java.util.Map;
import java.util.function.Predicate;

public class FixedCacheUniquenessFilter<T>
implements Predicate<T> {

	private static final Object staticValue = new Object();
	private final Map<T, Object> cache;

	public FixedCacheUniquenessFilter(final int capacity) {
		// cache = new LRUMap<>(capacity);
		this.cache = CacheBuilder.newBuilder().maximumSize(capacity).<T, Object>build().asMap();
	}

	@Override
	public boolean test(final T v) {
		//synchronized(cache) {
		boolean result = null == cache.putIfAbsent(v, staticValue);//return null == cache.putIfAbsent(v, staticValue);
		//}
		if (result)
			PerformanceMonitor.trackUrlsCollected(cache.size());
		return result;
	}
}
