/*-
 * Copyright © 2021 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.data;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.SocketUtils;

import gda.configuration.properties.LocalProperties;
import gda.factory.FactoryException;
import gda.factory.FindableConfigurableBase;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * Allows a Data Server to be created per client
 */
@ServiceInterface(DataServerSpawner.class)
public class DataServerProcessManager extends FindableConfigurableBase implements DataServerSpawner {

	private static final Logger logger = LoggerFactory.getLogger(DataServerProcessManager.class);
	private Map<Integer, DataServerProcess> dservers = new HashMap<>();
	/** Path to Data Server executable */
	private String executable;
	/** Optional path for Data Server logback configuration */
	private String customLoggingConfig;


	@Override
	public void configure() throws FactoryException {
		Objects.requireNonNull(executable, "DataServer executable not set");
		Runtime.getRuntime().addShutdownHook(new Thread(this::stopAllServers));
		setConfigured(true);
	}

	@Override
	public int createDataserver(int clientId) {
		synchronized (this) {
			if (!dservers.containsKey(clientId)) {
				var port = SocketUtils.findAvailableTcpPort(8690, 8720);
				logger.info("Creating Dataserver for client {} (port {})", clientId, port);
				try {
					dservers.put(clientId, new DataServerProcess(port, spawnDataServer(port)));
					return port;
				} catch (IOException e) {
					throw new IllegalStateException("Could not create new Dataserver", e);
				}
			} else {
				logger.warn("Client id {} already has a Dataserver", clientId);
				return dservers.get(clientId).getPort();
			}
		}

	}

	@Override
	public void destroyDataserver(int clientId) {
		synchronized (this) {
			if (dservers.containsKey(clientId)) {
				logger.info("Closing Dataserver for client {}", clientId);
				var ph = dservers.remove(clientId);
				ph.destroyServer();
			} else {
				logger.warn("Client id {} doesn't have a dataserver to destroy", clientId);
			}
		}
	}

	private void stopAllServers() {
		logger.info("DataServer shutdown hook called");
		dservers.values().forEach(DataServerProcess::destroyServer);
		dservers.clear();
	}

	/**
	 * @return port of the created server
	 * @throws IOException
	 */
	private ProcessHandle spawnDataServer(int port) throws IOException {
		String tstamp = ZonedDateTime.now(ZoneId.systemDefault())
				.format(DateTimeFormatter.ofPattern("uuuuMMdd-HHmmss"));
		var logFilePath = Paths.get(LocalProperties.get(LocalProperties.GDA_LOGS_DIR), "gda-dataserver-" + tstamp + "-" + port + ".txt");
		Stream<String> loggingArgs = customLoggingConfig == null ? Stream.empty() : Stream.of("-vmargs", "-Dlogback.configurationFile=" + customLoggingConfig);
		var appArgs = Stream.of(executable, "-port", Integer.toString(port));
		ProcessBuilder processBuilder = new ProcessBuilder(concat(appArgs, loggingArgs).collect(toList()));
		Process process = processBuilder.redirectErrorStream(true).redirectOutput(logFilePath.toFile()).start();
		return process.toHandle();
	}


	public String getExecutable() {
		return executable;
	}


	public void setExecutable(String executable) {
		this.executable = executable;
	}

	private static class DataServerProcess {
		private final int port;
		private final ProcessHandle processHandle;
		public DataServerProcess(int port, ProcessHandle processHandle) {
			this.port = port;
			this.processHandle = processHandle;
		}
		public int getPort() {
			return port;
		}
		public void destroyServer() {
			processHandle.descendants().forEach(ProcessHandle::destroyForcibly);
			processHandle.destroyForcibly();
		}
	}

	public String getCustomLoggingConfig() {
		return customLoggingConfig;
	}

	public void setCustomLoggingConfig(String customLoggingConfig) {
		this.customLoggingConfig = customLoggingConfig;
	}
}
