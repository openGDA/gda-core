/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.server.rcpcontroller;

import gda.factory.Findable;
import gda.observable.IObservable;

/**
 * Interface for object on server that can be used to open a view on the client
 */
public interface RCPController extends IObservable, Findable {
	void openView(String id);

	void openPerspective(String id);

	void setPreference(String id, Object value);

	void resetPreference(String id);
}
