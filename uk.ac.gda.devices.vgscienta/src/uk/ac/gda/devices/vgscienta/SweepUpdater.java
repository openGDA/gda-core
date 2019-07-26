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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Device;
import gda.device.DeviceBase;
import gda.epics.connection.EpicsController;
import gda.factory.FactoryException;
import gov.aps.jca.CAStatusException;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import uk.ac.gda.api.remoting.ServiceInterface;

@ServiceInterface(Device.class)
public class SweepUpdater extends DeviceBase {
	private static final Logger logger = LoggerFactory.getLogger(SweepUpdater.class);

	private EpicsController epicsController;
	private String currentPointPV;
	private String maxSweepPV;
	private String pctSweepPV;
	private String currentIterationPV;

	private int oldNumber = -1;
	private int maxSweep = 0;
	private int percentage = 0;
	private int currentIteration;

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		epicsController = EpicsController.getInstance();

		try {
			epicsController.setMonitor(epicsController.createChannel(maxSweepPV), new MonitorListener() {
				@Override
				public void monitorChanged(MonitorEvent ev) {
					try {
						maxSweep =((gov.aps.jca.dbr.INT) ev.getDBR().convert(DBRType.INT)).getIntValue()[0];
					} catch (CAStatusException e) {
						logger.error("error reading maxsweeps", e);
					}
				}
			});

			epicsController.setMonitor(epicsController.createChannel(currentPointPV),  new MonitorListener() {
				@Override
				public void monitorChanged(MonitorEvent ev) {
					try {
						oldNumber =((gov.aps.jca.dbr.INT) ev.getDBR().convert(DBRType.INT)).getIntValue()[0];
						dispatch();
					} catch (Exception e) {
						logger.error("exception caught preparing swept updates", e);
					}
				}
			});

			epicsController.setMonitor(epicsController.createChannel(pctSweepPV),  new MonitorListener() {
				@Override
				public void monitorChanged(MonitorEvent ev) {
					try {
						percentage =((gov.aps.jca.dbr.INT) ev.getDBR().convert(DBRType.INT)).getIntValue()[0];
						dispatch();
					} catch (Exception e) {
						logger.error("exception caught preparing swept updates", e);
					}
				}
			});

			epicsController.setMonitor(epicsController.createChannel(currentIterationPV),  new MonitorListener() {
				@Override
				public void monitorChanged(MonitorEvent ev) {
					try {
						currentIteration =((gov.aps.jca.dbr.INT) ev.getDBR().convert(DBRType.INT)).getIntValue()[0];
						dispatch();
					} catch (Exception e) {
						logger.error("exception caught preparing swept updates", e);
					}
				}
			});

		} catch (Exception e) {
			throw new FactoryException("Cannot set up monitoring of arrays", e);
		}
		setConfigured(true);
	}

	public void dispatch() {
		SweptProgress progress = new SweptProgress(oldNumber, maxSweep, percentage, currentIteration);
		logger.debug("publishing {}",progress);
		notifyIObservers(getName(), progress);
	}

	public String getCurrentPointPV() {
		return currentPointPV;
	}

	public void setCurrentPointPV(String currentSweepPV) {
		this.currentPointPV = currentSweepPV;
	}

	public String getMaxSweepPV() {
		return maxSweepPV;
	}

	public void setMaxSweepPV(String maxSweepPV) {
		this.maxSweepPV = maxSweepPV;
	}

	public String getPercentagePV() {
		return pctSweepPV;
	}

	public void setPercentagePV(String percentagePV) {
		this.pctSweepPV = percentagePV;
	}

	public String getCurrentIterationPV() {
		return currentIterationPV;
	}

	public void setCurrentIterationPV(String currentIterationPV) {
		this.currentIterationPV = currentIterationPV;
	}
}
