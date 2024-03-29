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

import org.eclipse.scanning.api.annotation.ui.TypeDescriptor;

import uk.ac.diamond.daq.util.logging.deprecation.DeprecationLogger;


/**
 * A model consisting of multiple {@link AxialStepModel}s to be iterated over sequentially.
 *
 * @author Matthew Dickie
 */
@TypeDescriptor(editor="org.eclipse.scanning.device.ui.composites.MultiStepComposite")
public class AxialMultiStepModel extends AbstractMultiModel<AxialStepModel> implements IAxialModel {

	private static final DeprecationLogger logger = DeprecationLogger.getLogger(AxialMultiStepModel.class);

	public AxialMultiStepModel() {
		setContinuous(false);
		setName("energy");
	}

	public AxialMultiStepModel(String name, double start, double stop, double step) {
		setName(name);
		addRange(start, stop, step);
		setContinuous(false);
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
	@Deprecated(since="GDA 9.16")
	public List<AxialStepModel> getStepModels(){
		logger.deprecatedMethod("getStepModels()", null, "getModels()");
		return getModels();
	}

	/**
	 *  Utility method to ensure backwards compatibility of user scripts.
	 *  Encourage use of {@link #setModels(List)}
	 */
	@Deprecated(since="GDA 9.16")
	public void setStepModels(List<AxialStepModel> models){
		logger.deprecatedMethod("setStepModels(List<AxialStepModel>)", null, "setModels(List<AxialStepModel>)");
		setModels(models);
	}

	public void addRange(AxialStepModel stepModel) {
		if (!getName().equals(stepModel.getName())) {
			throw new IllegalArgumentException(MessageFormat.format(
					"Child step model must have the same name as the MultiStepModel. Expected ''{0}'', was ''{1}''", getName(), stepModel.getName()));
		}
		addModel(stepModel);
	}

}
