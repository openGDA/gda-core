/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package gda.device.nexus;

import gda.device.detector.DetectorBase;

import org.eclipse.dawnsci.analysis.api.dataset.ILazyWriteableDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.StringDataset;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.impl.NXdetectorImpl;
import org.eclipse.dawnsci.nexus.impl.NXobjectFactory;

public class NexusScanDetector extends AbstractNexusDevice<NXdetector> {

	public NexusScanDetector(final DetectorBase detector) {
		super(detector);
	}

	@Override
	public Class<NXdetector> getNexusBaseClass() {
		return NXdetector.class;
	}

	@Override
	public NXdetector createBaseClassInstance(NXobjectFactory nxObjectFactory) {
		final NXdetectorImpl nxDetector = nxObjectFactory.createNXdetector();

		nxDetector.setLocal_name(StringDataset.createFromObject(""));
		defaultWritableDataset = nxDetector.initializeLazyDataset(NXdetectorImpl.NX_DATA, 1, Dataset.FLOAT64);

		return nxDetector;
	}

	@Override
	public DeviceType getDeviceType() {
		return DeviceType.INSTRUMENT;
	}

	@Override
	public ILazyWriteableDataset getDataset(String path) {
		return null; // Not used as this point
	}

}
