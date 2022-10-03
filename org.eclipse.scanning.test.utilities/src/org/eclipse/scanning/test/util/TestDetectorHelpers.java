/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package org.eclipse.scanning.test.util;

import java.io.IOException;
import org.eclipse.scanning.api.IConfigurable;
import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.device.IWritableDetector;
import org.eclipse.scanning.api.device.models.IMalcolmModel;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.example.detector.ConstantVelocityDevice;
import org.eclipse.scanning.example.detector.ConstantVelocityModel;
import org.eclipse.scanning.example.detector.DarkImageDetector;
import org.eclipse.scanning.example.detector.DarkImageModel;
import org.eclipse.scanning.example.detector.MandelbrotDetector;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.example.detector.PosDetector;
import org.eclipse.scanning.example.detector.PosDetectorModel;
import org.eclipse.scanning.example.detector.RandomLineDevice;
import org.eclipse.scanning.example.detector.RandomLineModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmDevice;
import org.eclipse.scanning.test.utilities.scan.mock.MockDetectorModel;
import org.eclipse.scanning.test.utilities.scan.mock.MockWritableDetector;
import static org.eclipse.scanning.sequencer.RunnableDeviceServiceImpl.configureAndFireAnnotations;
import static org.eclipse.scanning.sequencer.RunnableDeviceServiceImpl.applyNameIfInModel;

/**
 * Utility class for generating detectors for use in tests. In production they are
 * created by Spring config.
 * @author Callum Forrester
 *
 */
public final class TestDetectorHelpers {
	private TestDetectorHelpers() {}
	
	/**
	 * Create an empty, unconfigured mandelbrot detector.
	 * @return A new detector
	 * @throws IOException
	 * @throws ScanningException
	 */
	public static IWritableDetector<MandelbrotModel> createMandelbrotDetector()
			throws IOException, ScanningException {
		return new MandelbrotDetector();
	}

	/**
	 * Create and configure a mandelbrot detector.
	 * @param model The model providing a name and configuration.
	 * @return A new detector
	 * @throws ScanningException
	 * @throws IOException
	 */
	public static IWritableDetector<MandelbrotModel> createAndConfigureMandelbrotDetector(
			MandelbrotModel model) throws ScanningException, IOException {
		return configureAndTryToApplyNameFromModel(createMandelbrotDetector(), model);
	}

	/**
	 * Create an empty, unconfigured dummy Malcolm detector.
	 * @return A new detector
	 */
	public static IMalcolmDevice createDummyMalcolmDetector() {
		return new DummyMalcolmDevice();
	}

	/**
	 * Create and configure a dummy Malcolm detector.
	 * @param model The model providing a name and configuration.
	 * @return A new detector
	 * @throws ScanningException
	 */
	public static IMalcolmDevice createAndConfigureDummyMalcolmDetector(
			IMalcolmModel model) throws ScanningException {
		return configureAndTryToApplyNameFromModel(createDummyMalcolmDetector(), model);
	}

	/**
	 * Create an empty, unconfigured Constant Velocity detector.
	 * @return A new detector
	 * @throws ScanningException
	 */
	public static IWritableDetector<ConstantVelocityModel> createConstantVelocityDetector() 
			throws ScanningException {
		return new ConstantVelocityDevice();
	}

	/**
	 * Create and configure a Constant Velocity detector.
	 * @param model The model providing a name and configuration.
	 * @return A new detector
	 * @throws ScanningException
	 */
	public static IWritableDetector<ConstantVelocityModel> createAndConfigureConstantVelocityDetector(
			ConstantVelocityModel model) throws ScanningException {
		return configureAndTryToApplyNameFromModel(createConstantVelocityDetector(), model);
	}

	/**
	 * Create an empty, unconfigured Dark Image detector.
	 * @return A new detector
	 * @throws IOException
	 */
	public static IWritableDetector<DarkImageModel>  createDarkImageDetector() 
			throws IOException {
		return new DarkImageDetector();
	}

	/**
	 * Create and configure a Dark Image detector.
	 * @param model The model providing a name and configuration.
	 * @return A new detector
	 * @throws IOException
	 * @throws ScanningException
	 */
	public static IWritableDetector<DarkImageModel>  createAndConfigureDarkImageDetector(
			DarkImageModel model) throws IOException, ScanningException {
		return configureAndTryToApplyNameFromModel(createDarkImageDetector(), model);
	}

	/**
	 * Create an empty, unconfigured Mock detector.
	 * @return A new detector
	 */
	public static IWritableDetector<MockDetectorModel>  createMockDetector() {
		return new MockWritableDetector();
	}

	/**
	 * Create and configure a Mock detector.
	 * @param model The model providing a name and configuration.
	 * @return A new detector
	 * @throws ScanningException
	 */
	public static IWritableDetector<MockDetectorModel>  createAndConfigureMockDetector(
			MockDetectorModel model) throws ScanningException {
		return configureAndTryToApplyNameFromModel(createMockDetector(), model);
	}

	/**
	 * Create an empty, unconfigured Pos detector.
	 * @return A new detector
	 * @throws ScanningException
	 */
	public static IWritableDetector<PosDetectorModel> createPosDetector() throws ScanningException {
		return new PosDetector();
	}

	/**
	 * Create and configure a Pos detector.
	 * @param model The model providing a name and configuration.
	 * @return A new detector
	 * @throws ScanningException
	 */
	public static IWritableDetector<PosDetectorModel> createAndConfigurePosDetector(
			PosDetectorModel model) throws ScanningException {
		return configureAndTryToApplyNameFromModel(createPosDetector(), model);
	}

	/**
	 * Create an empty, unconfigured Random Line detector.
	 * @return A new detector
	 * @throws ScanningException
	 */
	public static IWritableDetector<RandomLineModel> createRandomLineDetector() throws ScanningException {
		return new RandomLineDevice();
	}

	/**
	 * Create and configure a Random Line detector.
	 * @param model The model providing a name and configuration.
	 * @return A new detector
	 * @throws ScanningException
	 */
	public static IWritableDetector<RandomLineModel> createAndConfigureRandomLineDetector(
			RandomLineModel model) throws ScanningException {
		return configureAndTryToApplyNameFromModel(createRandomLineDetector(), model);
	}

	/**
	 * Initialise a device from a compatible model. Transfer the model name
	 * to the device name if possible. Configure the device using the model
	 * and appropriate annotations. 
	 * @param <M> The model type
	 * @param <D> The device type
	 * @param device The device to initialise
	 * @param model The model providing a name and configuration.
	 * @return The configured device for chaining.
	 * @throws ScanningException
	 */
	public static <M, D extends IConfigurable<M> & INameable> D configureAndTryToApplyNameFromModel(
			D device, 
			M model) throws ScanningException {
		applyNameIfInModel(device, model);
		configureAndFireAnnotations(device, model);
		return device;
	}
}
