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

package uk.ac.diamond.daq.mapping.ui.test;

import static org.eclipse.scanning.api.script.ScriptLanguage.SPEC_PASTICHE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.scanning.api.IValidatorService;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.scan.ProcessingRequest;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.IAxialModel;
import org.eclipse.scanning.api.points.models.ScanRegion;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.scan.models.ScanMetadata;
import org.eclipse.scanning.api.scan.models.ScanMetadata.MetadataType;
import org.eclipse.scanning.api.script.ScriptRequest;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.points.PointGeneratorService;
import org.eclipse.scanning.points.validation.ValidatorService;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import gda.device.ScannableMotionUnits;
import gda.factory.Factory;
import gda.factory.Finder;
import uk.ac.diamond.daq.mapping.api.ConfigWrapper;
import uk.ac.diamond.daq.mapping.api.IMappingScanRegion;
import uk.ac.diamond.daq.mapping.api.IScanModelWrapper;
import uk.ac.diamond.daq.mapping.api.IScriptFiles;
import uk.ac.diamond.daq.mapping.api.TemplateFileWrapper;
import uk.ac.diamond.daq.mapping.impl.DetectorModelWrapper;
import uk.ac.diamond.daq.mapping.impl.MappingExperimentBean;
import uk.ac.diamond.daq.mapping.impl.MappingStageInfo;
import uk.ac.diamond.daq.mapping.impl.ScanPathModelWrapper;
import uk.ac.diamond.daq.mapping.impl.ScriptFiles;
import uk.ac.diamond.daq.mapping.impl.SimpleSampleMetadata;
import uk.ac.diamond.daq.mapping.region.RectangularMappingRegion;
import uk.ac.diamond.daq.mapping.ui.experiment.ScanRequestConverter;
import uk.ac.diamond.osgi.services.ServiceProvider;

public class ScanRequestConverterTest {

	private static final String X_AXIS_NAME = "testing_stage_x";
	private static final String Y_AXIS_NAME = "testing_stage_y";
	private static final String Z_AXIS_NAME = "testing_z_axis";
	private static final String BEAM_SIZE_NAME = "beamSize";

	//Explicitly non-overlapping in each axis; 2<x<5, 7<y<13,
	//Will catch if x-y mirroring occurs.
	private static final double X_START = 5;
	private static final double X_LENGTH = 2;
	private static final double Y_START = 7;
	private static final double Y_LENGTH = 6;
	private static final double DIFF_LIMIT = 1e-7;

	private static final String X_AXIS_UNITS = "mm";
	private static final String Y_AXIS_UNITS = "mdeg";

	private ScanRequestConverter scanRequestConverter;
	private MappingStageInfo mappingStageInfo;
	private MappingExperimentBean mappingBean;
	private MappingExperimentBean newMappingBean;
	private TwoAxisGridPointsModel scanPath;

	@BeforeClass
	public static void setUpServices() {
		ServiceProvider.setService(IPointGeneratorService.class, new PointGeneratorService());
		ServiceProvider.setService(IValidatorService.class, new ValidatorService());
	}

	@AfterClass
	public static void tearDownServices() {
		ServiceProvider.reset();
	}

	@Before
	public void setUp() throws Exception {
		// We wire things together without OSGi here
		// DO NOT COPY THIS IN NON-TEST CODE!
		mappingStageInfo = new MappingStageInfo();
		mappingStageInfo.setPlotXAxisName(X_AXIS_NAME);
		mappingStageInfo.setPlotYAxisName(Y_AXIS_NAME);
		mappingStageInfo.setBeamSize(BEAM_SIZE_NAME);


		// Prepare the Finder
		Factory testFactory = mock(Factory.class);

		ScannableMotionUnits xAxis = mock(ScannableMotionUnits.class);
		when(xAxis.getHardwareUnitString()).thenReturn(X_AXIS_UNITS);

		ScannableMotionUnits yAxis = mock(ScannableMotionUnits.class);
		when(yAxis.getHardwareUnitString()).thenReturn(Y_AXIS_UNITS);

		when(testFactory.getFindable(X_AXIS_NAME)).thenReturn(xAxis);
		when(testFactory.getFindable(Y_AXIS_NAME)).thenReturn(yAxis);

		Finder.addFactory(testFactory);

		scanRequestConverter = new ScanRequestConverter();
		scanRequestConverter.setMappingStageInfo(mappingStageInfo);

		// Set up the experiment bean with some sensible defaults
		mappingBean = new MappingExperimentBean();

		scanPath = new TwoAxisGridPointsModel();
		scanPath.setContinuous(true);
		mappingBean.getScanDefinition().getMappingScanRegion().setScanPath(scanPath);

		final RectangularMappingRegion scanRegion = new RectangularMappingRegion();
		scanRegion.setxStart(X_START);
		scanRegion.setxStop(X_START + X_LENGTH);
		scanRegion.setyStart(Y_START);
		scanRegion.setyStop(Y_START + Y_LENGTH);

		mappingBean.getScanDefinition().getMappingScanRegion().setRegion(scanRegion);

		mappingBean.setDetectorParameters(Collections.emptyList());

		mappingBean.setPerScanMonitorNames(new HashSet<>(Arrays.asList("perScan1", "perScan2")));
		mappingBean.setPerPointMonitorNames(new HashSet<>(Arrays.asList("perPoint1", "perPoint2", "perPoint3")));

		newMappingBean = new MappingExperimentBean();
	}

