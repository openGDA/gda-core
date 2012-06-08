/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.epics.client.views.model.impl;

import gda.device.detector.areadetector.v17.NDROI;
import gda.epics.interfaces.NDROIType;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Int;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.epics.client.views.controllers.INDROIModelViewController;
import uk.ac.gda.epics.client.views.model.NdRoiModel;

/**
 *
 */
public class NDROIModelImpl extends EPICSBaseModel<NDROIType> implements NdRoiModel {

	private final static Logger logger = LoggerFactory.getLogger(NDROIModelImpl.class);

	private StartXMonitorListener startXMonitorListener;

	private StartYMonitorListener startYMonitorListener;

	private SizeXMonitorListener sizeXMonitorListener;

	private SizeYMonitorListener sizeYMonitorListener;

	private BinYMonitorListener binYMonitorListener;

	private BinXMonitorListener binXMonitorListener;

	private Set<INDROIModelViewController> viewControllers = new HashSet<INDROIModelViewController>();

	@Override
	public boolean registerNDRoiModelViewController(INDROIModelViewController viewController) {
		return viewControllers.add(viewController);
	}

	@Override
	public boolean removeNDRoiModelViewController(INDROIModelViewController viewController) {
		return viewControllers.remove(viewController);
	}

	@Override
	protected Class<NDROIType> getConfigClassType() {
		return NDROIType.class;
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}

	public NDROIModelImpl() {
		startXMonitorListener = new StartXMonitorListener();
		startYMonitorListener = new StartYMonitorListener();
		sizeXMonitorListener = new SizeXMonitorListener();
		sizeYMonitorListener = new SizeYMonitorListener();

		binXMonitorListener = new BinXMonitorListener();
		binYMonitorListener = new BinYMonitorListener();
	}

	@Override
	protected void doCheckAfterPropertiesSet() throws Exception {
		if (deviceName != null && pluginBase == null) {
			throw new IllegalArgumentException("'pluginBase' needs to be declared");
		}

	}

	private class StartXMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr != null && dbr.isINT()) {
				for (INDROIModelViewController controller : viewControllers) {
					controller.updateROIStartX(((DBR_Int) dbr).getIntValue()[0]);
				}
			}
		}
	}

	private class BinXMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr != null && dbr.isINT()) {
				for (INDROIModelViewController controller : viewControllers) {
					controller.updateROIBinX(((DBR_Int) dbr).getIntValue()[0]);
				}
			}
		}
	}

	private class BinYMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr != null && dbr.isINT()) {
				for (INDROIModelViewController controller : viewControllers) {
					controller.updateROIBinY(((DBR_Int) dbr).getIntValue()[0]);
				}
			}
		}
	}

	@Override
	public int getMinX() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getMinX().getPv(), startXMonitorListener));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(NDROI.MinX, startXMonitorListener));
		} catch (Exception ex) {
			throw ex;
		}
	}

	private class StartYMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr != null && dbr.isINT()) {
				for (INDROIModelViewController controller : viewControllers) {
					controller.updateROIStartY(((DBR_Int) dbr).getIntValue()[0]);
				}
			}
		}
	}

	@Override
	public int getMinY() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getMinY().getPv(), startYMonitorListener));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(NDROI.MinY, startYMonitorListener));
		} catch (Exception ex) {
			throw ex;
		}
	}

	private class SizeXMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr != null && dbr.isINT()) {
				for (INDROIModelViewController controller : viewControllers) {
					controller.updateROISizeX(((DBR_Int) dbr).getIntValue()[0]);
				}
			}
		}
	}

	@Override
	public int getSizeX() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeX().getPv(), sizeXMonitorListener));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(NDROI.SizeX, sizeXMonitorListener));
		} catch (Exception ex) {
			throw ex;
		}
	}

	private class SizeYMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr != null && dbr.isINT()) {
				for (INDROIModelViewController controller : viewControllers) {
					controller.updateROISizeY(((DBR_Int) dbr).getIntValue()[0]);
				}
			}
		}
	}

	@Override
	public int getSizeY() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getSizeY().getPv(), sizeYMonitorListener));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(NDROI.SizeY, sizeYMonitorListener));
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public void setSizeX(int sizeX) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSizeX().getPv(), sizeXMonitorListener), sizeX);
			} else {
				EPICS_CONTROLLER.caput(getChannel(NDROI.SizeX, sizeXMonitorListener), sizeX);
			}
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public void setSizeY(int sizeY) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getSizeY().getPv(), sizeYMonitorListener), sizeY);
			} else {
				EPICS_CONTROLLER.caput(getChannel(NDROI.SizeY, sizeYMonitorListener), sizeY);
			}
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public void setStartX(int startX) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getMinX().getPv(), startXMonitorListener), startX);
			} else {
				EPICS_CONTROLLER.caput(getChannel(NDROI.MinX, startXMonitorListener), startX);
			}
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public void setStartY(int startY) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getMinY().getPv(), startYMonitorListener), startY);
			} else {
				EPICS_CONTROLLER.caput(getChannel(NDROI.MinY, startYMonitorListener), startY);
			}
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public int getBinX() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getBinX().getPv(), binXMonitorListener));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(NDROI.BinX, binXMonitorListener));
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public int getBinY() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getBinY().getPv(), binYMonitorListener));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(NDROI.BinY, binYMonitorListener));
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public void setBinX(int binX) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getBinX().getPv(), binXMonitorListener), binX);
			} else {
				EPICS_CONTROLLER.caput(getChannel(NDROI.BinX, binXMonitorListener), binX);
			}
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public void setBinY(int binY) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getBinY().getPv(), binYMonitorListener), binY);
			} else {
				EPICS_CONTROLLER.caput(getChannel(NDROI.MinY, binYMonitorListener), binY);
			}
		} catch (Exception ex) {
			throw ex;
		}
	}

}
