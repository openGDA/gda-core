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

package uk.ac.gda.richbeans.components.selector;

/**
 * Class used to ease the use of ListViewer with respect to 
 * arbitrary beans which may be equal in terms of value.
 * 
 * The bean wrapper has a unique name and avoids these problems.
 */
class BeanWrapper {

	public BeanWrapper(final Object bean) {
		setBean(bean);
	}
	
	// We override hashCode because BeanWrappers are mostly put into hash tables
	// and we have a reasonable hashCode we can calculate.
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bean == null) ? 0 : bean.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		// We very much want all BeanWrappers to compare differently, regardless of their
		// containing bean. Best way is to fall back on Object.equals.
		return super.equals(obj);
	}
	
	private Object bean;
	private String name;
	/**
	 * @return the bean
	 */
	public Object getBean() {
		return bean;
	}
	/**
	 * @param bean the bean to set
	 */
	public void setBean(Object bean) {
		this.bean = bean;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	public boolean isValidName() {
		if (name==null)             return false;
		if ("".equals(name.trim())) return false;
		return true;
	}
}

	