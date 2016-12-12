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

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;
import uk.ac.diamond.daq.mapping.api.IScanPathModelWrapper;
import uk.ac.diamond.daq.mapping.api.IScriptFiles;
import uk.ac.diamond.daq.mapping.impl.ClusterProcessingModelWrapper;
import uk.ac.diamond.daq.mapping.impl.DetectorModelWrapper;
import uk.ac.diamond.daq.mapping.impl.MappingExperimentBean;
import uk.ac.diamond.daq.mapping.impl.MappingStageInfo;
import uk.ac.diamond.daq.mapping.impl.ScanPathModelWrapper;
import uk.ac.diamond.daq.mapping.impl.ScriptFiles;
import uk.ac.diamond.daq.mapping.region.RectangularMappingRegion;
import uk.ac.diamond.daq.mapping.ui.experiment.ScanRequestConverter;

public class ScanRequestConverterTest {

	private static final String X_AXIS_NAME = "testing_stage_x";
	private static final String Y_AXIS_NAME = "testing_stage_y";
	private static final String Z_AXIS_NAME = "testing_z_axis";

	private ScanRequestConverter scanRequestConverter;
	private MappingStageInfo mappingStageInfo;
	private MappingExperimentBean experimentBean;
	private GridModel scanPath;

	@Before
	public void setUp() throws Exception {
		mappingStageInfo = new MappingStageInfo();
		mappingStageInfo.setActiveFastScanAxis(X_AXIS_NAME);
		mappingStageInfo.setActiveSlowScanAxis(Y_AXIS_NAME);

		scanRequestConverter = new ScanRequestConverter();
		scanRequestConverter.setMappingStageInfo(mappingStageInfo);

		// Set up the experiment bean with some sensible defaults
		experimentBean = new MappingExperimentBean();

		scanPath = new GridModel();
		experimentBean.getScanDefinition().getMappingScanRegion().setScanPath(scanPath);

		IMappingScanRegionShape scanRegion = new RectangularMappingRegion();
		experimentBean.getScanDefinition().getMappingScanRegion().setRegion(scanRegion);

		experimentBean.setDetectorParameters(Collections.emptyList());
	}

	@After
	public void tearDown() throws Exception {
		mappingStageInfo = null;
		scanRequestConverter = null;
	}

	@Test
	public void testDetectorIsIncludedCorrectly() {
		String detName = "mandelbrot";
		String displayName = "Mandelbrot Detector";
		IDetectorModel detModel = new MandelbrotModel();
		detModel.setName(detName);
		experimentBean.setDetectorParameters(Arrays.asList(new DetectorModelWrapper(detName, detModel, true)));

		ScanRequest<?> scanRequest = scanRequestConverter.convertToScanRequest(experimentBean);

		assertEquals(detModel, scanRequest.getDetectors().get(detName));
	}

	@Test
	public void testDetectorIsExcludedCorrectly() {
		String detName = "det1";
		IDetectorModel detModel = new MandelbrotModel();
		experimentBean.setDetectorParameters(Arrays.asList(new DetectorModelWrapper(detName, detModel, false)));

		ScanRequest<?> scanRequest = scanRequestConverter.convertToScanRequest(experimentBean);

		// This test relies on the implementation of ScanRequest, which lazily initialises its detectors field only
		// when a detector is added. If this fails in future because getDetectors() returns an empty map, this test
		// will need to be updated to match.
		assertThat(scanRequest.getDetectors(), is(nullValue()));
	}

	@Test
	public void testScanPathIsIncluded() {
		ScanRequest<?> scanRequest = scanRequestConverter.convertToScanRequest(experimentBean);

		assertEquals(scanRequest.getCompoundModel().getModels().get(0), scanPath);
	}

	@Test
	public void testStageNamesAreSetCorrectly() {
		assertThat(scanPath.getFastAxisName(), is(not(equalTo(X_AXIS_NAME))));
		assertThat(scanPath.getSlowAxisName(), is(not(equalTo(Y_AXIS_NAME))));

		scanRequestConverter.convertToScanRequest(experimentBean);

		assertThat(scanPath.getFastAxisName(), is(equalTo(X_AXIS_NAME)));
		assertThat(scanPath.getSlowAxisName(), is(equalTo(Y_AXIS_NAME)));
	}

