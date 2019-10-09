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

package gda.mscan.processor;

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.models.DeviceRole;
import org.eclipse.scanning.api.device.models.IDetectorModel;

import gda.mscan.ClauseContext;

public class IRunnableDeviceDetectorElementProcessor extends ElementProcessorBase<IRunnableDevice<IDetectorModel>> {

	public IRunnableDeviceDetectorElementProcessor(final IRunnableDevice<IDetectorModel> source) {
		super(source);
	}

	/**
	 * A dummy method in this case as Runnable Devices cannot form part of a scan path definition clause.
	 *
	 * @param context	The {@link ClauseContext} object being completed for the current MSCan clause
	 * @param index		The index of the clause element associated with the processor within the current clause
	 *
	 * @throws			IllegalStateException if the previous element of the context is null (this should never occur)
	 * @throws			IllegalArgumentException if there is no list of successors corresponding to the type of the
	 * 					previous element i.e. it is not a valid element type
	 */
	@Override
	public void process(final ClauseContext context, final int index) {
		// No operation here as runnable devices aren't part of a scanpath definition so are not included in the grammar
	}


	@Override
	public boolean hasDetector() {
		return enclosed.getRole() == DeviceRole.HARDWARE || enclosed.getRole() == DeviceRole.MALCOLM;
	}

	/**
	 * Retrieve the name of the enclosed {@link IRunnableDevice}
	 *
	 * @return the name of the enclosed {@link IRunnableDevice}
	 */
	@Override
	public String getElementValue() {
		return enclosed.getName();
	}
}
