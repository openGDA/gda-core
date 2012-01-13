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

package uk.ac.gda.ui.doe;

import java.util.List;

public interface DOEBeanProvider {

	/**
	 * 
	 * Implement to provide the beans which
	 * contain the DOE data.
	 * 
	 * @return beans
	 */
	public List<Object> getBeans() throws Exception;
	
	/**
	 * Returns the label that should be used for a field in a particular bean.
	 * If null is returned, the field will be used as the column label.
	 * 
	 * @param fieldName
	 * @param bean
	 * @return label
	 */
	public String getColumnLabel(final String fieldName, final Class<?> bean);
}
