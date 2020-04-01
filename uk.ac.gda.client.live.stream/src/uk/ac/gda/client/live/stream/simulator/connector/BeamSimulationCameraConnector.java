/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.gda.client.live.stream.simulator.connector;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.DataEvent;
import org.eclipse.january.dataset.IDataListener;
import org.eclipse.january.dataset.IDatasetChangeChecker;
import org.eclipse.january.dataset.IDatasetConnector;
import org.eclipse.january.dataset.ILazyDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.live.stream.simulator.stream.BeamSimulationStream;

/**
 * Simulate a live stream from a camera associated with a beam movable on X/Y axis. The configuration uses a
 * {@link BeamSimulationCamera} to define both the camera size and the number of pixels spanned by a single motor step.
 *
 * <p>
 * A typical configuration looks like
 *
 * <pre>
 * {@code     <bean id="displayPixelNumberOnAxes" class="uk.ac.gda.client.live.stream.calibration.PixelCalibration"/>
	<bean id="pco_cam_config"
		class="uk.ac.gda.client.live.stream.simulator.connector.BeamDrivenCamera">
		<property name="displayName" value="Imaging Camera" />
		<property name="arrayPv" value="gdaSimulator:BeamDrivenCamera" />
		<property name="calibratedAxesProvider" ref="displayPixelNumberOnAxes"/>

		<!-- In the simulation, represent camera array X size -->
		<property name="cameraWidth" value="1280"/>
		<!-- In the simulation, represent camera array Y size -->
		<property name="cameraHeight" value="960"/>

		<property name="driverX" value="kb_x"/>
		<!-- In the simulation, represent the ration between kbX position with with offset to pixel camera zero -->
		<property name="scaleX" value="100"/>

		<property name="driverY" value="kb_y"/>
		<!-- In the simulation, represent the ration between kbX position with with offset to pixel camera zero -->
		<property name="scaleY" value="100"/>
	</bean> }
 * </pre>
 *
 * <i>NOTE</i>
 * <p>
 * The configuration line
 *
 * <pre>
 * {@code <property name="arrayPv" value="gdaSimulator:BeamDrivenCamera" />}
 * </pre>
 *
 * is not essential for this class but it in the actual GDA architecture, it instructs the
 * {@code uk.ac.gda.client.live.stream.IDatasetConnectorFactory} to instant the right class defining the pv namespace to
 * <i>gdaSimulator</i>.
 * </p>
 *
 * @see BeamSimulationStream
 *
 * @author Maurizio Nagni
 */
public class BeamSimulationCameraConnector implements IDatasetConnector, PropertyChangeListener {
	private final String datasetName;
	private final Set<IDataListener> listeners = new CopyOnWriteArraySet<>();
	private final BeamSimulationCamera beamCamera;

	private BeamSimulationStream stream;
	private Thread streamThread;

	private final Logger logger = LoggerFactory.getLogger(BeamSimulationCameraConnector.class);

	public BeamSimulationCameraConnector(String datasetName, BeamSimulationCamera beamCamera) {
		super();
		this.datasetName = datasetName;
		this.beamCamera = beamCamera;
	}

	@Override
	public void addDataListener(IDataListener l) {
		listeners.add(l);
	}

	@Override
	public String connect(long time, TimeUnit unit) throws DatasetException {
		return connect();
	}

	@Override
	public String connect() throws DatasetException {
		stream = new BeamSimulationStream(beamCamera);
		getStream().addPropertyChangeListener(this);
		streamThread = new Thread(stream);
		streamThread.start();
		return null;
	}

	@Override
	public void fireDataListeners() {
		if (getDataset() == null) {
			return;
		}
		final DataEvent dataEvent = new DataEvent(getDatasetName(), getDataset().getShape());
		listeners.forEach(listener -> listener.dataChangePerformed(dataEvent));
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		this.fireDataListeners();
	}

	@Override
	public String getDatasetName() {
		return this.datasetName;
	}

	@Override
	public void removeDataListener(IDataListener l) {
		listeners.remove(l);
	}

	@Override
	public ILazyDataset getDataset() {
		if (getStream() == null) {
			return null;
		}
		return getStream().getDataset();
	}

	@Override
	public void setDatasetName(String datasetName) {
		// Do Nothing
	}

	@Override
	public String getPath() {
		return null;
	}

	@Override
	public void setPath(String path) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean resize(int... newShape) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int[] getMaxShape() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMaxShape(int... maxShape) {
		// TODO Auto-generated method stub

	}

	@Override
	public void startUpdateChecker(int milliseconds, IDatasetChangeChecker checker) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean refreshShape() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setWritingExpected(boolean expectWrite) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isWritingExpected() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void disconnect() throws DatasetException {
		logger.info("Disconnecting {}", getStream());
		streamThread.interrupt();
	}

	private BeamSimulationStream getStream() {
		return stream;
	}
}