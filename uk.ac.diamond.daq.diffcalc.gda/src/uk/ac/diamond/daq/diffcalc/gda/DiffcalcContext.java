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

import static gda.configuration.properties.LocalProperties.GDA_BEAMLINE_NAME;
import static java.util.stream.Collectors.toMap;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.ejml.simple.SimpleMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Doubles;
import com.google.gson.Gson;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.scannablegroup.ScannableGroup;
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
import uk.ac.diamond.daq.diffcalc.model.RefineUbParams;
import uk.ac.diamond.daq.diffcalc.model.Reflection;
import uk.ac.diamond.daq.diffcalc.model.ReflectionResponse;
import uk.ac.diamond.daq.diffcalc.model.SetLatticeParams;
import uk.ac.diamond.daq.diffcalc.model.SphericalCoordinates;
import uk.ac.diamond.daq.diffcalc.model.SphericalResponse;
import uk.ac.diamond.daq.diffcalc.model.StringResponse;
import uk.ac.diamond.daq.diffcalc.model.XyzModel;

public final class DiffcalcContext {

	private static final double HC_ANGSTROM_KEV = 12.39842;

	private static final Logger logger = LoggerFactory.getLogger(DiffcalcContext.class);

	private ApiClient client = (new ApiClient()).setBasePath(LocalProperties.get("gda.diffcalc.url"));
	private ConstraintsApi constraintsApi = new ConstraintsApi(client);
	private DefaultApi storeApi = new DefaultApi(client);
	private UbApi ubApi = new UbApi(client);
	private HklApi hklApi = new HklApi(client);

	private AngleTransform angleTransform;
	private AngleShift angleShift = new AngleShift();
	private AxesTransform axesTransform = new AxesTransform(SimpleMatrix.identity(3));

	private ScannableGroup diffractometer;

	private Scannable energy;
	private Double energyMultiplier = 1d;

	private String collectionName = LocalProperties.get(GDA_BEAMLINE_NAME);
	private String calculationName = "STO";

	public DiffcalcContext(ApiClient client, ConstraintsApi constraintRoutes, DefaultApi defaultRoutes, UbApi ubRoutes, HklApi hklRoutes) {
		this.client = client;
		this.constraintsApi = constraintRoutes;
		this.storeApi = defaultRoutes;
		this.ubApi = ubRoutes;
		this.hklApi = hklRoutes;
	}

	public DiffcalcContext() {
	}

	private double safeGetPosition(Scannable scannable) {
		try {
			return (double) scannable.getPosition();
		} catch (DeviceException e) {
			throw new IllegalStateException("cannot get position of scannable " + scannable.getName());
		}

	}

	private List<Double> getDiffractometerPositionAsList() throws DeviceException {
		Object[] position = (Object[]) diffractometer.getPosition();
		List<Double> convertedPosition = new ArrayList<>();

		for (int i=0; i < position.length; i++) {
			convertedPosition.add((Double) position[i]);
		}

		return convertedPosition;
	}

	/*
	 * Methods for easier integration with mapping
	 */

	/**
	 * Utility function which converts a raw beamline position from a list of doubles to a map, specifying
	 * the values of each motor of the diffractometer. This is useful because mapping functions which convert
	 * between beamline and reference frames use spring configurations specifying mapping for named motors,
	 * not indices.
	 *
	 * @param rawBeamlinePosition A list of motor positions
	 * @return A mapping between motor names and their respective positions
	 */
	public Map<String, Double> rawBeamlinePositionToBeamlinePosition(List<Double> rawBeamlinePosition) {
		List<String> scannableNames = Arrays.asList(diffractometer.getGroupMemberNames());
		return IntStream.range(0, scannableNames.size()).boxed().collect(Collectors.toMap(scannableNames::get, rawBeamlinePosition::get));
	}

	/**
	 * Utility function which converts a raw beamline position to a position model, required for API requests.
	 * Raw meaning unmapped to a specific motor. The order of the positions are assumed to be in the same
	 * order as the input names of the diffractometer.
	 *
	 * @param rawBeamlinePosition a list of diffractometer motor positions, in order of the input names.
	 * @return An object representing diffractometer positions which can be directly passed into the API for requests.
	 */
	public PositionModel rawBeamlinePositionToPositionModel(List<Double> rawBeamlinePosition) {
		Map<String, Double> beamlinePosition = rawBeamlinePositionToBeamlinePosition(rawBeamlinePosition);
		return beamlinePositionToPositionModel(beamlinePosition);
	}

	/**
	 * Utility function which converts a beamline position to a position model, required for API requests.
	 * The beamline position is a mapping between motor names and their respective positions.
	 *
	 * @param beamlinePosition mapping between motors and positions for the diffractometer.
	 * @return An object representing diffractometer positions which can be directly passed into the API for requests.
	 */
	public PositionModel beamlinePositionToPositionModel(Map<String, Double> beamlinePosition) {
		if (beamlinePosition.keySet().size() != diffractometer.getGroupMemberNames().length) {
			throw new IllegalArgumentException("Incorrect number of motors given in the beamline position");
		}
		Map<ReferenceGeometry, Double> referencePosition = angleTransform.getReferenceGeometry(beamlinePosition);

		PositionModel position = new PositionModel();

		position.setMu(BigDecimal.valueOf(referencePosition.get(ReferenceGeometry.MU)));
		position.setDelta(BigDecimal.valueOf(referencePosition.get(ReferenceGeometry.DELTA)));
		position.setNu(BigDecimal.valueOf(referencePosition.get(ReferenceGeometry.NU)));
		position.setEta(BigDecimal.valueOf(referencePosition.get(ReferenceGeometry.ETA)));
		position.setChi(BigDecimal.valueOf(referencePosition.get(ReferenceGeometry.CHI)));
		position.setPhi(BigDecimal.valueOf(referencePosition.get(ReferenceGeometry.PHI)));
		return position;
	}

	public List<Double> positionModelToRawBeamlinePosition(PositionModel position) {
		List<BigDecimal> posValues = List.of(
				position.getMu(), position.getDelta(), position.getNu(), position.getEta(), position.getChi(), position.getPhi()
				);
		List<ReferenceGeometry> angleNames = Arrays.asList(ReferenceGeometry.values());

		Map<ReferenceGeometry, Double> referenceGeometry = IntStream.range(0, angleNames.size()).boxed().collect(Collectors.toMap(angleNames::get, index -> posValues.get(index).doubleValue()));
		Map<String, Double> beamlineGeometry = angleTransform.getBeamlineGeometry(referenceGeometry);

		return List.of(diffractometer.getGroupMemberNames()).stream().map(beamlineGeometry::get).toList();
	}

