/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package uk.ac.gda.api.io;

import static gda.configuration.properties.LocalProperties.GDA_DATAWRITER_DIR;
import static gda.configuration.properties.LocalProperties.GDA_VISIT_DIR;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.data.metadata.GDAMetadataProvider;
import gda.device.DeviceException;

/**
 * A class for constructing path names based on a template. The template can be either passed in directly or stored in a
 * Java property and the name of the property is passed in.
 * <p>
 * The template should be composed of fixed text and names for GDA metadata items. These latter names need to be
 * delimited by two $ symbols
 * <p>
 * e.g. /dls/$instrument$/data/$year$/$visit$.
 * <p>
 * In this example the template contains three items that are to be substituted in the construction of the path. These
 * are instrument, year and visit. These values of these four items will be obtained from GDA metadata. In this example
 * they are likely to be of metadata types MetadataEntry.PROPERTY, MetadataEntry.DATE and MetadataEntry.ICAT
 * respectively.
 * <p>
 * For the path construction to work properly the GDA metadata must have been created, contain the relevant metadata
 * entries, and be named GDAMetadata. In the above example we would expect to get a path of the the general form
 * something like
 * <p>
 * /dls/i02/data/2007/mx667-1
 * <p>
 * If there isn't currently a valid visit and/or no Ikitten then the special case visit defaults to 0-0, producing for
 * the example
 * <p>
 * /dls/i02/data/2007/0-0
 * <p>
 * If the special cases of instrument and facility do not exist as metadata items then by default the values of the Java
 * properties gda.instrument and gda.facility are used.
 */
public class PathConstructor implements IPathConstructor{
	private static final Logger logger = LoggerFactory.getLogger(PathConstructor.class);

	private enum PathToken {

		PROPOSAL("", "0"),

		VISIT(LocalProperties.GDA_DEF_VISIT, "0-0"),

		INSTRUMENT(LocalProperties.GDA_INSTRUMENT, ""),

		FACILITY(LocalProperties.GDA_FACILITY, ""),

		SUBDIRECTORY("", ""),

		HOSTID("", HostId.getId()),

		;

		private PathToken(String defaultProperty, String defaultValue) {
			this.defaultProperty = defaultProperty;
			this.defaultValue = defaultValue;
		}

		private final String defaultProperty;
		private final String defaultValue;

		public String getDefaultProperty() {
			return defaultProperty;
		}

		public String getDefaultValue() {
			return defaultValue;
		}
	}

	/**
	 * @return the default java property name that contains the data directory template.
	 */
	@Override
	public String getDefaultPropertyName() {
		return GDA_DATAWRITER_DIR;
	}

	/**
	 * Construct a path based on a template containing text and metadata item names.
	 *
	 * @return The constructed path.
	 */
	@Override
	public String createFromDefaultProperty() {
		return createFromProperty(GDA_DATAWRITER_DIR);
	}

	/**
	 * For client-side RCP classes wanting the data directory for this client (not necessarily the data directory in current use).
	 *
	 * Construct the path based on a template containing text and metadata item names. Override the metadata if values
	 * placed in LocalProperties have been set by the RCP application - may not always want to use what's in the
	 * metadata. In GDA ObjectServers the metadata for visit and user only relate to the current baton holder.
	 *
	 * @return The constructed path.
	 * @deprecated use {@link PathConstructor#getClientVisitDirectory()}
	 *     or {@link PathConstructor#getVisitSubdirectory(String)}
	 */
	@Deprecated
	@Override
	public String createFromRCPProperties() {
		logger.warn("Using deprecated createFromRCPProperties. Use getClientVisitDirectory");
		HashMap<String, String> metadataOverrides = new HashMap<>();
		if (LocalProperties.get(LocalProperties.RCP_APP_VISIT) != null) {
			metadataOverrides.put("visit", LocalProperties.get(LocalProperties.RCP_APP_VISIT));
		}
		if (LocalProperties.get(LocalProperties.RCP_APP_USER) != null) {

			metadataOverrides.put("federalid", LocalProperties.get(LocalProperties.RCP_APP_USER));
			metadataOverrides.put("user", LocalProperties.get(LocalProperties.RCP_APP_USER));
		}
		return createFromProperty(GDA_DATAWRITER_DIR, metadataOverrides);
	}

	/**
	 * Construct a path based on a template containing text and metadata item names.
	 *
	 * @param property
	 *            The name of a Java property from which to obtain the path template.
	 * @return The constructed path.
	 */
	@Override
	public String createFromProperty(String property) {
		return createFromProperty(property, null);
	}

	/**
	 * Construct a path based on a template containing text and metadata item names.
	 *
	 * @param property
	 *            The name of a Java property from which to obtain the path template.
	 * @param overrides
	 * @see PathConstructor#createFromTemplate(String, Map)
	 * @return The constructed path.
	 */
	@Override
	public String createFromProperty(String property, Map<String, String> overrides) {
		String template = LocalProperties.get(property);
		if (template == null) {
			throw new IllegalArgumentException("Could not find property '" + property + "'");
		}
		return createFromTemplate(template, overrides);
	}

	/**
	 * Construct a path based on a template containing text and metadata item names.
	 *
	 * @param template
	 *            The path template.
	 * @return The constructed path.
	 */
	@Override
	public String createFromTemplate(String template) {
		return createFromTemplate(template, null);
	}

