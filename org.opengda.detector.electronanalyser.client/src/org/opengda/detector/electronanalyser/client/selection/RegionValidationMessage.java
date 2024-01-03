/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package org.opengda.detector.electronanalyser.client.selection;

import org.eclipse.jface.viewers.ISelection;
import org.opengda.detector.electronanalyser.model.regiondefinition.api.Region;

public class RegionValidationMessage implements ISelection{

	private Region region;
	private String message;
	private double spectrumEnergyLowLimit;
	private double spectrumEnergyHighLimit;

	public RegionValidationMessage(Region region, String message, double spectrumEnergyLowLimit, double spectrumEnergyHighLimit) {
		this.region=region;
		this.message = message;
		this.spectrumEnergyLowLimit  = spectrumEnergyLowLimit;
		this.spectrumEnergyHighLimit = spectrumEnergyHighLimit;
	}
	public double getSpectrumEnergyLowLimit() {
		return spectrumEnergyLowLimit;
	}

	public double getSpectrumEnergyHighLimit() {
		return spectrumEnergyHighLimit;
	}

	public Region getRegion() {
		return region;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}
}
