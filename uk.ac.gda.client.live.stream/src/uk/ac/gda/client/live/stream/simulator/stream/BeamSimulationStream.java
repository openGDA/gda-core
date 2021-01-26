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

package uk.ac.gda.client.live.stream.simulator.stream;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.stream.IntStream;

import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.graphics.ImageData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.IScannableMotor;
import uk.ac.gda.client.composites.FinderHelper;
import uk.ac.gda.client.live.stream.simulator.connector.BeamSimulationCamera;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.images.ClientImages;

/**
 * A class able to stream point controlled by two {@link IScannableMotor}s. The constructor uses a
 * {@link BeamSimulationCamera} to define both the camera size and the number of pixels spanned by a single motor step.
 *
 * @author Maurizio Nagni
 */
public class BeamSimulationStream implements Runnable {
	private final BeamSimulationCamera beamCamera;

	private IScannableMotor driverX;
	private IScannableMotor driverY;
	private IDataset dataset;
	private long[] dataArray;

	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	private final Logger logger = LoggerFactory.getLogger(BeamSimulationStream.class);

	/**
	 * @param beamCamera
	 */
	public BeamSimulationStream(BeamSimulationCamera beamCamera) {
		super();
		this.beamCamera = beamCamera;
	}

	public IDataset getDataset() {
		return dataset;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(listener);
	}

	@Override
	public void run() {
		initalise();
		Arrays.fill(dataArray, 0);
		dataset = DatasetFactory.zeros(getShape());
		dataUpdate();
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep(100);
				refreshImage();
			} catch (InterruptedException e) {
				logger.info("Thread {} interrupted.", Thread.currentThread().getName());
				Thread.currentThread().interrupt();
			}
		}
	}

	private void setDriverX(IScannableMotor driverX) {
		this.driverX = driverX;
	}

	private void setDriverY(IScannableMotor driverY) {
		this.driverY = driverY;
	}

	private void initalise() {
		dataArray = new long[beamCamera.getCameraWidth() * beamCamera.getCameraHeight()];
		FinderHelper.getIScannableMotor(beamCamera.getDriverX()).ifPresent(this::setDriverX);
		FinderHelper.getIScannableMotor(beamCamera.getDriverY()).ifPresent(this::setDriverY);
	}

	private void refreshImage() {
		try {
			long x = Math.round(getBeamCamera().getScaleX() * Double.class.cast(driverX.getPosition()));
			long y = Math.round(getBeamCamera().getScaleY() * Double.class.cast(driverY.getPosition()));
			// Sets all array pixels as black
			Arrays.fill(dataArray, 0);
			//backgroundImage(dataArray, ClientImages.BROKEN);
			dataArray[0] = 1; // this hack prevents the stream to be GREEN if all the pixels have the same value
			if (x >= 0 && y >= 0) {
				updateDataArray(x, y);
			}
			dataUpdate();
		} catch (DeviceException e) {
			logger.error("TODO put description of error here", e);
		}
	}

	/**
	 * Provides a background for the simulation (to be completed)
	 * @param dataArray
	 * @param background
	 */
	private void backgroundImage(long[] dataArray, ClientImages background) {
		final ImageData id = ClientSWTElements.getImage(background).getImageData();
		IntStream.range(0, dataArray.length).forEach(i -> {
			dataArray[i] = dataArray[i] + (id.data[i+1] +  id.data[i+2] + id.data[i+3])/3;
		} );
	}

	private void updateDataArray(long x, long y) {
		// The half width of the beam size. It could be parametrised in future.
		int beamHalfWidth = 3;
		// The beam brightness. It could be parametrised in future.
		int elementValue = 110;
		int reducedWidth = (beamHalfWidth - 1);
		// loops around the pixel it thickness
		for (int indexY = -1 * reducedWidth; indexY < beamHalfWidth; indexY++) {
			for (int indexX = -1 * reducedWidth; indexX < beamHalfWidth; indexX++) {
				long newPosition = ((y + indexY) * beamCamera.getCameraWidth()) + x + indexX;
				// the pixel is out of the stream frame size
				if (newPosition < 0 || newPosition >= dataArray.length) {
					continue;
				}
				int lowerDisplayBoundary = beamHalfWidth;
				int upperDisplayBoundary = beamCamera.getCameraWidth() - 1 - beamHalfWidth;
				// Is this the beam pixel into the array boundaries?
				if ((x >= lowerDisplayBoundary && x <= (beamCamera.getCameraWidth() - 1 - lowerDisplayBoundary))
						|| (x < lowerDisplayBoundary
								&& Math.floorMod(newPosition, beamCamera.getCameraWidth() - 1L) < lowerDisplayBoundary)
						|| (x > upperDisplayBoundary && Math.floorMod(newPosition,
								beamCamera.getCameraWidth() - 1L) > upperDisplayBoundary)) {
					dataArray[(int) newPosition] = elementValue;
				}
			}
		}
	}

	private BeamSimulationCamera getBeamCamera() {
		return beamCamera;
	}

	private void dataUpdate() {
		// Build the new dataset
		dataset = DatasetFactory.createFromObject(dataArray, getShape());
		this.pcs.firePropertyChange("dataset", null, dataset);
	}

	private int[] getShape() {
		return new int[] { beamCamera.getCameraHeight(), beamCamera.getCameraWidth() };
	}

	@Override
	public String toString() {
		return "BeamDrivenStream [beamCamera=" + beamCamera + "]";
	}
}
