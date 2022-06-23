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

package uk.ac.diamond.daq.mapping.ui.experiment;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.BoundingLine;
import org.eclipse.scanning.api.points.models.IBoundingBoxModel;
import org.eclipse.scanning.api.points.models.IBoundingLineModel;
import org.eclipse.scanning.api.points.models.IMapPathModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;

/**
 * Static helper class to map a scan region onto a scan path model
 * when the model alone does not contain enough information
 * to describe the scan.
 */
public final class RegionAndPathMapper {

	/**
	 * Updates a {@link IScanPointGeneratorModel} if needed with the
	 * boundary information in a {@link IMappingScanRegionShape}
	 * @param scanRegionShape The region to map onto the model
	 * @param scanPathModel The model to update
	 */
	public static void mapRegionOntoModel(
			IMappingScanRegionShape scanRegionShape,
			IMapPathModel scanPathModel) {
		if (scanRegionShape == null || scanRegionShape.toROI() == null)
			return;

		if (scanPathModel instanceof IBoundingBoxModel)
			mapRegionOntoBoundingBoxModel(scanRegionShape, (IBoundingBoxModel) scanPathModel);
		else if (scanPathModel instanceof IBoundingLineModel)
			mapRegionOntoBoundingLineModel((IBoundingLineModel) scanPathModel, scanRegionShape);
	}

	/**
	 * Updates the {@link BoundingBox} of an {@link IBoundingBoxModel} to conform to
	 * a {@link IMappingScanRegionShape}
	 * @param scanRegionShape The region to map onto the model
	 * @param boxModel The model to update
	 */
	private static void mapRegionOntoBoundingBoxModel(
			IMappingScanRegionShape scanRegionShape,
			IBoundingBoxModel boxModel) {
		IRectangularROI roi = scanRegionShape.toROI().getBounds();
		if (roi != null) {
			BoundingBox box = boxModel.getBoundingBox();
			if (box == null) {
				box = new BoundingBox();
				boxModel.setBoundingBox(box);
			}
			box.setxAxisStart(roi.getPointX());
			box.setyAxisStart(roi.getPointY());
			box.setxAxisLength(roi.getLength(0));
			box.setyAxisLength(roi.getLength(1));
		}
	}

	/**
	 * Updates the {@link BoundingLine} of an {@link IBoundingLineModel} to conform to
	 * a {@link IMappingScanRegionShape}
	 * @param scanRegionShape The region to map onto the model
	 * @param lineModel The model to update
	 */
	private static void mapRegionOntoBoundingLineModel(
			IBoundingLineModel lineModel,
			IMappingScanRegionShape scanRegionShape) {
		IROI roi = scanRegionShape.toROI();
		if (roi instanceof LinearROI) {
			LinearROI linearROI = (LinearROI) roi;
			BoundingLine line = lineModel.getBoundingLine();
			if (line == null) {
				line = new BoundingLine();
				lineModel.setBoundingLine(line);
			}
			line.setxStart(linearROI.getPointX());
			line.setyStart(linearROI.getPointY());
			line.setAngle(linearROI.getAngle());
			line.setLength(linearROI.getLength());
		}
	}
}