	/*
	 * REFLECTIONS
	 */

	/**
	 * Helper function to generate reflection parameters, which are then passed into the API requests for adding reflections.
	 *
	 * @param energy in keV by default, however the energyMultiplier parameter can be set to change the units of this energy value.
	 * @param millerIndices the [h, k, l] at which this reflection is seen
	 * @param rawBeamlinePosition the motor positions (in order of diffractometer scannable group input names) at which this reflection is seen.
	 * @return reflection parameters to be used for API requests regarding adding reflections.
	 */
	private AddReflectionParams generateAddReflectionParams(Double energy, List<Double> millerIndices, List<Double> rawBeamlinePosition) {
		AddReflectionParams body = new AddReflectionParams();
		HklModel hkl = TypeConversion.millerIndicesToHklModel(millerIndices);
		PositionModel position = rawBeamlinePositionToPositionModel(rawBeamlinePosition);

		body.setEnergy(BigDecimal.valueOf(energy));
		body.setHkl(hkl);
		body.setPosition(position);

		return body;
	}



	public int addReflection(Double energy, List<Double> millerIndices, List<Double> beamlinePosition) {
		AddReflectionParams body = generateAddReflectionParams(energy, millerIndices, beamlinePosition);
		ApiResponse<InfoResponse> response = null;
		String message;

		try {
			response = ubApi.addReflectionUbNameReflectionPostWithHttpInfo(body, calculationName, collectionName, null);
			message = response.getData().getMessage();
			logger.info(message);
		}
		catch (ApiException e) {
			message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
		}

		InterfaceProvider.getTerminalPrinter().print(message);
		return Objects.isNull(response)? 0: response.getStatusCode();

	}

	public int addReflection(Double energy, List<Double> millerIndices, List<Double> beamlinePosition, String tag) {
		AddReflectionParams body = generateAddReflectionParams(energy, millerIndices, beamlinePosition);
		ApiResponse<InfoResponse> response = null;
		String message;

		try {
			response = ubApi.addReflectionUbNameReflectionPostWithHttpInfo(body, calculationName, collectionName, tag);
			message = response.getData().getMessage();
			logger.info(message);
		} catch (ApiException e) {
			message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
		}

		InterfaceProvider.getTerminalPrinter().print(message);
		return Objects.isNull(response)? 0: response.getStatusCode();
	}

	public ReflectionResult getReflection(Reflection reflection) {
		List<Double> beamlinePosition = positionModelToRawBeamlinePosition(reflection.getPos());

		return new ReflectionResult(
				reflection.getEnergy().doubleValue(),
				List.of(reflection.getH().doubleValue(), reflection.getK().doubleValue(), reflection.getL().doubleValue()),
				beamlinePosition,
				reflection.getTag()
		);
	}

	public ReflectionResult getReflection(String tag) {
		ApiResponse<ReflectionResponse> response = null;

		try {
			response = ubApi.getReflectionUbNameReflectionGetWithHttpInfo(calculationName, collectionName, tag, null);
		} catch (ApiException e) {
			String message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
			InterfaceProvider.getTerminalPrinter().print(message);
			return null;
		}

		return getReflection(response.getData().getPayload());

	}

	public ReflectionResult getReflection(Integer idx) {
		ApiResponse<ReflectionResponse> response = null;

		try {
			response = ubApi.getReflectionUbNameReflectionGetWithHttpInfo(calculationName, collectionName, null, idx);
		} catch (ApiException e) {
			String message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
			InterfaceProvider.getTerminalPrinter().print(message);
			return null;
		}

		return getReflection(response.getData().getPayload());
	}

	public int deleteReflection(String tag) {
		ApiResponse<InfoResponse> response = null;
		String message;

		try {
			response = ubApi.deleteReflectionUbNameReflectionDeleteWithHttpInfo(calculationName, collectionName, tag, null);
			message = response.getData().getMessage();
			logger.info(message);
		} catch (ApiException e) {
			message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
		}

		InterfaceProvider.getTerminalPrinter().print(message);
		return Objects.isNull(response)? 0: response.getStatusCode();
	}

	public int deleteReflection(Integer idx) {
		ApiResponse<InfoResponse> response = null;
		String message;

		try {
			response = ubApi.deleteReflectionUbNameReflectionDeleteWithHttpInfo(calculationName, collectionName, null, idx);
			message = response.getData().getMessage();
			logger.info(message);
		} catch (ApiException e) {
			message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
		}

		InterfaceProvider.getTerminalPrinter().print(message);
		return Objects.isNull(response)? 0: response.getStatusCode();
	}

	/*
	 * ORIENTATIONS
	 */

	private AddOrientationParams generateAddOrientationParams(List<Double> millerIndices, List<Double> coords) {

		if ((millerIndices.size() != 3) || (coords.size() != 3)) {
			throw new IllegalArgumentException("provided hkl and coordinates must have exactly 3 values.");
		}

		HklModel hkl = TypeConversion.millerIndicesToHklModel(millerIndices);

		List<List<Double>> referenceVector = axesTransform.beamlineToReferenceColumnVector(
				Maths.columnVectorFromCoordsList(coords)
		);
		XyzModel xyz = TypeConversion.coordsToXyzModel(Maths.coordsListFromColumnVector(referenceVector));

		AddOrientationParams body = new AddOrientationParams();
		body.setHkl(hkl);
		body.setXyz(xyz);

		return body;
	}

	private AddOrientationParams generateAddOrientationParams(List<Double> millerIndices, List<Double> coords, List<Double> beamlinePosition) {
		AddOrientationParams body = generateAddOrientationParams(millerIndices, coords);

		PositionModel position = rawBeamlinePositionToPositionModel(beamlinePosition);
		body.setPosition(position);
		return body;
	}

