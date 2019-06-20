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

package org.eclipse.scanning.test.scan.nexus;

import static org.eclipse.dawnsci.nexus.test.util.NexusTestUtils.getNode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;

import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.junit.Before;
import org.junit.Test;

public class TemplateScanTest extends NexusTest {

	private static final String TEMPLATE_FILE_PATH = "testfiles/test-template.yaml";

	private IRunnableDevice<?> detector;

	@Before
	public void before() throws Exception {
		MandelbrotModel model = createMandelbrotModel();
		detector = runnableDeviceService.createRunnableDevice(model);
		assertThat(detector, is(notNullValue()));
	}

	@Test
	public void testTemplateScan() throws Exception {
		final IRunnableDevice<ScanModel> scanner = createAndRunTemplateScan(TEMPLATE_FILE_PATH);
		checkNexusFile(scanner);
	}

	@Test(expected = ScanningException.class)
	public void testTemplateScanNonExistantFile() throws Exception {
		createAndRunTemplateScan("testfiles/nonExist.yaml");
	}

	private IRunnableDevice<ScanModel> createAndRunTemplateScan(String templateFile) throws Exception {
		final ScanModel scanModel = createGridScanModel(detector, output, false, 8, 5);
		scanModel.setTemplateFilePath(new HashSet<>(Arrays.asList(templateFile)));
		final IRunnableDevice<ScanModel> scanner = runnableDeviceService.createRunnableDevice(scanModel, null);
		scanner.run();
		return scanner;
	}

	private void checkNexusFile(IRunnableDevice<ScanModel> scanner) throws Exception {
		final NXroot root = checkNexusFile(scanner, false, 8, 5);
		final NXentry entry = root.getEntry("scan");
		assertThat(entry, is(notNullValue()));

		assertThat(entry.getDataNode(NXentry.NX_START_TIME), is(sameInstance(getNode(root, "/entry/start_time"))));
		assertThat(entry.getDataNode(NXentry.NX_END_TIME), is(sameInstance(getNode(root, "/entry/end_time"))));
		assertThat(entry.getDefinitionScalar(), is(equalTo("NXscan")));
		assertThat(entry.getProgram_nameScalar(), is(equalTo("gda")));
		assertThat(entry.getProgram_nameAttributeVersion(), is(equalTo("9.13")));
		assertThat(entry.getProgram_nameAttributeConfiguration(), is(equalTo("dummy")));

		final NXinstrument instrument = entry.getInstrument();
		assertThat(instrument, is(notNullValue()));
		assertThat(instrument.getDetector(), is(sameInstance(getNode(root, "/entry/instrument/mandelbrot"))));
		assertThat(instrument.getPositioner("stagex"), is(sameInstance(getNode(root, "/entry/instrument/xNex"))));
		assertThat(instrument.getPositioner("stagey"), is(sameInstance(getNode(root, "/entry/instrument/yNex"))));

		assertThat(entry.getSample(), is(notNullValue()));

		final NXdata data = entry.getData();
		assertThat(data, is(notNullValue()));
		assertThat(data.getDataNode(NXdata.NX_DATA), is(sameInstance(getNode(root, "/entry/mandelbrot/data"))));
	}

}
