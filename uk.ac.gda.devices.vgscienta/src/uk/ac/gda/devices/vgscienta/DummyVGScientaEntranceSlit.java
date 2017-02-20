/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.vgscienta;

import java.util.List;

import gda.factory.Configurable;
import gda.factory.FactoryException;
import uk.ac.gda.devices.vgscienta.VGScientaEntranceSlit.EntranceSlit;

public class DummyVGScientaEntranceSlit implements EntranceSlitInformationProvider, Configurable {

	// The list of entrance slits available to be configured in Spring. Must be ordered as EPICS enum is
	private List<EntranceSlit> slits;

	// The currently selected slit
	private EntranceSlit currentSlit;

	// The direction the analyser is mounted in. To be set in spring
	private String direction = "unknown";

	@Override
	public Integer getRawValue() {
		return currentSlit.getRawValue();
	}

	@Override
	public Double getSizeInMM() {
		return currentSlit.getSize();
	}

	@Override
	public String getShape() {
		return currentSlit.getShape();
	}

	@Override
	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public List<EntranceSlit> getSlits() {
		return slits;
	}

	public void setSlits(List<EntranceSlit> slits) {
		this.slits = slits;
	}

	public EntranceSlit getCurrentSlit() {
		return currentSlit;
	}

	public void setSlitIndex(int index) {
		currentSlit = slits.get(index);
	}

	@Override
	public void configure() throws FactoryException {
		// Check the prerequisites for this to work
		if (slits == null || slits.isEmpty()) {
			throw new FactoryException("slits must be set");
		}
		// Set the current slit to avoid possible NPE
		currentSlit = slits.get(0);
	}
}