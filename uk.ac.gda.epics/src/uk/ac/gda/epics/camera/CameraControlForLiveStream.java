/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.gda.epics.camera;

import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.ImageMode;
import gda.device.detector.areadetector.v17.NDROI;

/**
 * Customisation of {@link EpicsCameraControl} to override specific methods in order in ensure
 * correct detector settings are set before acquiring for the purposes of a live stream.
 */
public class CameraControlForLiveStream extends EpicsCameraControl {

	private ADBase adBase;

	public CameraControlForLiveStream(ADBase adBase, NDROI ndRoi) {
		super(adBase, ndRoi);
		this.adBase = adBase;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Set the {@link ImageMode} to {@code CONTINUOUS} for live stream.
	 */
	@Override
	public void startAcquiring() throws DeviceException {
		try {
			adBase.setImageMode(ImageMode.CONTINUOUS);
			super.startAcquiring();
		} catch (Exception e) {
			throw new DeviceException("Error starting data acquisition", e);
		}

	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Set the acquire period to the same as the acquire time (exposure time).
	 * Of course each detector will have a different physical 'readout' time so the period
	 * will always be longer but it should be up to the specific AD driver to
	 * manage this itself and adjust the period accordingly.
	 */
	@Override
	public void setAcquireTime(double acquiretime) throws DeviceException {
		try {
			super.setAcquireTime(acquiretime);
			adBase.setAcquirePeriod(acquiretime);
		} catch (Exception e) {
			throw new DeviceException("Error setting camera acquire time", e);
		}

	}

}
