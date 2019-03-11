package com.dell.jobot;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.val;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import com.dell.jobot.monitor.PerformanceMonitor;
import static com.dell.jobot.HyperlinkUtil.extractLinks;

@Value
@EqualsAndHashCode(callSuper=false)
public class HttpUrlProcessingTask extends BufferingResponseListener
implements Comparable<HttpUrlProcessingTask> {

	private final HttpUrlStreamHandler handler;
	private final URL url;
	private final URL parent;

	public HttpUrlProcessingTask(final HttpUrlStreamHandler handler, final URL url,  final URL parent) {
		this.handler = handler;
		this.url = url;
		this.parent = parent;
	}
	
	/*
	 * On Jetty HttpCliet request complete (Each onComplete() call HttpClient make in dedicated
	 * Thread.)
	 */
	@Override
	public void onComplete(Result result) {
		PerformanceMonitor.trackUrlsRequestReceived();
		handleResponseContent(getContentAsInputStream());
	}

	void handleResponseContent(final InputStream stream) {
		System.out.print("."); // System.out.println("Downloading " + url + " ...");
		try (val contentReader = new BufferedReader(new InputStreamReader(stream))) {
			String line;
			/*
			 * Added 2 sets to separate internal an external links and put external to the top -> to
			 * distribute the load on external sites and reduce it for the start site
			 */
			val internalLinkBuff = new HashSet<String>();
			val externalLinkBuff = new HashSet<String>();
			while (null != (line = contentReader.readLine())) {
				extractLinks(url, line, internalLinkBuff, externalLinkBuff);
			}
			val resultUrlsList = new ArrayList<String>();
			resultUrlsList.addAll(externalLinkBuff);
			resultUrlsList.addAll(internalLinkBuff);
			if (!resultUrlsList.isEmpty()) {
				handler.handle(url, resultUrlsList.stream());
			}
		} catch (final IOException e) {
			System.err.println("I/O failure while reading the content from the url: \"" + url + "\"");
		} catch (final Throwable t) {
			t.printStackTrace(System.err);
		}
	}

	/*
	 * Priority uses for sorting in queue -> External links has high priority to distribute the load
	 * on external sites from initial site
	 */
	public int getPriority() {
		if (this.parent == null)
			return 2;
		else {
			if (this.parent.getHost().equals(this.url.getHost()))
				return 1;
			else {
				if (findSecondLevelDomain(this.parent.getHost())
						.equals(findSecondLevelDomain(this.url.getHost())))
					return 2;
				else
					return 3;
			}
		}
	}

	private String findSecondLevelDomain(String host) {
		val lastDot = host.lastIndexOf(".");
		if (lastDot != -1) {
			val secondDot = host.substring(0, lastDot).lastIndexOf(".");
			if (secondDot != -1) {
				return host.substring(secondDot + 1);
			} else {
				return host;
			}
		}
		throw new AssertionError(
				"Host exception host cannot contain less than one dot: [" + host + "]");
	}

	@Override
	public int compareTo(HttpUrlProcessingTask o) {
		Integer.compare(this.getPriority(), o.getPriority());
		return 0;
	}
}
