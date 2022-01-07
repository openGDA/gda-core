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

package uk.ac.diamond.daq.sample.plate.management.ui.models;

public class RegisteredSample {

	private String id;
	private String label;
	private Double positionX;
	private Double positionY;
	private Double thickness;
	private String description;

	public RegisteredSample() {}

	public RegisteredSample(String id, String label, Double positionX, Double positionY, Double thickness, String description) {
		this.id = id;
		this.label = label;
		this.positionX = positionX;
		this.positionY = positionY;
		this.thickness = thickness;
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Double getPositionX() {
		return positionX;
	}

	public void setPositionX(Double positionX) {
		this.positionX = positionX;
	}

	public Double getPositionY() {
		return positionY;
	}

	public void setPositionY(Double positionY) {
		this.positionY = positionY;
	}

	public Double getThickness() {
		return thickness;
	}

	public void setThickness(Double thickness) {
		this.thickness = thickness;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
