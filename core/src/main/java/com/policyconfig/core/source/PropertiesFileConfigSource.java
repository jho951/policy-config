package com.policyconfig.core.source;

import com.policyconfig.core.exception.ErrorCode;
import com.policyconfig.core.exception.PolicyConfigException;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * UTF-8 .properties 파일을 읽어서 제공하는 ConfigSource.
 */
public final class PropertiesFileConfigSource implements ConfigSource {

	private final Path path;

	public PropertiesFileConfigSource(Path path) {
		this.path = path;
	}

	@Override
	public java.util.Map<String, String> load() {
		Properties props = new Properties();
		try (Reader r = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			props.load(r);
		} catch (IOException e) {
			throw new PolicyConfigException(
				ErrorCode.SOURCE_LOAD_FAILED,
				"Failed to load properties file: " + path,
				e
			);
		}
		return new PropertiesConfigSource(props).load();
	}
}
