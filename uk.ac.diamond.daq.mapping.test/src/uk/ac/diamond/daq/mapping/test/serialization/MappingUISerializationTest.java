/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.test.serialization;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.scanning.api.device.models.ClusterProcessingModel;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.example.classregistry.ScanningExampleClassRegistry;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.points.classregistry.ScanningAPIClassRegistry;
import org.eclipse.scanning.points.serialization.PointsModelMarshaller;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegion;
import uk.ac.diamond.daq.mapping.api.IScanDefinition;
import uk.ac.diamond.daq.mapping.impl.ClusterProcessingModelWrapper;
import uk.ac.diamond.daq.mapping.impl.DetectorModelWrapper;
import uk.ac.diamond.daq.mapping.impl.MappingExperimentBean;
import uk.ac.diamond.daq.mapping.impl.MappingScanDefinition;
import uk.ac.diamond.daq.mapping.impl.MappingScanRegion;
import uk.ac.diamond.daq.mapping.impl.MappingUIClassRegistry;
import uk.ac.diamond.daq.mapping.impl.ScanPathModelWrapper;
import uk.ac.diamond.daq.mapping.impl.ScriptFiles;
import uk.ac.diamond.daq.mapping.impl.SimpleSampleMetadata;
import uk.ac.diamond.daq.mapping.region.CentredRectangleMappingRegion;
import uk.ac.diamond.daq.mapping.region.CircularMappingRegion;
import uk.ac.diamond.daq.mapping.region.LineMappingRegion;
import uk.ac.diamond.daq.mapping.region.PointMappingRegion;
import uk.ac.diamond.daq.mapping.region.PolygonMappingRegion;
import uk.ac.diamond.daq.mapping.region.RectangularMappingRegion;

public class MappingUISerializationTest {

	private IMarshallerService service;

	@Before
	public void createService() {
		service = new MarshallerService(
				Arrays.asList(new ScanningAPIClassRegistry(),
						new ScanningExampleClassRegistry(),
						new MappingUIClassRegistry()),
				Arrays.asList(new PointsModelMarshaller()));
	}

	@Test
	public void testCircleMappingSerialization() throws Exception {

		CircularMappingRegion originalRegion = new CircularMappingRegion();
		String json = service.marshal(originalRegion);

		System.out.println(json);
		final CircularMappingRegion deserialisedRegion = service.unmarshal(json, CircularMappingRegion.class);

		assertEquals("Deserialised and serialised object are different", originalRegion, deserialisedRegion);
	}

	@Test
	public void testCircleMappingInvalidDeserializeAfterJsonModification() throws Exception {

		CircularMappingRegion originalRegion = new CircularMappingRegion();
		String json = service.marshal(originalRegion);

		System.out.println(json);
		json = json.replace("CIRCLE", RegionType.ELLIPSE.name());
		System.out.println(json);

		final CircularMappingRegion deserialisedRegion = service.unmarshal(json, CircularMappingRegion.class);

		assertEquals("The region type was sucessfully altered!", originalRegion.whichPlottingRegionType(), deserialisedRegion.whichPlottingRegionType());
		assertEquals("Deserialised and serialised object are different", originalRegion, deserialisedRegion);
	}

	@Test
	public void testSerializeDetectorModelWrapper() throws Exception {
		MandelbrotModel model = createMandelbrotModel();
		DetectorModelWrapper detectorModelWrapper = new DetectorModelWrapper(
				"Mandelbrot Detector", model, true);

		String json = service.marshal(detectorModelWrapper);
		DetectorModelWrapper newDetectorModelWrapper = service.unmarshal(json, DetectorModelWrapper.class);

		assertEquals(detectorModelWrapper, newDetectorModelWrapper);
	}

	private MandelbrotModel createMandelbrotModel() {
		MandelbrotModel model = new MandelbrotModel();
		model.setName("mandelbrot");
		model.setRealAxisName("xNex");
		model.setImaginaryAxisName("yNex");
		model.setColumns(64);
		model.setRows(64);
		model.setMaxIterations(1);
		model.setExposureTime(0.0);
		return model;
	}

	@Test
	public void testSerializeClusterProcessingModelWrapper() throws Exception {
		ClusterProcessingModel model = new ClusterProcessingModel();
		model.setName("processing");
		model.setDetectorName("mandelbrot");
		model.setProcessingFilePath("/path/to/processing.file");

		ClusterProcessingModelWrapper wrapper = new ClusterProcessingModelWrapper(
				"Cluster Processing", model, true);

		String json = service.marshal(wrapper);
		ClusterProcessingModelWrapper newWrapper = service.unmarshal(json, ClusterProcessingModelWrapper.class);

		assertEquals(wrapper, newWrapper);
	}

