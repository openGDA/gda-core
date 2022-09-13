/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.diffcalc.gda;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.jython.ITerminalPrinter;
import gda.jython.InterfaceProvider;
import uk.ac.diamond.daq.diffcalc.ApiClient;
import uk.ac.diamond.daq.diffcalc.ApiException;
import uk.ac.diamond.daq.diffcalc.ApiResponse;
import uk.ac.diamond.daq.diffcalc.api.ConstraintsApi;
import uk.ac.diamond.daq.diffcalc.api.DefaultApi;
import uk.ac.diamond.daq.diffcalc.api.HklApi;
import uk.ac.diamond.daq.diffcalc.api.UbApi;
import uk.ac.diamond.daq.diffcalc.model.AddOrientationParams;
import uk.ac.diamond.daq.diffcalc.model.AddReflectionParams;
import uk.ac.diamond.daq.diffcalc.model.ArrayResponse;
import uk.ac.diamond.daq.diffcalc.model.DiffractorAnglesResponse;
import uk.ac.diamond.daq.diffcalc.model.HklModel;
import uk.ac.diamond.daq.diffcalc.model.InfoResponse;
import uk.ac.diamond.daq.diffcalc.model.MiscutModel;
import uk.ac.diamond.daq.diffcalc.model.MiscutResponse;
import uk.ac.diamond.daq.diffcalc.model.Orientation;
import uk.ac.diamond.daq.diffcalc.model.OrientationResponse;
import uk.ac.diamond.daq.diffcalc.model.PositionModel;
import uk.ac.diamond.daq.diffcalc.model.ReciprocalSpaceResponse;
import uk.ac.diamond.daq.diffcalc.model.SetLatticeParams;
import uk.ac.diamond.daq.diffcalc.model.XyzModel;

/**
 * Class which tests DiffcalcContext class. This makes API calls to a server, to store/retrieve
 * things from a database. There is an extra layer between the API and the class, which is a
 * client library that is auto-generated.
 *
 * The API has it's own tests to check that endpoints correctly store and retrieve items in
 * the database. Therefore, to avoid code duplication, the tests for this class simply check
 * the interim logic, such as transforms between axes and mappings that are necessary for calls
 * to be made.
 */
class DiffcalcContextTest {
	DiffcalcContext dc;

	FourCircle mapping = new FourCircle();

	UbApi ubApi;
	ConstraintsApi constraintsApi;
	HklApi hklApi;
	DefaultApi storeApi;

	ScannableGroup diffractometer;
	Scannable energy;
	double energyValue = 120d;

	String collectionName = "fake";
	String calculationName = "testing";

	/*
	 * Sets up mapping for the geometry mapping object, for the test.
	 */
	@BeforeEach
	public void setup() throws DeviceException {

		ITerminalPrinter printer = mock(ITerminalPrinter.class);
		InterfaceProvider.setTerminalPrinterForTesting(printer);

		String[] motors = {"motor1", "motor2", "motor3", "motor4"};

		mapping.setFirstAngle(ReferenceGeometry.MU);
		mapping.setSecondAngle(ReferenceGeometry.ETA);
		mapping.setFirstAngleValue(0.0);
		mapping.setSecondAngleValue(10.0);

		Map<ReferenceGeometry, String> referenceToBeamlineNames = new HashMap<>();

		referenceToBeamlineNames.put(ReferenceGeometry.DELTA, "motor1");
		referenceToBeamlineNames.put(ReferenceGeometry.NU, "motor2");
		referenceToBeamlineNames.put(ReferenceGeometry.CHI, "motor4");
		referenceToBeamlineNames.put(ReferenceGeometry.PHI, "motor3");

		mapping.setReferenceAnglesToBeamlineMotors(referenceToBeamlineNames);

		diffractometer = mock(ScannableGroup.class);
		when(diffractometer.getGroupMemberNames()).thenReturn(motors);
		when(diffractometer.getPosition()).thenReturn(new Object[] {0d, 0d, 0d, 0d});

		ApiClient client = mock(ApiClient.class);
		constraintsApi = mock(ConstraintsApi.class);
		storeApi = mock(DefaultApi.class);
		ubApi = mock(UbApi.class);
		hklApi = mock(HklApi.class);

		dc = new DiffcalcContext(client, constraintsApi, storeApi, ubApi, hklApi);
		dc.setCalculationName(calculationName);
		dc.setCollectionName(collectionName);
		dc.setDiffractometer(diffractometer);
		dc.setAngleTransform(mapping);

		energy = mock(Scannable.class);
		when(energy.getPosition()).thenReturn(energyValue);
		dc.setEnergy(energy);
	}

