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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.api.device.models.ClusterProcessingModel;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.ScanRegion;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.api.scan.models.ScanMetadata;
import org.eclipse.scanning.api.scan.models.ScanMetadata.MetadataType;
import org.eclipse.scanning.api.script.ScriptRequest;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.mapping.api.IClusterProcessingModelWrapper;
import uk.ac.diamond.daq.mapping.api.IDetectorModelWrapper;
import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;
import uk.ac.diamond.daq.mapping.api.IScanPathModelWrapper;
import uk.ac.diamond.daq.mapping.api.IScriptFiles;
import uk.ac.diamond.daq.mapping.impl.ClusterProcessingModelWrapper;
import uk.ac.diamond.daq.mapping.impl.DetectorModelWrapper;
import uk.ac.diamond.daq.mapping.impl.MappingExperimentBean;
import uk.ac.diamond.daq.mapping.impl.MappingStageInfo;
import uk.ac.diamond.daq.mapping.impl.ScanPathModelWrapper;
import uk.ac.diamond.daq.mapping.impl.ScriptFiles;
import uk.ac.diamond.daq.mapping.impl.SimpleSampleMetadata;
import uk.ac.diamond.daq.mapping.region.RectangularMappingRegion;
import uk.ac.diamond.daq.mapping.ui.experiment.ScanRequestConverter;

public class ScanRequestConverterTest {

	private static final String X_AXIS_NAME = "testing_stage_x";
	private static final String Y_AXIS_NAME = "testing_stage_y";
	private static final String Z_AXIS_NAME = "testing_z_axis";

	private ScanRequestConverter scanRequestConverter;
	private MappingStageInfo mappingStageInfo;
	private MappingExperimentBean mappingBean;
	private MappingExperimentBean newMappingBean;
	private GridModel scanPath;

	@Before
	public void setUp() throws Exception {
		mappingStageInfo = new MappingStageInfo();
		mappingStageInfo.setActiveFastScanAxis(X_AXIS_NAME);
		mappingStageInfo.setActiveSlowScanAxis(Y_AXIS_NAME);

		scanRequestConverter = new ScanRequestConverter();
		scanRequestConverter.setMappingStageInfo(mappingStageInfo);

		// Set up the experiment bean with some sensible defaults
		mappingBean = new MappingExperimentBean();

		scanPath = new GridModel();
		mappingBean.getScanDefinition().getMappingScanRegion().setScanPath(scanPath);

		final IMappingScanRegionShape scanRegion = new RectangularMappingRegion();
		mappingBean.getScanDefinition().getMappingScanRegion().setRegion(scanRegion);

		mappingBean.setDetectorParameters(Collections.emptyList());

		newMappingBean = new MappingExperimentBean();
	}

	@After
	public void tearDown() throws Exception {
		mappingStageInfo = null;
		scanRequestConverter = null;
	}

	@Test
	public void testDetectorIsIncludedCorrectly() {
		// Arrange
		final String detName = "mandelbrot";
		final String displayName = "Mandelbrot Detector";
		final IDetectorModel detModel = new MandelbrotModel();
		detModel.setName(detName);
		mappingBean.setDetectorParameters(Arrays.asList(new DetectorModelWrapper(displayName, detModel, true)));

		// Act - convert mapping bean to scan request
		final ScanRequest<IROI> scanRequest = scanRequestConverter.convertToScanRequest(mappingBean);

		// Assert
		assertEquals(detModel, scanRequest.getDetectors().get(detName));

		// Act again - convert scan request back to mapping bean
		((DetectorModelWrapper) mappingBean.getDetectorParameters().get(0)).setIncludeInScan(false);
		scanRequestConverter.mergeIntoMappingBean(scanRequest, mappingBean);

		// Assert - check the new mapping bean is the same as the original
		List<IDetectorModelWrapper> newDetectorParams = mappingBean.getDetectorParameters();
		assertThat(newDetectorParams.size(), is(1));
		IDetectorModelWrapper wrapper = newDetectorParams.get(0);
		assertThat(wrapper.getName(), is(equalTo(displayName)));
		assertThat(wrapper.getModel(), is(equalTo(detModel)));
		assertThat(wrapper.isIncludeInScan(), is(true));
	}

