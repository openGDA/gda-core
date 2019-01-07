/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.timing;

import java.io.File;
import java.io.IOException;

import gda.factory.Findable;
import gda.observable.IObservable;
import uk.ac.gda.server.ncd.timing.data.SimpleTimerConfiguration;

public interface TimerController extends Findable, IObservable {
	SimpleTimerConfiguration getLastUsedConfiguration();

	boolean configureTimer (SimpleTimerConfiguration simpleTimerConfiguration) throws HardwareTimerException;

	SimpleTimerConfiguration loadTimer (File configurationFile) throws IOException;

	void saveTimer (File configurationFile, SimpleTimerConfiguration simpleTimerConfiguration) throws IOException;

	String getConfigurationFileExtension ();
}
