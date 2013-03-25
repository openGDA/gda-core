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

import org.apache.commons.beanutils.BeanUtils;

import uk.ac.gda.doe.DOEField;

public class TestBean implements Serializable {

	@DOEField(value=1, type=java.lang.Integer.class)
	private String i;
	
	@DOEField(5)
	private String j;
	
	@DOEField(9)
	private String k;
	
	private int d;

	public TestBean(int d) {
		this.d = d;
	}
	public TestBean() {
		
	}
	public TestBean(String i, String j, String k) {
		setI(i);
		setJ(j);
		setK(k);
	}

	/**
	 * @return Returns the i.
	 */
	public String getI() {
		return i;
	}

	/**
	 * @param i The i to set.
	 */
	public void setI(String i) {
		this.i = i;
	}

	/**
	 * @return Returns the j.
	 */
	public String getJ() {
		return j;
	}

	/**
	 * @param j The j to set.
	 */
	public void setJ(String j) {
		this.j = j;
	}

	/**
	 * @return Returns the k.
	 */
	public String getK() {
		return k;
	}

	/**
	 * @param k The k to set.
	 */
	public void setK(String k) {
		this.k = k;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + d;
		result = prime * result + ((i == null) ? 0 : i.hashCode());
		result = prime * result + ((j == null) ? 0 : j.hashCode());
		result = prime * result + ((k == null) ? 0 : k.hashCode());
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
		TestBean other = (TestBean) obj;
		if (d != other.d)
			return false;
		if (i == null) {
			if (other.i != null)
				return false;
		} else if (!i.equals(other.i))
			return false;
		if (j == null) {
			if (other.j != null)
				return false;
		} else if (!j.equals(other.j))
			return false;
		if (k == null) {
			if (other.k != null)
				return false;
		} else if (!k.equals(other.k))
			return false;
		return true;
	}

	@Override
	public String toString() {
		try {
			return BeanUtils.describe(this).toString();
		} catch (Exception e) {
			return e.getMessage();
		}
	}
	public int getD() {
		return d;
	}
	public void setD(int d) {
		this.d = d;
	}
}
