/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package uk.ac.gda.apres.ui.config;

import gda.factory.Findable;

public class AnalyserPVConfig implements Findable {

	private String name;
	private String analyserEnergyAxisPv;
	private String analyserEnergyAxisCountPv;
	private String analyserAngleAxisPv;
	private String analyserAngleAxisCountPv;
	private String analyserLensModePv;
	private String analyserAcquisitionModePv;
	private String analyserManufacturerPV;

	public String getAnalyserEnergyAxisPv() {
		return analyserEnergyAxisPv;
	}

	public void setAnalyserEnergyAxisPv(String analyserEnergyAxisPv) {
		this.analyserEnergyAxisPv = analyserEnergyAxisPv;
	}

	public String getAnalyserEnergyAxisCountPv() {
		return analyserEnergyAxisCountPv;
	}

	public void setAnalyserEnergyAxisCountPv(String analyserEnergyAxisCountPv) {
		this.analyserEnergyAxisCountPv = analyserEnergyAxisCountPv;
	}

	public String getAnalyserAngleAxisPv() {
		return analyserAngleAxisPv;
	}

	public void setAnalyserAngleAxisPv(String analyserAngleAxisPv) {
		this.analyserAngleAxisPv = analyserAngleAxisPv;
	}

	public String getAnalyserAngleAxisCountPv() {
		return analyserAngleAxisCountPv;
	}

	public void setAnalyserAngleAxisCountPv(String analyserAngleAxisCountPv) {
		this.analyserAngleAxisCountPv = analyserAngleAxisCountPv;
	}

	public String getAnalyserLensModePv() {
		return analyserLensModePv;
	}

	public void setAnalyserLensModePv(String analyserLensModePv) {
		this.analyserLensModePv = analyserLensModePv;
	}


	public String getAnalyserAcquisitionModePv() {
		return analyserAcquisitionModePv;
	}

	public void setAnalyserAcquisitionModePv(String analyserAcquisitionModePv) {
		this.analyserAcquisitionModePv = analyserAcquisitionModePv;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	public String getAnalyserManufacturerPV() {
		return analyserManufacturerPV;
	}

	public void setAnalyserManufacturerPV(String analyserManufacturerPV) {
		this.analyserManufacturerPV = analyserManufacturerPV;
	}
}
