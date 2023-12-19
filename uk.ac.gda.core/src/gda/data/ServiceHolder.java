/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

import org.eclipse.dawnsci.nexus.device.INexusDeviceService;

import gda.data.scan.datawriter.NexusDataWriterConfiguration;
import uk.ac.gda.core.GDACoreActivator;

/**
 * Holds the IRunnableDeviceService which the file registrar uses
 * to register itself with.
 */
public class ServiceHolder {

	private static volatile NexusDataWriterConfiguration nexusDataWriterConfiguration;

	public void setNexusWriterConfiguration(NexusDataWriterConfiguration nexusDataWriterConfiguration) {
		// Note: this method is not typically called by OSGi as this bean is declared in spring which is loaded after OSGi wiring
		// it should be set by unit tests that require it
		if (nexusDataWriterConfiguration == null && ServiceHolder.nexusDataWriterConfiguration != null) {
			// TODO delete this when deprecated NDW methods are removed
			ServiceHolder.nexusDataWriterConfiguration.unregisterFromOsgi();
		}
		ServiceHolder.nexusDataWriterConfiguration = nexusDataWriterConfiguration;
	}

	public static NexusDataWriterConfiguration getNexusDataWriterConfiguration() {
		if (nexusDataWriterConfiguration == null) {
			synchronized (ServiceHolder.class) { // safe double-checked locking idiom
				if (nexusDataWriterConfiguration == null) {
					nexusDataWriterConfiguration = GDACoreActivator.getService(NexusDataWriterConfiguration.class)
							.orElseGet(NexusDataWriterConfiguration::new); // create an empty bean if not defined in spring
				}
			}
		}

		return nexusDataWriterConfiguration;
	}

}
