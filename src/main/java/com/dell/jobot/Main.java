package com.dell.jobot;

import lombok.val;

import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Predicate;

import static com.dell.jobot.UrlUtil.HTTP_FILTER;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Main {

	static final int QUEUE_CAPACITY = 1_000_000;
	static final int CACHE_CAPACITY = 1_000_000;

	public static void main(final String... args) {
		if(0 == args.length) {
			printUsage();
		} else {
			val parallelism = Runtime.getRuntime().availableProcessors();
			val queue = new ArrayBlockingQueue<Runnable>(QUEUE_CAPACITY);
			val threadFactory = new DaemonThreadFactory();
			val rejectionHandler = new IgnoringRejectionExecutionHandler();
			val executor = new ThreadPoolExecutor(
				parallelism, parallelism, 0, SECONDS, queue, threadFactory, rejectionHandler
			);
			val uniqueUrlFilter = new FixedCacheUniquenessFilter<URL>(CACHE_CAPACITY);
			val handler = new HttpUrlStreamHandler(executor, url -> HTTP_FILTER.test(url) && uniqueUrlFilter.test(url));
			try {
				handler.handle(null, Arrays.stream(args));
				try {
					executor.awaitTermination(Long.MAX_VALUE, SECONDS);
				} catch(final InterruptedException ok) {
				}
			} catch(final Exception e) {
				e.printStackTrace(System.err);
			}
		}
	}

	static void printUsage() {
		System.out.println("Useless internet crawler command line options: url1 [url2 [url3 ...]]");
	}
}
