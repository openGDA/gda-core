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

import static org.eclipse.scanning.api.constants.PathConstants.ALTERNATING;
import static org.eclipse.scanning.api.constants.PathConstants.CONTINUOUS;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.scanning.api.AbstractNameable;

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
public abstract class AbstractPointsModel extends AbstractNameable implements IScanPointGeneratorModel {

	protected static final String HARDCODED_UNITS = "mm";
	private List<String> units = List.of(HARDCODED_UNITS);

	/** Alternating/'Snake': if <code>true</code>, switches direction with every iteration of wrapping model */
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

	/** If <code>true</code>, the motors move continuously; if <code>false</code>, they stop at each point in the scan to take an image */
	private boolean continuous = true;

	@Override
	public List<String> getScannableNames() {
		return Arrays.asList(getName());
	}

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
	public boolean isContinuous() {
		return continuous;
	}

	@Override
	public void setContinuous(boolean newValue) {
		pcs.firePropertyChange(CONTINUOUS, continuous, newValue);
		continuous = newValue;
	}

	@Override
	public List<String> getUnits(){
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
		this.pcs.firePropertyChange(ALTERNATING, oldValue, alternating);
	}

	@Override
	public void updateFromPropertiesMap(Map<String, Object> properties) {
		if (properties.containsKey(ALTERNATING)) {
			setAlternating((boolean) properties.get(ALTERNATING));
		}
		if (properties.containsKey(CONTINUOUS)) {
			setContinuous((boolean) properties.get(CONTINUOUS));
		}
	}
	public static boolean supportsAlternating(Class<? extends IScanPointGeneratorModel> model) {
		return !(model.equals(OneAxisPointRepeatedModel.class) || model.equals(TwoAxisPointSingleModel.class) || model.equals(StaticModel.class));
	}
	public static boolean supportsContinuous(Class<? extends IScanPointGeneratorModel> model) {
		return !(model.equals(OneAxisPointRepeatedModel.class) || model.equals(TwoAxisPointSingleModel.class) || model.equals(StaticModel.class));
	}
	public static boolean supportsRandomOffset(Class<? extends IScanPointGeneratorModel> model) {
		return (model.equals(TwoAxisGridPointsModel.class));
	}
	public static boolean supportsVertical(Class<? extends IScanPointGeneratorModel> model) {
		return (model.equals(TwoAxisGridPointsModel.class) || model.equals(TwoAxisGridPointsRandomOffsetModel.class) || model.equals(TwoAxisGridStepModel.class));
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (alternating ? 1231 : 1237);
		result = prime * result + (continuous ? 1231 : 1237);
		result = prime * result + ((units == null) ? 0 : units.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractPointsModel other = (AbstractPointsModel) obj;
		if (alternating != other.alternating)
			return false;
		if (continuous != other.continuous)
			return false;
		if (units == null) {
			if (other.units != null)
				return false;
		} else if (!units.equals(other.units))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "AbstractPointsModel [units=" + units + ", alternating=" + alternating + ", continuous=" + continuous
				+ "]";
	}
}
