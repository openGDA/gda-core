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

package uk.ac.diamond.daq.mapping.impl;

import java.util.List;

import gda.factory.FindableBase;

/**
 * Spring-configure this bean to specify which motors may be set
 * as the fast, slow and (optionally) associated axes for mapping experiments.
 */
public class MappingStageOptions extends FindableBase {

	private List<String> fastAxes;
	private List<String> slowAxes;
	private List<String> associatedAxes;


	public List<String> getFastAxes() {
		return fastAxes;
	}
	public void setFastAxes(List<String> fastAxes) {
		this.fastAxes = fastAxes;
	}
	public List<String> getSlowAxes() {
		return slowAxes;
	}
	public void setSlowAxes(List<String> slowAxes) {
		this.slowAxes = slowAxes;
	}
	public List<String> getAssociatedAxes() {
		return associatedAxes;
	}
	public void setAssociatedAxes(List<String> associatedAxes) {
		this.associatedAxes = associatedAxes;
	}

}
