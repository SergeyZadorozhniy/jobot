package com.dell.jobot;

import lombok.NonNull;
import lombok.val;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.dell.jobot.monitor.PerformanceMonitor;

public class HttpUrlStreamHandler
implements RawUrlStreamHandler {

	public static final String OUTPUT_DIR = System.getProperty("user.home") + File.separator + ".jobot";
	public static final String LINKS_FILE_NAME = "links.txt";
	private static final Pattern PATH_SPECIAL_SYMBOLS = Pattern.compile("[:]");

	private final Predicate<URL> urlFilter;
	private final Queue<HttpUrlProcessingTask> urlsQueue;

	public HttpUrlStreamHandler(final Predicate<URL> urlFilter, final Queue<HttpUrlProcessingTask> urlsQueue) {
		this.urlFilter = urlFilter;
		this.urlsQueue = urlsQueue;
	}
	
	private void addUrlToQueue(HttpUrlProcessingTask urlTask) {
		urlsQueue.add(urlTask);
		PerformanceMonitor.trackTasksSubmit(urlTask);
	}

	@Override
	public void handle(final URL parent, final @NonNull String[] urls) {
		handle(parent, Arrays.stream(urls).filter(HyperlinkUtil.HTTP_FILTER)
				.map(HyperlinkUtil::cutAnchorAndQuery));
	}

	@Override
	public void handle(final URL parent, final @NonNull Stream<String> inStream) {
		List<URL> urls = inStream
				/* Cleaned up redundant/double checks */
				.map(UrlUtil::convertToUrl)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.filter(urlFilter)
				.map(url -> new HttpUrlProcessingTask(this, url, parent))
				.peek(this::addUrlToQueue)
				.map(HttpUrlProcessingTask::getUrl)
				.collect(Collectors.toList());

		if (urls.size() > 0) {
			val outputPath = parent == null ? Paths.get(OUTPUT_DIR, LINKS_FILE_NAME)
					: Paths.get(OUTPUT_DIR, parent.getHost(),
							PATH_SPECIAL_SYMBOLS.matcher(parent.getPath()).replaceAll("_"), LINKS_FILE_NAME);
			try {
				Files.createDirectories(outputPath.getParent());
			} catch (final IOException e) {
				e.printStackTrace(System.err);
			}
			try (val linksFileWriter = Files.newBufferedWriter(outputPath, StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING)) {
				urls.forEach(url -> {
					try {
						linksFileWriter.append(url.toString());
						linksFileWriter.newLine();
					} catch (final IOException e) {
						e.printStackTrace(System.err);
					}
				});
			} catch (final IOException e) {
				e.printStackTrace(System.err);
			}
		}
	}
}
