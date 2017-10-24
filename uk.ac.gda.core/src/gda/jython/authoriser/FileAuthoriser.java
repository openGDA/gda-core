/*-
 * Copyright © 2010 Diamond Light Source Ltd., Science and Technology
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

package gda.jython.authoriser;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.persistence.jythonshelf.LocalParameters;

/**
 * Performs authentication via a file listing usernames and access levels.
 */
public class FileAuthoriser implements Authoriser {

	private static final Logger logger = LoggerFactory.getLogger(FileAuthoriser.class);

	/**
	 * The java property which defines the default authorisation level for a user if not explicitly listed.
	 */
	public static final String DEFAULTLEVELPROPERTY = "gda.accesscontrol.defaultAuthorisationLevel";

	/**
	 * The java property which defines the default authorisation level for a member of staff if not explicitly listed.
	 */
	public static final String DEFAULTSTAFFLEVELPROPERTY = "gda.accesscontrol.defaultStaffAuthorisationLevel";

	private static final String GDA_USER_PERMISSIONS_DIR = "gda.user.permissions.dir";

	private static final String AUTHORISATIONLEVELS = "user_permissions";

	private static final String BEAMLINESTAFF = "beamlinestaff";

	/**
	 * Program to print out what's in a password file
	 *
	 * @param args
	 */
	public static void main(String[] args) {

		FileAuthoriser lister = new FileAuthoriser();
		UserEntry[] entries = lister.getEntries();

		for (UserEntry entry : entries) {
			System.out.println("User: " + entry.getUserName() + " level: " + entry.getAuthorisationLevel());
		}
	}

	/**
	 * Constructor
	 */
	public FileAuthoriser() {
	}

	/**
	 * @return Vector of strings of the entries in this file
	 */
	public UserEntry[] getEntries() {
		try {
			FileConfiguration configFile = openConfigFile();
			UserEntry[] entries = new UserEntry[0];

			@SuppressWarnings("rawtypes")
			Iterator i = configFile.getKeys();
			while (i.hasNext()) {
				String username = (String) i.next();
				if (!username.equals("")) {
					int level = configFile.getInt(username);
					entries = (UserEntry[]) ArrayUtils.add(entries, new UserEntry(username, level,
							isLocalStaff(username)));
				}
			}

			return entries;

		} catch (Exception e) {
			logger.error("Exception while trying to read file of list of user authorisation levels", e);
			return null;
		}

	}

	/**
	 * Adds an entry to the file
	 *
	 * @param username
	 * @param newLevel
	 * @param isStaff
	 */
	public void addEntry(String username, int newLevel, boolean isStaff) {
		try {
			FileConfiguration configFile = openConfigFile();
			if (!configFile.containsKey(username)) {
				configFile.setProperty(username, newLevel);
				configFile.save();
			}
			if (isStaff) {
				FileConfiguration configFile2 = openStaffFile();
				if (!configFile2.containsKey(username)) {
					configFile2.setProperty(username, newLevel);
					configFile2.save();
				}
			}
		} catch (Exception e) {
			logger.error("Exception while trying to write new entry to file of list of user authorisation levels", e);
		}
	}

	/**
	 * Removes an entry from the file
	 *
	 * @param username
	 */
	public void deleteEntry(String username) {
		try {
			FileConfiguration configFile = openConfigFile();
			if (configFile.containsKey(username)) {
				configFile.clearProperty(username);
				configFile.save();
			}
		} catch (Exception e) {
			logger.error("Exception while trying to delete an entry from file", e);
		}
	}
	@Override
	public int getAuthorisationLevel(String username) {
		try {
			return openConfigFile().getInt(username);
		} catch (Exception e) {
			if (isLocalStaff(username))
			{
				return LocalProperties.getInt(FileAuthoriser.DEFAULTSTAFFLEVELPROPERTY, 2);
			}
			return LocalProperties.getInt(FileAuthoriser.DEFAULTLEVELPROPERTY, 1);
		}
	}

	@Override
	public boolean isLocalStaff(String username) {
		try {
			return openStaffFile().containsKey(username);
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean hasAuthorisationLevel(String username) {
		try {
			return openConfigFile().getInt(username) != -1;
		} catch (Exception e) {
			return false;
		}
	}

	private FileConfiguration openConfigFile() throws ConfigurationException, IOException {
		String defXmlDir = LocalProperties.get(LocalProperties.GDA_CONFIG) + File.separator + "xml";
		String xmlDir = LocalProperties.get(GDA_USER_PERMISSIONS_DIR, defXmlDir);
		FileConfiguration configFile = LocalParameters.getXMLConfiguration(xmlDir, AUTHORISATIONLEVELS, true);
		configFile.clear();
		configFile.load();
		return configFile;
	}

	private FileConfiguration openStaffFile() throws ConfigurationException, IOException {
		String defXmlDir = LocalProperties.get(LocalProperties.GDA_CONFIG) + File.separator + "xml";
		String xmlDir = LocalProperties.get(GDA_USER_PERMISSIONS_DIR, defXmlDir);
		FileConfiguration configFile = LocalParameters.getXMLConfiguration(xmlDir, BEAMLINESTAFF, true);
		configFile.clear();
		configFile.load();
		return configFile;
	}
}
