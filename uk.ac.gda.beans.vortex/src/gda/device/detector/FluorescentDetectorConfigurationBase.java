/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.device.detector;

import gda.factory.Configurable;
import gda.factory.Findable;
import gda.jython.IJythonNamespace;
import gda.jython.InterfaceProvider;

import java.util.concurrent.FutureTask;
import java.util.concurrent.Callable;

public abstract class FluorescentDetectorConfigurationBase implements FluorescentDetectorConfiguration, Findable, Configurable {

	protected void placeInJythonNamespace(final String name, final FluorescentDetectorConfiguration config) {

		FutureTask<Void> placeInJythonTask = new FutureTask<Void>(new Callable<Void>() {
		@Override
		public Void call() {
//			try for 10 secs and give up
			for (int i=0; i<10; i++) {
				try {
					Thread.sleep(1000);
					IJythonNamespace jythonNamespace = InterfaceProvider.getJythonNamespace();
					jythonNamespace.placeInJythonNamespace(name, config);
					return null;
				} catch (Exception e) {
					// ignore
				}
			}
			throw new IllegalArgumentException("Failed to put fluorescence detector configuration '" + name + "' into the Jython namespace!");
		}
	});

	new Thread(placeInJythonTask, "placeEnergyScanIntoJythonNamespace").start();
	}
}
