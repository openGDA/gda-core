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

package uk.ac.diamond.daq.mapping.ui.experiment.file;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.StringJoiner;
import java.util.function.Function;

import org.eclipse.scanning.api.points.models.AbstractPointsModel;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.LissajousModel;
import org.eclipse.scanning.api.points.models.OneDEqualSpacingModel;
import org.eclipse.scanning.api.points.models.OneDStepModel;
import org.eclipse.scanning.api.points.models.RandomOffsetGridModel;
import org.eclipse.scanning.api.points.models.RasterModel;
import org.eclipse.scanning.api.points.models.SinglePointModel;
import org.eclipse.scanning.api.points.models.SpiralModel;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;
import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;
import uk.ac.diamond.daq.mapping.region.CentredRectangleMappingRegion;
import uk.ac.diamond.daq.mapping.region.CircularMappingRegion;
import uk.ac.diamond.daq.mapping.region.LineMappingRegion;
import uk.ac.diamond.daq.mapping.region.PointMappingRegion;
import uk.ac.diamond.daq.mapping.region.PolygonMappingRegion;
import uk.ac.diamond.daq.mapping.region.RectangularMappingRegion;


/**
 * Factory to create a filename in a format which describes the shape and path characteristics of a solstice scan in it
 * using the form <user supplied name>.<description>.map. It covers all current shapes and paths and can be extended by
 * adding any new definitions to the appropriate enum and BiMap. Decimal parameter values are formatted as 3 sig. figs.
 * scientific notation * 100 to remove the possibility of decimal points; e.g. 0.0065748 which would normally be
 * 6.57E-3 becomes 657E-5.
 *
 * @since GDA 9.13
 */
public class DescriptiveFilenameFactory {

	private static NumberFormat formatter = new DecimalFormat("000E0"); // to encode decimal values in a consistent way

	/**
	 * Partially maps {@link IMappingScanRegionShape}s to the id and encoded text for that shape. The general format is
	 * S<id>(<dimensions>). Numeric ids are used to get round clashes in e.g. shape initials etc.
	 */
	enum RegionParameterSource {
		POINT(0, "", shape -> ""),
		RECT(1,  "%s x %s", shape -> {RectangularMappingRegion rect = (RectangularMappingRegion)shape;
			return enclose(format(rect.toROI().getLength(0)) + "," + format(rect.toROI().getLength(1)));
			}),
		CREC(2, " %s x %s", shape -> {CentredRectangleMappingRegion rect = (CentredRectangleMappingRegion)shape;
			return enclose(format(rect.getxRange()) + "," + format(rect.getyRange()));
			}),
		LINE(3, "%s at %s°", shape -> {LineMappingRegion line = (LineMappingRegion)shape;
			return enclose(format(line.toROI().getLength()) + "," + Math.round(Math.toDegrees(line.toROI().getAngle())));
			}),
		CIRC(4, "rad %s", shape -> {CircularMappingRegion circ = (CircularMappingRegion)shape;
			return enclose(format(circ.getRadius()));
			}),
		POLY(5, "%s sides", shape -> {PolygonMappingRegion poly = (PolygonMappingRegion)shape;
			return enclose(format(poly.getPoints().size()));
			});

		private final int id;
		private final String summaryFormat;
		private final Function<IMappingScanRegionShape, String> paramSupplier;

		private static RegionParameterSource values[] = values();

		private RegionParameterSource(final int id, final String summaryFormat,
				final Function<IMappingScanRegionShape, String> paramSupplier) {
			this.paramSupplier = paramSupplier;
			this.summaryFormat = summaryFormat;
			this.id = id;
		}

		private static String format(Number value) {
			return formatter.format(value);
		}

		static String getSummary(final String shapeId, final Object... args) {
			int id = Integer.valueOf(shapeId.substring(1));
			String format = values[id].summaryFormat;
			return String.format(format, args);
		}
	}

	/**
	 * Partially maps {@link IScanPathModel}s to the id and encoded text for that apth. The general format is
	 * P<id>(<dimensions>)<mutator initials>. Numeric ids are used to get round clashes in e.g. shape initials etc.
	 */
	enum PathParameterSource {
		POINT(0, "1 pt", path -> "", path -> ""),
		RAST(1, "%s step per side, %s %s",
			path -> enclose(((RasterModel)path).getxAxisStep() + "," + ((RasterModel)path).getyAxisStep()),
			path -> isContinuous(path) + isAlternating(path)),
		GRID(2, "%s pts per side. %s %s",
			path -> enclose(((GridModel)path).getxAxisPoints() + "," + ((GridModel)path).getyAxisPoints()),
			path -> isContinuous(path) + isAlternating(path)),
		RAND(3, "%s pts per side. %s %s",
			path -> enclose(((RandomOffsetGridModel)path).getxAxisPoints() + ","
							+ ((RandomOffsetGridModel)path).getyAxisPoints()),
			path -> isContinuous(path) + isAlternating(path)),
		EQUAL(4, "%s pts, %s %s",
			path -> enclose(String.valueOf(((OneDEqualSpacingModel)path).getPoints())),
			path -> isContinuous(path) + isAlternating(path)),
		STEP(5, "%s step, %s %s",
			path -> enclose(String.valueOf(((OneDStepModel)path).getStep())),
			path -> isContinuous(path) + isAlternating(path)),
		SPIR(6, "%s scale %s %s",
			path -> enclose(String.valueOf(((SpiralModel)path).getScale())),
			path -> isContinuous(path) + isAlternating(path)),
		LISS(7,	"%s a %s b, %s %s",
			path -> enclose(((LissajousModel)path).getA() + "," + ((LissajousModel)path).getB()),
			path -> isContinuous(path) + isAlternating(path));

