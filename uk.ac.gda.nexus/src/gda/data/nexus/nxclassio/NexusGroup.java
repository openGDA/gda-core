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

class NexusGroup {
	public static String NXDataClassName = "NXdata";
	public static String NXEntryClassName = "NXentry";
	public static String SDSClassName = "SDS";

	static public NexusGroup getInstance(NexusGroup source) {
		return new NexusGroup(source.name, source.NXclass);
	}

	final String name;
	final String NXclass;

	public NexusGroup(String name, String NXclass) {
		this.name = name;
		this.NXclass = NXclass;
	}

	public boolean containsSDS() {
		return getNXclass().equals(SDSClassName);
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((NXclass == null) ? 0 : NXclass.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof NexusGroup)) {
			return false;
		}
		NexusGroup other = (NexusGroup) o;
		return name.equals(other.name) && NXclass.equals(other.NXclass);
	}

	public String getName() {
		return name;
	}

	public String getNXclass() {
		return NXclass;
	}

	@Override
	public String toString() {
		return name + "." + NXclass;
	}
}

	