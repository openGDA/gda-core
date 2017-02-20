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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.epics.connection.EpicsController;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gov.aps.jca.Channel;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

/**
 * A class to provide information about the analyser entrance slit which is in uses. It gets the current slit by monitoring a EPICS enum PV but looks up the
 * information from Spring this allows GDA to be decoupled from the EPICS enum.
 *
 * @author James Mudd
 */
public class VGScientaEntranceSlit implements EntranceSlitInformationProvider, Configurable {
	private static final Logger logger = LoggerFactory.getLogger(VGScientaEntranceSlit.class);

	private static final EpicsController EPICS_CONTROLLER = EpicsController.getInstance();

	private String basePv; // eg BL05I-EA-SLITS-01:POS

	// The list of entrance slits available to be configured in Spring. Must be ordered as EPICS enum is
	private List<EntranceSlit> slits;

	// The currently selected slit
	private EntranceSlit currentSlit;

	// The direction the analyser is mounted in. To be set in spring
	private String direction = "unknown";

	private final MonitorListener slitListener = new MonitorListener() {
		@Override
		public void monitorChanged(MonitorEvent ev) {
			logger.debug("Received entrnace slit update: {}", ev);
			if (ev.getDBR() instanceof DBR_Enum) {
				try {
					// Get the enum index and lookup the new slit
					int pos = ((DBR_Enum) ev.getDBR()).getEnumValue()[0];
					currentSlit = slits.get(pos);
					logger.debug("New slit is: {}", currentSlit);
				} catch (Exception e) {
					logger.error("Error processing entrance slit update", e);
				}
			}
		}
	};

	@Override
	public void configure() throws FactoryException {
		// Check the prerequisites for this to work
		if (basePv == null) {
			throw new FactoryException("basePv must be set");
		}
		if (slits == null || slits.isEmpty()) {
			throw new FactoryException("slits must be set");
		}
		// Set the current slit to avoid possible NPE
		currentSlit = slits.get(0);

		try {
			Channel channel = EPICS_CONTROLLER.createChannel(basePv);
			// Add a monitor so it can update the slit
			EPICS_CONTROLLER.setMonitor(channel, slitListener);
		} catch (Exception e) {
			String msg = "Error setting up analyser entrance slit monitoring";
			logger.error(msg, e);
			throw new FactoryException("Error setting up analyser entrance slit monitoring", e);
		}
		logger.info("Finished configuring analyser entrance slit");
	}

	/**
	 * Small immutable class to encapsulate the information about a entrance slit
	 */
	public static class EntranceSlit {
		private final int rawValue;
		private final double size;
		private final String shape;

		public EntranceSlit(int rawValue, double size, String shape) {
			this.rawValue = rawValue; // eg. 100, 200, 300
			this.size = size; // in mm eg 0.1 mm 0.3 mm
			this.shape = shape; // e.g. curved, straight, aperture
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

		@Override
		public String toString() {
			return "EntranceSlit [rawValue=" + rawValue + ", size=" + size + ", shape=" + shape + "]";
		}
	}

	@Override
	public Number getRawValue() {
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

	public String getBasePv() {
		return basePv;
	}

	public void setBasePv(String basePv) {
		this.basePv = basePv;
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

}
