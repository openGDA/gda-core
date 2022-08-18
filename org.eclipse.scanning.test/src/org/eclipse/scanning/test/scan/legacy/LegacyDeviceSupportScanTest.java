/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.test.scan.legacy;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.File;

import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableEventDevice;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.scan.IScanService;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.test.ServiceTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("DAQ-2088 Fails due to: 'array lengths differed, expected.length=14 actual.length=2'")
public class LegacyDeviceSupportScanTest {

	private IScanService scanService;
	private IPointGeneratorService pointGeneratorService;
	private INexusFileFactory fileFactory;

	@BeforeEach
	public void before() {
		ServiceTestHelper.setupServices();
		fileFactory = ServiceTestHelper.getNexusFileFactory();
		scanService = ServiceTestHelper.getScanService();
		pointGeneratorService = ServiceTestHelper.getPointGeneratorService();
	}

	@Test
	public void testLegacyDeviceSupportScan() throws Exception {
		int[] shape = new int[] { 8, 5 };
		IRunnableDevice<ScanModel> scanner = createStepScan(shape);
		scanner.run(null);
		checkNexusFile(scanner, shape);
	}

	private void checkNexusFile(final IRunnableDevice<ScanModel> scanner, int... sizes) throws Exception {
		final ScanModel scanModel = ((AbstractRunnableDevice<ScanModel>) scanner).getModel();
		assertEquals(DeviceState.ARMED, scanner.getDeviceState());

		String filePath = ((AbstractRunnableDevice<ScanModel>) scanner).getModel().getFilePath();
		try (NexusFile nf = fileFactory.newNexusFile(filePath)) {
			nf.openToRead();

			TreeFile nexusTree = NexusUtils.loadNexusTree(nf);
			NXroot rootNode = (NXroot) nexusTree.getGroupNode();
			NXentry entry = rootNode.getEntry();
			NXinstrument instrument = entry.getInstrument();

			// check the expected metadata scannables have been included in the scan
			// global metadata scannables: a, b, c, requires d, e, f
			// required by nexusScannable1: x, requires y, z
			// required by nexusScannable2: p, requires q, r
			String[] expectedPositionerNames = new String[] {
					"a", "b", "c", "d", "e", "f",
					"neXusScannable1", "neXusScannable2",
					"p", "q", "r", "x", "y", "z"
			};
			String[] actualPositionerNames = instrument.getAllPositioner().keySet().stream().
					sorted().toArray(String[]::new);
			assertArrayEquals(expectedPositionerNames, actualPositionerNames);
		}
	}

	private IRunnableDevice<ScanModel> createStepScan(int... size) throws Exception {

		CompoundModel cModel = new CompoundModel();
		for (int dim = 0; dim < size.length; dim++) {
			cModel.addModel(new AxialStepModel("neXusScannable"+(dim+1), 10,20,
					size[dim] > 1 ? 9.9d/(size[dim]-1) : 30)); // Either N many points or 1 point at 10
		}

		IPointGenerator<CompoundModel> gen = pointGeneratorService.createCompoundGenerator(cModel);

		// Create the model for a scan.
		final ScanModel  smodel = new ScanModel();
		smodel.setPointGenerator(gen);
//		if (monitor!=null) smodel.setMonitors(monitor); // TODO remove
//		if (metadataScannable != null) smodel.setMetadataScannables(metadataScannable);

		// Create a file to scan into.
		File output = File.createTempFile("test_simple_nexus", ".nxs");
		output.deleteOnExit();
		smodel.setFilePath(output.getAbsolutePath());
		System.out.println("File writing to " + smodel.getFilePath());

		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = scanService.createScanDevice(smodel);

		final IPointGenerator<?> fgen = gen;
		((IRunnableEventDevice<ScanModel>)scanner).addRunListener(IRunListener.createRunWillPerformListener(
				event -> System.out.println("Running acquisition scan of size "+fgen.size())));

		return scanner;
	}
}
