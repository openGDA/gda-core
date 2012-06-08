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

package gda.device.attenuator;

/**
 * A subclass of {@link AttenuatorFilter} that communicates with an EPICS attenuator filter.
 */
public class EpicsAttenuatorFilter extends AttenuatorFilter {
	
	protected String pv;
	
	/**
	 * Sets the PV for controlling this filter.
	 */
	public void setPv(String pv) {
		this.pv = pv;
	}
	
	public String getPv() {
		return pv;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		if (pv == null) {
			throw new IllegalStateException("PV not set");
		}
	}
	
	@Override
	public String toString() {
		return String.format("EpicsAttenuatorFilter(%s, absorption=%.3f, pv=%s)", name, absorptionPercentage, pv);
	}
}
