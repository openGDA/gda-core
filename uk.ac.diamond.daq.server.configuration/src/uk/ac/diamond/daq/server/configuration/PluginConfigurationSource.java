/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.server.configuration;

import static java.util.Collections.emptyList;
import static java.util.Collections.list;

import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Stream;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginConfigurationSource implements CoreConfigurationSource {
	private static final Logger logger = LoggerFactory.getLogger(PluginConfigurationSource.class);

	/** File pattern used to match properties files */
	private static final String PROPERTIES = "*.properties";
	/** File pattern used to match XML files */
	private static final String XML = "*.xml";

	private Bundle bundle;

	private List<URL> springXml = emptyList();
	private List<URL> propertiesFiles = emptyList();
	private List<URL> logging = emptyList();

	public void activate(BundleContext ctx) {
		bundle = ctx.getBundle();
		springXml = findFiles("spring", XML);
		propertiesFiles = findFiles("properties", PROPERTIES);
		logging = findFiles("logging", XML);
	}

	@Override
	public Stream<URL> getSpringXml() {
		return springXml.stream();
	}

	@Override
	public Stream<URL> getPropertiesFiles() {
		return propertiesFiles.stream();
	}

	@Override
	public Stream<URL> getLoggingConfiguration() {
		return logging.stream();
	}

	private List<URL> findFiles(String path, String filter) {
		Enumeration<URL> entries =
				bundle.findEntries("/resource/core_config/" + path, filter, true);
		if (entries == null) {
			logger.debug("Found no entries for {}/{} [{}]", path, filter, bundle.getSymbolicName());
			return emptyList();
		}
		return list(entries);
	}

	@Override
	public String toString() {
		return "PluginCoreConfiguration(%s)".formatted(bundle.getSymbolicName());
	}
}