	@After
	public void tearDown() throws Exception {
		mappingStageInfo = null;
		scanRequestConverter = null;
		Finder.removeAllFactories();
	}

	@Test
	public void testDetectorIsIncludedCorrectly() throws Exception {
		// Arrange
		final String detName = "mandelbrot";
		final String displayName = "Mandelbrot Detector";
		final IDetectorModel detModel = new MandelbrotModel();
		detModel.setName(detName);
		mappingBean.setDetectorParameters(Arrays.asList(new DetectorModelWrapper(displayName, detModel, true)));

		// Act - convert mapping bean to scan request
		final ScanRequest scanRequest = scanRequestConverter.convertToScanRequest(mappingBean);

		// Assert
		assertEquals(detModel, scanRequest.getDetectors().get(detName));

		// Act again - convert scan request back to mapping bean
		((DetectorModelWrapper) mappingBean.getDetectorParameters().get(0)).setIncludeInScan(false);
		scanRequestConverter.mergeIntoMappingBean(scanRequest, mappingBean);

		// Assert - check the new mapping bean is the same as the original
		List<IScanModelWrapper<IDetectorModel>> newDetectorParams = mappingBean.getDetectorParameters();
		assertThat(newDetectorParams.size(), is(1));
		IScanModelWrapper<IDetectorModel> wrapper = newDetectorParams.get(0);
		assertThat(wrapper.getName(), is(equalTo(displayName)));
		assertThat(wrapper.getModel(), is(equalTo(detModel)));
		assertThat(wrapper.isIncludeInScan(), is(true));
	}

