package com.policyconfig.resolvercore.source;

import com.policyconfig.resolvercore.exception.ErrorCode;
import com.policyconfig.resolvercore.exception.PolicyConfigException;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * UTF-8 .properties 파일을 읽어서 제공하는 ConfigSource.
 */
public final class PropertiesFileConfigSource implements RefreshableConfigSource {

	private final Path path;
	private final AtomicBoolean watching = new AtomicBoolean(false);
	private final AtomicReference<Thread> watcherThread = new AtomicReference<>();
	private volatile java.nio.file.WatchService watchService;
	private volatile Runnable onChange;

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

	@Override
	public void watch(Runnable onChange) {
		if (watching.compareAndSet(false, true)) {
			this.onChange = onChange;
			try {
				startWatcher();
			} catch (IOException e) {
				watching.set(false);
				throw new PolicyConfigException(
					ErrorCode.SOURCE_WATCH_FAILED,
					"Failed to watch properties file: " + path,
					e
				);
			}
		}
	}

	@Override
	public void close() throws Exception {
		watching.set(false);
		WatchServiceCloser.closeQuietly(watchService);
		Thread thread = watcherThread.getAndSet(null);
		if (thread != null) {
			thread.interrupt();
		}
	}

	private void startWatcher() throws IOException {
		java.nio.file.Path absolute = path.toAbsolutePath();
		java.nio.file.Path parent = absolute.getParent();
		if (parent == null) {
			throw new IOException("properties file has no parent directory");
		}
		java.nio.file.WatchService service = absolute.getFileSystem().newWatchService();
		parent.register(service,
			StandardWatchEventKinds.ENTRY_CREATE,
			StandardWatchEventKinds.ENTRY_MODIFY,
			StandardWatchEventKinds.ENTRY_DELETE);
		this.watchService = service;
		Thread thread = new Thread(() -> watchLoop(parent, absolute.getFileName().toString()), "policy-config-properties-watch");
		thread.setDaemon(true);
		watcherThread.set(thread);
		thread.start();
	}

	private void watchLoop(java.nio.file.Path parent, String fileName) {
		try {
			while (watching.get()) {
				java.nio.file.WatchKey key = watchService.take();
				boolean changed = key.pollEvents().stream().anyMatch(event -> {
					java.nio.file.Path changedPath = (java.nio.file.Path) event.context();
					return changedPath != null && changedPath.getFileName().toString().equals(fileName);
				});
				key.reset();
				if (changed && onChange != null) {
					onChange.run();
				}
			}
		} catch (InterruptedException ignored) {
			Thread.currentThread().interrupt();
		} catch (Exception e) {
			throw new PolicyConfigException(
				ErrorCode.SOURCE_WATCH_FAILED,
				"File watch loop failed for: " + path,
				e
			);
		}
	}

	private static final class WatchServiceCloser {
		private static void closeQuietly(java.nio.file.WatchService watchService) {
			if (watchService != null) {
				try {
					watchService.close();
				} catch (IOException ignored) {
				}
			}
		}
	}
}
