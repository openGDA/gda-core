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

package uk.ac.diamond.tomography.reconstruction.commands;

public class DoubleTomographyParameter implements ITomographyParameter {

	private String name;
	private double value;
	private double max;
	private double min;
	private double very_fine_step;
	private double fine_step;
	private double coarse_step;
	
	
	public DoubleTomographyParameter(String parameterName) {
		name = parameterName;
	}


	@Override
	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	@Override
	public Object getValue() {
		return value;
	}


	public void setValue(double value) {
		this.value = value;
	}


	public double getMax() {
		return max;
	}


	public void setMax(double max) {
		this.max = max;
	}


	public double getMin() {
		return min;
	}


	public void setMin(double min) {
		this.min = min;
	}


	public double getVery_fine_step() {
		return very_fine_step;
	}


	public void setVery_fine_step(double very_fine_step) {
		this.very_fine_step = very_fine_step;
	}


	public double getFine_step() {
		return fine_step;
	}


	public void setFine_step(double fine_step) {
		this.fine_step = fine_step;
	}


	public double getCoarse_step() {
		return coarse_step;
	}


	public void setCoarse_step(double coarse_step) {
		this.coarse_step = coarse_step;
	}
	
}
