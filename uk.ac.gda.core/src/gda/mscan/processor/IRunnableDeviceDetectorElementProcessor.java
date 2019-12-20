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

import java.util.List;

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.models.DeviceRole;
import org.eclipse.scanning.api.device.models.IDetectorModel;

import gda.device.Scannable;
import gda.mscan.ClausesContext;

public class IRunnableDeviceDetectorElementProcessor extends ElementProcessorBase<IRunnableDevice<IDetectorModel>> {

	public IRunnableDeviceDetectorElementProcessor(final IRunnableDevice<IDetectorModel> source) {
		super(source);
	}

	/**
	 * A dummy method in this case as Runnable Devices cannot form part of a scan path definition clause.
	 *
	 * @param context	The {@link ClausesContext} object being completed for the current MSCan clause
	 * @param index		The index of the clause element associated with the processor within the current clause
	 *
	 * @throws			IllegalStateException if the previous element of the context is null (this should never occur)
	 * @throws			IllegalArgumentException if there is no list of successors corresponding to the type of the
	 * 					previous element i.e. it is not a valid element type
	 */
	@Override
	public void process(final ClausesContext context,
			final List<IClauseElementProcessor> clauseProcessors, final int index) throws Exception {
		throwIf(!context.isScanPathSeen(), "No scan path defined - SPEC style scans not yet supported");
		throwIf(clauseProcessors.size() > 2, "too many elements in Detector clause");

		double exposure = 0;
		if (clauseProcessors.size() == 2) {
			IClauseElementProcessor procTwo = withNullProcessorCheck(clauseProcessors.get(1));
			throwIf(!procTwo.hasNumber(), "2nd element of unexpected type in Detector clause");
			exposure = Double.valueOf(procTwo.getElementValue());
		}
		if(isValidElement(context, this.getClass().getName(), Scannable.class)) {
			context.addDetector(enclosed, exposure);
		}
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
