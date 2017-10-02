/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.device.scannable;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.FileConfiguration;
import org.springframework.beans.factory.InitializingBean;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.displayscaleprovider.DisplayScaleProvider;
import gda.device.scannable.corba.impl.ScannableAdapter;
import gda.device.scannable.corba.impl.ScannableImpl;
import gda.factory.FactoryException;
import gda.factory.corba.util.CorbaAdapterClass;
import gda.factory.corba.util.CorbaImplClass;
import gda.observable.IObserver;
import uk.ac.diamond.daq.persistence.jythonshelf.LocalParameters;

@CorbaImplClass(ScannableImpl.class)
@CorbaAdapterClass(ScannableAdapter.class)
public class RotationAxisXScannable extends ScannableBase implements InitializingBean {
	// private static final Logger logger = LoggerFactory.getLogger(RotationAxisXScannable.class);

	private FileConfiguration configuration;

	private String configurationName = "configuration";
	private String offsetPropertyName = "rotationXScannableOffset";

	private Scannable sampleStageXScannable;
	private Scannable cameraStageXScannable;
	private Scannable lensScannable;
	private DisplayScaleProvider cameraScaleProvider;
	private IObserver observer;

	private String getAllowedKey(String key) {
		return key.replace(" ", "_");
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

	@Override
	public void rawAsynchronousMoveTo(Object positionInCameraImage) throws DeviceException {
		try {
			Double[] array = ScannableUtils.objectToArray(positionInCameraImage);
			Double pos = array[0];
			double offset = getOffsetForRotationAxisX(pos);
			setOffset(offset);
			notifyIObservers(getName(), new ScannablePositionChangeEvent(pos));
		} catch (ConfigurationException e) {
			throw new DeviceException("Error saving new value", e);
		}
	}

	private void setOffset(double offset) throws DeviceException, ConfigurationException {
		String lensPos = getLensValue();
		configuration.setProperty(getAllowedKey(offsetPropertyName + lensPos), offset);
		configuration.save();
	}

	private String getLensValue() throws DeviceException {
		return (String) lensScannable.getPosition();
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		return getRotationAxisX();
	}

	@Override
	public void configure() throws FactoryException {
		super.configure();
		try {
			configuration = LocalParameters.getThreadSafeXmlConfiguration(getConfigurationName());
			if (observer == null) {
				observer = new IObserver() {

					@Override
					public void update(Object source, Object arg) {
						if (arg instanceof ScannableStatus || arg instanceof ScannablePositionChangeEvent) {
							notifyIObservers(RotationAxisXScannable.this, arg);
						}
					}
				};
				sampleStageXScannable.addIObserver(observer);
				cameraStageXScannable.addIObserver(observer);
				cameraScaleProvider.addIObserver(observer);
				lensScannable.addIObserver(observer);

			}
		} catch (Exception e) {
			throw new FactoryException("Error in configure for " + getName(), e);
		}

	}

	/**
	 * Move sample stage so that rotation axis is moved by pixelsX
	 *
	 * @param pixelsX
	 * @throws DeviceException
	 * @throws InterruptedException
	 */
	public void autoCentre(double pixelsX) throws DeviceException, InterruptedException {
		double x2 = ScannableUtils.getCurrentPositionArray(cameraStageXScannable)[0];

		double move = getOffset() - pixelsX / cameraScaleProvider.getPixelsPerMMInX() - x2;
		sampleStageXScannable.asynchronousMoveTo(move);
		sampleStageXScannable.waitWhileBusy();

	}

	private int getRotationAxisX() throws DeviceException {
		double x1 = ScannableUtils.getCurrentPositionArray(sampleStageXScannable)[0];
		double x2 = ScannableUtils.getCurrentPositionArray(cameraStageXScannable)[0];
		double offset = getOffset();
		double dist = (offset + x1 - x2) * cameraScaleProvider.getPixelsPerMMInX();
		return (int) Math.round(dist);
	}

	private double getOffset() throws DeviceException {
		String lensPos = getLensValue();
		double offset = configuration.getDouble(getAllowedKey(offsetPropertyName + lensPos), 0.0);
		return offset;
	}

	/**
	 * The value position of the rotation axis in the camera image is given by
	 *
	 * <pre>
	 * ((offset + sampleStageXInMM)* pixelsPerMMinX) - cameraStageXInMM*pixelsPerMMinX = positionInCameraImage
	 *
	 * (offset + sampleStageXInMM - cameraStageXInMM)*pixelsPerMMinX = positionInCameraImage
	 *
	 * (offset + sampleStageXInMM - cameraStageXInMM)*pixelsPerMMinX = positionInCameraImage
	 * </pre>
	 *
	 * where offset is the offset of the rotationAxis from the sampleStage 0 position + the offset of the cameraStage 0 position from the sample stage.
	 */
	private double getOffsetForRotationAxisX(double positionInCameraImage) throws DeviceException {
		double x1 = ScannableUtils.getCurrentPositionArray(sampleStageXScannable)[0];
		double x2 = ScannableUtils.getCurrentPositionArray(cameraStageXScannable)[0];
		return positionInCameraImage / cameraScaleProvider.getPixelsPerMMInX() + x2 - x1;
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		if (sampleStageXScannable == null) {
			throw new Exception("sampleStageXScannable == null");
		}
		if (cameraStageXScannable == null) {
			throw new Exception("cameraStageXScannable == null");
		}
		if (lensScannable == null) {
			throw new Exception("lenScannable == null");
		}
	}

	public String getConfigurationName() {
		return configurationName;
	}

	public void setConfigurationName(String configurationName) {
		this.configurationName = configurationName;
	}

	public String getPropertyName() {
		return offsetPropertyName;
	}

	public void setPropertyName(String propertyName) {
		this.offsetPropertyName = propertyName;
	}

	public Scannable getSampleStageXScannable() {
		return sampleStageXScannable;
	}

	public void setSampleStageXScannable(Scannable sampleStageXScannable) {
		this.sampleStageXScannable = sampleStageXScannable;
	}

	public Scannable getCameraStageXScannable() {
		return cameraStageXScannable;
	}

	public void setCameraStageXScannable(Scannable cameraStageXScannable) {
		this.cameraStageXScannable = cameraStageXScannable;
	}

	public DisplayScaleProvider getCameraScaleProvider() {
		return cameraScaleProvider;
	}

	public void setCameraScaleProvider(DisplayScaleProvider cameraScaleProvider) {
		this.cameraScaleProvider = cameraScaleProvider;
	}

	public Scannable getLensScannable() {
		return lensScannable;
	}

	public void setLensScannable(Scannable lensScannable) {
		this.lensScannable = lensScannable;
	}
}
