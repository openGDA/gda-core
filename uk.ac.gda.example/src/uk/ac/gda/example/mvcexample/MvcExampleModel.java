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

package uk.ac.gda.example.mvcexample;

import gda.device.DeviceException;

import java.beans.PropertyChangeListener;

import org.eclipse.core.databinding.observable.list.IObservableList;

import uk.ac.gda.client.observablemodels.ScannableWrapper;

public interface MvcExampleModel {
	boolean isSelected();
	void setSelected(boolean selected);
	
	double getPosition();
	void setPosition(double position);
	
	
	public void addPropertyChangeListener(PropertyChangeListener listener);
	public void removePropertyChangeListener(PropertyChangeListener listener);
	public static final String SELECTED_PROPERTY_NAME="selected";
	public static final String POSITION_PROPERTY_NAME="position";
	ScannableWrapper getScannableWrapper() throws DeviceException, Exception;
	
	//To allow the list to be updated outside of the UI we need to return an ObservableList whose getElementType returns MvcExampleItem
	//rather than a List
	IObservableList getItems();
}
