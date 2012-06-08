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
import java.util.Arrays;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;




/**
 * @author tjs15132
 *
 */
public class ArrayPropertySource implements IPropertySource {
	
	private double[] array;
	private String[] rawDescriptors;
	private IPropertyDescriptor[] descriptors;

	
	/**
	 * @param array
	 * @param descrips
	 */
	public ArrayPropertySource(double[] array, String[] descrips){
		this.array = array;
		rawDescriptors = descrips;
	}
	
	/**
	 * @return The editablevalue of the object
	 */
	@Override
	public Object getEditableValue(){
		return this;
	}
	
	/**
	 * @return propertydescriptor
	 */
	@Override
	public IPropertyDescriptor[] getPropertyDescriptors(){
		if (descriptors == null) {
			descriptors = new IPropertyDescriptor[array.length];
			for (int i = 0; i < array.length; i++) {
				if (i < rawDescriptors.length)
					descriptors[i] = new PropertyDescriptor(i, rawDescriptors[i].toString());
				else
					descriptors[i] = new PropertyDescriptor(i, "Element " + i);
			}
		}
		return descriptors;
	}
	
	private int getIndexOfProperty(Object id) {
		for (int i = 0; i < descriptors.length; i++) {
			IPropertyDescriptor desc = descriptors[i];
			if (desc.getId() == id) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * @param propName
	 * @return propertyValue
	 */
	@Override
	public Object getPropertyValue(Object propName){
		int index = getIndexOfProperty(propName);
		if (index >= 0) {
			return array[index];
		}
		return null;
	}

	
	/**
	 * @see org.eclipse.ui.views.properties.IPropertySource#isPropertySet(Object)
	 */
	@Override
	public boolean isPropertySet(Object propName){
		int index = getIndexOfProperty(propName);
		if (index >= 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * @param propName
	 */
	@Override
	public void resetPropertyValue(Object propName){}
	
	/**
	 * @param propName
	 * @param value
	 */
	@Override
	public void setPropertyValue(Object propName, Object value){
	}
	

	@Override
	public String toString(){
		return Arrays.toString(array);
	}
	

}