	public int addOrientation(List<Double> millerIndices, List<Double> coords) throws DeviceException {
		AddOrientationParams body = generateAddOrientationParams(millerIndices, coords, getDiffractometerPositionAsList());
		ApiResponse<InfoResponse> response = null;
		String message;

		try {
			response = ubApi.addOrientationUbNameOrientationPostWithHttpInfo(body, calculationName, collectionName, null);
			message = response.getData().getMessage();
			logger.info(message);
		} catch (ApiException e) {
			message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
		}

		InterfaceProvider.getTerminalPrinter().print(message);
		return Objects.isNull(response)? 0: response.getStatusCode();
	}

	public int addOrientation(List<Double> millerIndices, List<Double> coords, String tag) throws DeviceException {
		AddOrientationParams body = generateAddOrientationParams(millerIndices, coords, getDiffractometerPositionAsList());
		ApiResponse<InfoResponse> response = null;
		String message;

		try {
			response = ubApi.addOrientationUbNameOrientationPostWithHttpInfo(body, calculationName, collectionName, tag);
			message = response.getData().getMessage();
			logger.info(message);
		} catch (ApiException e) {
			message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
		}

		InterfaceProvider.getTerminalPrinter().print(message);
		return Objects.isNull(response)? 0: response.getStatusCode();
	}

	public int addOrientation(List<Double> millerIndices, List<Double> coords, List<Double> beamlinePosition) {
		AddOrientationParams body = generateAddOrientationParams(millerIndices, coords, beamlinePosition);
		ApiResponse<InfoResponse> response = null;
		String message;

		try {
			response = ubApi.addOrientationUbNameOrientationPostWithHttpInfo(body, calculationName, collectionName, null);
			message = response.getData().getMessage();
			logger.info(message);
		} catch (ApiException e) {
			message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
		}

		InterfaceProvider.getTerminalPrinter().print(message);
		return Objects.isNull(response)? 0: response.getStatusCode();
	}

	public int addOrientation(List<Double> millerIndices, List<Double> coords, List<Double> beamlinePosition, String tag) {
		AddOrientationParams body = generateAddOrientationParams(millerIndices, coords, beamlinePosition);
		ApiResponse<InfoResponse> response = null;
		String message;

		try {
			response = ubApi.addOrientationUbNameOrientationPostWithHttpInfo(body, calculationName, collectionName, tag);
			message = response.getData().getMessage();
			logger.info(message);
		} catch (ApiException e) {
			message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
		}

		InterfaceProvider.getTerminalPrinter().print(message);
		return Objects.isNull(response)? 0: response.getStatusCode();
	}

	private OrientationResult getOrientation(Orientation orient) {
		List<Double> beamlinePosition = positionModelToRawBeamlinePosition(orient.getPos());

		List<Double> referenceXyz = Arrays.asList(orient.getX().doubleValue(), orient.getY().doubleValue(), orient.getZ().doubleValue());
		List<Double> beamlineXyz = Maths.coordsListFromColumnVector(
				axesTransform.referenceToBeamlineColumnVector(
						Maths.columnVectorFromCoordsList(referenceXyz)
				)
		);
		return new OrientationResult(
				List.of(orient.getH().doubleValue(), orient.getK().doubleValue(), orient.getL().doubleValue()),
				beamlineXyz,
				beamlinePosition,
				orient.getTag()
		);
	}


	public OrientationResult getOrientation(String tag) {
		ApiResponse<OrientationResponse> response = null;

		try {
			response = ubApi.getOrientationUbNameOrientationGetWithHttpInfo(calculationName, collectionName, tag, null);
		} catch (ApiException e) {
			String message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
			InterfaceProvider.getTerminalPrinter().print(message);
			return null;
		}

		return getOrientation(response.getData().getPayload());
	}

	public OrientationResult getOrientation(Integer idx) {
		ApiResponse<OrientationResponse> response = null;

		try {
			response = ubApi.getOrientationUbNameOrientationGetWithHttpInfo(calculationName, collectionName, null, idx);
		} catch (ApiException e) {
			String message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
			InterfaceProvider.getTerminalPrinter().print(message);
			return null;
		}

		return getOrientation(response.getData().getPayload());
	}


	public int deleteOrientation(String tag) {
		ApiResponse<InfoResponse> response = null;
		String message;

		try {
			response = ubApi.deleteOrientationUbNameOrientationDeleteWithHttpInfo(calculationName, collectionName, tag, null);
			message = response.getData().getMessage();
			logger.info(message);
		} catch (ApiException e) {
			message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
		}

		InterfaceProvider.getTerminalPrinter().print(message);
		return Objects.isNull(response)? 0: response.getStatusCode();
	}

	public int deleteOrientation(Integer idx) {
		ApiResponse<InfoResponse> response = null;
		String message;

		try {
			response = ubApi.deleteOrientationUbNameOrientationDeleteWithHttpInfo(calculationName, collectionName, null, idx);
			message = response.getData().getMessage();
			logger.info(message);
		} catch (ApiException e) {
			message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
		}

		InterfaceProvider.getTerminalPrinter().print(message);
		return Objects.isNull(response)? 0: response.getStatusCode();
	}

	/*
	 * Surface normals and reference vectors
	 */

	public int setLabSurfaceNormal(List<Double> coords) {
		XyzModel body = TypeConversion.coordsToXyzModel(
				Maths.coordsListFromColumnVector(
						axesTransform.beamlineToReferenceColumnVector(
								Maths.columnVectorFromCoordsList(coords)
						)
				)
		);
		ApiResponse<InfoResponse> response = null;
		String message;

		try {
			response = ubApi.setLabSurfaceNormalUbNameSurfaceNphiPutWithHttpInfo(body, calculationName, collectionName);
			message = response.getData().getMessage();
			logger.info(message);
		}
		catch (ApiException e) {
			message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
		}

		InterfaceProvider.getTerminalPrinter().print(message);
		return Objects.isNull(response)? 0: response.getStatusCode();
	}

	public List<Double> getLabSurfaceNormal() {
		ApiResponse<ArrayResponse> response = null;
		String message;

		try {
			response = ubApi.getLabSurfaceNormalUbNameSurfaceNphiGetWithHttpInfo(calculationName, collectionName);
		}
		catch (ApiException e) {
			message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
			return Collections.emptyList();
		}

		SimpleMatrix surfaceNormalMatrix = Maths.listOfListsToSimpleMatrix(
				TypeConversion.bigDecimalArrayToDoubleArray(
					response.getData().getPayload()
				)
			);

		return Maths.coordsListFromColumnVector(
				axesTransform.referenceToBeamlineColumnVector(surfaceNormalMatrix)
		);
	}

