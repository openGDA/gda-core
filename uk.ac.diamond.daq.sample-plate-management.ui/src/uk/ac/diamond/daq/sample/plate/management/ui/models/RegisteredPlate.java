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

import java.util.List;

public class RegisteredPlate {

	private String visit;
	private String summary;
	private String holder;
	private String description;

	// ID, Label, Position, Thickness, Description
	private List<RegisteredSample> samples;

	private int[] datasetShape;
	private byte[] dataset;

	private double[] xCalibratedAxis;
	private double[] yCalibratedAxis;

	public RegisteredPlate() {}

	public RegisteredPlate(int[] datasetShape, byte[] dataset, String visit, String summary, String holder, String description,
			List<RegisteredSample> samples, double[] xCalibratedAxis, double[] yCalibratedAxis) {

		this.datasetShape = datasetShape;
		this.dataset = dataset;

		this.xCalibratedAxis = xCalibratedAxis;
		this.yCalibratedAxis = yCalibratedAxis;

		this.visit = visit;
		this.summary = summary;
		this.holder = holder;
		this.description = description;

		this.samples = samples;
	}

	public String getVisit() {
		return visit;
	}

	public void setVisit(String visit) {
		this.visit = visit;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getHolder() {
		return holder;
	}

	public void setHolder(String holder) {
		this.holder = holder;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<RegisteredSample> getSamples() {
		return samples;
	}

	public void setSamples(List<RegisteredSample> samples) {
		this.samples = samples;
	}

	public byte[] getDataset() {
		return dataset;
	}

	public void setDataset(byte[] dataset) {
		this.dataset = dataset;
	}

	public int[] getDatasetShape() {
		return datasetShape;
	}

	public void setDatasetShape(int[] datasetShape) {
		this.datasetShape = datasetShape;
	}

	public double[] getXCalibratedAxis() {
		return xCalibratedAxis;
	}

	public void setXCalibratedAxis(double[] xCalibratedAxis) {
		this.xCalibratedAxis = xCalibratedAxis;
	}

	public double[] getYCalibratedAxis() {
		return yCalibratedAxis;
	}

	public void setYCalibratedAxis(double[] yCalibratedAxis) {
		this.yCalibratedAxis = yCalibratedAxis;
	}
}