	@Test
	public void testSerializeScanPathModelWrapper() throws Exception {
		StepModel model = new StepModel("energy", 10000.0, 10200.0, 10.0);
		ScanPathModelWrapper wrapper = new ScanPathModelWrapper("energy", model, true);

		String json = service.marshal(wrapper);
		ScanPathModelWrapper newWrapper = service.unmarshal(json, ScanPathModelWrapper.class);

		assertEquals(wrapper, newWrapper);
	}

	@Test
	public void testSerializeCircularMappingRegion() throws Exception {
		CircularMappingRegion region = new CircularMappingRegion();
		region.setxCenter(10.0);
		region.setyCenter(25.0);
		region.setRadius(5.8);

		String json = service.marshal(region);
		CircularMappingRegion newRegion = service.unmarshal(json, CircularMappingRegion.class);

		assertEquals(region, newRegion);
	}

	@Test
	public void testSerializeLineMappingRegion() throws Exception {
		LineMappingRegion region = new LineMappingRegion();
		region.setxStart(10.0);
		region.setxStop(20.0);
		region.setyStart(5.5);
		region.setyStop(13.2);

		String json = service.marshal(region);
		LineMappingRegion newRegion = service.unmarshal(json, LineMappingRegion.class);

		assertEquals(region, newRegion);
	}

	@Test
	public void testSerializePointMappingRegion() throws Exception {
		PointMappingRegion region = new PointMappingRegion();
		region.setxPosition(7.5);
		region.setyPosition(2.2);

		String json = service.marshal(region);
		PointMappingRegion newRegion = service.unmarshal(json, PointMappingRegion.class);

		assertEquals(region, newRegion);
	}

	@Test
	@Ignore("Point does not serialize properly - this should be investigated")
	public void testSerializePoint() throws Exception {
		// TODO remove: a temporary test to test serializing a Point in isolation.
		// Point is required by PolygonMappingRegion. This test should be removed when
		// testSerializePolygonRegion is passing
		Point point = new Point(0, 1.0, 0, 1.0);

		String json = service.marshal(point);
		Point newPoint = service.unmarshal(json, Point.class);

		assertEquals(point, newPoint);
	}

	@Test
	@Ignore("Point does not serialize properly - this should be investigated")
	public void testSerializePolygonRegion() throws Exception {
		PolygonMappingRegion region = new PolygonMappingRegion();
		List<Point> points = new ArrayList<>();
		points.add(new Point(0, 1.0, 0, 2.7));
		points.add(new Point(1, 8.5, 0, 2.7));
		points.add(new Point(1, 8.5, 1, 6.4));
		points.add(new Point(0, 1.0, 1, 6.4));
		region.setPoints(points);

		String json = service.marshal(region);
		PolygonMappingRegion newRegion = service.unmarshal(json, PolygonMappingRegion.class);

		assertEquals(region, newRegion);
	}

	@Test
	public void testSerializeRectangularMappingRegion() throws Exception {
		RectangularMappingRegion region = new RectangularMappingRegion();
		region.setxStart(10.0);
		region.setxStop(20.0);
		region.setyStart(5.5);
		region.setyStop(13.2);

		String json = service.marshal(region);
		RectangularMappingRegion newRegion = service.unmarshal(json, RectangularMappingRegion.class);

		assertEquals(region, newRegion);
	}

	@Test
	public void testSerializeCentredRectangleMappingRegion() throws Exception {
		CentredRectangleMappingRegion region = new CentredRectangleMappingRegion();
		region.setxCentre(15.0);
		region.setxRange(10.0);
		region.setyCentre(8.5);
		region.setyRange(13.0);

		String json = service.marshal(region);
		CentredRectangleMappingRegion newRegion = service.unmarshal(json, CentredRectangleMappingRegion.class);

		assertEquals(region, newRegion);
	}

	@Test
	public void testSerializeMappingScanRegion() throws Exception {
		IMappingScanRegion scanRegion = createScanRegion();

		String json = service.marshal(scanRegion);
		MappingScanRegion newScanRegion = service.unmarshal(json, MappingScanRegion.class);

		assertEquals(scanRegion, newScanRegion);
	}

