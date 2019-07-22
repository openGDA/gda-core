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

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.detector.areadetector.v17.NDStats;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import uk.ac.gda.epics.client.views.controllers.INDStatModelViewController;
import uk.ac.gda.epics.client.views.model.NdStatModel;

/**
 *
 */
public class NDStatModelImpl extends EPICSBaseModel implements NdStatModel {
	private final static Logger logger = LoggerFactory.getLogger(NDStatModelImpl.class);
	private MinMonitorListener minMonitorListener;
	private MaxMonitorListener maxMonitorListener;
	private MeanMonitorListener meanMonitorListener;
	private SigmaMonitorListener sigmaMonitorListener;
	private Set<INDStatModelViewController> viewControllers = new HashSet<INDStatModelViewController>();

	@Override
	public boolean registerStatViewController(INDStatModelViewController viewController) {
		return viewControllers.add(viewController);
	}

	@Override
	public boolean removeStatViewController(INDStatModelViewController viewController) {
		return viewControllers.remove(viewController);
	}

	public NDStatModelImpl() {
		minMonitorListener = new MinMonitorListener();
		maxMonitorListener = new MaxMonitorListener();
		meanMonitorListener = new MeanMonitorListener();
		sigmaMonitorListener = new SigmaMonitorListener();
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}

	private class MeanMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr != null && dbr.isDOUBLE()) {
				for (INDStatModelViewController controller : viewControllers) {
					controller.updateMean(((DBR_Double) dbr).getDoubleValue()[0]);
				}
			}
		}
	}

	private class SigmaMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr != null && dbr.isDOUBLE()) {
				for (INDStatModelViewController controller : viewControllers) {
					controller.updateSigma(((DBR_Double) dbr).getDoubleValue()[0]);
				}
			}
		}
	}

	private class MinMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr != null && dbr.isDOUBLE()) {
				for (INDStatModelViewController controller : viewControllers) {
					controller.updateMin(((DBR_Double) dbr).getDoubleValue()[0]);
				}
			}
		}
	}

	private class MaxMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr != null && dbr.isDOUBLE()) {
				for (INDStatModelViewController controller : viewControllers) {
					controller.updateMax(((DBR_Double) dbr).getDoubleValue()[0]);
				}
			}
		}
	}

	@Override
	public double getMin() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(NDStats.MinValue_RBV, minMonitorListener));
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public double getMax() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(NDStats.MaxValue_RBV, maxMonitorListener));
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public double getMean() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(NDStats.MeanValue_RBV, meanMonitorListener));
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public double getSigma() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(NDStats.Sigma_RBV, sigmaMonitorListener));
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public double[] getHistogram() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDoubleArray(getChannel(NDStats.Histogram_RBV, null));
		} catch (Exception ex) {
			throw ex;
		}
	}
}
