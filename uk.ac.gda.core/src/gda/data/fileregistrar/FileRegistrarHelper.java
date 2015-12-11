/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.data.fileregistrar;

import gda.factory.Finder;

import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Class that can be used by devices to register files for archiving etc.
 * Mainly for use outside of scans.
 */
public class FileRegistrarHelper {

	private static List<IFileRegistrar> registrars;

	private static void fillRegistrars() {
		if (registrars == null || registrars.isEmpty()) {
			Map<String, IFileRegistrar> findables = Finder.getInstance().getFindablesOfType(IFileRegistrar.class);

			registrars = new Vector<IFileRegistrar>(findables.values());
		}
	}

	public static void forceRediscover() {
		registrars = null;
	}

	/**
	 * Register file with all configured instances of IFileRegistrar
	 * @param file
	 */
	public static void registerFile(String file) {
		fillRegistrars();

		for (IFileRegistrar registry : registrars) {
			registry.registerFile(file);
		}
	}
	/**
	 * Register files with all configured instances of IFileRegistrar
	 * @param files
	 */
	public static void registerFiles(String[] files) {
		fillRegistrars();

		for (IFileRegistrar registry : registrars) {
			registry.registerFiles(files);
		}
	}
	/**
	 * Register files with all configured instances of IFileRegistrar
	 * @param files
	 */
	public static void registerFiles(List<String> files) {
		fillRegistrars();

		for (IFileRegistrar registry : registrars) {
			registry.registerFiles(files.toArray(new String[0]));
		}
	}
}