	@Test
	public void testDetectorIsExcludedCorrectly() {
		// Arrange
		final String displayName = "Mandelbrot Detector";
		final IDetectorModel detModel = new MandelbrotModel();
		mappingBean.setDetectorParameters(Arrays.asList(new DetectorModelWrapper(displayName, detModel, false)));

		// Act - convert mapping bean to scan request
		final ScanRequest<IROI> scanRequest = scanRequestConverter.convertToScanRequest(mappingBean);

		// Assert
		assertThat(scanRequest.getDetectors(), is(nullValue()));

		// Act again - merge the scan request back into the same mapping bean
		scanRequestConverter.mergeIntoMappingBean(scanRequest, mappingBean);

		// Assert again - check the mapping bean is the same as the original
		List<IDetectorModelWrapper> newDetectorParams = mappingBean.getDetectorParameters();
		assertThat(newDetectorParams.size(), is(1));
		IDetectorModelWrapper wrapper = newDetectorParams.get(0);
		assertThat(wrapper.getName(), is(equalTo(displayName)));
		assertThat(wrapper.getModel(), is(equalTo(detModel))); // names must be same, i.e. mandlebrot
		assertThat(wrapper.isIncludeInScan(), is(false));
	}

	@Test
	public void testScanPathIsIncluded() {
		// Act - convert mapping bean to scan request (with the default set-up)
		final ScanRequest<IROI> scanRequest = scanRequestConverter.convertToScanRequest(mappingBean);

		// Assert
		assertEquals(scanRequest.getCompoundModel().getModels().get(0), scanPath);

		// Act again - convert scan request back to mapping bean
		scanRequestConverter.mergeIntoMappingBean(scanRequest, newMappingBean);

		// Assert again
		assertEquals(newMappingBean.getScanDefinition().getMappingScanRegion().getScanPath(), scanPath);
	}

	@Test
	public void testStageNamesAreSetCorrectly() {
		// Initially the scan path doesn't have the correct axis names
		assertThat(scanPath.getFastAxisName(), is(not(equalTo(X_AXIS_NAME))));
		assertThat(scanPath.getSlowAxisName(), is(not(equalTo(Y_AXIS_NAME))));

		// Act - they're set according to the MappingStageInfo when the mapping bean is
		// converted to a scan request
		ScanRequest<IROI> scanRequest = scanRequestConverter.convertToScanRequest(mappingBean);

		// Assert - the axis names are now set to the names of the stage
		assertThat(scanPath.getFastAxisName(), is(equalTo(X_AXIS_NAME)));
		assertThat(scanPath.getSlowAxisName(), is(equalTo(Y_AXIS_NAME)));

		scanRequestConverter.mergeIntoMappingBean(scanRequest, newMappingBean);
	}

	@Test(expected = RuntimeException.class)
	public void testStageNamesChanged() {
		// Initially the scan path doesn't have the correct axis names
		assertThat(scanPath.getFastAxisName(), is(not(equalTo(X_AXIS_NAME))));
		assertThat(scanPath.getSlowAxisName(), is(not(equalTo(Y_AXIS_NAME))));

		// Act - they're set according to the MappingStageInfo when the mapping bean is
		// converted to a scan request
		ScanRequest<IROI> scanRequest = scanRequestConverter.convertToScanRequest(mappingBean);

		// Assert - the axis names are now set to the names of the stage
		assertThat(scanPath.getFastAxisName(), is(equalTo(X_AXIS_NAME)));
		assertThat(scanPath.getSlowAxisName(), is(equalTo(Y_AXIS_NAME)));

		// change the mapping stage axis names
		mappingStageInfo.setActiveFastScanAxis("new_x_axis");
		mappingStageInfo.setActiveSlowScanAxis("new_y_axis");

		// merging the scan request back into a mapping bean should fail
		scanRequestConverter.mergeIntoMappingBean(scanRequest, newMappingBean);
	}

