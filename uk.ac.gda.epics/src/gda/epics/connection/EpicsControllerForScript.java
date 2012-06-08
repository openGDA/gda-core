/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.epics.connection;

import gov.aps.jca.CAException;

/**
 * EpicsControllerForScript Class
 */
public class EpicsControllerForScript extends EpicsController {
	/**
	 * Singleton instance.
	 */
	private static EpicsControllerForScript instanceForScript = null;

	/**
	 * Singleton pattern to get instance of EpicsController.
	 * @return <code>EpicsController</code> instance.
	 */
	public static synchronized EpicsControllerForScript getInstance()
	{
		return getInstance(true);
	}

	/**
	 * Singleton pattern to get instance of EpicsController.
	 * @param contextRequired is normally True, but False for testing, to avoid leaving an orphan process.
	 * @return <code>EpicsController</code> instance.
	 */
	public static synchronized EpicsControllerForScript getInstance(boolean contextRequired)
	{
		// TODO not nice and clean
		try {
			if (instanceForScript == null)
				instanceForScript = new EpicsControllerForScript(contextRequired);
			return instanceForScript;
		} catch (Throwable th) {
			th.printStackTrace();
			throw new RuntimeException("failed to create EpicsControllerForScript instance", th);
		}
	}

	/**
	 * Protected constructor.
	 * 
	 * @throws CAException
	 */
	protected EpicsControllerForScript(boolean contextRequired) throws CAException {
		super(contextRequired);
	}

}
