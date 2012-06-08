/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.rcp.views;

import gda.device.DeviceException;
import gda.device.Scannable;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class GDAScannablePropertySource implements IPropertySource {

	private static final String ID_POSITION = "position";
	private static final String ID_NAME = "name";


	private Scannable scannable;
	protected static IPropertyDescriptor[] descriptors;
	
	static{
		descriptors = new IPropertyDescriptor[] {
				new PropertyDescriptor(ID_NAME,"Name"),
				new PropertyDescriptor(ID_POSITION,"Position")
		};
	}	
	
	public GDAScannablePropertySource(Scannable scannable) {
		this.scannable = scannable;
	}

	@Override
	public Object getEditableValue() {
		return this;
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return descriptors;
	}

	@Override
	public Object getPropertyValue(Object id) {
		if (ID_NAME.equals(id)){
			return new String(scannable.getName());
		}
		if (ID_POSITION.equals(id)){
			try {
				Object pos = scannable.getPosition();
				if (pos instanceof double[]) {
					double[] array = (double[]) pos;
					return new ArrayPropertySource(array, scannable.getExtraNames());
				}
				return pos;
			} catch (DeviceException e) {
				return "Failed to get Position: " + e.getMessage();
			}
		}
		return null;
	}

	@Override
	public boolean isPropertySet(Object id) {
		if (ID_NAME.equals(id)) return true;
		return false;
	}

	@Override
	public void resetPropertyValue(Object id) {
	}

	@Override
	public void setPropertyValue(Object id, Object value) {
		if (ID_NAME.equals(id)){
			//set value;
		}
		//firepropertychanged;
	}

}
