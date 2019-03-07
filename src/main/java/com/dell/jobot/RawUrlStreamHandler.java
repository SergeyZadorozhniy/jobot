package com.dell.jobot;

import lombok.NonNull;

import java.net.URL;
import java.util.stream.Stream;

public interface RawUrlStreamHandler {

	/**
	 @param parent The origin URL, may be null
	 @param inStream The stream of the raw URLs which should be converted, filtered and handled
	 */
	void handle(final URL parent, final @NonNull Stream<String> inStream);
}
