/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package gda.jython.authoriser;

import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.persistence.jythonshelf.LocalParameters;

/**
 * Authoriser implementation that reads authorisation level and staff status from
 * the same xml file. File should be of the form
 * <pre>
 * {@code
 * <permissions>
 *   <user fedid="fed12345" level="3" staff="true" />
 *   <user fedid="abc54321" level="2" />
 * </permissions>
 * }
 * </pre>
 * <ul>
 * <li>Any non "user" elements are ignored</li>
 * <li>Any entries without a fedid attribute are ignored</li>
 * <li>"staff" is optional and defaults to false</li>
 * <li>"level" is optional and defaults to 1 (usually the default anyway)</li>
 * </ul>
 */
public class SingleFileAuthoriser implements Authoriser {

	private static final Logger logger = LoggerFactory.getLogger(SingleFileAuthoriser.class);

	private static final String GDA_USER_PERMISSIONS_DIR = "gda.user.permissions.dir";
	private static final String AUTHORISATION_FILENAME = "permissions";

	private final int userLevel = LocalProperties.getAsInt(DEFAULT_LEVEL_PROPERTY, DEFAULT_LEVEL);
	private final int staffLevel = LocalProperties.getAsInt(DEFAULT_STAFF_LEVEL_PROPERTY, DEFAULT_STAFF_LEVEL);

	private Map<String, User> users;

	public SingleFileAuthoriser() throws ConfigurationException, IOException {
		this(defaultPermissionsDirectory(), AUTHORISATION_FILENAME);
	}

	public SingleFileAuthoriser(String directory, String filename) throws ConfigurationException, IOException {
		var parameters = LocalParameters.getXMLConfiguration(directory, filename, false);
		logger.debug("Loading user permissions from '{}'", parameters.getFile());
		parameters.refresh();
		users = parameters.getRootNode()
				.getChildren("user")
				.stream()
				.map(this::userFromNode)
				.flatMap(Optional::stream)
				.collect(toMap(u -> u.name, u -> u));
	}

	@Override
	public int getAuthorisationLevel(String username) {
		return users.computeIfAbsent(username, User::new).level();
	}

	@Override
	public boolean isLocalStaff(String username) {
		return users.computeIfAbsent(username, User::new).staff();
	}

	private static String defaultPermissionsDirectory() {
		var directory = LocalProperties.get(GDA_USER_PERMISSIONS_DIR);
		if (directory == null) {
			directory = Paths.get(LocalProperties.getConfigDir(), "xml").toString();
		}
		return directory;
	}

	public Optional<User> userFromNode(ConfigurationNode node) {
		String name = null;
		var level = userLevel;
		var levelSet = false;
		var staff = false;
		for (var att: node.getAttributes()) {
			switch (att.getName()) {
			case "fedid" -> name = att.getValue().toString();
			case "level" -> {
				try {
					level = Integer.valueOf(att.getValue().toString());
					levelSet = true;
				} catch (NumberFormatException nfe) {
					logger.error("Invalid level for user", nfe);
				}
			}
			case "staff" -> {
				staff = "true".equals(att.getValue());
				if (staff && !levelSet) {
					level = staffLevel;
				}
			}
			default -> logger.debug("unrecognised attribute: {}", att.getName());
			}
		}
		if (name == null) {
			logger.error("No name set for user");
			return Optional.empty();
		}
		return Optional.of(new User(name, level, staff));
	}

	private record User (String name, int level, boolean staff) {
		private User(String name) {
			this(name, DEFAULT_LEVEL, false);
		}
	}
}