	/**
	 * Construct a path based on a template containing text and metadata item names and resolve any
	 * symlinks or .. components.
	 *
	 * If it's not possible to resolve the canonical path (due to IOExecption), this returns
	 * the path as written in the template.
	 *
	 * @param template
	 *            The path template.
	 * @param overrides
	 *            HashMap(<String>,<String>) - values to override any values found in the metadata system. This is
	 *            because some of the metadata values are correct only for the current baton holder. There will be times
	 *            when the user might not be the current beamline baton holder e.g. during client startup before the
	 *            baton has been taken.; or be working 'offline' or unit testing.
	 * @return The canonical form of the constructed path.
	 */
	public String createFromTemplate(String template, Map<String, String> overrides) {
		overrides = overrides == null ? new HashMap<>() : new HashMap<>(overrides);
		overrides.computeIfAbsent("year", s -> String.valueOf(LocalDate.now().getYear()));
		StringTokenizer st = new StringTokenizer(template, "$");
		StringBuilder path = new StringBuilder();

		while (st.hasMoreTokens()) {
			path.append(st.nextToken());
			if (st.hasMoreTokens()) {
				path.append(interpret(st.nextToken(), overrides));
			}
		}

		try {
			return new File(path.toString()).getCanonicalPath();
		} catch (IOException | SecurityException e) {
			logger.warn("Could not get canonical path from {} - may not exist", path, e);
			return path.toString();
		}
	}

	/**
	 * Get the path to the root of the current visit directory
	 * based on template at {link {@link LocalProperties#GDA_VISIT_DIR}
	 * usually ${gda.data}/$year$/$visit$
	 *
	 * @return path to visit directory
	 */
	@Override
	public String getVisitDirectory() {
		return createFromProperty(GDA_VISIT_DIR);
	}

	/**
	 * Return the path of a subdirectory in the root of the client's visit
	 *
	 * @param subdirectory
	 * @return Path of visit subdirectory without trailing /
	 */
	@Override
	public String getVisitSubdirectory(String subdirectory) {
		return Paths.get(getVisitDirectory(), subdirectory).toString();
	}

	/**
	 * For client-side RCP classes wanting the root visit directory for this client
	 * (not necessarily the data directory in current use).
	 *
	 * Based on template at {link {@link LocalProperties#GDA_VISIT_DIR}
	 * usually ${gda.data}/$year$/$visit$
	 *
	 * Construct the path based on a template containing text and metadata item names. Override the metadata if values
	 * placed in LocalProperties have been set by the RCP application - may not always want to use what's in the
	 * metadata. In GDA ObjectServers the metadata for visit and user only relate to the current baton holder.
	 *
	 * @return The constructed path.
	 */
	@Override
	public String getClientVisitDirectory() {
		Map<String, String> metadataOverrides = new HashMap<>();
		if (LocalProperties.get(LocalProperties.RCP_APP_VISIT) != null) {
			metadataOverrides.put("visit", LocalProperties.get(LocalProperties.RCP_APP_VISIT));
		}
		if (LocalProperties.get(LocalProperties.RCP_APP_USER) != null) {

			metadataOverrides.put("federalid", LocalProperties.get(LocalProperties.RCP_APP_USER));
			metadataOverrides.put("user", LocalProperties.get(LocalProperties.RCP_APP_USER));
		}
		return createFromProperty(GDA_VISIT_DIR, metadataOverrides);
	}

	/**
	 * Return the path of a subdirectory in the root of the client's visit
	 *
	 * @param subdirectory
	 * @return Path of visit subdirectory without trailing /
	 */
	@Override
	public String getClientVisitSubdirectory(String subdirectory) {
		return Paths.get(getClientVisitDirectory(), subdirectory).toString();
	}

	private String interpret(String s, Map<String, String> overrides) {
		String value = "";

		// Use the overrides in preference to metadata values. This is useful as the metadata is correct for the
		// client with the baton, so in certain circumstances (e.g. a new client starting up) the metadata values may be
		// misleading.
		if (overrides != null && overrides.containsKey(s)) {
			return overrides.get(s);
		}

		if (s.equals("proposal")) {
			 return interpret("visit", overrides).split("-")[0];
		}

		if (GDAMetadataProvider.getInstance() != null) {
			try {
				final Optional<PathToken> token = findToken(s);
				final String fallbackProperty = token.map(PathToken::getDefaultProperty).orElse("");
				final String defaultValue = token.map(PathToken::getDefaultValue).orElse("");
				value = GDAMetadataProvider.getInstance().getMetadataValue(s, fallbackProperty, defaultValue);
			} catch (DeviceException e) {
				logger.error("exception received querying for metadata", e);
			}
		} else {
			logger.error("cannot find metadata object");
		}

		return value;
	}

	private Optional<PathToken> findToken(String tokenToFind) {
		try {
			return Optional.of(PathToken.valueOf(tokenToFind.toUpperCase()));
		} catch (IllegalArgumentException e) {
			logger.warn("Unknown token '{}'", tokenToFind);
			return Optional.empty();
		}
	}

	@Override
	public String getDefaultDataDir() {
		return new PathConstructor().createFromRCPProperties();
	}

	@Override
	public String getFromTemplate(String template) {
		return new PathConstructor().createFromTemplate(template);
	}
}
