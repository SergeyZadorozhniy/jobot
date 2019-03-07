package com.dell.jobot.monitor;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import com.dell.jobot.HttpUrlProcessingTask;

public class PerformanceMonitor {
	private static final int STEP = 1000;
	private static final int REUESTS_STEP = 100;
	private static final AtomicLong startTime = new AtomicLong(System.currentTimeMillis());
	private static final AtomicLong countFromLastTime = new AtomicLong(0);
	private static final AtomicLong timeSum = new AtomicLong(0L);
	private static final AtomicInteger steps = new AtomicInteger(0);
	private static final AtomicLong completedRequests = new AtomicLong(0L);

	public static final void trackTasksSubmit(HttpUrlProcessingTask task) {
		if (countFromLastTime.incrementAndGet() % STEP == 0) {
			Long result = System.currentTimeMillis() - startTime.get();
			startTime.set(System.currentTimeMillis());
			countFromLastTime.set(0);
			long currentTimeSum = timeSum.addAndGet(result);
			int currentSteps = steps.addAndGet(1);
			System.err.println("\n>>>>>>> [ADD " + STEP + " Urls takes]: " + result + "ms Avg: "
					+ currentTimeSum / currentSteps + "ms");
		}
	}

	public static final void trackUrlsCollected(int count) {
		int size;
		if ((size = count) % STEP == 0)
			System.err.println("\n>>>>>>> [Urls Collected]: " + size + " urls");
	}

	public static final void trackUrlsRequestReceived() {
		long requests;
		if ((requests = completedRequests.incrementAndGet()) % REUESTS_STEP == 0) {
			System.err.println("\n>>>>>>> [" + REUESTS_STEP + " Request completed]: " + requests);
		}
	}
}
