/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.spring.parsers;

import org.exolab.castor.xml.XMLClassDescriptor;
import org.exolab.castor.xml.XMLFieldDescriptor;

/**
 * Holds the names of a field and the class in which it appears.
 */
class CastorClassField {
	
	private String className;
	
	private String fieldName;
	
	public CastorClassField(Class<?> clazz, String fieldName) {
		this.className = clazz.getName();
		this.fieldName = fieldName;
	}
	
	/**
	 * Determines whether the given field matches this field.
	 * 
	 * @param field a Castor field descriptor
	 * 
	 * @return {@code true} if the Castor field descriptor matches this field
	 */
	public boolean matches(XMLFieldDescriptor field) {
		XMLClassDescriptor classDesc = (XMLClassDescriptor) field.getContainingClassDescriptor();
		return classDesc.getJavaClass().getName().equals(className) && field.getFieldName().equals(fieldName);
	}
	
	@Override
	public String toString() {
		return className + "." + fieldName;
	}
}