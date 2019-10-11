/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.xanes.ui;

import java.util.Arrays;
import java.util.List;

/**
 * An element and its edge(s) to be used in XANES scanning.<br>
 * Indicates whether the element is radioactive, as this may need to be treated differently.<br>
 * The object can be initialised with a single edge or a list.
 */
public class ElementAndEdges {
	private String elementName;
	private boolean radioactive = false;
	private List<String> edges;

	public String getElementName() {
		return elementName;
	}

	public void setElementName(String elementName) {
		this.elementName = elementName;
	}

	public boolean isRadioactive() {
		return radioactive;
	}

	public void setRadioactive(boolean radioactive) {
		this.radioactive = radioactive;
	}

	public List<String> getEdges() {
		return edges;
	}

	public void setEdges(List<String> edges) {
		this.edges = edges;
	}

	public void setEdge(String edge) {
		this.edges = Arrays.asList(edge);
	}

	@Override
	public String toString() {
		return "ElementAndEdges [elementName=" + elementName + ", radioactive=" + radioactive + ", edges=" + edges
				+ "]";
	}

}
