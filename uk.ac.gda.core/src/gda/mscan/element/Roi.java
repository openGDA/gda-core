/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package gda.mscan.element;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import java.util.List;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolygonalROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;

/**
 * Links the revised mscan syntax to corresponding DAWN {@link IROI} factory functions in a typed way. The abbreviation
 * to specify a Roi is linked to the {@link IROI} factory function via the constructor along with the number of
 * parameters required to specify it. The factory functions need to be a nested static class so that they are in scope
 * when the constructor is called.
 * @since GDA 9.9
 */
public enum Roi implements IMScanElementEnum {
	RECTANGLE("rect", 4, true, RectangularROI.class, Factory::createRectangularROI),
	CENTRED_RECTANGLE("crec", 4, true, RectangularROI.class, Factory::createCenteredRectangularROI),
	CIRCLE("circ", 3, true, CircularROI.class, Factory::createCircularROI),
	POLYGON("poly", 6, false, PolygonalROI.class, Factory::createPolygonalROI),
	LINE("line", 4, true, LinearROI.class, Factory::createLinearROI);

	private final String text;
	private final int valueCount;
	private final boolean hasFixedValueCount;
	private final Class<? extends IROI> roiType;
	private final RoiFactoryFunction factory;

	private Roi(final String text, final int valueCount, final boolean hasFixedValueCount,
				final Class<? extends IROI> roiType, final RoiFactoryFunction factoryFunction) {
		this.text = text;
		this.valueCount = valueCount;
		this.hasFixedValueCount = hasFixedValueCount;
		this.roiType = roiType;
		this.factory = factoryFunction;
	}

	/**
	 * The default instance value to be used if one is not specified in the scan command
	 *
	 * @return	The {@link #RECTANGLE} instance
	 */
	public static Roi defaultValue() {
		return RECTANGLE;
	}

	/**
	 * The default text values that correspond to the instances of Roi
	 *
	 * @return		List of default text for the instances
	 */
	public static List<String> strValues() {
		return stream(values()).map(val -> val.text).collect(toList());
	}

	/**
	 * The number of values required to specify the {@link IROI}. In the case of a shapes with no fixed number of
	 * vertices, this will return the minimum number required
	 *
	 * @return	The number of points necessary to specify the IROI or the minimum if this is not a fixed amount
	 */
	public int valueCount() {
		return valueCount;
	}

	public boolean hasFixedValueCount() {
		return hasFixedValueCount;
	}

	/**
	 * The type of {@link IROI} that a particular instance's {@link #createIROI} method will construct
	 *
	 * @return		The {@link IROI} implementing type associated with the instance.
	 */
	public Class<? extends IROI> roiType() {
		return roiType;
	}

	/**
	 * Creates the correct {@link IROI} implementing object for this instance of Roi
	 * Where possible the number of supplied points will be validated against {@link #valueCount()}
	 *
	 * @param params	The param list that defines the points of the Roi
	 *
	 * @return			The correct {@link IROI} implementing object for this instance of Roi
	 *
	 * @throws 			IllegalArgumentException if the validation of parameters fails
	 */
	public IROI createIROI(final List<Number> params) {
		validateParamSize(params);
		return factory.createROI(params);
	}

	/**
	 * Check that the correct number of parameters has been supplied for the required {@link IROI} except for Polygons
	 * where we can only check the minimum number were supplied.
	 *
	 * @param params				The numeric coordinate sets for the Roi vertices
	 * @param nParams				The required number of params for this Roi type
	 * @param roiName				The name of the Roi for exception message purposes
	 * @param noFixedPointCount		Flag to indicate whether this Roi has a fixed number of vertices
	 */
	private void validateParamSize(final List<Number> params) {
		if (params.size() != valueCount) {
			String prefix = "";
			if (!hasFixedValueCount) {
				prefix = "at least ";
				if (params.size() >= valueCount) {
					return;
				}
			}
			throw new IllegalArgumentException(String.format(
					"%s requires %s%s numeric values to be specified" , roiType.getSimpleName(), prefix, valueCount));
		}
	}

	/**
	 * This Class contains factory methods for each of the required {@link IROI} implementing types mapped
	 * by the instance constructor.
	 */
	private static class Factory {

		/**
		 * Creates a {@link RectangularROI} using the supplied params.
		 *
		 * @param params	A par of diagonally opposite corner coordinates of the rectangle as a {@link List} in the
		 * 					order: x1, y1, x2, y2
		 * @return			An {@link IROI} of the requested shape and dimensions
		 */
		private static IROI createRectangularROI(final List<Number> params) {
			return new RectangularROI(
					params.get(0).doubleValue(),
					params.get(1).doubleValue(),
					params.get(2).doubleValue(),
					params.get(3).doubleValue(), 0);
		}

		/**
		 * Creates a {@link RectangularROI} using the supplied params.
		 *
		 * @param params	The centre coordinate and x and y widths of the rectangle as a {@link List} in the order:
		 * 					xcentre, ycentre, xwidth, ywidth
		 * @return			An {@link IROI} of the requested shape and dimensions
		 */
		private static IROI createCenteredRectangularROI(final List<Number> params) {
			return new RectangularROI(
					params.get(0).doubleValue() - params.get(2).doubleValue()/2,
					params.get(1).doubleValue() - params.get(3).doubleValue()/2,
					params.get(2).doubleValue(),
					params.get(3).doubleValue(), 0);
		}

		/**
		 * Creates a {@link CircularROI} using the supplied params.
		 *
		 * @param params 	The centre coordinate and radius of the Circle as a {@link List} in the order:
		 * 					xcentre, ycentre, radius
		 * @return			An {@link IROI} of the requested shape and dimensions
		 */
		private static IROI createCircularROI(final List<Number> params) {
			return new CircularROI(
					params.get(2).doubleValue(),
					params.get(0).doubleValue(),
					params.get(1).doubleValue());
		}

		/**
		 * Creates a {@link PolygonalROI} using the supplied params.
		 *
		 * @param params 	The coordinates of vertices the polygon as a {@link List} of pairs in the order:
		 * 					x1, y1, x2, y2, x3, y3 ..... There must be an even number of params
		 * @return			An {@link IROI} of the requested shape and dimensions
		 */
		private static IROI createPolygonalROI(final List<Number> params) {
			if ((params.size() & 1) != 0) {
				throw new IllegalArgumentException("PolygonalROI requires an even number of params");
			}
			PolygonalROI roi = new PolygonalROI();
			for (int index = 0; index < params.size(); index += 2) {
				roi.insertPoint(params.get(index).doubleValue(), params.get(index + 1).doubleValue());
			}
			return roi;
		}

		/**
		 * Creates a {@link LinearROI} using the supplied params.
		 *
		 * @param params	The start and end coordinates of the line as a {@link List} in the order
		 * 					xstart, ystart, xstop, ystop
		 * @return			An {@link IROI} of the requested shape and dimensions
		 */
		private static IROI createLinearROI(final List<Number> params) {
			return new LinearROI(
					new double[] {params.get(0).doubleValue(), params.get(1).doubleValue()},
					new double[] {params.get(2).doubleValue(), params.get(3).doubleValue()});
		}
	}
}
