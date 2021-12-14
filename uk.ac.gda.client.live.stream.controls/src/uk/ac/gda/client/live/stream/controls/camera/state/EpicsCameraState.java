/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.gda.client.live.stream.controls.camera.state;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IEvaluationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.epics.connection.EpicsController;
import gda.epics.connection.EpicsController.MonitorType;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.Monitor;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;

/**
 * An {@link ISourceProvider} supports and updates Camera State variable
 * {@link #getStateVariableName()}.
 * <p>
 * It monitors Camera State change in EPICS, and update this variable in
 * {@link ISources#WORKBENCH} scope.
 * </p>
 * <p>
 * The {@link #getStateVariableName()} variable name must be unique for each
 * camera state instance and can be defined in Spring bean for that camera state
 * instance.
 * </p>
 * <p>
 * The same variable name must be declared in {@link org.eclipse.ui.services}
 * extension point under this {@link ISourceProvider}. Which then can be used in
 * core expression (visibleWhen, activeWhen, or enableWhen) to create dynamic
 * camera control contribution to the view's toolbar.
 * </p>
 * It also caches the current state which can be queried programmatically using
 * {@link #isRunning()}.
 */
public class EpicsCameraState extends AbstractSourceProvider implements ICameraState {

	private static final Logger logger = LoggerFactory.getLogger(EpicsCameraState.class);
	private static final String CAM_ACQUIRE = ":CAM:Acquire";
	private static final EpicsController EPICS_CONTROLLER = EpicsController.getInstance();
	private String stateVariableName;
	private State curState = State.NOT_LOADED;
	private Channel startCh = null;
	private Monitor monitor = null;
	private CameraConfiguration cameraConfig;
	private String pvName;
	private boolean connected = false;
	/**
	 * a camera state monitor used to synchronise Camera state in GDA with EPICS
	 * control
	 */
	private final MonitorListener ml = new MonitorListener() {
		// used to synchronise state when changed outside GDA
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr.isENUM()) {
				short enumValue = ((DBR_Enum) dbr).getEnumValue()[0];
				if (enumValue == 1) {
					setState(State.RUNNING);
					requestEvaluation(RUNNING);
					logger.info("Camera is started");
				} else if (enumValue == 0) {
					setState(State.STOPPED);
					requestEvaluation(STOPPED);
					logger.info("Camera is stopped");
				} else {
					setState(State.NOT_LOADED);
					requestEvaluation(NOT_LOADED);
					logger.warn("Camera state is not recognised!");
				}
			}
		}
	};

	public boolean isRunning() {
		return curState == State.RUNNING;
	}

	private void setState(State newstate) {
		curState = newstate;
	}

	@Override
	public void connect() throws DeviceException {
		try {
			if (getCameraConfig() != null) {
				startCh = EPICS_CONTROLLER.createChannel(getCameraConfig().getArrayPv().split(":")[0] + CAM_ACQUIRE);
			} else if (getPvName() != null) {
				startCh = EPICS_CONTROLLER.createChannel(getPvName());
			}
			monitor = EPICS_CONTROLLER.setMonitor(startCh, ml, MonitorType.NATIVE);
			connected = true;
		} catch (CAException | TimeoutException | InterruptedException e) {
			logger.error("Exception while create channel to camera {}",
					getCameraConfig().getArrayPv().split(":")[0] + CAM_ACQUIRE, e);
			throw new DeviceException(String.format("Exception while create channel to camera %s",
					getCameraConfig().getArrayPv().split(":")[0] + CAM_ACQUIRE), e);
		}
	}

	public void disconnect() {
		if (monitor != null) {
			monitor.removeMonitorListener(ml);
			monitor = null;
		}
		if (startCh != null) {
			startCh.dispose();
			startCh = null;
		}
		connected = false;
	}

	@Override
	public void dispose() {
		disconnect();
	}

	@Override
	public Map<String, String> getCurrentState() {
		Map<String, String> map = new HashMap<String, String>(1);
		if (curState == State.RUNNING) {
			map.put(getStateVariableName(), RUNNING);
		} else if (curState == State.STOPPED) {
			map.put(getStateVariableName(), STOPPED);
		} else if (curState == State.NOT_LOADED) {
			map.put(getStateVariableName(), NOT_LOADED);
		}
		return map;
	}

	@Override
	public String[] getProvidedSourceNames() {
		return new String[] { getStateVariableName() };
	}

	public void setRunning() {
		fireSourceChanged(ISources.WORKBENCH, getStateVariableName(), RUNNING);
	}

	public void setStopped() {
		fireSourceChanged(ISources.WORKBENCH, getStateVariableName(), STOPPED);
	}

	private void requestEvaluation(String currstate) {
		Display.getDefault().asyncExec(() -> {
			IEvaluationService evaluationService = PlatformUI.getWorkbench().getService(IEvaluationService.class);
			evaluationService.getCurrentState().addVariable(getStateVariableName(), currstate);
			evaluationService.requestEvaluation(getStateVariableName());
		});
	}

	public CameraConfiguration getCameraConfig() {
		return cameraConfig;
	}

	public void setCameraConfig(CameraConfiguration cameraConfig) {
		this.cameraConfig = cameraConfig;
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	public void afterPropertiesSet() throws Exception {
		if (getCameraConfig() == null && getPvName() == null) {
			throw new IllegalStateException("Both camera configuration and pvName are NOT set!, You must set one of these!");
		}
		if (getStateVariableName() == null) {
			throw new IllegalStateException("State variable name is NOT set!");
		}
	}

	/**
	 * @return Returns the stateVariableName.
	 */
	public String getStateVariableName() {
		return stateVariableName;
	}

	/**
	 * @param stateVariableName The stateVariableName to set.
	 */
	public void setStateVariableName(String stateVariableName) {
		this.stateVariableName = stateVariableName;
	}

	public String getPvName() {
		return pvName;
	}

	public void setPvName(String pvName) {
		this.pvName = pvName;
	}
}
