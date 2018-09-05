/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.api;

public interface IMappingExperimentBeanProvider {

	public IMappingExperimentBean getMappingExperimentBean();

	public void setMappingExperimentBean(IMappingExperimentBean mappingBean);

	/**
	 * @return Whether the bean has been set by a view (typically by being restored from a saved Eclipse configuration),
	 *         rather than injected by Spring
	 */
	public boolean isSetByView();

	/**
	 * Set the flag to say whether the bean has been set by a view
	 * @param setByView true if set by a view, false otherwise
	 */
	public void setSetByView(boolean setByView);

}
