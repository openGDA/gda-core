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
import java.util.List;

import org.eclipse.scanning.api.annotation.ui.FieldDescriptor;
import org.eclipse.scanning.api.annotation.ui.TypeDescriptor;

/**
 * A model consisting of multiple {@link AxialStepModel}s to be iterated over sequentially.
 *
 * @author Matthew Dickie
 */
@TypeDescriptor(editor="org.eclipse.scanning.device.ui.composites.MultiStepComposite")
public class AxialMultiStepModel extends AbstractMultiModel<AxialStepModel> {

	@FieldDescriptor(visible=true, label="The scannable name over which the multiple steps will run.")
	private String name;

	public AxialMultiStepModel() {
		setName("energy");
	}

	public AxialMultiStepModel(String name, double start, double stop, double step) {
		setName(name);
		addRange(start, stop, step);
	}

	/**
	 * Used from mapping_scan_commands.py
	 * @param name
	 * @param stepModels
	 */
	public AxialMultiStepModel(String name, List<AxialStepModel> stepModels) {
		setName(name);
		setModels(stepModels);
	}

	public void addRange(double start, double stop, double step) {
		addModel(new AxialStepModel(getName(), start, stop, step));
	}

	/**
	 *  Utility method to ensure backwards compatibility of user scripts.
	 *  Encourage use of {@link #getModels()}
	 */
	@Deprecated
	public List<AxialStepModel> getStepModels(){
		return getModels();
	}

	/**
	 *  Utility method to ensure backwards compatibility of user scripts.
	 *  Encourage use of {@link #setModels()}
	 */
	@Deprecated
	public void setStepModels(List<AxialStepModel> models){
		setModels(models);
	}

	public void addRange(AxialStepModel stepModel) {
		if (!getName().equals(stepModel.getName())) {
			throw new IllegalArgumentException(MessageFormat.format(
					"Child step model must have the same name as the MultiStepModel. Expected ''{0}'', was ''{1}''", getName(), stepModel.getName()));
		}
		addModel(stepModel);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName() +" [stepModels=[");
		for (AxialStepModel stepModel : getModels()) {
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
