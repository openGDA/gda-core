/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.detectors.addetector;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.scanning.api.annotation.scan.LevelEnd;
import org.eclipse.scanning.api.annotation.scan.LevelStart;
import org.eclipse.scanning.api.annotation.scan.PointEnd;
import org.eclipse.scanning.api.annotation.scan.PointStart;
import org.eclipse.scanning.api.annotation.scan.PostConfigure;
import org.eclipse.scanning.api.annotation.scan.PreConfigure;
import org.eclipse.scanning.api.annotation.scan.ScanAbort;
import org.eclipse.scanning.api.annotation.scan.ScanEnd;
import org.eclipse.scanning.api.annotation.scan.ScanFault;
import org.eclipse.scanning.api.annotation.scan.ScanFinally;
import org.eclipse.scanning.api.annotation.scan.ScanPause;
import org.eclipse.scanning.api.annotation.scan.ScanResume;
import org.eclipse.scanning.api.annotation.scan.ScanStart;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.IWritableDetector;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.LevelInformation;
import org.eclipse.scanning.api.scan.ScanInformation;
import org.eclipse.scanning.api.scan.ScanningException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.detectors.addetector.api.AreaDetectorRunnableDeviceModel;

public abstract class AbstractAreaDetectorRunnableDevice extends AbstractRunnableDevice<AreaDetectorRunnableDeviceModel>
		implements IWritableDetector<AreaDetectorRunnableDeviceModel>, INexusDevice<NXdetector> {

	private static final Logger logger = LoggerFactory.getLogger(AbstractAreaDetectorRunnableDevice.class);

	protected AbstractAreaDetectorRunnableDevice(IRunnableDeviceService dservice) {
		super(dservice);
	}

	// Annotated methods

	/**
	 * NOTE: Arguments are filled with annotations using introspection. OSGi services may be requested (null if not found)
	 * NOTE: More than one method may be annotated.
	 * @param model - TODO: This should probably have a type which is more specific than Object. Currently in a scan
	 *        it gets populated with a ScanInformation object, but RunnableDeviceServiceImpl.createRunnableDevice(),
	 *        AcquireRequestHandler.configureDetector() and ScanProcess.configureDetectors() all explicitly invoke
	 *        it with a model, hence the param name here.
	 */
	@SuppressWarnings("unused")
	@PreConfigure
	public void preConfigure(Object model) throws ScanningException {
		logger.trace("preConfigure({})", model);
	}

	/**
	 * NOTE: Arguments are filled with annotations using introspection. OSGi services may be requested (null if not found)
	 * NOTE: More than one method may be annotated.
	 * @param model - TODO: This should probably have a type which is more specific than Object. Currently in a scan
	 *        it gets populated with a ScanInformation object, but RunnableDeviceServiceImpl.createRunnableDevice(),
	 *        AcquireRequestHandler.configureDetector() and ScanProcess.configureDetectors() all explicitly invoke
	 *        it with a model, hence the param name here.
	 */
	@SuppressWarnings("unused")
	@PostConfigure
	public void postConfigure(Object model) throws ScanningException {
		logger.trace("postConfigure({})", model);
	}

	/**
	 * NOTE: Arguments are filled with annotations using introspection. OSGi services may be requested (null if not found)
	 * NOTE: More than one method may be annotated.
	 * @param info
	 */
	@SuppressWarnings("unused")
	@LevelStart
	public void levelStart(LevelInformation info)  throws ScanningException { // Other arguments are allowed
		logger.trace("levelStart({})", info);
	}

	/**
	 * NOTE: Arguments are filled with annotations using introspection. OSGi services may be requested (null if not found)
	 * NOTE: More than one method may be annotated.
	 * @param info
	 */
	@SuppressWarnings("unused")
	@LevelEnd
	public void levelEnd(LevelInformation info) throws ScanningException { // Other arguments are allowed
		logger.trace("levelEnd({})", info);
	}

	/**
	 * NOTE: Arguments are filled with annotations using introspection. OSGi services may be requested (null if not found)
	 * NOTE: More than one method may be annotated.
	 * @param point
	 */
	@SuppressWarnings("unused")
	@PointStart
	public void pointStart(IPosition point) throws ScanningException {
		logger.trace("pointStart({}) stepIndex={}", point, point.getStepIndex());
	}

	/**
	 * NOTE: Arguments are filled with annotations using introspection. OSGi services may be requested (null if not found)
	 * NOTE: More than one method may be annotated.
	 * @param point
	 */
	@SuppressWarnings("unused")
	@PointEnd
	public void pointEnd(IPosition point) throws ScanningException {
		logger.trace("pointEnd({}) stepIndex={}", point, point.getStepIndex());
	}

	/**
	 * NOTE: Arguments are filled with annotations using introspection. OSGi services may be requested (null if not found)
	 * NOTE: More than one method may be annotated.
	 * @param info
	 */
	@SuppressWarnings("unused")
	@ScanStart
	public void scanStart(ScanInformation info) throws ScanningException {
		logger.trace("scanStart({}) filePath={}", info, info.getFilePath());
	}

	/**
	 * NOTE: Arguments are filled with annotations using introspection. OSGi services may be requested (null if not found)
	 * NOTE: More than one method may be annotated.
	 * @param info
	 */
	@SuppressWarnings("unused")
	@ScanEnd
	public void scanEnd(ScanInformation info) throws ScanningException {
		logger.trace("scanEnd({}) filePath={}", info, info.getFilePath());
	}

	/**
	 * NOTE: Arguments are filled with annotations using introspection. OSGi services may be requested (null if not found)
	 * NOTE: More than one method may be annotated.
	 * @param info
	 */
	@SuppressWarnings("unused")
	@ScanAbort
	public void scanAbort(ScanInformation info) throws ScanningException {
		logger.trace("scanAbort({})", info);
	}

	/**
	 * NOTE: Arguments are filled with annotations using introspection. OSGi services may be requested (null if not found)
	 * NOTE: More than one method may be annotated.
	 * @param info
	 */
	@SuppressWarnings("unused")
	@ScanFault
	public void scanFault(ScanInformation info) throws ScanningException {
		logger.trace("scanFault({})", info);
	}

	/**
	 * NOTE: Arguments are filled with annotations using introspection. OSGi services may be requested (null if not found)
	 * NOTE: More than one method may be annotated.
	 * @param info
	 */
	@SuppressWarnings("unused")
	@ScanFinally
	public void scanFinally(ScanInformation info) throws ScanningException {
		logger.trace("scanFinally({})", info);
	}

	/**
	 * NOTE: Arguments are filled with annotations using introspection. OSGi services may be requested (null if not found)
	 * NOTE: More than one method may be annotated.
	 */
	@SuppressWarnings("unused")
	@ScanPause
	public void scanPaused() throws ScanningException {
		logger.trace("scanPaused() @ScanPause");
	}

	/**
	 * NOTE: Arguments are filled with annotations using introspection. OSGi services may be requested (null if not found)
	 * NOTE: More than one method may be annotated.
	 */
	@SuppressWarnings("unused")
	@ScanResume
	public void scanResumed() throws ScanningException {
		logger.trace("scanResumed() @ScanResume");
	}

}
