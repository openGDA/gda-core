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

import static org.eclipse.dawnsci.nexus.test.utilities.NexusTestUtils.getNode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NXsample;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.models.SimpleDetectorModel;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.detector.RandomIntDetector;
import org.eclipse.scanning.test.ServiceTestHelper;
import org.junit.Before;
import org.junit.Test;

public class TemplateScanTest extends NexusTest {

	private static final String TEMPLATE_FILE_PATH = "testfiles/test-template.yaml";

	private RandomIntDetector detector;
	private IScannable<?> monitor;

	@Before
	public void before() throws Exception {
		detector = new RandomIntDetector();
		detector.configure(new SimpleDetectorModel("det1", 0.1));
		monitor = connector.getScannable("temp");
	}

	@Test
	public void testTemplateScan() throws Exception {
		final String templateFileAbsolutePath =
				Paths.get(TEMPLATE_FILE_PATH).toAbsolutePath().toString();
		final IRunnableDevice<ScanModel> scanner = createAndRunTemplateScan(templateFileAbsolutePath);
		checkNexusFile(scanner);
	}

	@Test(expected = ScanningException.class)
	public void testTemplateScanNonExistantFile() throws Exception {
		createAndRunTemplateScan("testfiles/nonExist.yaml");
	}

	@Test
	public void testTemplateRelativeFilePath() throws Exception {
		// create a temporary file path within the persistence dir (/tmp/var here, gda.var in GDA)
		final String persistenceDir = ServiceTestHelper.getFilePathService().getPersistenceDir();
		final Path templateFilePath = Files.createTempFile(Paths.get(persistenceDir), "test-template", ".yaml");
		templateFilePath.toFile().delete();

		// copy the template file to that path
		final Path existingTemplateFilePath = Paths.get(TEMPLATE_FILE_PATH);
		assertThat(existingTemplateFilePath.toFile().exists(), is(true));
		Files.copy(existingTemplateFilePath, templateFilePath);
		assertThat(templateFilePath.toFile().exists(), is(true));
		templateFilePath.toFile().deleteOnExit();

		// run the template with the
		final String templateFileName = templateFilePath.getFileName().toString();
		createAndRunTemplateScan(templateFileName);
	}

	private IRunnableDevice<ScanModel> createAndRunTemplateScan(String templateFile) throws Exception {
		final CompoundModel model = new CompoundModel(new AxialStepModel("theta", 0, 90, 15));
		final IPointGenerator<CompoundModel> pointGen = pointGenService.createCompoundGenerator(model);

		final ScanModel scanModel = new ScanModel();
		scanModel.setScanPathModel(model);
		scanModel.setPointGenerator(pointGen);
		scanModel.setDetector(detector);
		scanModel.setMonitorsPerPoint(monitor);
		scanModel.setFilePath(output.getAbsolutePath());
		scanModel.setTemplateFilePaths(new HashSet<>(Arrays.asList(templateFile)));

		final IRunnableDevice<ScanModel> scanner = scanService.createScanDevice(scanModel);
		scanner.run();
		return scanner;
	}

	private void checkNexusFile(IRunnableDevice<ScanModel> scanner) throws Exception {
		final NXroot root = getNexusRoot(scanner);
		final NXentry scanEntry = root.getEntry("scan");
		assertThat(scanEntry, is(notNullValue()));

		assertThat(scanEntry.getDataNode(NXentry.NX_START_TIME), is(sameInstance(getNode(root, "/entry/start_time"))));
		assertThat(scanEntry.getDataNode(NXentry.NX_END_TIME), is(sameInstance(getNode(root, "/entry/end_time"))));
		assertThat(scanEntry.getDefinitionScalar(), is(equalTo("NXscan")));
		assertThat(scanEntry.getProgram_nameScalar(), is(equalTo("gda")));
		assertThat(scanEntry.getProgram_nameAttributeVersion(), is(equalTo("9.13")));
		assertThat(scanEntry.getProgram_nameAttributeConfiguration(), is(equalTo("dummy")));

		final NXinstrument instrument = scanEntry.getInstrument();
		assertThat(instrument, is(notNullValue()));
		assertThat(instrument.getDetector(), is(sameInstance(getNode(root, "/entry/instrument/det1"))));
		assertThat(instrument.getPositioner("theta"), is(sameInstance(getNode(root, "/entry/instrument/theta"))));
		final NXsample sample = scanEntry.getSample();
		assertThat(sample, is(notNullValue()));
		assertThat(sample.getDataNode("rotation_angle"), is(sameInstance(getNode(root, "/entry/instrument/theta/value"))));

		final NXdata data = scanEntry.getData();
		assertThat(data, is(notNullValue()));
		assertThat(data.getDataNode(NXdata.NX_DATA), is(sameInstance(getNode(root, "/entry/instrument/det1/data"))));
		assertThat(data.getDataNode("rotation_angle"), is(sameInstance(getNode(root, "/entry/instrument/theta/value"))));
	}

}