	public int setMillerSurfaceNormal(List<Double> millerIndices) {
		HklModel hkl = TypeConversion.millerIndicesToHklModel(millerIndices);
		ApiResponse<InfoResponse> response = null;
		String message;

		try {
			response = ubApi.setMillerSurfaceNormalUbNameSurfaceNhklPutWithHttpInfo(hkl, calculationName, collectionName);
			message = response.getData().getMessage();
			logger.info(message);
		}
		catch (ApiException e) {
			message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
		}

		InterfaceProvider.getTerminalPrinter().print(message);
		return Objects.isNull(response)? 0: response.getStatusCode();
	}

	public List<Double> getMillerSurfaceNormal() {
		ApiResponse<ArrayResponse> response = null;
		String message;

		try {
			response = ubApi.getMillerSurfaceNormalUbNameSurfaceNhklGetWithHttpInfo(calculationName, collectionName);
		}
		catch (ApiException e) {
			message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
			InterfaceProvider.getTerminalPrinter().print(message);
			return Collections.emptyList();
		}
		return Maths.coordsListFromColumnVector(
				TypeConversion.bigDecimalArrayToDoubleArray(
						response.getData().getPayload()
				)
		);
	}

	public int setLabReferenceVector(List<Double> coords) {
		XyzModel body = TypeConversion.coordsToXyzModel(
				Maths.coordsListFromColumnVector(
						axesTransform.beamlineToReferenceColumnVector(
								Maths.columnVectorFromCoordsList(coords)
						)
				)
		);
		ApiResponse<InfoResponse> response = null;
		String message;

		try {
			response = ubApi.setLabReferenceVectorUbNameNphiPutWithHttpInfo(body, calculationName, collectionName);
			message = response.getData().getMessage();
			logger.info(message);
		} catch (ApiException e) {
			message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
		}

		InterfaceProvider.getTerminalPrinter().print(message);
		return Objects.isNull(response)? 0: response.getStatusCode();
	}

	public List<Double> getLabReferenceVector() {
		ApiResponse<ArrayResponse> response = null;

		try {
			response = ubApi.getLabReferenceVectorUbNameNphiGetWithHttpInfo(calculationName, collectionName);
		} catch (ApiException e) {
			String message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
			InterfaceProvider.getTerminalPrinter().print(message);
			return Collections.emptyList();
		}
		SimpleMatrix surfaceNormalMatrix = Maths.listOfListsToSimpleMatrix(
				TypeConversion.bigDecimalArrayToDoubleArray(
					response.getData().getPayload()
				)
			);

		return Maths.coordsListFromColumnVector(
				axesTransform.referenceToBeamlineColumnVector(surfaceNormalMatrix)
		);
	}

	public int setMillerReferenceVector(List<Double> millerIndices) {
		HklModel hkl = TypeConversion.millerIndicesToHklModel(millerIndices);
		ApiResponse<InfoResponse> response = null;
		String message;

		try {
			response = ubApi.setMillerReferenceVectorUbNameNhklPutWithHttpInfo(hkl, calculationName, collectionName);
			message = response.getData().getMessage();
			logger.info(message);
		} catch (ApiException e) {
			message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
		}

		InterfaceProvider.getTerminalPrinter().print(message);
		return Objects.isNull(response)? 0: response.getStatusCode();
	}

	public List<Double> getMillerReferenceVector() {
		ApiResponse<ArrayResponse> response = null;

		try {
			response = ubApi.getMillerReferenceVectorUbNameNhklGetWithHttpInfo(calculationName, collectionName);
		} catch (ApiException e) {
			String message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
			InterfaceProvider.getTerminalPrinter().print(message);
			return Collections.emptyList();
		}
		return Maths.coordsListFromColumnVector(TypeConversion.bigDecimalArrayToDoubleArray(response.getData().getPayload()));
	}

	/*
	 * LATTICE
	 */

	private SetLatticeParams generateSetLatticeParams(String name, Double a, Double b, Double c, Double alpha, Double beta, Double gamma) {
		SetLatticeParams body = new SetLatticeParams();

		body.setName(name);
		body.setA(BigDecimal.valueOf(a));
		body.setB(BigDecimal.valueOf(b));
		body.setC(BigDecimal.valueOf(c));
		body.setAlpha(BigDecimal.valueOf(alpha));
		body.setBeta(BigDecimal.valueOf(beta));
		body.setGamma(BigDecimal.valueOf(gamma));

		return body;
	}

	private SetLatticeParams generateSetLatticeParams(String name, String system, Double a, Double b, Double c, Double alpha, Double beta, Double gamma) {
		SetLatticeParams body = generateSetLatticeParams(name, a, b, c, alpha, beta, gamma);
		body.setSystem(system);

		return body;
	}

	public int setLattice(String name, Double a, Double b, Double c, Double alpha, Double beta, Double gamma) {
		SetLatticeParams body = generateSetLatticeParams(name, a, b, c, alpha, beta, gamma);

		ApiResponse<InfoResponse> response = null;
		String message;
		try {
			response = ubApi.setLatticeUbNameLatticePatchWithHttpInfo(body, calculationName, collectionName);
			message = response.getData().getMessage();
			logger.info(message);
		} catch (ApiException e) {
			message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
		}

		InterfaceProvider.getTerminalPrinter().print(message);
		return Objects.isNull(response)? 0: response.getStatusCode();
	}

	public int setLattice(String name, String system, Double a, Double b, Double c, Double alpha, Double beta, Double gamma) {
		SetLatticeParams body = generateSetLatticeParams(name, system, a, b, c, alpha, beta, gamma);

		ApiResponse<InfoResponse> response = null;
		String message;
		try {
			response = ubApi.setLatticeUbNameLatticePatchWithHttpInfo(body, calculationName, collectionName);
			message = response.getData().getMessage();
			logger.info(message);
		} catch (ApiException e) {
			message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
		}

		InterfaceProvider.getTerminalPrinter().print(message);
		return Objects.isNull(response)? 0: response.getStatusCode();
	}

	/*
	 * MISCUTS
	 */

