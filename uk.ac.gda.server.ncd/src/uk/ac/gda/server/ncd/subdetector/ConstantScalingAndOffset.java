/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.subdetector;

public class ConstantScalingAndOffset implements ScalingAndOffset {

	private Double scaling, offset;
	private String description;
	
	@Override
	public Double getScaling() {
		return scaling;
	}
	public void setScaling(Double scaling) {
		this.scaling = scaling;
	}
	@Override
	public Double getOffset() {
		return offset;
	}
	public void setOffset(Double offset) {
		this.offset = offset;
	}
	@Override
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}
