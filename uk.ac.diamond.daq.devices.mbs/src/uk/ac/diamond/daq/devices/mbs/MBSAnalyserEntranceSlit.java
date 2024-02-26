/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.devices.mbs;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.ConfigurableBase;
import gda.factory.FactoryException;

public class MBSAnalyserEntranceSlit extends ConfigurableBase implements MBSEntranceSlitInformationProvider {
	private static final Logger logger = LoggerFactory.getLogger(MBSAnalyserEntranceSlit.class);
	// The list of entrance slits available to be configured in Spring.
	private List<EntranceSlit> slits;
	private EntranceSlit currentSlit;

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) return;

		if (slits == null || slits.isEmpty()) {
			throw new FactoryException("slits must be set");
		}
		// Set the current slit to avoid possible NPE
		currentSlit = slits.get(0);

		// Here monitor will be set to change currentSlit accordingly
		logger.info("Finished configuring analyser entrance slit");
		setConfigured(true);
	}

	/**
	 * Small immutable class to encapsulate the information about a entrance slit
	 */
	public static class EntranceSlit {
		private final int rawValue;
		private final double size;
		private final String shape;
		private final String direction;

		public EntranceSlit(int rawValue, double size, String shape, String direction) {
			this.rawValue = rawValue; // eg. 100, 200, 300
			this.size = size; // in mm eg 0.1 mm 0.3 mm
			this.shape = shape; // e.g. curved, straight, aperture
			this.direction = direction; // vertical or horizontal
		}

		public int getRawValue() {
			return rawValue;
		}

		public double getSize() {
			return size;
		}

		public String getShape() {
			return shape;
		}

		public String getDirection() {
			return direction;
		}

		@Override
		public String toString() {
			return "EntranceSlit [rawValue=" + rawValue + ", size=" + size + ", direction="+direction+", shape=" + shape + "]";
		}
	}

	@Override
	public Number getRawValue() {
		return currentSlit.getRawValue();
	}

	@Override
	public Double getSize() {
		return currentSlit.getSize();
	}

	@Override
	public String getShape() {
		return currentSlit.getShape();
	}

	@Override
	public String getDirection() {
		return currentSlit.direction;
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

	@Override
	public void setCurrentSlitByValue (int number) {
		for (EntranceSlit slit:slits) {
			if (slit.rawValue == number) currentSlit = slit;
		}
	}
}