	@Test
	void testRawBeamlinePositionToPositionModel() {
		List<Double> beamlinePosition = Arrays.asList(1.0, 2.0, 3.0, 4.0);
		PositionModel referencePosition = dc.rawBeamlinePositionToPositionModel(beamlinePosition);

		assertEquals(0.0, referencePosition.getMu().doubleValue());
		assertEquals(1.0, referencePosition.getDelta().doubleValue());
		assertEquals(2.0, referencePosition.getNu().doubleValue());
		assertEquals(10.0, referencePosition.getEta().doubleValue());
		assertEquals(4.0, referencePosition.getChi().doubleValue());
		assertEquals(3.0, referencePosition.getPhi().doubleValue());
	}

	@Test
	void testRawBeamlinePositionToPositionModelFailsForWrongInputs() {
		List<Double> beamlinePositionValues = Arrays.asList(1.0, 2.0, 3.0);

		ArrayIndexOutOfBoundsException exception = assertThrows(
				ArrayIndexOutOfBoundsException.class,
				() -> {
					dc.rawBeamlinePositionToPositionModel(beamlinePositionValues);
					}
				);

		assertEquals("Index 3 out of bounds for length 3", exception.getMessage());
	}

	@Test
	void testPositionModelToRawBeamlinePosition() {
		List<Double> rawBeamlinePosition = List.of(1.0, 2.0, 3.0, 4.0);
		PositionModel referencePosition = dc.rawBeamlinePositionToPositionModel(rawBeamlinePosition);

		List<Double> remadeRawBeamlinePosition = dc.positionModelToRawBeamlinePosition(referencePosition);

		assertEquals(remadeRawBeamlinePosition, rawBeamlinePosition);

	}

	@Test
	void testAddReflection() throws ApiException {
		ApiResponse<InfoResponse> dummyResponse = new ApiResponse<>(200, new HashMap<>(), new InfoResponse().message("dummy"));
		when(ubApi.addReflectionUbNameReflectionPostWithHttpInfo(any(), any(), any(), any())).thenReturn(dummyResponse);

		double energy = 12.0;
		List<Double> millerIndices = Arrays.asList(1.0, 2.0, 3.0);
		List<Double> beamlinePosition= Arrays.asList(1.0, 2.0, 3.0, 4.0);
		String tag = "some tag";

		AddReflectionParams body = new AddReflectionParams();

		HklModel hkl = new HklModel();
		hkl.setH(BigDecimal.valueOf(millerIndices.get(0)));
		hkl.setK(BigDecimal.valueOf(millerIndices.get(1)));
		hkl.setL(BigDecimal.valueOf(millerIndices.get(2)));

		PositionModel position = dc.rawBeamlinePositionToPositionModel(beamlinePosition);

		body.setEnergy(BigDecimal.valueOf(energy));
		body.setHkl(hkl);
		body.setPosition(position);

		dc.addReflection(energy, millerIndices, beamlinePosition);
		verify(ubApi).addReflectionUbNameReflectionPostWithHttpInfo(body, calculationName, collectionName, null);

		dc.addReflection(energy, millerIndices, beamlinePosition, tag);
		verify(ubApi).addReflectionUbNameReflectionPostWithHttpInfo(body, calculationName, collectionName, tag);
	}

