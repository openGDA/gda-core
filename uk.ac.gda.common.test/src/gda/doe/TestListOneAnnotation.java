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

package gda.doe;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TestListOneAnnotation implements Serializable {

	private List<TestDimsData> testBeans;

	/**
	 * @return Returns the testBeans.
	 */
	public List<TestDimsData> getTestBeans() {
		return testBeans;
	}

	/**
	 * @param testBeans The testBeans to set.
	 */
	public void setTestBeans(List<TestDimsData> testBeans) {
		this.testBeans = testBeans;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((testBeans == null) ? 0 : testBeans.hashCode());
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
		TestListOneAnnotation other = (TestListOneAnnotation) obj;
		if (testBeans == null) {
			if (other.testBeans != null)
				return false;
		} else if (!testBeans.equals(other.testBeans))
			return false;
		return true;
	}

	public void add(TestDimsData t) {
		if (testBeans==null) testBeans = new ArrayList<TestDimsData>(3);
		testBeans.add(t);
	}
	
}
