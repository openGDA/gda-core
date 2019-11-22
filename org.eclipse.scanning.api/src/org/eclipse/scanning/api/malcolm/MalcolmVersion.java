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

package org.eclipse.scanning.api.malcolm;

public class MalcolmVersion implements Comparable<MalcolmVersion> {

	public static final MalcolmVersion VERSION_4_0 = new MalcolmVersion(4, 0);
	public static final MalcolmVersion VERSION_4_2 = new MalcolmVersion(4, 2);

	private int major;

	private int minor;

	private MalcolmVersion() {
		// private constructor for json (de)serialization
	}

	public MalcolmVersion(int major, int minor) {
		this.major = major;
		this.minor = minor;
	}

	public int getMajor() {
		return major;
	}

	public void setMajor(int major) {
		this.major = major;
	}

	public int getMinor() {
		return minor;
	}

	public void setMinor(int minor) {
		this.minor = minor;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + major;
		result = prime * result + minor;
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
		MalcolmVersion other = (MalcolmVersion) obj;
		if (major != other.major)
			return false;
		if (minor != other.minor)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "version " + major + "." + minor;
	}

	@Override
	public int compareTo(MalcolmVersion other) {
		if (other == this) return 0;

		int result = major - other.major;
		if (result != 0)  {
			return result;
		}

		return minor - other.minor;
	}

	public boolean isVersionOrAbove(MalcolmVersion version) {
		return this.compareTo(version) >= 0;
	}

}
