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

import gda.factory.Findable;

public interface DataServerSpawner extends Findable {

	/**
	 * Create a new Dataserver (spawned by the GDA server) for
	 * the given client ID
	 * @param clientId the baton manager ID for the client
	 * @return port of newly created dataserver
	 */
	int createDataserver(int clientId);

	/**
	 * Destroy the associated Dataserver process for the given client
	 * @param clientId
	 */
	void destroyDataserver(int clientId);

}
