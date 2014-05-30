/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.epics.adviewer;

public class SimpleADPVSuffices implements ADPVSuffices {

	private String mpgProcSuffix;
	private String mpgSuffix;
	private String statSuffix;
	private String arraySuffix;
	private String adBaseSuffix;
	private String arrayROISuffix;

	@Override
	public String getADBaseSuffix() {
		return adBaseSuffix;
	}

	@Override
	public String getArrayROISuffix() {
		return arrayROISuffix;
	}

	@Override
	public String getArraySuffix() {
		return arraySuffix;
	}

	@Override
	public String getStatSuffix() {
		return statSuffix;
	}

	@Override
	public String getMPGProcSuffix() {
		return mpgProcSuffix;
	}

	@Override
	public String getMPGSuffix() {
		return mpgSuffix;
	}

	public String getMpgProcSuffix() {
		return mpgProcSuffix;
	}

	public void setMpgProcSuffix(String mpgProcSuffix) {
		this.mpgProcSuffix = mpgProcSuffix;
	}

	public String getMpgSuffix() {
		return mpgSuffix;
	}

	public void setMpgSuffix(String mpgSuffix) {
		this.mpgSuffix = mpgSuffix;
	}

	public String getAdBaseSuffix() {
		return adBaseSuffix;
	}

	public void setAdBaseSuffix(String adBaseSuffix) {
		this.adBaseSuffix = adBaseSuffix;
	}

	public void setStatSuffix(String statSuffix) {
		this.statSuffix = statSuffix;
	}

	public void setArraySuffix(String arraySuffix) {
		this.arraySuffix = arraySuffix;
	}

	public void setArrayROISuffix(String arrayROISuffix) {
		this.arrayROISuffix = arrayROISuffix;
	}

}
