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

import gda.device.scannable.ScannableMotor;

import org.eclipse.dawnsci.analysis.api.dataset.ILazyWriteableDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.StringDataset;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.impl.NXobjectFactory;
import org.eclipse.dawnsci.nexus.impl.NXpositionerImpl;

public class NexusScannableMotor extends AbstractNexusDevice<NXpositioner> {

	public NexusScannableMotor(final ScannableMotor scannableMotor) {
		super(scannableMotor);
	}

	@Override
	public Class<NXpositioner> getNexusBaseClass() {
		return NXpositioner.class;
	}

	@Override
	public NXpositioner createBaseClassInstance(NXobjectFactory nxObjectFactory) {
		final NXpositionerImpl nxPositioner = nxObjectFactory.createNXpositioner();

		nxPositioner.setName(StringDataset.createFromObject(getName()));
		this.defaultWritableDataset = nxPositioner.initializeLazyDataset(NXpositionerImpl.NX_VALUE, 1, Dataset.FLOAT64);

		return nxPositioner;
	}

	@Override
	public DeviceType getDeviceType() {
		return DeviceType.INSTRUMENT;
	}

	@Override
	public String getName() {
		return device.getName();
	}

	@Override
	public ILazyWriteableDataset getDataset(String path) {
		return null; // Not used as yet
	}

}
