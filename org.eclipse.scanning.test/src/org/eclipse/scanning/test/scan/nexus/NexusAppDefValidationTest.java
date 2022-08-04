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

package org.eclipse.scanning.test.scan.nexus;

import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.SYSTEM_PROPERTY_NAME_VALIDATE_NEXUS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXsubentry;
import org.eclipse.dawnsci.nexus.NexusApplicationDefinition;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.dawnsci.nexus.scan.ServiceHolder;
import org.eclipse.dawnsci.nexus.validation.NexusValidationService;
import org.eclipse.dawnsci.nexus.validation.NexusValidationServiceImpl;
import org.eclipse.dawnsci.nexus.validation.ValidationReport;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.models.SimpleDetectorModel;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.scan.models.ScanMetadata;
import org.eclipse.scanning.api.scan.models.ScanMetadata.MetadataType;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.detector.RandomIntDetector;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NexusAppDefValidationTest extends NexusTest {

	private static final String VALID_TEMPLATE_PATH = "testfiles/test-template.yaml";
	private static final String INVALID_TEMPLATE_PATH = "testfiles/invalid-template.yaml";

	private RandomIntDetector detector;
	private IScannable<?> monitor;

	@BeforeAll
	static void setUpBeforeClass() {
		System.setProperty(SYSTEM_PROPERTY_NAME_VALIDATE_NEXUS, Boolean.toString(true));
		final NexusValidationService validationService = new NexusValidationServiceImpl();
		validationService.setValidateDiamond(false);
		new ServiceHolder().setNexusValidationService(validationService);
	}

	@AfterAll
	static void tearDownAfterClass() {
		System.clearProperty(SYSTEM_PROPERTY_NAME_VALIDATE_NEXUS);
	}

	@BeforeEach
	void setUp() throws Exception {
		detector = new RandomIntDetector();
		detector.configure(new SimpleDetectorModel("det1", 0.1));
		monitor = connector.getScannable("temp");
	}

	private IRunnableDevice<ScanModel> createScan(Consumer<ScanModel> modelUpdater) throws Exception {
		final ScanModel scanModel = createScanModel();
		modelUpdater.accept(scanModel);

		// Create a scan and run it without publishing events
		return scanService.createScanDevice(scanModel);
	}

	private ScanModel createScanModel() throws Exception {
		final CompoundModel model = new CompoundModel(new AxialStepModel("theta", 0, 90, 15));
		final IPointGenerator<CompoundModel> pointGen = pointGenService.createCompoundGenerator(model);

		final ScanModel scanModel = new ScanModel();
		scanModel.setScanPathModel(model);
		scanModel.setPointGenerator(pointGen);
		scanModel.setDetector(detector);
		scanModel.setMonitorsPerPoint(monitor);
		scanModel.setFilePath(output.getAbsolutePath());

		return scanModel;
	}

	@Test
	void testNexusValidationMainEntry() throws Exception {
		final ScanMetadata entryMetadata = new ScanMetadata(MetadataType.ENTRY);
		entryMetadata.addField(NXentry.NX_DEFINITION, NexusApplicationDefinition.NX_TOMO.toString());
		final List<ScanMetadata> scanMetadata = Arrays.asList(entryMetadata);

		final IRunnableDevice<ScanModel> scanner = createScan(scanModel -> scanModel.setScanMetadata(scanMetadata));
		scanner.run(null);
		assertThat(getLastValidationReport().isError(), is(true));
	}

	@Test
	void testNexusValidationMainSubentry() throws Exception {
		final INexusDevice<NXsubentry> subEntryDevice = new INexusDevice<NXsubentry>() {

			@Override
			public String getName() {
				return "tomo";
			}

			@Override
			public NexusObjectProvider<NXsubentry> getNexusProvider(NexusScanInfo info) throws NexusException {
				final NXsubentry subentry = NexusNodeFactory.createNXsubentry();
				subentry.setDefinitionScalar(NexusApplicationDefinition.NX_TOMO.toString());
				return new NexusObjectWrapper<>(getName(), subentry);
			}
		};

		final IRunnableDevice<ScanModel> scanner = createScan(scanModel ->
				scanModel.setAdditionalScanObjects(Arrays.asList(subEntryDevice)));
		scanner.run(null);

		assertThat(getLastValidationReport().isError(), is(true));
	}

	@Test
	void testNexusValidationSecondEntryValid() throws Exception {
		assertThat(testNexusValidatationTemplate(VALID_TEMPLATE_PATH).isOk(), is(true));
	}

	@Test
	void testNexusValidationSecondEntryInvalid() throws Exception {
		assertThat(testNexusValidatationTemplate(INVALID_TEMPLATE_PATH).isError(), is(true));
	}

	private ValidationReport testNexusValidatationTemplate(final String templatePath) throws Exception {
		final String templateAbsPath = Paths.get(templatePath).toAbsolutePath().toString();
		final Set<String> templatePaths = new HashSet<>(Arrays.asList(templateAbsPath));
		final Consumer<ScanModel> addTemplateUpdater = scanModel -> scanModel.setTemplateFilePaths(templatePaths);

		final IRunnableDevice<ScanModel> scanner = createScan(addTemplateUpdater);
		scanner.run(null);

		return getLastValidationReport();
	}

	private ValidationReport getLastValidationReport() {
		final NexusValidationServiceImpl validationService = (NexusValidationServiceImpl) ServiceHolder.getNexusValidationService();
		final ValidationReport report = validationService.getLastValidationReport();
		assertThat(report, is(notNullValue()));
		return report;
	}

}
