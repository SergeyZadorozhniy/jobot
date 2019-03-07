package com.dell.jobot;

import lombok.val;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.http.HttpClientTransportOverHTTP;

public class Main {

	static final int QUEUE_CAPACITY = 1_000_000;
	static final int CACHE_CAPACITY = 1_000_000;
	static final int SELECTORS = 1;

	public static void main(final String... args) {
		if(0 == args.length) {
			printUsage();
		} else {

			/* Creating Jetty NIO HttpClient */
			val httpClient = new HttpClient(new HttpClientTransportOverHTTP(SELECTORS), null);
			try {
				httpClient.start();
			} catch (Exception e1) {
				e1.printStackTrace(System.err);
			}
			/* Creating Blocking queue for HttpRequest tasks */
			val urlsQueue = new PriorityBlockingQueue<HttpUrlProcessingTask>(QUEUE_CAPACITY);

			val uniqueUrlFilter = new FixedCacheUniquenessFilter<String>(CACHE_CAPACITY);
			val handler = new HttpUrlStreamHandler(url -> /* Removed as duplicate check HTTP_FILTER.test(url) &&*/ uniqueUrlFilter.test(url.toString()), urlsQueue);

			/* Creating separate Thread to listen Blocking urlsQueue and send new requests to NIO HttpClient(When Queue is empty Thread is just sleeping) */
			new Thread() {
				@Override
				public void run() {
					while (true) {
						try {
							val task = urlsQueue.poll(10, TimeUnit.DAYS);
							httpClient.newRequest(task.getUrl().toString())
								.send(task);
						} catch (InterruptedException e) {
							e.printStackTrace(System.err);
						}
					}
				}
			}.start();

			try {
				handler.handle(null, args);
			} catch(final Exception e) {
				e.printStackTrace(System.err);
			}
		}
	}

	static void printUsage() {
		System.out.println("Useless internet crawler command line options: url1 [url2 [url3 ...]]");
	}
}
