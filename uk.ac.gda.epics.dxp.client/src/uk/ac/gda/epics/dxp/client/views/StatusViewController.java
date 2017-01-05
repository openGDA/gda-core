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

package uk.ac.gda.epics.dxp.client.views;

import gov.aps.jca.TimeoutException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.epics.dxp.client.views.model.DXPModel;

/**
 * Controller for the status view. This controller connects the Status View part to the EPICS model - showing relevant
 * statuses on the GUI.
 */
/**
 *
 */
public class StatusViewController implements InitializingBean {

	private final static Logger logger = LoggerFactory
			.getLogger(StatusViewController.class);

	private List<StatusView> statusViews = new ArrayList<StatusView>();
	private DXPModel dxpModel;

	public void updateView() {
		// Do nothing
	}

	public void updateInstantDeadTime(double ideadtime) {
		for (StatusView sv : statusViews) {
			sv.setInstantDeadTime(String.valueOf(ideadtime));
		}
	}

	public void updateDeadTime(double deadtime) {
		for (StatusView sv : statusViews) {
			sv.setDeadTime(String.valueOf(deadtime));
		}
	}

	public void updateRealTime(double realtime) {
		for (StatusView sv : statusViews) {
			sv.setRealTime(String.valueOf(realtime));
		}
	}

	public void updateLiveTime(double livetime) {
		for (StatusView sv : statusViews) {
			sv.setLiveTime(String.valueOf(livetime));
		}
	}

	public void updateAcquireState(short acquireState) {
		for (StatusView sv : statusViews) {
			sv.setAcquireState(acquireState);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (dxpModel == null) {
			throw new IllegalArgumentException("'dxpModel' is required");
		}
	}

	/**
	 */
	public Future<Boolean> updateAllFields() {
		ExecutorService executorService = Executors.newFixedThreadPool(3);
		return executorService.submit(updateFields);
	}

	private Callable<Boolean> updateFields = new Callable<Boolean>() {
		@Override
		public Boolean call() throws Exception {
			try {
				// adbase model values update
				updateDeadTime(dxpModel.getDeadTime());
				updateInstantDeadTime(dxpModel.getInstantDeadTime());
				updateAcquireState(dxpModel.getAcquireState());
				updateRealTime(dxpModel.getRealTime());
				updateLiveTime(dxpModel.getLiveTime());
			} catch (TimeoutException tme) {
				logger.error("IOC doesn't seem to be running", tme);
				throw tme;
			} catch (Exception ex) {
				logger.error("Problem with loading the channel", ex);
				throw ex;
			}
			return Boolean.TRUE;

		}

	};

	public void addListener(StatusView statusView) {
		statusViews.add(statusView);
	}

	public void removeStatusView(StatusView statusView) {
		statusViews.remove(statusView);
	}

	public void setDxpModel(DXPModel dxpModel) {
		this.dxpModel = dxpModel;
	}

	public DXPModel getDxpModel() {
		return dxpModel;
	}

}