	public int setMiscut(Double angle, List<Double> axis) {
		XyzModel rotationAxis = TypeConversion.coordsToXyzModel(
				Maths.coordsListFromColumnVector(
						axesTransform.beamlineToReferenceColumnVector(
								Maths.columnVectorFromCoordsList(axis)
						)
				)
		);

		ApiResponse<InfoResponse> response = null;
		String message;

		try {
			response = ubApi.setMiscutUbNameMiscutPutWithHttpInfo(rotationAxis, BigDecimal.valueOf(angle), calculationName, true, collectionName);
			message = response.getData().getMessage();
			logger.info(message);
		} catch (ApiException e) {
			message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
		}

		InterfaceProvider.getTerminalPrinter().print(message);
		return Objects.isNull(response)? 0: response.getStatusCode();
	}

	public MiscutModel getMiscut() {
		ApiResponse<MiscutResponse> response = null;
		try {
			response = ubApi.getMiscutUbNameMiscutGetWithHttpInfo(calculationName, collectionName);
		} catch (ApiException e) {
			String message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
			InterfaceProvider.getTerminalPrinter().print(message);
			return new MiscutModel();
		}

		MiscutModel miscutData = response.getData().getPayload();

		XyzModel beamlineXyz = TypeConversion.coordsToXyzModel(
				Maths.coordsListFromColumnVector(
						axesTransform.referenceToBeamlineColumnVector(
								Maths.columnVectorFromCoordsList(
										TypeConversion.xyzModelToCoords(miscutData.getRotationAxis())
								)
						)
				)
		);
		miscutData.setRotationAxis(beamlineXyz);

		return miscutData;
	}


	public MiscutModel getMiscutFromHkl(List<Double> millerIndices, List<Double> beamlinePosition) {
		HklModel hkl = TypeConversion.millerIndicesToHklModel(millerIndices);
		PositionModel position = rawBeamlinePositionToPositionModel(beamlinePosition);

		ApiResponse<MiscutResponse> response = null;
		try {
			response = ubApi.getMiscutFromHklUbNameMiscutHklGetWithHttpInfo(calculationName, hkl.getH(), hkl.getK(), hkl.getL(), position.getMu(), position.getDelta(), position.getNu(), position.getEta(), position.getChi(), position.getPhi(), collectionName);
		} catch (ApiException e) {
			String message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
			InterfaceProvider.getTerminalPrinter().print(message);
			return new MiscutModel();
		}

		MiscutModel miscutData = response.getData().getPayload();

		List<List<Double>> referenceAxis = Maths.columnVectorFromCoordsList(TypeConversion.xyzModelToCoords(miscutData.getRotationAxis()));

		XyzModel beamlineAxis = TypeConversion.coordsToXyzModel(
				Maths.coordsListFromColumnVector(
						axesTransform.referenceToBeamlineColumnVector(referenceAxis)
				)
		);

		miscutData.setRotationAxis(beamlineAxis);
		return miscutData;

	}

	/*
	 * U AND UB MATRICES
	 */

	public int setU(List<List<Double>> uMatrix) {
		List<List<BigDecimal>> referenceUMatrixBigDecimal = TypeConversion.doubleArrayToBigDecimalArray(
				axesTransform.beamlineToReferenceU(uMatrix)
		);
		ApiResponse<InfoResponse> response = null;
		String message;
		try {
			response = ubApi.setUUbNameUPutWithHttpInfo(referenceUMatrixBigDecimal, calculationName, collectionName);
			message = response.getData().getMessage();
			logger.info(message);
		} catch (ApiException e) {
			message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
		}

		InterfaceProvider.getTerminalPrinter().print(message);
		return Objects.isNull(response)? 0: response.getStatusCode();
	}

	public List<List<Double>> getU() {
		ApiResponse<ArrayResponse> response = null;
		try {
			response = ubApi.getUUbNameUGetWithHttpInfo(calculationName, collectionName);
		} catch (ApiException e) {
			String message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
			InterfaceProvider.getTerminalPrinter().print(message);
			return Collections.emptyList();
		}

		List<List<Double>> referenceU = TypeConversion.bigDecimalArrayToDoubleArray(response.getData().getPayload());
		return axesTransform.referenceToBeamlineU(referenceU);

	}


	public int setUb(List<List<Double>> ubMatrix) {
		List<List<BigDecimal>> referenceUbMatrixBigDecimal = TypeConversion.doubleArrayToBigDecimalArray(axesTransform.beamlineToReferenceUb(ubMatrix));
		ApiResponse<InfoResponse> response = null;
		String message;

		try {
			response = ubApi.setUUbNameUPutWithHttpInfo(referenceUbMatrixBigDecimal, calculationName, collectionName);
			message = response.getData().getMessage();
			logger.info(message);
		} catch (ApiException e) {
			message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
		}

		InterfaceProvider.getTerminalPrinter().print(message);
		return Objects.isNull(response)? 0: response.getStatusCode();
	}

	public List<List<Double>> getUb() {
		ApiResponse<ArrayResponse> response = null;
		try {
			response = ubApi.getUbUbNameUbGetWithHttpInfo(calculationName, collectionName);
		} catch (ApiException e) {
			String message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
			InterfaceProvider.getTerminalPrinter().print(message);
			return Collections.emptyList();
		}
		List<List<Double>> referenceUb = TypeConversion.bigDecimalArrayToDoubleArray(response.getData().getPayload());

		return axesTransform.referenceToBeamlineUb(referenceUb);
	}

	public List<List<Double>> calculateUb() {
		ApiResponse<ArrayResponse> response = null;
		try {
			response = ubApi.calculateUbUbNameCalculateGetWithHttpInfo(calculationName, null, null, null, null, collectionName);
		} catch (ApiException e) {
			String message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
			InterfaceProvider.getTerminalPrinter().print(message);
			return Collections.emptyList();
		}

		return TypeConversion.bigDecimalArrayToDoubleArray(response.getData().getPayload());
	}

	public List<List<Double>> calculateUb(String tag1, String tag2) {
		ApiResponse<ArrayResponse> response = null;
		try {
			response = ubApi.calculateUbUbNameCalculateGetWithHttpInfo(calculationName, tag1, null, tag2, null, collectionName);
		} catch (ApiException e) {
			String message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
			InterfaceProvider.getTerminalPrinter().print(message);
			return Collections.emptyList();
		}
		return TypeConversion.bigDecimalArrayToDoubleArray(response.getData().getPayload());
	}

