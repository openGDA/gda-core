/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.api.points.models;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.scanning.api.annotation.ui.FieldDescriptor;
import org.eclipse.scanning.api.annotation.ui.TypeDescriptor;

/**
 * A model consisting of multiple {@link AxialStepModel}s to be iterated over sequentially.
 *
 * @author Matthew Dickie
 */
// TODO: Can this be replaced with a CompoundConsecutive model?
@TypeDescriptor(editor="org.eclipse.scanning.device.ui.composites.MultiStepComposite")
public class AxialMultiStepModel extends AbstractPointsModel {

	private static final String FIELD_NAME = "stepModels";

	private List<AxialStepModel> stepModels;

	@FieldDescriptor(visible=true, label="The scannable name over which the multiple steps will run.")
	private String name;

	public AxialMultiStepModel() {
		stepModels = new ArrayList<>(4);
		setName("energy");
	}

	public AxialMultiStepModel(String name, double start, double stop, double step) {
		setName(name);
		stepModels = new ArrayList<>(4);
		stepModels.add(new AxialStepModel(name, start, stop, step));
	}

	/**
	 * Used from mapping_scan_commands.py
	 * @param name
	 * @param stepModels
	 */
	public AxialMultiStepModel(String name, List<AxialStepModel> stepModels) {
		setName(name);
		this.stepModels = stepModels;
	}

	/**
	 * Must implement clear() method on beans being used with BeanUI.
	 */
	public void clear() {
		List<AxialStepModel> oldModels = new ArrayList<>(stepModels);
		stepModels.clear();
		pcs.firePropertyChange(FIELD_NAME, oldModels, stepModels);
	}

	public void addRange(double start, double stop, double step) {
		List<AxialStepModel> oldModels = new ArrayList<>(stepModels);
		stepModels.add(new AxialStepModel(getName(), start, stop, step));
		pcs.firePropertyChange(FIELD_NAME, oldModels, stepModels);
	}

	public void addRange(AxialStepModel stepModel) {
		if (!getName().equals(stepModel.getName())) {
			throw new IllegalArgumentException(MessageFormat.format(
					"Child step model must have the same name as the MultiStepModel. Expected ''{0}'', was ''{1}''", getName(), stepModel.getName()));
		}

		stepModels.add(stepModel);
	}

	public List<AxialStepModel> getStepModels() {
		return stepModels;
	}

	public void setStepModels(List<AxialStepModel> stepModels) {
		List<AxialStepModel> oldModels = stepModels;
		this.stepModels = stepModels;
		pcs.firePropertyChange(FIELD_NAME, oldModels, stepModels);
	}

	/**
	 * This method is accessed by reflection, it helps out BeanUI
	 * @param smodel
	 */
	public void addStepModel(AxialStepModel smodel) {
		stepModels.add(smodel);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((stepModels == null) ? 0 : stepModels.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;
		AxialMultiStepModel other = (AxialMultiStepModel) obj;
		if (stepModels == null) {
			return other.stepModels == null;
			}
		return stepModels.equals(other.stepModels);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName() +" [stepModels=[");
		for (AxialStepModel stepModel : stepModels) {
			sb.append("start=");
			sb.append(stepModel.getStart());
			sb.append(", stop=");
			sb.append(stepModel.getStop());
			sb.append(", step=");
			sb.append(stepModel.getStep());
			sb.append("; ");
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append("], " + super.toString() + "]");

		return sb.toString();
	}

}