	@Test
	public void testScriptFilesIncludedCorrectly() {
		// Arrange
		final String beforeScanScript = "/path/to/before.py";
		final String afterScanScript = "/path/to/after.py";

		final IScriptFiles scriptFiles = new ScriptFiles();
		mappingBean.setScriptFiles(scriptFiles);
		scriptFiles.setBeforeScanScript(beforeScanScript);
		scriptFiles.setAfterScanScript(afterScanScript);

		// Act - covert mapping bean to scan request
		final ScanRequest<IROI> scanRequest = scanRequestConverter.convertToScanRequest(mappingBean);

		// Assert
		final ScriptRequest beforeScriptReq = scanRequest.getBefore();
		assertThat(beforeScriptReq, is(notNullValue()));
		assertThat(beforeScriptReq.getLanguage(), is(SPEC_PASTICHE));
		assertThat(beforeScriptReq.getFile(), is(equalTo(beforeScanScript)));
		final ScriptRequest afterScriptReq = scanRequest.getAfter();
		assertThat(afterScriptReq, is(notNullValue()));
		assertThat(afterScriptReq.getLanguage(), is(SPEC_PASTICHE));
		assertThat(afterScriptReq.getFile(), is(equalTo(afterScanScript)));

		// Act again - convert the scan request back to a mapping bean
		scanRequestConverter.mergeIntoMappingBean(scanRequest, newMappingBean);

		// Assert again
		final IScriptFiles newScriptFiles = newMappingBean.getScriptFiles();
		assertThat(newScriptFiles, is(notNullValue()));
		assertThat(newScriptFiles.getBeforeScanScript(), is(equalTo(beforeScanScript)));
		assertThat(newScriptFiles.getAfterScanScript(), is(equalTo(afterScanScript)));
	}

	@Test
	public void testBeamlineConfigurationIncludedCorrectly() {
		// Arrange
		final Map<String, Object> beamlineConfiguration = new HashMap<>();
		beamlineConfiguration.put("energy", 12345.67);
		beamlineConfiguration.put("attenuator_pos", "Gap");
		beamlineConfiguration.put("kb_mirror_pos", 7.0);
		mappingBean.setBeamlineConfiguration(beamlineConfiguration);

		// Act - convert mapping bean to scan request
		final ScanRequest<IROI> scanRequest = scanRequestConverter.convertToScanRequest(mappingBean);

		// Assert
		final IPosition startPos = scanRequest.getStart();
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
		// Act - convert mapping bean to scan request
		final ScanRequest<IROI> scanRequest = scanRequestConverter.convertToScanRequest(mappingBean);
		final Collection<ScanRegion<IROI>> regions = scanRequest.getCompoundModel().getRegions();

		// Assert - ensure only one region
		assertThat(regions.size(), is(equalTo(1)));
		for (final ScanRegion<?> scanRegion : regions) {
			final List<String> scannables = scanRegion.getScannables();
			assertThat(scannables.get(0), is(equalTo(Y_AXIS_NAME)));
			assertThat(scannables.get(1), is(equalTo(X_AXIS_NAME)));
		}

		// Act again - convert scan request back to mapping bean
		scanRequestConverter.mergeIntoMappingBean(scanRequest, newMappingBean);
		assertEquals(mappingBean.getScanDefinition().getMappingScanRegion().getRegion(),
				newMappingBean.getScanDefinition().getMappingScanRegion().getRegion());
	}

