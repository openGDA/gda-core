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

package gda.data.nexus.nxclassio;

import java.util.Vector;

/**
 *
 */
public class NexusPath {
	private Vector<NexusGroup> groups = new Vector<NexusGroup>();
	private String dataSetName;

	/**
	 * @return dataset name
	 */
	public String getDataSetName() {
		return dataSetName == null ? "" : dataSetName;
	}

	/**
	 * @return a vector of nexus groups
	 */
	public Vector<NexusGroup> getGroups() {
		return groups;
	}

	/**
	 * @param dataSetName
	 */
	public void setDataSetName(String dataSetName) {
		this.dataSetName = dataSetName;
	}

	/**
	 * @param group
	 */
	public void addGroupPath(NexusGroup group) {
		this.groups.add( group );
	}
	
	/**
	 * @param className
	 * @return true or false
	 */
	public boolean isOfClass(String className){
		return getGroups().lastElement().getNXclass().equals(className);
	}

	/**
	 * @return Nexus class name
	 */
	public String getNXClassName() {
		return getGroups().lastElement().getNXclass();
	}	

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dataSetName == null) ? 0 : dataSetName.hashCode());
		result = prime * result + ((groups == null) ? 0 : groups.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof NexusPath)) {
			return false;
		}	
		NexusPath other = (NexusPath) o;		
		return groups.equals(other.groups) && dataSetName.equals(other.dataSetName);
	}

	/**
	 * @param source
	 * @return the nexus path
	 */
	static public NexusPath getInstance(NexusPath source){
		NexusPath copy = new NexusPath();
		Vector<NexusGroup> groups = source.getGroups();
		for(NexusGroup group : groups){
			copy.addGroupPath(NexusGroup.getInstance(group));
		}
		copy.setDataSetName( new String (source.getDataSetName()));
		return copy;
	}
}
