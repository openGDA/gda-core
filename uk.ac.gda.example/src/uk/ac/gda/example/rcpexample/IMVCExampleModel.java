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

package uk.ac.gda.example.rcpexample;

import gda.device.DeviceException;

import java.beans.PropertyChangeListener;

import uk.ac.gda.client.observablemodels.ScannableWrapper;

public interface IMVCExampleModel {
	boolean isSelected();

	void setSelected(boolean selected);

	double getPosition();

	void setPosition(double position);

	public void addPropertyChangeListener(PropertyChangeListener listener);

	public void removePropertyChangeListener(PropertyChangeListener listener);

	public static final String SELECTED_PROPERTY_NAME = "selected";
	public static final String POSITION_PROPERTY_NAME = "position";

	ScannableWrapper getScannableWrapper() throws DeviceException, Exception;
}
