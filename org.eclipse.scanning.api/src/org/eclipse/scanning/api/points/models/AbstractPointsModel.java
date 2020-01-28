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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.scanning.api.annotation.UiHidden;
import org.eclipse.scanning.api.annotation.ui.FieldDescriptor;

/**
 * Abstract base class for models using the ScanPointGenerator module, providing property change support for the convenience of subclasses.
 *
 * Models should correlate to a Generator or series of Generator from ScanPointGenerator, that are valid targets for CompoundGenerators.
 * A Generator must be able to produce from a Model its Axes, Units (of length 1 or length number of axes), size and whether it should alternate in
 * upper dimensions.
 *
 * Additionally, if the model is capable of acting continuously [the Generator has a prepare_bounds method] then the generator should pass
 * this argument from the model to the CompoundGenerator that will wrap the Generator.
 *
 * @author Matthew Gerring
 * @author Joseph Ware
 *
 */
public abstract class AbstractPointsModel implements IScanPointGeneratorModel {

	private static final String HARDCODED_UNITS = "mm";
	private List<String> units;

	@FieldDescriptor(label="Alternating/'Snake' - switches direction with every iteration of wrapping model")
	private boolean alternating = false;

	protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}
	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

	@FieldDescriptor(visible=false)
	private String name;

	@FieldDescriptor(label = "Continuous", hint = "Whether the motors should move continuously or stop at each point in the scan to take an image")
	private boolean continuous = true;

	@Override
	public String getName() {
		return name;
	}
	/**
	 * Name of the model as it will appear in the UI.
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public List<String> getScannableNames() {
		return Arrays.asList(getName());
	}

	@UiHidden
	public String getSummary() {
		StringBuilder buf = new StringBuilder();
		String sname = getClass().getSimpleName();
		if (sname.toLowerCase().endsWith("model")) sname = sname.substring(0, sname.length()-5);
		buf.append(sname);
		String names = getScannableNames().toString();
		names = names.replace('[', '(');
		names = names.replace(']', ')');
		buf.append(names);
		return buf.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (alternating ? 1231 : 1237);
		result = prime * result + (continuous ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || obj.getClass()!=getClass()) {
			return false;
		}
		AbstractPointsModel other = (AbstractPointsModel) obj;
		if (alternating != other.alternating) {
			return false;
		}
		if (continuous != other.continuous) {
			return false;
		}
		if (name == null) {
			return (other.name == null);
		}
		return (name.equals(other.name));
	}


	public static List<String> getScannableNames(Object model) {
		if (model instanceof IScanPathModel) return ((IScanPathModel)model).getScannableNames();
		try {
			Method method = model.getClass().getMethod("getScannableNames");
			Object ret    = method.invoke(model);
			if (ret instanceof List) {
				@SuppressWarnings("unchecked")
				final List<String> names = (List<String>) ret;
				return names;
			}
		} catch (Exception ne) {
			// fall through to return empty list
		}

		return Collections.emptyList();
	}
	@Override
	public String toString() {
		return "name=" + name + ", continuous="+ continuous + ", alternating=" + alternating;
	}

	@Override
	public boolean isContinuous() {
		return continuous;
	}

	@Override
	public void setContinuous(boolean newValue) {
		pcs.firePropertyChange("continuous", continuous, newValue);
		continuous = newValue;
	}

	@Override
	public List<String> getUnits(){
		if (units == null) units = new ArrayList<>();
		while (units.size() < getScannableNames().size()) {
			units.add(HARDCODED_UNITS);
		}
		return units;
	}

	@Override
	public void setUnits(List<String> units) {
		pcs.firePropertyChange("units", this.units, units);
		this.units = units;
	}

	@Override
	public boolean isAlternating() {
		return alternating;
	}

	@Override
	public void setAlternating(boolean alternating) {
		boolean oldValue = this.alternating;
		this.alternating = alternating;
		this.pcs.firePropertyChange("alternating", oldValue, alternating);
	}

	public static boolean supportsAlternating(Class<? extends IScanPathModel> model) {
		return !(model.equals(OneAxisPointRepeatedModel.class) || model.equals(TwoAxisPointSingleModel.class) || model.equals(StaticModel.class));
	}
	public static boolean supportsContinuous(Class<? extends IScanPathModel> model) {
		return !(model.equals(OneAxisPointRepeatedModel.class) || model.equals(TwoAxisPointSingleModel.class) || model.equals(StaticModel.class));
	}
	public static boolean supportsRandomOffset(Class<? extends IScanPathModel> model) {
		return (model.equals(TwoAxisGridPointsModel.class));
	}

}