		private final int id;
		private final String summaryFormat;
		private final Function<IScanPathModel, String> paramSupplier;
		private final Function<IScanPathModel, String> mutatorSupplier;

		private static PathParameterSource values[] = values();

		private PathParameterSource(final int id, final String summaryFormat,
				final Function<IScanPathModel, String> paramSupplier,
				final Function<IScanPathModel, String> mutatorSupplier) {
			this.id = id;
			this.summaryFormat = summaryFormat;
			this.paramSupplier = paramSupplier;
			this.mutatorSupplier = mutatorSupplier;
		}

		private static String isContinuous(final IScanPathModel path) {
			return (path instanceof AbstractPointsModel && ((AbstractPointsModel)path).isContinuous()) ? "c" : "";
		}

		private static String isAlternating(final IScanPathModel path) {
			return (path instanceof AbstractPointsModel && ((AbstractPointsModel)path).isAlternating()) ? "a" : "";
		}

		static String getSummary(final String shapeId, final Object... args) {
			int id = Integer.valueOf(shapeId.substring(1));
			String format = values[id].summaryFormat;
			return String.format(format, args);
		}
	}

	/**
	 * Completes the mapping for {@link IMappingScanRegionShape}s
	 */
	private final BiMap<Class<? extends IMappingScanRegionShape>, RegionParameterSource> regionLookup =
			new ImmutableBiMap.Builder<Class<? extends IMappingScanRegionShape>, RegionParameterSource>()
			.put(PointMappingRegion.class, RegionParameterSource.POINT)
			.put(RectangularMappingRegion.class, RegionParameterSource.RECT)
			.put(CentredRectangleMappingRegion.class, RegionParameterSource.CREC)
			.put(LineMappingRegion.class, RegionParameterSource.LINE)
			.put(CircularMappingRegion.class, RegionParameterSource.CIRC)
			.put(PolygonMappingRegion.class, RegionParameterSource.POLY)
			.build();

	/**
	 * Completes the mapping for {@link IScanPathModel}s
	 */
	private final BiMap<Class<? extends IScanPathModel>, PathParameterSource> pathLookup =
			new ImmutableBiMap.Builder<Class<? extends IScanPathModel>, PathParameterSource>()
			.put(SinglePointModel.class, PathParameterSource.POINT)
			.put(RasterModel.class, PathParameterSource.RAST)
			.put(GridModel.class, PathParameterSource.GRID)
			.put(RandomOffsetGridModel.class, PathParameterSource.RAND)
			.put(OneDEqualSpacingModel.class, PathParameterSource.EQUAL)
			.put(OneDStepModel.class, PathParameterSource.STEP)
			.put(SpiralModel.class, PathParameterSource.SPIR)
			.put(LissajousModel.class, PathParameterSource.LISS)
			.build();

	/**
	 * Wraps the supplied text in brackets
	 * @param params	The text to be wrapped
	 * @return			The enclosed text
	 */
	private static String enclose(final String params) {
		return String.format("(%s)", params);
	}

	/**
	 * Generates the shape/path descriptor text for the supplied mapping bean
	 *
	 * @param bean		The mapping bean containing the {@link IMappingScanRegionShape} and {@link IScanPathModel} to
	 * 					be described
	 * @return			The corresponding descriptive text.
	 */
	private final String getFilenameDescriptor(final IMappingExperimentBean bean) {
		IMappingScanRegionShape region = bean.getScanDefinition().getMappingScanRegion().getRegion();
		IScanPathModel path = bean.getScanDefinition().getMappingScanRegion().getScanPath();
		if (!regionLookup.containsKey(region.getClass()) || !pathLookup.containsKey(path.getClass())) {
			throw new IllegalArgumentException("Unknown region or path");
		}
		StringBuilder descriptor = new StringBuilder("S");
		descriptor.append(regionLookup.get(region.getClass()).id);
		descriptor.append(regionLookup.get(region.getClass()).paramSupplier.apply(region));
		descriptor.append("P");
		descriptor.append(pathLookup.get(path.getClass()).id);
		descriptor.append(pathLookup.get(path.getClass()).paramSupplier.apply(path));
		descriptor.append(pathLookup.get(path.getClass()).mutatorSupplier.apply(path));
		return descriptor.toString();
	}

	/**
	 * Builds a filename that describes the supplied mapping bean from the supplied body of the form
	 * <body>.<description>.map
	 *
	 * @param body	The user supplied text to be used as the leftmost part of the filename
	 * @param bean	The mapping bean to be described
	 * @return		The filename text including the supplied body plus the bean description
	 */
	public String getFilename(final String body, final IMappingExperimentBean bean) {
		return new StringJoiner(".").add(body).add(getFilenameDescriptor(bean)).add("map").toString();
	}
}