	@Test
	public void testDetectorIsExcludedCorrectly() throws Exception {
		// Arrange
		final String displayName = "Mandelbrot Detector";
		final IDetectorModel detModel = new MandelbrotModel();
		mappingBean.setDetectorParameters(Arrays.asList(new DetectorModelWrapper(displayName, detModel, false)));

		// Act - convert mapping bean to scan request
		final ScanRequest scanRequest = scanRequestConverter.convertToScanRequest(mappingBean);

		// Assert
		assertThat(scanRequest.getDetectors().entrySet(), is(empty()));

		// Act again - merge the scan request back into the same mapping bean
		scanRequestConverter.mergeIntoMappingBean(scanRequest, mappingBean);

		// Assert again - check the mapping bean is the same as the original
		List<IScanModelWrapper<IDetectorModel>> newDetectorParams = mappingBean.getDetectorParameters();
		assertThat(newDetectorParams.size(), is(1));
		IScanModelWrapper<IDetectorModel> wrapper = newDetectorParams.get(0);
		assertThat(wrapper.getName(), is(equalTo(displayName)));
		assertThat(wrapper.getModel(), is(equalTo(detModel))); // names must be same, i.e. mandlebrot
		assertThat(wrapper.isIncludeInScan(), is(false));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUnknownDetector() throws Exception {
		final String detName = "mandelbrot";
		final String displayName = "Mandelbrot Detector";
		final IDetectorModel detModel = new MandelbrotModel();
		detModel.setName(detName);
		mappingBean.setDetectorParameters(Arrays.asList(new DetectorModelWrapper(displayName, detModel, true)));

		// Act - convert mapping bean to scan request
		final ScanRequest scanRequest = scanRequestConverter.convertToScanRequest(mappingBean);

		// Assert
		assertEquals(detModel, scanRequest.getDetectors().get(detName));

		// Act again - convert scan request back to mapping bean, throws exception
		scanRequestConverter.mergeIntoMappingBean(scanRequest, newMappingBean);
	}

	@Test
	public void testMonitorsIncluded() throws Exception {
		// Act
		final ScanRequest scanRequest = scanRequestConverter.convertToScanRequest(mappingBean);

		// Assert - these are the monitors that are set as active in MockScannableConnector
		String[] expectedMonitorNamesPerPoint = new String[] { "perPoint1", "perPoint2", "perPoint3" };
		assertThat(scanRequest.getMonitorNamesPerPoint(), hasItems(expectedMonitorNamesPerPoint));
		assertThat(scanRequest.getMonitorNamesPerPoint().size(), CoreMatchers.is(expectedMonitorNamesPerPoint.length));

		String[] expectedMonitorNamesPerScan = new String[] { "perScan1", "perScan2", BEAM_SIZE_NAME };
		assertThat(scanRequest.getMonitorNamesPerScan(), hasItems(expectedMonitorNamesPerScan));
		assertThat(scanRequest.getMonitorNamesPerScan().size(), CoreMatchers.is(expectedMonitorNamesPerScan.length));
	}

	@Test
	public void testScanPathIsIncluded() throws Exception {
		// Act - convert mapping bean to scan request (with the default set-up)
		final ScanRequest scanRequest = scanRequestConverter.convertToScanRequest(mappingBean);

		// Assert
		assertEquals(scanRequest.getCompoundModel().getModels().get(0), scanPath);

		// Act again - convert scan request back to mapping bean
		scanRequestConverter.mergeIntoMappingBean(scanRequest, newMappingBean);

		// Assert again
		assertEquals(newMappingBean.getScanDefinition().getMappingScanRegion().getScanPath(), scanPath);
	}

	@Test
	public void testStageNamesAreSetCorrectly() throws Exception {
		// Initially the scan path doesn't have the correct axis names
		assertThat(scanPath.getxAxisName(), is(not(equalTo(X_AXIS_NAME))));
		assertThat(scanPath.getyAxisName(), is(not(equalTo(Y_AXIS_NAME))));

		// Act - they're set according to the MappingStageInfo when the mapping bean is
		// converted to a scan request
		ScanRequest scanRequest = scanRequestConverter.convertToScanRequest(mappingBean);

		// Assert - the axis names are now set to the names of the stage
		assertThat(scanPath.getxAxisName(), is(equalTo(X_AXIS_NAME)));
		assertThat(scanPath.getyAxisName(), is(equalTo(Y_AXIS_NAME)));

		scanRequestConverter.mergeIntoMappingBean(scanRequest, newMappingBean);
	}

	@Test
	public void testStageUnitsAreSetCorrectly() throws Exception {
		scanRequestConverter.convertToScanRequest(mappingBean);

		assertThat(scanPath.getxAxisUnits(), is(equalTo(X_AXIS_UNITS)));
		assertThat(scanPath.getyAxisUnits(), is(equalTo(Y_AXIS_UNITS)));
	}

	@Test
	public void testStageNamesChanged() throws Exception {
		// Initially the scan path doesn't have the correct axis names
		assertThat(scanPath.getxAxisName(), is(not(equalTo(X_AXIS_NAME))));
		assertThat(scanPath.getyAxisName(), is(not(equalTo(Y_AXIS_NAME))));

		// Act - they're set according to the MappingStageInfo when the mapping bean is
		// converted to a scan request
		ScanRequest scanRequest = scanRequestConverter.convertToScanRequest(mappingBean);

		// Assert - the axis names are now set to the names of the stage
		assertThat(scanPath.getxAxisName(), is(equalTo(X_AXIS_NAME)));
		assertThat(scanPath.getyAxisName(), is(equalTo(Y_AXIS_NAME)));

		// change the mapping stage axis names
		mappingStageInfo.setPlotXAxisName("new_x_axis");
		mappingStageInfo.setPlotYAxisName("new_y_axis");

		// the new mapping bean hasn't been set up with a scan path at this stage
		IMappingScanRegion newRegion = newMappingBean.getScanDefinition().getMappingScanRegion();
		assertThat(newRegion.getScanPath(), is(nullValue()));

		// merging the scan request back into a mapping bean
		scanRequestConverter.mergeIntoMappingBean(scanRequest, newMappingBean);

		// check that the new mapping bean has the correct stage names
		assertThat(newRegion.getScanPath(), is(instanceOf(TwoAxisGridPointsModel.class)));
		TwoAxisGridPointsModel newScanPath = (TwoAxisGridPointsModel) newRegion.getScanPath();
		assertThat(newScanPath.getxAxisName(), is(equalTo(X_AXIS_NAME)));
		assertThat(newScanPath.getyAxisName(), is(equalTo(Y_AXIS_NAME)));

		// check that the mapping stage info has been updated with the stage names from the scan request
		assertThat(mappingStageInfo.getPlotXAxisName(), is(equalTo(X_AXIS_NAME)));
		assertThat(mappingStageInfo.getPlotYAxisName(), is(equalTo(Y_AXIS_NAME)));
	}

	@Test
	public void testBeamSizeScannableIncluded() throws Exception {
		ScanRequest scanRequest = scanRequestConverter.convertToScanRequest(mappingBean);
		assertTrue(scanRequest.getMonitorNamesPerScan().contains(BEAM_SIZE_NAME));

		mappingStageInfo.setBeamSize(null);
		scanRequest = scanRequestConverter.convertToScanRequest(mappingBean);
		assertFalse(scanRequest.getMonitorNamesPerScan().contains(BEAM_SIZE_NAME));
	}

	@Test
	public void testScriptFilesIncludedCorrectly() throws Exception {
		// Arrange
		final String beforeScanScript = "/path/to/before.py";
		final String afterScanScript = "/path/to/after.py";

		final IScriptFiles scriptFiles = new ScriptFiles();
		mappingBean.setScriptFiles(scriptFiles);
		scriptFiles.setBeforeScanScript(beforeScanScript);
		scriptFiles.setAfterScanScript(afterScanScript);
		scriptFiles.setAlwaysRunAfterScript(true);

		// Act - covert mapping bean to scan request
		final ScanRequest scanRequest = scanRequestConverter.convertToScanRequest(mappingBean);

		// Assert
		final ScriptRequest beforeScriptReq = scanRequest.getBeforeScript();
		assertThat(beforeScriptReq, is(notNullValue()));
		assertThat(beforeScriptReq.getLanguage(), is(SPEC_PASTICHE));
		assertThat(beforeScriptReq.getFile(), is(equalTo(beforeScanScript)));
		final ScriptRequest afterScriptReq = scanRequest.getAfterScript();
		assertThat(afterScriptReq, is(notNullValue()));
		assertThat(afterScriptReq.getLanguage(), is(SPEC_PASTICHE));
		assertThat(afterScriptReq.getFile(), is(equalTo(afterScanScript)));

		assertThat(scanRequest.isAlwaysRunAfterScript(), is(true));

		// Act again - convert the scan request back to a mapping bean
		scanRequestConverter.mergeIntoMappingBean(scanRequest, newMappingBean);

		// Assert again
		final IScriptFiles newScriptFiles = newMappingBean.getScriptFiles();
		assertThat(newScriptFiles, is(notNullValue()));
		assertThat(newScriptFiles.getBeforeScanScript(), is(equalTo(beforeScanScript)));
		assertThat(newScriptFiles.getAfterScanScript(), is(equalTo(afterScanScript)));
	}

	@Test
	public void testBeamlineConfigurationIncludedCorrectly() throws Exception {
		// Arrange
		final Map<String, Object> beamlineConfiguration = new HashMap<>();
		beamlineConfiguration.put("energy", 12345.67);
		beamlineConfiguration.put("attenuator_pos", "Gap");
		beamlineConfiguration.put("kb_mirror_pos", 7.0);
		mappingBean.setBeamlineConfiguration(beamlineConfiguration);

		// Act - convert mapping bean to scan request
		final ScanRequest scanRequest = scanRequestConverter.convertToScanRequest(mappingBean);

		// Assert
		final IPosition startPos = scanRequest.getStartPosition();
		assertThat(startPos.getNames().size(), is(equalTo(beamlineConfiguration.size())));
		for (String scannableName : beamlineConfiguration.keySet()) {
			assertThat(startPos.get(scannableName), is(equalTo(beamlineConfiguration.get(scannableName))));
		}

		// Act again - convert scan request back to a mapping bean
		scanRequestConverter.mergeIntoMappingBean(scanRequest, newMappingBean);

		// Assert - the new beamline configuration should be equal to the original one
		final Map<String, Object> newBeamlineConfiguration = newMappingBean.getBeamlineConfiguration();
		assertThat(newBeamlineConfiguration, is(notNullValue()));
		assertThat(newBeamlineConfiguration, is(equalTo(beamlineConfiguration)));
	}

	@Test
	public void testRoiAxisNamesAreSet() throws Exception {

		IPointGeneratorService pointGeneratorService = new PointGeneratorService();

		// Act - convert mapping bean to scan request
		final ScanRequest scanRequest = scanRequestConverter.convertToScanRequest(mappingBean);
		final Collection<ScanRegion> regions = scanRequest.getCompoundModel().getRegions();

		// Assert - ensure only one region
		assertThat(regions.size(), is(equalTo(1)));
		for (final ScanRegion scanRegion : regions) {
			final List<String> scannables = scanRegion.getScannables();
			final RectangularROI roi = (RectangularROI) scanRegion.getRoi();
			/* Post DAQ-2739, this ensures if x-y axes of regions are flipped again it will be noticed either here or in the num of point
			 * [n.b. shortly before 2739, Regions were not passed to CompoundModels, then they were but reflected, which broke scanning]
			 */
			assertThat(scannables.get(0), is(equalTo(X_AXIS_NAME)));
			assertThat(scannables.get(1), is(equalTo(Y_AXIS_NAME)));
			assertEquals(X_START, roi.getPoint()[0], DIFF_LIMIT);
			assertEquals(Y_START, roi.getPoint()[1], DIFF_LIMIT);
			assertEquals(X_LENGTH, roi.getLengths()[0], DIFF_LIMIT);
			assertEquals(Y_LENGTH, roi.getLengths()[1], DIFF_LIMIT);
		}

		// Act again - convert scan request back to mapping bean
		scanRequestConverter.mergeIntoMappingBean(scanRequest, newMappingBean);
		assertEquals(mappingBean.getScanDefinition().getMappingScanRegion().getRegion(),
				newMappingBean.getScanDefinition().getMappingScanRegion().getRegion());

		List<IPosition> allPositions = pointGeneratorService.createCompoundGenerator(scanRequest.getCompoundModel()).createPoints();
		assertEquals(25, allPositions.size());
	}

	@Test
	public void testOuterScannableIsSet() throws Exception {
		// Arrange
		final AxialStepModel outerModel = new AxialStepModel(Z_AXIS_NAME, -3, 2, 0.5);
		final List<IScanModelWrapper<IAxialModel>> outerScannables =
				Arrays.asList(new ScanPathModelWrapper<>(Z_AXIS_NAME, outerModel, true));

		mappingBean.getScanDefinition().setOuterScannables(outerScannables);

		// Act - convert mapping bean to scan request
		final ScanRequest scanRequest = scanRequestConverter.convertToScanRequest(mappingBean);

		// Assert
		// Check there are now 2 models the outer z AxialStepModel and the inner Grid model
		assertThat(scanRequest.getCompoundModel().getModels().size(), is(equalTo(2)));
		final AxialStepModel recoveredOuterModel = (AxialStepModel) scanRequest.getCompoundModel().getModels().get(0);
		// Check the outer scannable model is first in the list
		assertThat(recoveredOuterModel, is(outerModel));
		// Check it has the correct axis name
		assertThat(recoveredOuterModel.getName(), is(Z_AXIS_NAME));

		// setup the new mapping bean with an outer scannable for the same axis, but disabled
		// and with a different model, and another enabled and for a different axis
		newMappingBean.getScanDefinition().setOuterScannables(Arrays.asList(
				new ScanPathModelWrapper<>(Z_AXIS_NAME, new AxialStepModel(Z_AXIS_NAME, 0, 5, 0.25), false),
				new ScanPathModelWrapper<>("energy", new AxialStepModel("energy", 10000, 15000, 1000), true)));
		scanRequestConverter.mergeIntoMappingBean(scanRequest, newMappingBean);

		// Assert again - check the new mapping bean is the same as the old one
		final List<IScanModelWrapper<IAxialModel>> newOuterScannables = newMappingBean.getScanDefinition().getOuterScannables();
		assertThat(newOuterScannables.size(), is(2));
		assertThat(newOuterScannables.get(0).getName(), is(equalTo(Z_AXIS_NAME)));
		assertThat(newOuterScannables.get(0).getModel(), is(equalTo(outerModel)));
		assertThat(newOuterScannables.get(0).isIncludeInScan(), is(true));
		assertThat(newOuterScannables.get(1).getName(), is(equalTo("energy")));
		assertThat(newOuterScannables.get(1).isIncludeInScan(), is(false));
	}

	@Test
	public void testOuterScannableAddedToMappingBeanIfNotThereAlready() {
		// Arrange
		final IAxialModel outerModel = new AxialStepModel(Z_AXIS_NAME, -3, 2, 0.5);
		final List<IScanModelWrapper<IAxialModel>> outerScannables =
				Arrays.asList(new ScanPathModelWrapper<>(Z_AXIS_NAME, outerModel, true));

		mappingBean.getScanDefinition().setOuterScannables(outerScannables);

		// Act - convert mapping bean to scan request
		final ScanRequest scanRequest = scanRequestConverter.convertToScanRequest(mappingBean);

		// Creates a new wrapper for the outer scannable in newMappingBean
		scanRequestConverter.mergeIntoMappingBean(scanRequest, newMappingBean);
		assertThat(newMappingBean.getScanDefinition().getOuterScannables(), contains(outerScannables.toArray()));
	}

	@Test
	public void testProcessingRequestIncludedCorrectly() throws Exception {

		ConfigWrapper wrapper = new ConfigWrapper();
		String appName = "test";
		String pathToConfig = "/path/to/config";
		wrapper.setAppName(appName);
		wrapper.setConfigObject(pathToConfig);
		wrapper.setActive(true);

		mappingBean.addProcessingRequest(wrapper);

		// Act - convert mapping bean to scan request
		final ScanRequest scanRequest = scanRequestConverter.convertToScanRequest(mappingBean);

		// Assert
		ProcessingRequest processingRequest = scanRequest.getProcessingRequest();
		testProcessingRequest(appName,pathToConfig,processingRequest);

		// Act again - merge scan request into new mapping bean
		scanRequestConverter.mergeIntoMappingBean(scanRequest, newMappingBean);

		// Assert again - check the new mapping bean is the same as the old one
		ProcessingRequest r= new ProcessingRequest();
		r.setRequest(newMappingBean.getProcessingRequest());
		testProcessingRequest(appName,pathToConfig,r);
	}

	private void testProcessingRequest(String appName, String pathToConfig, ProcessingRequest processingRequest) {
		assertTrue(processingRequest.getRequest().containsKey(appName));
		Object object = processingRequest.getRequest().get(appName);
		assertTrue(processingRequest.getRequest().get(appName) instanceof List);

		@SuppressWarnings("unchecked")
		List<String> paths = (List<String>)object;
		assertEquals(pathToConfig, paths.get(0));
	}

	@Test
	public void testSampleMetadataSet() throws Exception {
		// Arrange
		final String sampleName = "testSample";
		final String sampleDescription = "This is a description of the test sample.";
		mappingBean.getSampleMetadata().setSampleName(sampleName);
		mappingBean.getSampleMetadata().setDescription(sampleDescription);

		// Act - convert mapping bean to scan request
		ScanRequest scanRequest = scanRequestConverter.convertToScanRequest(mappingBean);

		// Assert
		List<ScanMetadata> scanMetadataList = scanRequest.getScanMetadata();
		assertEquals(1, scanMetadataList.size());
		ScanMetadata scanMetadata = scanMetadataList.get(0);
		assertEquals(MetadataType.SAMPLE, scanMetadata.getType());
		assertEquals(sampleName, scanMetadata.getFieldValue("name"));
		assertEquals(sampleDescription, scanMetadata.getFieldValue("description"));

		// Act again - merge scan request back into new mapping bean
		scanRequestConverter.mergeIntoMappingBean(scanRequest, newMappingBean);

		// Assert again - check the new mapping bean is the same as the old one
		SimpleSampleMetadata sampleMetadata = newMappingBean.getSampleMetadata();
		assertEquals(sampleName, sampleMetadata.getSampleName());
		assertEquals(sampleDescription, sampleMetadata.getDescription());
	}

	@Test
	public void testTemplateFilesAdded() throws Exception {
		final String first = "first.yaml";
		final String second = "second.yaml";
		final String third = "third.yaml";
		final String fourth = "fourth.yaml";

		final TemplateFileWrapper firstWrapper = new TemplateFileWrapper(first, true);
		final TemplateFileWrapper secondWrapper = new TemplateFileWrapper(second, true);
		final TemplateFileWrapper thirdWrapper = new TemplateFileWrapper(third, false);
		final TemplateFileWrapper fourthWrapper = new TemplateFileWrapper(fourth, true);

		// Arrange
		final TemplateFileWrapper[] templateFiles = { firstWrapper, secondWrapper, thirdWrapper };
		mappingBean.setTemplateFiles(Arrays.asList(templateFiles));

		// Act
		final ScanRequest scanRequest = scanRequestConverter.convertToScanRequest(mappingBean);

		// Assert: only active files should be in scan request
		final Set<String> scanRequestTemplateFiles = scanRequest.getTemplateFilePaths();
		assertThat(scanRequestTemplateFiles, contains(first, second));

		// Arrange again - to convert back to mapping bean
		mappingBean.setTemplateFiles(Arrays.asList(secondWrapper, fourthWrapper));

		// Act
		scanRequestConverter.mergeIntoMappingBean(scanRequest, mappingBean);

		// Assert: the merge should add any files that were not already in the mapping bean and deactivate any that were
		// not in the scan request.
		final List<TemplateFileWrapper> mappingBeanTemplateFiles = mappingBean.getTemplateFiles();
		assertEquals(3, mappingBeanTemplateFiles.size());
		assertContainsWrapper(mappingBeanTemplateFiles, first, true);
		assertContainsWrapper(mappingBeanTemplateFiles, second, true);
		assertContainsWrapper(mappingBeanTemplateFiles, fourth, false);
	}

	@Test
	public void testNullTemplateListInScanRequest() {
		final String first = "first.yaml";
		final String second = "second.yaml";

		final TemplateFileWrapper firstWrapper = new TemplateFileWrapper(first, true);
		final TemplateFileWrapper secondWrapper = new TemplateFileWrapper(second, true);

		mappingBean.setTemplateFiles(Arrays.asList(firstWrapper, secondWrapper));

		final ScanRequest scanRequest = scanRequestConverter.convertToScanRequest(mappingBean);
		scanRequest.setTemplateFilePaths(null);
		assertNull(scanRequest.getTemplateFilePaths());

		scanRequestConverter.mergeIntoMappingBean(scanRequest, mappingBean);

		// Merging a null list of file paths should deactivate (but not delete) all files in the mapping bean
		final List<TemplateFileWrapper> templateFiles = mappingBean.getTemplateFiles();
		assertEquals(2, templateFiles.size());
		assertContainsWrapper(templateFiles, first, false);
		assertContainsWrapper(templateFiles, second, false);
	}

	/**
	 * Check that a list of {@link TemplateFileWrapper} contains just one wrapper for the specified file path, and that
	 * it is in the required activation state.
	 */
	private static void assertContainsWrapper(List<TemplateFileWrapper> templateFiles, String filePath, boolean active) {
		final Set<TemplateFileWrapper> wrappers = templateFiles.stream()
				.filter(f -> f.getFilePath().equals(filePath))
				.filter(f -> f.isActive() == active)
				.collect(Collectors.toSet());
		assertEquals(1, wrappers.size());
	}

	@Test
	public void testMonitorNameSetCorrectly() throws Exception {
		// Arrange
		final Set<String> perPointMonitorNames = mappingBean.getPerPointMonitorNames();
		final Set<String> perScanMonitorNames = mappingBean.getPerScanMonitorNames();

		// Act - convert mapping bean to scan request
		ScanRequest scanRequest = scanRequestConverter.convertToScanRequest(mappingBean);

		// Assert
		assertEquals(perPointMonitorNames, scanRequest.getMonitorNamesPerPoint());

		Set<String> expectedPerScanMonitorNames = new HashSet<>(perScanMonitorNames);
		expectedPerScanMonitorNames.add(BEAM_SIZE_NAME);
		assertEquals(expectedPerScanMonitorNames, scanRequest.getMonitorNamesPerScan());
	}

}
