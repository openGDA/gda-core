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

package org.eclipse.scanning.test.scan.nexus;

import java.io.File;
import java.util.Arrays;

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableEventDevice;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.models.StaticModel;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmModel;
import org.junit.Test;

public class MalcolmAcquireTest extends AbstractMalcolmScanTest {

	@Test
	public void testMalcolmAcquire() throws Exception {
		final IRunnableDevice<ScanModel> scanner = createAcquireScan(malcolmDevice, output);
		scanner.run(null);

		checkNexusFile(scanner, false);
	}

	@Override
	protected DummyMalcolmModel createMalcolmModel() {
		final DummyMalcolmModel model = createMalcolmModelTwoDetectors();
		model.setAxesToMove(Arrays.asList("stage_x", "stage_y" ));
		model.setPositionerNames(Arrays.asList("stage_x", "j1", "j2", "j3"));
		model.setMonitorNames(Arrays.asList("i0"));

		return model;
	}

	private IRunnableDevice<ScanModel> createAcquireScan(final IRunnableDevice<? extends IDetectorModel> detector, File file) throws Exception {
		final StaticModel staticModel = new StaticModel();
		final IPointGenerator<StaticModel> pointGen = pointGenService.createGenerator(staticModel);

		// Create the model for an acquire scan
		final ScanModel scanModel = new ScanModel();
		scanModel.setDetector(detector);

		// Create a file to scan into
		scanModel.setFilePath(file.getAbsolutePath());
		scanModel.setPointGenerator(pointGen);
		scanModel.setScanPathModel(staticModel);

		// configure the malcolm device with the point generator
		malcolmDevice.setPointGenerator(pointGen);
		System.out.println("File writing to " + scanModel.getFilePath());

		// Create a scan and run it without publishing events
		final IRunnableDevice<ScanModel> scanner = scanService.createScanDevice(scanModel);
		((IRunnableEventDevice<ScanModel>) scanner).addRunListener(IRunListener.createRunWillPerformListener(
				event -> System.out.println("Running acquire scan for detector " + detector.getName())));

		return scanner;
	}


}