	public List<List<Double>> calculateUb(Integer tag1, Integer tag2) {
		ApiResponse<ArrayResponse> response = null;
		try {
			response = ubApi.calculateUbUbNameCalculateGetWithHttpInfo(calculationName, null, tag1, null, tag2, collectionName);
		} catch (ApiException e) {
			String message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
			InterfaceProvider.getTerminalPrinter().print(message);
			return Collections.emptyList();
		}
		return TypeConversion.bigDecimalArrayToDoubleArray(response.getData().getPayload());
	}

	/**
	 *
	 * @param millerIndices miller indices that the current diffractometer position should equal to,
	 * @param refineLattice if true, if there is a scaling between current crystal and recalculated crystal parameters, then the crystal settings will be updated.
	 * @param refineUMatrix if true, if there is an angle between the diffractometer position and desired hkl direction, this will set the miscut and therefore change the U/UB matrix.
	 * @return integer that specifies the success of the underlying API call.
	 * @throws DeviceException if the current diffractometer position cannot be determined.
	 */
	public int refineUb(List<Double> millerIndices, boolean refineLattice, boolean refineUMatrix) throws DeviceException {
		List<Double> currentPosition = getDiffractometerPositionAsList();
		double wavelength = energyToWavelength(energy);

		HklModel hkl = TypeConversion.millerIndicesToHklModel(millerIndices);
		PositionModel position = rawBeamlinePositionToPositionModel(currentPosition);

		RefineUbParams body = new RefineUbParams();

		body.setHkl(hkl);
		body.setPosition(position);
		body.setWavelength(BigDecimal.valueOf(wavelength));

		ApiResponse<InfoResponse> response = null;
		String message;

		try {
			response = ubApi.refineUbUbNameRefinePatchWithHttpInfo(body, calculationName, refineLattice, refineUMatrix, collectionName);
			message = response.getData().getMessage();
			logger.info(message);
		} catch (ApiException e) {
			message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
		}

		InterfaceProvider.getTerminalPrinter().print(message);
		return Objects.isNull(response)? 0: response.getStatusCode();
	}

	/*
	 * OFFSETS BETWEEN HKL VECTORS
	 */

	public List<Double> calculateVectorFromHklAndOffset(List<Double> millerIndices, Double polarAngle, Double azimuthAngle) {
		HklModel hkl = TypeConversion.millerIndicesToHklModel(millerIndices);
		return calculateVectorFromHklAndOffset(hkl, polarAngle, azimuthAngle);
	}

	public List<Double> calculateVectorFromHklAndOffset(HklModel hkl, Double polarAngle, Double azimuthAngle) {
		BigDecimal polar = BigDecimal.valueOf(polarAngle);
		BigDecimal azimuth = BigDecimal.valueOf(azimuthAngle);

		ApiResponse<ReciprocalSpaceResponse> response = null;
		try {
			response = ubApi.calculateVectorFromHklAndOffsetUbNameVectorGetWithHttpInfo(calculationName, polar, azimuth, hkl.getH(), hkl.getK(), hkl.getL(), collectionName);
		} catch (ApiException e) {
			String message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
			InterfaceProvider.getTerminalPrinter().print(message);
			return Collections.emptyList();
		}
		return TypeConversion.hklModelToMillerIndices(response.getData().getPayload());
	}

	public SphericalCoordinates calculateOffsetFromVectorAndHkl(HklModel hkl1, HklModel hkl2) {
		ApiResponse<SphericalResponse> response;
		try {
			response = ubApi.calculateOffsetFromVectorAndHklUbNameOffsetGetWithHttpInfo(
					calculationName, hkl1.getH(), hkl1.getK(), hkl1.getL(), hkl2.getH(), hkl2.getK(), hkl2.getL(), collectionName
			);
		} catch (ApiException e) {
			String message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
			InterfaceProvider.getTerminalPrinter().print(message);
			return new SphericalCoordinates();
		}

		return response.getData().getPayload();
	}

	public List<List<Double>> solveForIndex(
			HklModel hkl, String idx, Double value, Double a, Double b, Double c, Double d
			) {
		BigDecimal fixedIndex= BigDecimal.valueOf(value);
		BigDecimal coeffA = BigDecimal.valueOf(a);
		BigDecimal coeffB = BigDecimal.valueOf(b);
		BigDecimal coeffC = BigDecimal.valueOf(c);
		BigDecimal coeffD = BigDecimal.valueOf(d);

		ApiResponse<ArrayResponse> response;
		try {
			response = ubApi.hklSolverForFixedQUbNameSolveHklFixedQGetWithHttpInfo(calculationName, idx, fixedIndex, coeffA, coeffB, coeffC, coeffD, hkl.getH(), hkl.getK(), hkl.getL(), collectionName);
		} catch (ApiException e) {
			String message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
			InterfaceProvider.getTerminalPrinter().print(message);
			return Collections.emptyList();
		}

		return TypeConversion.bigDecimalArrayToDoubleArray(response.getData().getPayload());
	}

	/*
	 * CONSTRAINTS
	 */

	public int constrain(Map<String, Double> constraints) {
		Map<String, BigDecimal> body = new HashMap<>();

		Map<String, Double> prependConstraints = angleTransform.defaultConstraints();
		prependConstraints.entrySet().stream().forEach(entry -> body.put(entry.getKey(), BigDecimal.valueOf(entry.getValue())));
		constraints.entrySet().stream().forEach(newEntry -> body.put(newEntry.getKey(), BigDecimal.valueOf(newEntry.getValue())));

		ApiResponse<InfoResponse> response = null;
		String message;
		try {
			response = constraintsApi.setConstraintsConstraintsNamePostWithHttpInfo(body, calculationName, collectionName);
			message = response.getData().getMessage();
			logger.info(message);
		} catch (ApiException e) {
			message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
		}

		InterfaceProvider.getTerminalPrinter().print(message);
		return Objects.isNull(response)? 0: response.getStatusCode();
	}

	/*
	 * CREATION/DELETION
	 */

