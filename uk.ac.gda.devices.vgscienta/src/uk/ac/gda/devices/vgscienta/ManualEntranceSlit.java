/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.vgscienta;

public class ManualEntranceSlit implements EntranceSlitInformationProvider {

	private Integer rawValue;
	private String label;
	private Double sizeInMM;
	private String shape;
	private String direction;

	@Override
	public Integer getRawValue() {
		return rawValue;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public Double getSizeInMM() {
		return sizeInMM;
	}

	public void setRawValue(Integer rawValue) {
		this.rawValue = rawValue;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setSizeInMM(Double sizeInMM) {
		this.sizeInMM = sizeInMM;
	}

	@Override
	public String getShape() {
		return shape;
	}

	public void setShape(String shape) {
		this.shape = shape;
	}

	@Override
	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}
}