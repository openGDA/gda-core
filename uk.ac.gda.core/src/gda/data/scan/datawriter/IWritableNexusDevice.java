/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package gda.data.scan.datawriter;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.january.dataset.SliceND;


/**
 * An {@link INexusDevice} that can be written to at each point in the scan. This is
 * required if the device does already not write its data at each point in the scan,
 * when it is either moved to (scannables) or exposed (detectors). The method called would depend
 * on the scanning API being used.
 *
 * TODO where does this interface belong? move it to dawnsci, or keep in same package
 *
 * @param <N> the type of nexus object to be created, a sub-interface of {@link NXobject},
 *   e.g. {@link NXdetector}
 */
public interface IWritableNexusDevice<N extends NXobject> extends INexusDevice<N> {

	/**
	 * Write the given data object at the given scan slice.
	 *
	 * @param data data object to write
	 * @param scanSlice scan slice specifying where in the dataset to write to.
	 * @throws NexusException
	 */
	public void writePosition(Object data, SliceND scanSlice) throws NexusException;

}