	public int checkExists() {
		ApiResponse<StringResponse> response = null;
		String message;
		try {
			response = ubApi.getUbStatusUbNameStatusGetWithHttpInfo(calculationName, collectionName);
			message = response.getData().getPayload();
			logger.info(message);
		} catch (ApiException e) {
			message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
		}

		InterfaceProvider.getTerminalPrinter().print(message);
		return Objects.isNull(response)? 0: response.getStatusCode();
	}

	public int create() {
		ApiResponse<InfoResponse> response = null;
		String message;
		try {
			response = storeApi.createHklObjectNamePostWithHttpInfo(calculationName, collectionName);
			message = response.getData().getMessage();
			logger.info(message);
		} catch (ApiException e) {
			message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
		}

		InterfaceProvider.getTerminalPrinter().print(message);
		return Objects.isNull(response)? 0: response.getStatusCode();
	}

	public int delete() {
		ApiResponse<InfoResponse> response = null;
		String message;
		try {
			response = storeApi.deleteHklObjectNameDeleteWithHttpInfo(calculationName, collectionName);
			message = response.getData().getMessage();
			logger.info(message);
		} catch (ApiException e) {
			message = (new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage();
			logger.error(message);
		}

		InterfaceProvider.getTerminalPrinter().print(message);
		return Objects.isNull(response)? 0: response.getStatusCode();
	}

	/*
	 * Scannable Base functions, used in HklScannable.
	 */

	/**
	 * Gets the current diffractometer motor position in hkl space.
	 *
	 * Finds the current motor positions, then queries the API for this position in hkl coordinates.
	 * @return a list containing hkl positions.
	 * @throws DeviceException
	 */
	public List<Double> getHklPosition() throws DeviceException {
		var scannables = diffractometer.getGroupMembers();
		Map<String, Double> beamlinePosition = scannables.stream().collect(Collectors.toMap(Scannable::getName, this::safeGetPosition));

		PositionModel position = beamlinePositionToPositionModel(beamlinePosition);
		return millerIndicesFromPositionModel(position);
	}

	//TODO: in future add a call to get virtual angles also, adding this to the string.
	public String simulateDiffractometerMoveTo(List<Double> position) throws DeviceException {
		List<Double> hkl = getHklPosition(position);
		return String.format("hkl: %f %f %f", hkl.get(0), hkl.get(1), hkl.get(2));
	}

	public String simulateMoveTo(List<Double> hkl) throws DeviceException {
		List<Double> motorPositions = null;

		try {
			motorPositions = getMotorPositions(hkl.get(0), hkl.get(1), hkl.get(2));
		} catch (NoSolutionsFoundException e) {
			return e.getMessage();
		}

		StringBuilder build = new StringBuilder();
		String displayFormat = "\n%9s : %9.4f";

		build.append(String.format("%s would move to: ", diffractometer.getName()));

		for (int i=0; i<diffractometer.getInputNames().length; i++) {
			build.append(
					String.format(
							displayFormat,
							diffractometer.getInputNames()[i],
							motorPositions.get(i)
							)
					);
			}


		return build.toString();
	}

	/**
	 * Gets the diffractometer motor position in hkl space.
	 *
	 * Takes the position of diffractometer motors, turns this into a position model and
	 * then queries the API for this position in hkl coordinates.
	 *
	 * @param rawBeamlinePosition a list containing motor positions of the diffractometer.
	 * @return a list containing the coordinates of this position in hkl space.
	 * @throws DeviceException
	 */
	public List<Double> getHklPosition(List<Double> rawBeamlinePosition) throws DeviceException {
		PositionModel position = rawBeamlinePositionToPositionModel(rawBeamlinePosition);
		return millerIndicesFromPositionModel(position);
	}

	/**
	 * Gets the diffractometer motor position in hkl space.
	 *
	 * Takes a position model to call the API directly for comparable solutions in hkl space.
	 *
	 * @param position the position of diffractometer motors as a PositionModel object.
	 * @return a list containing the coordinates of this position in hkl space.
	 * @throws DeviceException caught ApiExceptions are converted into this type of exception also.
	 */
	private List<Double> millerIndicesFromPositionModel(PositionModel position) throws DeviceException {
		Double wavelength = energyToWavelength(energy);
		ApiResponse<ReciprocalSpaceResponse> response;
		try {
			response = hklApi.millerIndicesFromLabPositionHklNamePositionHklGetWithHttpInfo(calculationName,
					BigDecimal.valueOf(wavelength), position.getMu(), position.getDelta(),
					position.getNu(), position.getEta(), position.getChi(), position.getPhi(),
					collectionName);
		} catch (ApiException e) {
			throw new DeviceException(e);
		}

		return TypeConversion.hklModelToMillerIndices(response.getData().getPayload());
	}

	/**
	 * Given several beamline positions, selects the one closest to the current position of the motors.
	 *
	 * @param possibleSolutions
	 * @return beamline position that is closest to the current position
	 */
	public Map<String, Double> chooseClosestSolution(List<Map<String, Double>> possibleSolutions) {

		var scannables = diffractometer.getGroupMembers();
		Map<String, Double> currentPosition = scannables.stream().collect(Collectors.toMap(Scannable::getName, this::safeGetPosition));

		// find absolute difference between each position and the current,
		List<Map<String, Double>> absoluteDifferences = new ArrayList<>();
		for (var possibleSolution: possibleSolutions) {

			Map<String, Double> absoluteDifference = possibleSolution.entrySet().stream().collect(toMap(
					Entry::getKey,
					entry -> Maths.angleDifference(entry.getValue(), currentPosition.get(entry.getKey()))
					));

			absoluteDifferences.add(absoluteDifference);
		}

		List<Double> sums = absoluteDifferences.stream().map((Map<String, Double> diff) ->
			diff.values().stream().mapToDouble(f -> f).sum()
		).toList();

		int smallestIndex = 0;
		Double smallestNumber = sums.get(smallestIndex);
		for (int i=0; i<sums.size(); i++) {
			if (sums.get(i) < smallestNumber) {
				smallestNumber = sums.get(i);
				smallestIndex = i;
			}
		}

		return possibleSolutions.get(smallestIndex);
	}

	/**
	 * Converts the API response for a diffractometer position to the beamline position.
	 * The API response will just be a mapping between a string and value, for each motor,
	 * in the reference frame.
	 *
	 * the resulting beamline position needs to take into account the mapping between reference
	 * angles and motors on the beamline, as well as linear scalings between these. In addition,
	 * angles need to be cut to reside between -180 and 180 degrees by default, although the
	 * cut angles for specific motors can be changed (default=-180).
	 *
	 * @param position diffractometer position in reference frame, directly from API response.
	 * @return beamline position after appropriate mappings and other processing.
	 */
	private Map<String, Double> convertFromApi(Map<String, BigDecimal> position) {
		var reference = new EnumMap<ReferenceGeometry, Double>(ReferenceGeometry.class);

		for (ReferenceGeometry angle: ReferenceGeometry.values()) {
			String angleString = angle.getName();
			reference.put(angle, position.get(angleString).doubleValue());
		}

		Map<String, Double> beamlinePosition = angleTransform.getBeamlineGeometry(reference);

		return angleShift.cutAngles(beamlinePosition);
	}

	private List<Double> getMotorPositions(double h, double k, double l) throws DeviceException, NoSolutionsFoundException {
		Double wavelength = energyToWavelength(energy);
		ApiResponse<DiffractorAnglesResponse> response;
		try {
			response = hklApi.labPositionFromMillerIndicesHklNamePositionLabGetWithHttpInfo(calculationName,
					BigDecimal.valueOf(wavelength), BigDecimal.valueOf(h), BigDecimal.valueOf(k), BigDecimal.valueOf(l),
					null, null, null, collectionName);
		} catch (ApiException e) {
			throw new DeviceException((new Gson()).fromJson(e.getResponseBody(), ExceptionContent.class).getMessage());
		}

		List<Map<String, BigDecimal>> possiblePositions = response.getData().getPayload();
		List<String> scannableNames = Arrays.asList(diffractometer.getGroupMemberNames());

		List<Map<String, Double>> validSolutions = new ArrayList<>();

		for (var position: possiblePositions) {
			Map<String, Double> convertedPosition = convertFromApi(position);

			List<Double> solution = new ArrayList<>();
			scannableNames.stream().forEach(name -> solution.add(convertedPosition.get(name)));

			if (diffractometer.checkPositionValid(solution) == null) {
				validSolutions.add(convertedPosition);
			}
		}

		if (validSolutions.size() == 0)
			throw new NoSolutionsFoundException("No solutions have been found");

		Map<String, Double> mapOfResults = chooseClosestSolution(validSolutions);
		return Arrays.stream(diffractometer.getGroupMemberNames()).map(mapOfResults::get).toList();

	}

	public void moveToHkl(double h, double k, double l) throws DeviceException {

		try {
			diffractometer.asynchronousMoveTo(getMotorPositions(h, k, l));
		} catch (NoSolutionsFoundException e) {
			throw new DeviceException("No solutions found for this hkl");
		}
	}

	/**
	 * Method which takes an energy scannable and converts the current energy value into a wavelength.
	 *
	 *
	 * @param energy in keV, unless the energyMultiplier is not equal to 1.0 (default). For example,
	 * if energyMultiplier=0.001 the energy should be in eV instead.
	 * @return wavelength in angstroms
	 * @throws DeviceException
	 */
	private double energyToWavelength(Scannable energy) throws DeviceException {
		Double energyValue = (Double) energy.getPosition();
		return HC_ANGSTROM_KEV / (energyValue * energyMultiplier);
	}

	public List<Double> closestBeamlinePositionFromHklList(List<List<Double>> hklList) throws DeviceException, NoSolutionsFoundException {
		List<Map<String, Double>> positions = new ArrayList<>();
		for (List<Double> eachHkl: hklList) {
			try {
				Map<String, Double> viablePosition= new HashMap<>();
				List<Double> position = getMotorPositions(eachHkl.get(0), eachHkl.get(1), eachHkl.get(2));

				for (int i=0;i<position.size(); i++) {
					viablePosition.put(diffractometer.getGroupMemberNames()[i], position.get(i));
				}

				positions.add(viablePosition);
			} catch (NoSolutionsFoundException e) {
				logger.warn(String.format(
						"No solutions found for hkl: %f, %f, %f", eachHkl.get(0), eachHkl.get(1), eachHkl.get(2))
					);
			}
		}

		Map<String, Double> mapOfResults;
		switch (positions.size()) {
		case 0:
			throw new NoSolutionsFoundException("Neither hkl provided generated a solution");
		case 1:
			mapOfResults = positions.get(0);
			break;
		default:
			mapOfResults = chooseClosestSolution(positions);
		}
		return Arrays.stream(diffractometer.getGroupMemberNames()).map(mapOfResults::get).toList();

	}

	public boolean isDiffractometerBusy() throws DeviceException {
		return diffractometer.isBusy();
	}

	public ScannableGroup getDiffractometer() {
		return diffractometer;
	}

	public void setDiffractometer(ScannableGroup diffractometer) {
		this.diffractometer = diffractometer;
	}

	public String getCollectionName() {
		return collectionName;
	}

	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

	public String getCalculationName() {
		return calculationName;
	}

	public void setCalculationName(String calculationName) {
		this.calculationName = calculationName;
	}

	public List<List<Double>> getTransform() {
		return axesTransform.getBeamlineToReferenceTransform();
	}

	public void setTransform(List<List<Double>> matrix) {
		axesTransform.setBeamlineToReferenceTransform(matrix);
	}

	public void setTransform(double[][] matrix) {
		List<List<Double>> matrixArray = Arrays.stream(matrix).map(Doubles::asList).toList();
		setTransform(matrixArray);
	}

	public Scannable getEnergy() {
		return energy;
	}

	public void setEnergy(Scannable energy) {
		this.energy = energy;
	}


	public Double getEnergyMultiplier() {
		return energyMultiplier;
	}

	public void setEnergyMultiplier(Double energyMultiplier) {
		this.energyMultiplier = energyMultiplier;
	}


	public AngleShift getAngleShift() {
		return angleShift;
	}


	public void setAngleShift(AngleShift angleShift) {
		this.angleShift = angleShift;
	}

	public AngleTransform getAngleTransform() {
		return angleTransform;
	}

	public void setAngleTransform(AngleTransform angleTransform) {
		this.angleTransform = angleTransform;
	}

	public AxesTransform getAxesTransform() {
		return axesTransform;
	}

	public void setAxesTransform(AxesTransform axesTransform) {
		this.axesTransform = axesTransform;
	}

}