	private IMappingScanRegion createScanRegion() {
		IMappingScanRegion scanRegion = new MappingScanRegion();

		GridModel gmodel = new GridModel();
		gmodel.setFastAxisName("xNex");
		gmodel.setFastAxisPoints(50);
		gmodel.setSlowAxisName("yNex");
		gmodel.setSlowAxisPoints(20);
		gmodel.setBoundingBox(new BoundingBox(0,0,3,3));

		RectangularMappingRegion region = new RectangularMappingRegion();
		region.setxStart(10.0);
		region.setxStop(20.0);
		region.setyStart(5.5);
		region.setyStop(13.2);

		scanRegion.setScanPath(gmodel);
		scanRegion.setRegion(region);
		return scanRegion;
	}

	@Test
	public void testSerializeMappingScanDefinition() throws Exception {
		IScanDefinition scanDefinition = createScanDefinition();

		String json = service.marshal(scanDefinition);
		MappingScanDefinition newScanDefinition = service.unmarshal(json, MappingScanDefinition.class);

		assertEquals(scanDefinition, newScanDefinition);
	}

	private IScanDefinition createScanDefinition() {
		IMappingScanRegion scanRegion = createScanRegion();

		StepModel energy = new StepModel("energy", 10000.0, 10200.0, 10.0);
		ScanPathModelWrapper energyWrapper = new ScanPathModelWrapper("energy", energy, true);
		StepModel z = new StepModel("z", 2.5, 17.5, 0.1);
		ScanPathModelWrapper zWrapper = new ScanPathModelWrapper("z", z, true);

		IScanDefinition scanDefinition = new MappingScanDefinition();
		scanDefinition.setMappingScanRegion(scanRegion);
		scanDefinition.setOuterScannables(Arrays.asList(energyWrapper, zWrapper));
		return scanDefinition;
	}

	@Test
	public void testSerializeSimpleSampleMetadata() throws Exception {
		SimpleSampleMetadata sampleMetadata = new SimpleSampleMetadata();
		sampleMetadata.setSampleName("SampleName");
		sampleMetadata.setDescription("Description of sample");

		String json = service.marshal(sampleMetadata);
		SimpleSampleMetadata newSampleMetadata = service.unmarshal(json, SimpleSampleMetadata.class);

		assertEquals(sampleMetadata, newSampleMetadata);
	}

	@Test
	public void testSerializeScriptFiles() throws Exception {
		ScriptFiles scriptFiles = new ScriptFiles();
		scriptFiles.setBeforeScanScript("/path/to/before.py");
		scriptFiles.setAfterScanScript("/path/to/after.py");

		String json = service.marshal(scriptFiles);
		ScriptFiles newScriptFiles = service.unmarshal(json, ScriptFiles.class);

		assertEquals(scriptFiles, newScriptFiles);
	}

	@Test
	public void testSerializeMappingExperimentBean() throws Exception {
		ScriptFiles scriptFiles = new ScriptFiles();
		scriptFiles.setBeforeScanScript("/path/to/before.py");
		scriptFiles.setAfterScanScript("/path/to/after.py");

		Map<String, Object> beamlineConfiguration = new HashMap<>();
		beamlineConfiguration.put("D7A", "Gap");
		beamlineConfiguration.put("D7B", "gap");
		beamlineConfiguration.put("kb_vfm_x", "7.0");

		MandelbrotModel model = createMandelbrotModel();
		DetectorModelWrapper mandelbrotWrapper = new DetectorModelWrapper(
				"Mandelbrot Detector", model, true);

		ClusterProcessingModel clusterProcessingModel = new ClusterProcessingModel(
				"processing", "mandelbrot", "/path/to/processing_file.nxs");
		ClusterProcessingModelWrapper processingWrapper = new ClusterProcessingModelWrapper(
				"processing", clusterProcessingModel, true);

		SimpleSampleMetadata sampleMetadata = new SimpleSampleMetadata();
		sampleMetadata.setSampleName("SampleName");
		sampleMetadata.setDescription("Description of sample");

		IScanDefinition scanDefinition = createScanDefinition();

		MappingExperimentBean mappingBean = new MappingExperimentBean();
		mappingBean.setScriptFiles(scriptFiles);
		mappingBean.setBeamlineConfiguration(beamlineConfiguration);
		mappingBean.setDetectorParameters(Arrays.asList(mandelbrotWrapper));
		mappingBean.setClusterProcessingConfiguration(Arrays.asList(processingWrapper));
		mappingBean.setSampleMetadata(sampleMetadata);
		mappingBean.setScanDefinition(scanDefinition);

		String json = service.marshal(mappingBean);
		MappingExperimentBean newMappingBean = service.unmarshal(json, MappingExperimentBean.class);

		assertEquals(mappingBean, newMappingBean);
	}

}
