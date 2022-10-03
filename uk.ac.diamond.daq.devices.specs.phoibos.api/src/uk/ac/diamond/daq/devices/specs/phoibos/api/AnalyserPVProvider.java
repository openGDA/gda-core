/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.devices.specs.phoibos.api;

import gda.factory.Findable;

public class AnalyserPVProvider implements Findable {

	private String name;
	private String spectrumPV;
	private String imagePV;
	private String totalPointsIterationPV;
	private String slicesPV;


	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	public String getSpectrumPV() {
		return spectrumPV;
	}

	public void setSpectrumPV(String spectrumPV) {
		this.spectrumPV = spectrumPV;
	}

	public String getImagePV() {
		return imagePV;
	}

	public void setImagePV(String imagePV) {
		this.imagePV = imagePV;
	}

	public String getTotalPointsIterationPV() {
		return totalPointsIterationPV;
	}

	public void setTotalPointsIterationPV(String totalPointsIterationPV) {
		this.totalPointsIterationPV = totalPointsIterationPV;
	}

	public String getSlicesPV() {
		return slicesPV;
	}

	public void setSlicesPV(String slicesPV) {
		this.slicesPV = slicesPV;
	}

}
