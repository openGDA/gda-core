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

package uk.ac.gda.doe;

import java.lang.reflect.Field;

public class FieldContainer {

	private int listIndex;
	private FieldContainer parent;
	private Field  field;
	private Object originalObject;
	private DOEField annotation;
	/**
	 * @return Returns the field.
	 */
	public Field getField() {
		return field;
	}
	/**
	 * @param field The field to set.
	 */
	public void setField(Field field) {
		this.field = field;
	}
	/**
	 * @return Returns the object, if it was a list returns a
	 *         list of them otherwise a size 1 list with the
	 *         original value.
	 */
	public Object getOriginalObject() {
		return originalObject;
	}
	/**
	 * @param object The object to set.
	 */
	public void setOriginalObject(Object object) {
		this.originalObject = object;
	}

	/**
	 * IMPORTANT DO NOT AUTOGENERATE
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		result = prime * result + listIndex;
		result = prime * result + ((originalObject == null) ? 0 : originalObject.hashCode());
		return result;
	}

	/**
	 * IMPORTANT DO NOT AUTOGENERATE
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FieldContainer other = (FieldContainer) obj;
		if (field == null) {
			if (other.field != null)
				return false;
		} else if (!field.getName().equals(other.field.getName()))
			return false;
		if (listIndex != other.listIndex)
			return false;
		if (originalObject == null) {
			if (other.originalObject != null)
				return false;
		} else if (!originalObject.equals(other.originalObject))
			return false;

		return true;
	}
	public String getName() {
		return field.getName();
	}

	public void setParent(FieldContainer parent) {
		this.parent = parent;
	}
	public FieldContainer getParent() {
		return parent;
	}
	/**
	 * @return Returns the listIndex.
	 */
	public int getListIndex() {
		return listIndex;
	}
	/**
	 * @param listIndex The listIndex to set.
	 */
	public void setListIndex(int listIndex) {
		this.listIndex = listIndex;
	}

	/**
	 *
	 * @param object
	 * @param i
	 * @return clone
	 *
	 */
	public FieldContainer clone(Object object, int i) {
		final FieldContainer clone = new FieldContainer();
	    clone.parent = this.parent;
	    clone.field  = this.field;
		clone.setOriginalObject(object);
		clone.setListIndex(i);
		clone.setAnnotation(this.annotation);
		return clone;
	}

	@Override
	public String toString() {
		return "field='"+getField().getName()+"' object='"+getOriginalObject()+"'" + "{"+getOriginalObject().hashCode()+"}";
	}
	/**
	 * @return Returns the annotation.
	 */
	public DOEField getAnnotation() {
		return annotation;
	}
	/**
	 * @param annotation The annotation to set.
	 */
	public void setAnnotation(DOEField annotation) {
		this.annotation = annotation;
	}

}
