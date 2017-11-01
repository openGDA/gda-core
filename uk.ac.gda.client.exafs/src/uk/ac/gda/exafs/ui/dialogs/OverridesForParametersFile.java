/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.dialogs;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.thoughtworks.xstream.XStream;

public class OverridesForParametersFile {
	private String xmlFileName = ""; // Full path to XML file
	private String containingClassType = ""; // as returned by object.getClass().getName();
	private List<ParameterOverride> parameterOverrides;

	// Constructor
	public OverridesForParametersFile() {
		parameterOverrides = new ArrayList<ParameterOverride>();
	}

	// Constructor
	public OverridesForParametersFile(String xmlName, String className) {
		parameterOverrides = new ArrayList<ParameterOverride>();
		this.xmlFileName = xmlName;
		this.containingClassType = className;
	}

	public void copyFrom(OverridesForParametersFile paramOverrides) {
		setXmlFileName(paramOverrides.getXmlFileName());
		setContainingClassType(paramOverrides.getContainingClassType());
		parameterOverrides.clear();
		for(ParameterOverride override : paramOverrides.getOverrides()) {
			addOverride(override.getFullPathToGetter(), override.getNewValue());
		}
	}

	public void addOverride(String fullPathToGetter, Object newValue) {
		parameterOverrides.add( new ParameterOverride(fullPathToGetter, newValue) );
	}

	public List<ParameterOverride> getOverrides() {
		return parameterOverrides;
	}

	public String getXmlFileName() {
		return xmlFileName;
	}

	public void setXmlFileName(String xmlFileName) {
		this.xmlFileName = xmlFileName;
	}

	public String getContainingClassType() {
		return containingClassType;
	}

	public void setContainingClassType(String containingClassType) {
		this.containingClassType = containingClassType;
	}

	static public class ParameterOverride {
		private String fullPathToGetter; //e.g.  getXPosition,  getXYThetaStage.getX
		private Object newValue; // new value for parameter

		public ParameterOverride() {
		}
		// Constructor
		public ParameterOverride(String fullPathToGetter, Object newValue) {
			this.fullPathToGetter = fullPathToGetter;
			this.newValue = newValue;
		}

		public String getFullPathToGetter() {
			return fullPathToGetter;
		}

		public Object getNewValue() {
			return newValue;
		}

		public void setNewValue(Object newValue) {
			this.newValue = newValue;
		}
	}

	// Comparator for sorting ParameterOverrides by name
    private class ParameterComparator implements Comparator<ParameterOverride> {
        @Override
        public int compare(ParameterOverride p1, ParameterOverride p2) {
            return p1.getFullPathToGetter().compareTo(p2.getFullPathToGetter());
        }
    }

    /**
     * Sort parameter override list into alphabetical order
     */
	public void sort() {
		parameterOverrides.sort(new ParameterComparator());
	}

	static public void addAliases(XStream xstream) {
		xstream.alias("OverridesForParametersFile", OverridesForParametersFile.class);
		xstream.alias("ParameterOverride", ParameterOverride.class);
		xstream.addImplicitCollection(OverridesForParametersFile.class, "parameterOverrides");
	}

	static public XStream getXStream() {
		XStream xstream = new XStream();
		addAliases(xstream);
		return xstream;
	}

	public String toXML() {
		return getXStream().toXML(this);
	}
}