	@Test
	public void testOuterScannableIsSet() throws Exception {
		// Arrange
		final IScanPathModel outerModel = new StepModel(Z_AXIS_NAME, -3, 2, 0.5);
		final List<IScanPathModelWrapper> outerScannables = Arrays.asList(new ScanPathModelWrapper(
				Z_AXIS_NAME, outerModel, true));

		mappingBean.getScanDefinition().setOuterScannables(outerScannables);

		// Act - convert mapping bean to scan request
		final ScanRequest<IROI> scanRequest = scanRequestConverter.convertToScanRequest(mappingBean);

		// Assert
		// Check there are now 2 models the outer z StepModel and the inner Grid model
		assertThat(scanRequest.getCompoundModel().getModels().size(), is(equalTo(2)));
		final StepModel recoveredOuterModel = (StepModel) scanRequest.getCompoundModel().getModels().get(0);
		// Check the outer scannable model is first in the list
		assertThat(recoveredOuterModel, is(outerModel));
		// Check it has the correct axis name
		assertThat(recoveredOuterModel.getName(), is(Z_AXIS_NAME));

		// setup the new mapping bean with an outer scannable for the same axis, but disabled
		// and with a different model, and another enabled and for a different axis
		newMappingBean.getScanDefinition().setOuterScannables(Arrays.asList(
				new ScanPathModelWrapper(Z_AXIS_NAME, new StepModel(Z_AXIS_NAME, 0, 5, 0.25), false),
				new ScanPathModelWrapper("energy", new StepModel("energy", 10000, 15000, 1000), true)));
		scanRequestConverter.mergeIntoMappingBean(scanRequest, newMappingBean);

		// Assert again - check the new mapping bean is the same as the old one
		List<IScanPathModelWrapper> newOuterScannables = newMappingBean.getScanDefinition().getOuterScannables();
		assertThat(newOuterScannables.size(), is(2));
		assertThat(newOuterScannables.get(0).getName(), is(equalTo(Z_AXIS_NAME)));
		assertThat(newOuterScannables.get(0).getModel(), is(equalTo(outerModel)));
		assertThat(newOuterScannables.get(0).isIncludeInScan(), is(true));
		assertThat(newOuterScannables.get(1).getName(), is(equalTo("energy")));
		assertThat(newOuterScannables.get(1).isIncludeInScan(), is(false));
	}

	@Test(expected = RuntimeException.class)
	public void testOuterScannableNotFound() throws Exception {
		// Arrange
		final IScanPathModel outerModel = new StepModel(Z_AXIS_NAME, -3, 2, 0.5);
		final List<IScanPathModelWrapper> outerScannables = Arrays.asList(new ScanPathModelWrapper(
				Z_AXIS_NAME, outerModel, true));

		mappingBean.getScanDefinition().setOuterScannables(outerScannables);

		// Act - convert mapping bean to scan request
		final ScanRequest<IROI> scanRequest = scanRequestConverter.convertToScanRequest(mappingBean);

		// Throws an exception as there is no wrapper for the outer scannable in the new mapping bean
		scanRequestConverter.mergeIntoMappingBean(scanRequest, newMappingBean);
	}

	@Test
	public void testClusterProcessingIncludedCorrectly() {
		// Arrange
		final String processingStepName = "sum";
		final ClusterProcessingModel clusterProcessingModel = new ClusterProcessingModel();
		clusterProcessingModel.setName(processingStepName);
		clusterProcessingModel.setDetectorName("mandelbrot");
		clusterProcessingModel.setProcessingFilePath("/tmp/sum.nxs");
		final ClusterProcessingModelWrapper wrapper = new ClusterProcessingModelWrapper(
				processingStepName, clusterProcessingModel, true);

		mappingBean.setClusterProcessingConfiguration(Arrays.asList(wrapper));

		// Act - convert mapping bean to scan request
		final ScanRequest<IROI> scanRequest = scanRequestConverter.convertToScanRequest(mappingBean);

		// Assert
		assertThat(scanRequest.getDetectors().get(processingStepName), is(equalTo(clusterProcessingModel)));

		// Act again - merge scan request into new mapping bean
		scanRequestConverter.mergeIntoMappingBean(scanRequest, newMappingBean);

		// Assert again - check the new mapping bean is the same as the old one
		List<IClusterProcessingModelWrapper> newClusterProcessingList = newMappingBean.getClusterProcessingConfiguration();
		assertThat(newClusterProcessingList.size(), is(1));
		IClusterProcessingModelWrapper clusterProcessingWrapper = newClusterProcessingList.get(0);
		assertThat(clusterProcessingWrapper.getName(), is(equalTo(wrapper.getName())));
		assertThat(clusterProcessingWrapper.getModel(), is(equalTo(clusterProcessingModel)));
	}

	@Test
	public void testSampleMetadataSet() throws Exception {
		// Arrange
		final String sampleName = "testSample";
		final String sampleDescription = "This is a description of the test sample.";
		mappingBean.getSampleMetadata().setSampleName(sampleName);
		mappingBean.getSampleMetadata().setDescription(sampleDescription);

		// Act - convert mapping bean to scan request
		ScanRequest<IROI> scanRequest = scanRequestConverter.convertToScanRequest(mappingBean);

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

}