	@Test
	void testAddReflectionFailsForWrongMillerIndices() throws ApiException {
		Double energy = 12.0;
		List<Double> millerIndices = Arrays.asList(2.0, 3.0);
		List<Double> beamlinePosition= Arrays.asList(1.0, 2.0, 3.0, 4.0);

		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> {
					dc.addReflection(energy, millerIndices, beamlinePosition);
					}
				);

		assertEquals("3 miller indices must be provided", exception.getMessage());
	}

	@Test
	void testAddOrientation() throws ApiException, DeviceException {

		ApiResponse<InfoResponse> dummyResponse = new ApiResponse<>(200, new HashMap<>(), new InfoResponse().message("dummy"));
		String tag = "some tag";
		List<Double> millerIndices = Arrays.asList(1.0, 2.0, 3.0);
		List<Double> coords = Arrays.asList(1.0, 2.0, 3.0);

		AddOrientationParams body = new AddOrientationParams();

		HklModel hkl = new HklModel();
		hkl.setH(BigDecimal.valueOf(millerIndices.get(0)));
		hkl.setK(BigDecimal.valueOf(millerIndices.get(1)));
		hkl.setL(BigDecimal.valueOf(millerIndices.get(2)));

		XyzModel xyz = new XyzModel();
		xyz.setX(BigDecimal.valueOf(coords.get(0)));
		xyz.setY(BigDecimal.valueOf(coords.get(1)));
		xyz.setZ(BigDecimal.valueOf(coords.get(2)));

		PositionModel position = new PositionModel();
		position.setMu(BigDecimal.valueOf(0d));
		position.setDelta(BigDecimal.valueOf(0d));
		position.setNu(BigDecimal.valueOf(0d));
		position.setEta(BigDecimal.valueOf(10d));
		position.setChi(BigDecimal.valueOf(0d));
		position.setPhi(BigDecimal.valueOf(0d));

		body.setXyz(xyz);
		body.setHkl(hkl);
		body.setPosition(position);

		when(ubApi.addOrientationUbNameOrientationPostWithHttpInfo(any(), any(), any(), any())).thenReturn(dummyResponse);

		dc.addOrientation(millerIndices, coords);
		verify(ubApi).addOrientationUbNameOrientationPostWithHttpInfo(body, calculationName, collectionName, null);

		dc.addOrientation(millerIndices, coords, tag);
		verify(ubApi).addOrientationUbNameOrientationPostWithHttpInfo(body, calculationName, collectionName, tag);
	}

	@Test
	void testAddOrientationFailsForWrongMillerIndices() {
		List<Double> millerIndices = Arrays.asList(2.0, 3.0);
		List<Double> coords= Arrays.asList(1.0, 2.0, 3.0);

		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> {
					dc.addOrientation(millerIndices, coords);
					}
				);

		assertEquals("provided hkl and coordinates must have exactly 3 values.", exception.getMessage());
	}

	//add some sort of scaling to this test... and others.
	@Test
	void testAddOrientationForFlippedCoords() throws ApiException, DeviceException {
		dc.setTransform(new double[][] { new double[] {0d, 0d, 1d}, new double[] {0d, 1d, 0d}, new double[] {1d, 0d, 0d}});

		ApiResponse<InfoResponse> dummyResponse = new ApiResponse<>(200, new HashMap<>(), new InfoResponse().message("dummy"));

		List<Double> millerIndices = Arrays.asList(1.0, 2.0, 3.0);
		List<Double> coords = Arrays.asList(1.0, 2.0, 3.0);

		AddOrientationParams body = new AddOrientationParams();

		HklModel hkl = new HklModel();
		hkl.setH(BigDecimal.valueOf(millerIndices.get(0)));
		hkl.setK(BigDecimal.valueOf(millerIndices.get(1)));
		hkl.setL(BigDecimal.valueOf(millerIndices.get(2)));

		XyzModel xyz = new XyzModel();
		xyz.setX(BigDecimal.valueOf(coords.get(2)));
		xyz.setY(BigDecimal.valueOf(coords.get(1)));
		xyz.setZ(BigDecimal.valueOf(coords.get(0)));

		PositionModel position = new PositionModel();
		position.setMu(BigDecimal.valueOf(0d));
		position.setDelta(BigDecimal.valueOf(0d));
		position.setNu(BigDecimal.valueOf(0d));
		position.setEta(BigDecimal.valueOf(10d));
		position.setChi(BigDecimal.valueOf(0d));
		position.setPhi(BigDecimal.valueOf(0d));

		body.setXyz(xyz);
		body.setHkl(hkl);
		body.setPosition(position);

		when(ubApi.addOrientationUbNameOrientationPostWithHttpInfo(any(), any(), any(), any())).thenReturn(dummyResponse);

		dc.addOrientation(millerIndices, coords);
		verify(ubApi).addOrientationUbNameOrientationPostWithHttpInfo(body, calculationName, collectionName, null);
	}

	@Test
	void testGetOrientationForFlippedCoords() throws ApiException {
		dc.setTransform(new double[][] { new double[] {0d, 0d, 1d}, new double[] {0d, 1d, 0d}, new double[] {1d, 0d, 0d}});

		OrientationResponse orientationResponse = new OrientationResponse();
		Orientation orientation = new Orientation();

		orientation.setX(BigDecimal.valueOf(1.0));
		orientation.setY(BigDecimal.valueOf(2.0));
		orientation.setZ(BigDecimal.valueOf(3.0));

		orientation.setPos(dc.rawBeamlinePositionToPositionModel(List.of(1.0, 2.0, 3.0, 4.0)));

		orientation.setH(BigDecimal.valueOf(1.0));
		orientation.setK(BigDecimal.valueOf(1.0));
		orientation.setL(BigDecimal.valueOf(1.0));

		orientationResponse.payload(orientation);

		ApiResponse<OrientationResponse> response = new ApiResponse<OrientationResponse>(200, new HashMap<>(), orientationResponse);

		when(ubApi.getOrientationUbNameOrientationGetWithHttpInfo(anyString(), anyString(), isNull(), anyInt())).thenReturn(response);
		when(ubApi.getOrientationUbNameOrientationGetWithHttpInfo(anyString(), anyString(), anyString(), isNull())).thenReturn(response);

		OrientationResult convertedOrientationIdx = dc.getOrientation(1);
		OrientationResult convertedOrientationTag = dc.getOrientation("some tag");

		assertEquals(convertedOrientationIdx.xyz().get(0), orientation.getZ().doubleValue());
		assertEquals(convertedOrientationIdx.xyz().get(1), orientation.getY().doubleValue());
		assertEquals(convertedOrientationIdx.xyz().get(2), orientation.getX().doubleValue());

		assertEquals(convertedOrientationTag.xyz().get(0), orientation.getZ().doubleValue());
		assertEquals(convertedOrientationTag.xyz().get(1), orientation.getY().doubleValue());
		assertEquals(convertedOrientationTag.xyz().get(2), orientation.getX().doubleValue());
	}

	@Test
	void testSetLabSurfaceNormal() throws ApiException {
		ApiResponse<InfoResponse> dummyResponse = new ApiResponse<>(200, new HashMap<>(), new InfoResponse().message("dummy"));
		when(ubApi.setLabSurfaceNormalUbNameSurfaceNphiPutWithHttpInfo(any(), any(), any())).thenReturn(dummyResponse);

		dc.setTransform(new double[][] { new double[] {0d, 0d, 1d}, new double[] {0d, 1d, 0d}, new double[] {1d, 0d, 0d}});

		List<Double> coords = Arrays.asList(1.0, 2.0, 3.0);

		XyzModel body = new XyzModel();
		body.setX(BigDecimal.valueOf(coords.get(2)));
		body.setY(BigDecimal.valueOf(coords.get(1)));
		body.setZ(BigDecimal.valueOf(coords.get(0)));

		dc.setLabSurfaceNormal(coords);
		verify(ubApi).setLabSurfaceNormalUbNameSurfaceNphiPutWithHttpInfo(body, calculationName, collectionName);
	}

	@Test
	void testGetLabSurfaceNormal() throws ApiException {
		dc.setTransform(new double[][] { new double[] {0d, 0d, 1d}, new double[] {0d, 1d, 0d}, new double[] {1d, 0d, 0d}});

		List<List<BigDecimal>> surfaceNormal = Arrays.asList(
				Arrays.asList(new BigDecimal(1.0)),
				Arrays.asList(new BigDecimal(2.0)),
				Arrays.asList(new BigDecimal(3.0))
		);

		ArrayResponse array = new ArrayResponse();
		array.payload(surfaceNormal);

		ApiResponse<ArrayResponse> response = new ApiResponse<ArrayResponse>(200, new HashMap<>(), array);

		when(ubApi.getLabSurfaceNormalUbNameSurfaceNphiGetWithHttpInfo(calculationName, collectionName)).thenReturn(response);

		List<Double> beamlineSurfaceNormal = dc.getLabSurfaceNormal();
		assertEquals(beamlineSurfaceNormal.get(0), surfaceNormal.get(2).get(0).doubleValue());
		assertEquals(beamlineSurfaceNormal.get(1), surfaceNormal.get(1).get(0).doubleValue());
		assertEquals(beamlineSurfaceNormal.get(2), surfaceNormal.get(0).get(0).doubleValue());
	}

	@Test
	void testSetLabReferenceVector() throws ApiException {
		ApiResponse<InfoResponse> dummyResponse = new ApiResponse<>(200, new HashMap<>(), new InfoResponse().message("dummy"));
		when(ubApi.setLabReferenceVectorUbNameNphiPutWithHttpInfo(any(), any(), any())).thenReturn(dummyResponse);

		dc.setTransform(new double[][] { new double[] {0d, 0d, 1d}, new double[] {0d, 1d, 0d}, new double[] {1d, 0d, 0d}});

		List<Double> coords = Arrays.asList(1.0, 2.0, 3.0);

		XyzModel body = new XyzModel();
		body.setX(BigDecimal.valueOf(coords.get(2)));
		body.setY(BigDecimal.valueOf(coords.get(1)));
		body.setZ(BigDecimal.valueOf(coords.get(0)));

		dc.setLabReferenceVector(coords);
		verify(ubApi).setLabReferenceVectorUbNameNphiPutWithHttpInfo(body, calculationName, collectionName);

	}

	@Test
	void testGetLabReferenceVector() throws ApiException {
		dc.setTransform(new double[][] { new double[] {0d, 0d, 1d}, new double[] {0d, 1d, 0d}, new double[] {1d, 0d, 0d}});

		List<List<BigDecimal>> referenceVector = Arrays.asList(
				Arrays.asList(new BigDecimal(1.0)),
				Arrays.asList(new BigDecimal(2.0)),
				Arrays.asList(new BigDecimal(3.0))
		);

		ArrayResponse array = new ArrayResponse().payload(referenceVector);

		ApiResponse<ArrayResponse> response = new ApiResponse<ArrayResponse>(200, new HashMap<>(), array);

		when(ubApi.getLabReferenceVectorUbNameNphiGetWithHttpInfo(calculationName, collectionName)).thenReturn(response);

		List<Double> beamlineVector = dc.getLabReferenceVector();
		assertEquals(beamlineVector.get(0), referenceVector.get(2).get(0).doubleValue());
		assertEquals(beamlineVector.get(1), referenceVector.get(1).get(0).doubleValue());
		assertEquals(beamlineVector.get(2), referenceVector.get(0).get(0).doubleValue());
	}

	@Test
	void testSetLattice() throws ApiException {
		SetLatticeParams body = new SetLatticeParams();

		ApiResponse<InfoResponse> dummyResponse = new ApiResponse<>(200, new HashMap<>(), new InfoResponse().message("dummy"));

		body.setName("test");
		body.setA(BigDecimal.valueOf(1.1));
		body.setB(BigDecimal.valueOf(2.2));
		body.setC(BigDecimal.valueOf(3.0));
		body.setAlpha(BigDecimal.valueOf(90.0));
		body.setBeta(BigDecimal.valueOf(90.0));
		body.setGamma(BigDecimal.valueOf(90.0));

		when(ubApi.setLatticeUbNameLatticePatchWithHttpInfo(any(), any(), any())).thenReturn(dummyResponse);

		dc.setLattice(
				body.getName(),
				body.getA().doubleValue(),
				body.getB().doubleValue(),
				body.getC().doubleValue(),
				body.getAlpha().doubleValue(),
				body.getBeta().doubleValue(),
				body.getGamma().doubleValue()
		);

		verify(ubApi).setLatticeUbNameLatticePatchWithHttpInfo(body, calculationName, collectionName);

		body.setSystem("cubic");
		dc.setLattice(
				body.getName(),
				body.getSystem(),
				body.getA().doubleValue(),
				body.getB().doubleValue(),
				body.getC().doubleValue(),
				body.getAlpha().doubleValue(),
				body.getBeta().doubleValue(),
				body.getGamma().doubleValue()
		);
		verify(ubApi).setLatticeUbNameLatticePatchWithHttpInfo(body, calculationName, collectionName);
	}

	@Test
	void testSetMiscut() throws ApiException {
		ApiResponse<InfoResponse> dummyResponse = new ApiResponse<>(200, new HashMap<>(), new InfoResponse().message("dummy"));
		when(ubApi.setMiscutUbNameMiscutPutWithHttpInfo(any(), any(), any(), any(), any())).thenReturn(dummyResponse);

		dc.setTransform(new double[][] { new double[] {0d, 0d, 1d}, new double[] {0d, 1d, 0d}, new double[] {1d, 0d, 0d}});

		List<Double> beamlineAxis = Arrays.asList(1.0, 2.0, 3.0);
		Double angle = 45.0;

		XyzModel referenceAxis = new XyzModel();

		referenceAxis.setX(BigDecimal.valueOf(beamlineAxis.get(2)));
		referenceAxis.setY(BigDecimal.valueOf(beamlineAxis.get(1)));
		referenceAxis.setZ(BigDecimal.valueOf(beamlineAxis.get(0)));

		dc.setMiscut(angle, beamlineAxis);
		verify(ubApi).setMiscutUbNameMiscutPutWithHttpInfo(referenceAxis, BigDecimal.valueOf(angle), calculationName, true, collectionName);
	}

	@Test
	void testGetMiscut() throws ApiException {
		dc.setTransform(new double[][] { new double[] {0d, 0d, 1d}, new double[] {0d, 1d, 0d}, new double[] {1d, 0d, 0d}});
		double x = 3.0;
		double y = 2.0;
		double z = 1.0;

		XyzModel reference = new XyzModel();
		reference.setX(BigDecimal.valueOf(x));
		reference.setY(BigDecimal.valueOf(y));
		reference.setZ(BigDecimal.valueOf(z));

		MiscutModel miscutData = new MiscutModel().angle(BigDecimal.valueOf(10.0)).rotationAxis(reference);
		ApiResponse<MiscutResponse> response = new ApiResponse<MiscutResponse>(200, new HashMap<>(), new MiscutResponse().payload(miscutData));

		when(ubApi.getMiscutUbNameMiscutGetWithHttpInfo(calculationName, collectionName)).thenReturn(response);

		miscutData = dc.getMiscut();

		XyzModel beamline = miscutData.getRotationAxis();

		assertEquals(beamline.getX().doubleValue(), z);
		assertEquals(beamline.getY().doubleValue(), y);
		assertEquals(beamline.getZ().doubleValue(), x);
	}

	@Test
	void testGetMiscutFromHkl() throws ApiException {
		dc.setTransform(new double[][] { new double[] {0d, 0d, 1d}, new double[] {0d, 1d, 0d}, new double[] {1d, 0d, 0d}});

		List<Double> beamlineCoords = Arrays.asList(1.0, 2.0, 3.0, 4.0);
		List<Double> millerIndices = Arrays.asList(1.0, 1.0, 0.0);

		HklModel hkl = TypeConversion.millerIndicesToHklModel(millerIndices);
		PositionModel position = dc.rawBeamlinePositionToPositionModel(beamlineCoords);

		XyzModel reference = new XyzModel();
		reference.setX(BigDecimal.valueOf(1.0));
		reference.setY(BigDecimal.valueOf(2.0));
		reference.setZ(BigDecimal.valueOf(3.0));

		MiscutModel miscutData = new MiscutModel().angle(BigDecimal.valueOf(10.0)).rotationAxis(reference);
		ApiResponse<MiscutResponse> response = new ApiResponse<MiscutResponse>(200, new HashMap<>(), new MiscutResponse().payload(miscutData));

		when(ubApi.getMiscutFromHklUbNameMiscutHklGetWithHttpInfo(
				calculationName,
				hkl.getH(),
				hkl.getK(),
				hkl.getL(),
				position.getMu(),
				position.getDelta(),
				position.getNu(),
				position.getEta(),
				position.getChi(),
				position.getPhi(),
				collectionName)).thenReturn(response);

		miscutData = dc.getMiscutFromHkl(millerIndices, beamlineCoords);
		XyzModel beamline = miscutData.getRotationAxis();

		assertEquals(beamline.getX(), reference.getZ());
		assertEquals(beamline.getY(), reference.getY());
		assertEquals(beamline.getZ(), reference.getX());
	}

	@Test
	void testGetHklPosition() throws DeviceException, ApiException {
		Map<String, Double> motorPositions = Map.of("motor1", 10.0, "motor2", 2.0, "motor3", 3.0, "motor4", 40.0);
		List<Scannable> scannableList = createScannablesFromMap(motorPositions);

		PositionModel pos = dc.beamlinePositionToPositionModel(motorPositions);
		HklModel responseHkl = new HklModel();
		responseHkl.setH(BigDecimal.valueOf(0.0));
		responseHkl.setK(BigDecimal.valueOf(0.0));
		responseHkl.setL(BigDecimal.valueOf(0.0));

		ApiResponse<ReciprocalSpaceResponse> response = new ApiResponse<>(200, new HashMap<>(), new ReciprocalSpaceResponse().payload(responseHkl));

		when(diffractometer.getGroupMembers()).thenReturn(scannableList);
		when(hklApi.millerIndicesFromLabPositionHklNamePositionHklGetWithHttpInfo(
				calculationName,
				BigDecimal.valueOf(12.39842 / energyValue),
				pos.getMu(),
				pos.getDelta(),
				pos.getNu(),
				pos.getEta(),
				pos.getChi(),
				pos.getPhi(),
				collectionName)).thenReturn(response);

		dc.getHklPosition();
		verify(hklApi).millerIndicesFromLabPositionHklNamePositionHklGetWithHttpInfo(
				calculationName,
				BigDecimal.valueOf(12.39842 / energyValue),
				pos.getMu(),
				pos.getDelta(),
				pos.getNu(),
				pos.getEta(),
				pos.getChi(),
				pos.getPhi(),
				collectionName);
	}

	@Test
	void testChooseClosestSolution() throws DeviceException {
		Map<String, Double> motorPositions = Map.of("motor1", 0.0, "motor2", 0.0, "motor3", 0.0, "motor4", 0.0);
		List<Scannable> scannableList = createScannablesFromMap(motorPositions);

		when(diffractometer.getGroupMembers()).thenReturn(scannableList);

		Map<String, Double> positionOne = Map.of("motor1", 10.0, "motor2", 20.0, "motor3", 30.0, "motor4", 40.0);
		Map<String, Double> positionTwo = Map.of("motor1", 10.0, "motor2", 360+20.0, "motor3", -30.0, "motor4", -360.0 + 20.0);

		Map<String, Double> closestPosition = dc.chooseClosestSolution(Arrays.asList(positionOne, positionTwo));


		assertEquals(closestPosition, positionTwo);
	}

	@Test
	void testMoveToHkl() throws DeviceException, ApiException {
		Map<String, Double> motorPositions = Map.of("motor1", 0.0, "motor2", 0.0, "motor3", 0.0, "motor4", 0.0);
		List<Scannable> scannableList = createScannablesFromMap(motorPositions);

		when(diffractometer.getGroupMembers()).thenReturn(scannableList);

		DiffractorAnglesResponse data = new DiffractorAnglesResponse();

		Map<String, BigDecimal> positionOne = makePosition(Arrays.asList(0.0, 0.0, 0.0, 10.0, 0.0, 0.0));
		Map<String, BigDecimal> positionTwo = makePosition(Arrays.asList(0.0, -300.0, 0.0, 10.0, 0.0, 0.0));

		data.addPayloadItem(positionOne).addPayloadItem(positionTwo);

		ApiResponse<DiffractorAnglesResponse> response = new ApiResponse<>(200, new HashMap<>(), data);

		when(
			hklApi.labPositionFromMillerIndicesHklNamePositionLabGetWithHttpInfo(
				any(), any(), any(), any(), any(), any(), any(), any(), any()
			)
		).thenReturn(response);

		List<Double> beamlinePositionOne = Arrays.asList(0.0, 0.0, 0.0, 0.0);
		List<Double> beamlinePositionTwo = Arrays.asList(60.0, 0.0, 0.0, 0.0);

		//Test for two valid solutions,
		dc.moveToHkl(1.0, 2.0, 3.0);
		verify(diffractometer).asynchronousMoveTo(beamlinePositionOne);

		//Test for one valid solution,

		when(diffractometer.checkPositionValid(beamlinePositionOne)).thenReturn("reason for invalidity");
		dc.moveToHkl(1.0, 2.0, 3.0);
		verify(diffractometer).asynchronousMoveTo(beamlinePositionTwo);

		//Test for no valid solutions,
		when(diffractometer.checkPositionValid(beamlinePositionTwo)).thenReturn("reason for invalidity");

		DeviceException exception = assertThrows(DeviceException.class, () -> dc.moveToHkl(1.0, 2.0, 3.0));

		String expectedMessage = "No solutions found for this hkl";
		String actualMessage = exception.getMessage();

		assertEquals(expectedMessage, actualMessage);
	}

	private Map<String, BigDecimal> makePosition(List<Double> pos) {
		return Map.of(
				"mu", BigDecimal.valueOf(pos.get(0)),
				"delta", BigDecimal.valueOf(pos.get(1)),
				"nu", BigDecimal.valueOf(pos.get(2)),
				"eta", BigDecimal.valueOf(pos.get(3)),
				"chi", BigDecimal.valueOf(pos.get(4)),
				"phi", BigDecimal.valueOf(pos.get(5))
				);
	}

	private List<Scannable> createScannablesFromMap(Map<String, Double> map) throws DeviceException {
		List<Scannable> scannableList = new ArrayList<>();

		for(Entry<String, Double> motorPos : map.entrySet()) {
			Scannable motor = createMockScannable(motorPos.getKey(), motorPos.getValue());
			scannableList.add(motor);
		}

		return scannableList;
	}

	private Scannable createMockScannable(String name, double position) throws DeviceException {
		Scannable motor = mock(Scannable.class);
		when(motor.getName()).thenReturn(name);
		when(motor.getPosition()).thenReturn(position);

		return motor;
	}

}
