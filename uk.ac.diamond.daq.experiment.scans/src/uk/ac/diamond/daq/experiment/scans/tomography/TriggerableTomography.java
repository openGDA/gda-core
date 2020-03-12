/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.experiment.scans.tomography;

import uk.ac.diamond.daq.experiment.api.TriggerableScan;
import uk.ac.gda.tomography.service.message.TomographyRunMessage;

public class TriggerableTomography implements TriggerableScan {

	private String tomographyParams;
	private String acquisitionScriptPath;

	public TriggerableTomography(String tomographyParams, String acquisitionScriptPath) {
		this.tomographyParams = tomographyParams;
		this.acquisitionScriptPath = acquisitionScriptPath;
	}

	@Override
	public Object trigger() {
		TomographyRunMessage tomographyRunMessage = new TomographyRunMessage(tomographyParams);
		//SpringApplicationContextProxy.publishEvent(new TomographyRunAcquisitionEvent(this, tomographyRunMessage));
		return null;
	}

	/**
	 * Used by Marshaller
	 */
	public String getTomographyParams() {
		return tomographyParams;
	}

	/**
	 * Used by Marshaller
	 */
	public String getAcquisitionScriptPath() {
		return acquisitionScriptPath;
	}

}