	@Test
	public void testScriptFilesIncludedCorrectly() {
		IScriptFiles scriptFiles = new ScriptFiles();
		experimentBean.setScriptFiles(scriptFiles);
		scriptFiles.setBeforeScanScript("/tmp/before.py");
		scriptFiles.setAfterScanScript("/tmp/after.py");

		ScanRequest<?> scanRequest = scanRequestConverter.convertToScanRequest(experimentBean);

		ScriptRequest beforeScriptReq = scanRequest.getBefore();
		assertThat(beforeScriptReq, is(notNullValue()));
		assertThat(beforeScriptReq.getLanguage(), is(SPEC_PASTICHE));
		assertThat(beforeScriptReq.getFile(), is(equalTo("/tmp/before.py")));
		ScriptRequest afterScriptReq = scanRequest.getAfter();
		assertThat(afterScriptReq, is(notNullValue()));
		assertThat(afterScriptReq.getLanguage(), is(SPEC_PASTICHE));
		assertThat(afterScriptReq.getFile(), is(equalTo("/tmp/after.py")));
	}

	@Test
	public void testBeamlineConfigurationIncludedCorrectly() {
		Map<String, Object> beamlineConfiguration = new HashMap<>();
		beamlineConfiguration.put("energy", 2675.3);
		beamlineConfiguration.put("attenuator_pos", "Gap");
		beamlineConfiguration.put("kb_mirror_pos", 7.0);
		experimentBean.setBeamlineConfiguration(beamlineConfiguration);

		ScanRequest<?> scanRequest = scanRequestConverter.convertToScanRequest(experimentBean);

		IPosition startPos = scanRequest.getStart();
		assertThat(startPos.getNames().size(), is(3));
		assertThat(startPos.get("energy"), is(2675.3));
		assertThat(startPos.get("attenuator_pos"), is("Gap"));
		assertThat(startPos.get("kb_mirror_pos"), is(7.0));
	}

	@Test
	public void testRoiAxisNamesAreSet() throws Exception {
		ScanRequest<IROI> scanRequest = scanRequestConverter.convertToScanRequest(experimentBean);

		Collection<ScanRegion<IROI>> regions = scanRequest.getCompoundModel().getRegions();

		// Ensure only on region
		assertThat(regions.size(), is(equalTo(1)));

		for (ScanRegion<?> scanRegion : regions) {
			List<String> scannables = scanRegion.getScannables();
			assertThat(scannables.get(0), is(equalTo(Y_AXIS_NAME)));
			assertThat(scannables.get(1), is(equalTo(X_AXIS_NAME)));
		}
	}

	@Test
	public void testOuterScannableIsSet() throws Exception {

		IScanPathModel outerModel = new StepModel(Z_AXIS_NAME, -3, 2, 0.5);
		List<IScanPathModelWrapper> outerScannables = Arrays.asList(new ScanPathModelWrapper("z", outerModel, true));

		experimentBean.getScanDefinition().setOuterScannables(outerScannables);

		// Get the scan request
		ScanRequest<?> scanRequest = scanRequestConverter.convertToScanRequest(experimentBean);

		// Check there are now 2 models the outer z StepModel and the inner Grid model
		assertThat(scanRequest.getCompoundModel().getModels().size(), is(equalTo(2)));

		StepModel recoveredOuterModel = (StepModel) scanRequest.getCompoundModel().getModels().get(0);

		// Check the outer scannable model is first in the list
		assertThat(recoveredOuterModel, is(outerModel));

		// Check it has the correct axis name
		assertThat(recoveredOuterModel.getName(), is(Z_AXIS_NAME));
	}

	public void testClusterProcessingIncludedCorrectly() {
		String processingStepName = "sum";
		ClusterProcessingModel clusterProcessingModel = new ClusterProcessingModel();
		clusterProcessingModel.setName(processingStepName);
		clusterProcessingModel.setDetectorName("mandelbrot");
		clusterProcessingModel.setProcessingFilePath("/tmp/sum.nxs");
		ClusterProcessingModelWrapper wrapper = new ClusterProcessingModelWrapper(
				processingStepName, clusterProcessingModel, true);

		experimentBean.setClusterProcessingConfiguration(Arrays.asList(wrapper));

		ScanRequest<?> scanRequest = scanRequestConverter.convertToScanRequest(experimentBean);

		assertEquals(scanRequest.getDetectors().get(processingStepName), clusterProcessingModel);
	}

	@Test
	public void testSampleMetadataSet() throws Exception {
		final String sampleName = "testSample";
		final String sampleDescription = "This is a description of the test sample.";
		experimentBean.getSampleMetadata().setSampleName(sampleName);
		experimentBean.getSampleMetadata().setDescription(sampleDescription);

		ScanRequest<?> scanRequest = scanRequestConverter.convertToScanRequest(experimentBean);

		List<ScanMetadata> scanMetadataList = scanRequest.getScanMetadata();
		assertEquals(1, scanMetadataList.size());
		ScanMetadata sampleMetadata = scanMetadataList.get(0);
		assertEquals(MetadataType.SAMPLE, sampleMetadata.getType());
		assertEquals(sampleName, sampleMetadata.getFieldValue("name"));
		assertEquals(sampleDescription, sampleMetadata.getFieldValue("description"));
	}

}
