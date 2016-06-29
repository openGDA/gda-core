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

package uk.ac.gda.devices.vgscienta.i05_1;

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.enumpositioner.EpicsEnumConstants;
import gda.epics.connection.EpicsController;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gov.aps.jca.Channel;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import uk.ac.gda.devices.vgscienta.EntranceSlitInformationProvider;

public class I05_1EntranceSlit implements EntranceSlitInformationProvider, Configurable, MonitorListener{
	private static final Logger logger = LoggerFactory.getLogger(I05_1EntranceSlit.class);

	// BL05I-EA-SLITS-01:POS
	private String labelPV = "BL05I-EA-SLITS-01:POS";
	private EpicsController epicsController;
	private Number rawValue = new Integer(0);
	private Double size = 0.0;
	private String shape = "unknown";
	private String label= "unknown";
	private String direction= "unknown";
	private Vector<String> positions = new Vector<String>(12);

	@Override
	public void configure() throws FactoryException {
		epicsController = EpicsController.getInstance();
		try {
			// loop over the pv's in the record
			for (int i = 0; i < 12; i++) {
				Channel thisStringChannel = epicsController.createChannel(labelPV + "." + EpicsEnumConstants.CHANNEL_NAMES[i]);
				String positionName = epicsController.cagetString(thisStringChannel);
				epicsController.destroy(thisStringChannel);
				positions.add(positionName);
			}

			epicsController.setMonitor(epicsController.createChannel(labelPV), this);
		} catch (Exception e) {
			throw new FactoryException("error setting up entract slit monitoring", e);
		}
	}

	@Override
	public Number getRawValue() {
		return rawValue;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public Double getSizeInMM() {
		return size;
	}

	@Override
	public String getShape() {
		return shape;
	}
	@Override
	public void monitorChanged(MonitorEvent ev) {
		logger.debug(ev.toString());
		if (ev.getDBR() instanceof DBR_Enum) {
			try {
				int pos = ((DBR_Enum) ev.getDBR()).getEnumValue()[0];
				label = positions.get(pos);
				String[] strings = label.split(" ");
				rawValue = Integer.valueOf(strings[0]);
				size = Double.valueOf(strings[1]);
				shape = strings[2];
				logger.debug(String.format("processed updates for entrance slit %s: %s",labelPV, label));
			} catch (Exception e) {
				logger.error("problem processing slit update", e);
			}
		}
	}

	public String getLabelPV() {
		return labelPV;
	}

	public void setLabelPV(String labelPV) {
		this.labelPV = labelPV;
	}

	@Override
	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}
}
