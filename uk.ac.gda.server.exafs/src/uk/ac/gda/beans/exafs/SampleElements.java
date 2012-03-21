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

package uk.ac.gda.beans.exafs;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;

import uk.ac.gda.beans.IRichBean;

/**
 * A Bean for keeping the elements used in the sample table.
 */
public class SampleElements implements IRichBean{

	static public final URL mappingURL = SampleElements.class.getResource("ExafsParameterMapping.xml");

	static public final URL schemaURL  = SampleElements.class.getResource("ExafsParameterMapping.xsd");

	private List<ElementPosition>    elementPositions;
	
	/**
	 * Method required to use with BeanUI. Called using reflection.
	 */
	@Override
	public void clear() {
		if (elementPositions!=null)    elementPositions.clear();
	}

	public SampleElements() {
		elementPositions    = new ArrayList<ElementPosition>(7);
	}

	public List<ElementPosition> getElementPositions() {
		return elementPositions;
	}

	public void addElementPosition(ElementPosition e) {
		elementPositions.add(e);
	}

    public void setElementPositions(List<ElementPosition> e) {
    	if(elementPositions!=null) elementPositions.clear();
    	if (e!=null) this.elementPositions.addAll(e);
    }

	@Override
	public String toString() {
		try {
			return BeanUtils.describe(this).toString();
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((elementPositions == null) ? 0 : elementPositions.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SampleElements other = (SampleElements) obj;
		if (elementPositions == null) {
			if (other.elementPositions != null)
				return false;
		} else if (!elementPositions.equals(other.elementPositions))
			return false;
		return true;
	}

}
