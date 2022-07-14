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

package org.eclipse.scanning.api.points.models;

import static org.eclipse.scanning.api.constants.PathConstants.ORIENTATION;

import java.util.Map;

/**
 * Abstract superclass for models representing a raster scan within a rectangular box in two-dimensional space.
 *
 * Previously AbstractGridModel
 */
public abstract class AbstractTwoAxisGridModel extends AbstractBoundingBoxModel implements IBoundsToFit {

	private Orientation orientation = Orientation.HORIZONTAL;


	/**
	 * Only relevant when scanning with an outer axis i.e. a CompoundGenerator has been created with this model
	 * and another model outside of it, when this model is set to Alternating, the alternating should be on both axes:
	 * so that the first point after moving the outer axis is the final point visited, optimising travel of the inner motors
	 *
	 * a->b    a<-b
	 *    v       ^
	 * d<-c    d->c
	 *
	 * If set to false, instead alternating is only set on the innermost axis, which is necessary for Odin rewinding.
	 * As with both axes set to alternate, the resulting pattern is odd in the outer axis if the slow axis of the grid
	 * if of an odd length.
	 * a->b    a<-b
	 *    v    v
	 * d<-c    d->c
	 * v          v
	 * e->f    e<-f
	 */
	private boolean alternateBothAxes = true;
	private boolean boundsToFit;

	protected AbstractTwoAxisGridModel(){
		defaultBoundsToFit();
	}

	public enum Orientation {
		HORIZONTAL("Horizontal"), VERTICAL("Vertical");

		private final String orientationString;

		Orientation(String orientationString) {
	        this.orientationString = orientationString;
	    }

	    @Override
		public String toString() {
	        return this.orientationString;
	    }}

	/**
	 * By default the horizontal axis is the scanned first, i.e. is the fast axis. If this
	 * property is set the vertical axis is the scanned first.
	 *
	 * @return <code>true</code> if the vertical axis is scanned first, <code>false</code> if
	 * the horizontal axis is scanned first.
	 */

	public Orientation getOrientation() {
		return orientation;
	}

	public void setOrientation(Orientation orientation) {
		Orientation oldValue = this.orientation;
		this.orientation = orientation;
		this.pcs.firePropertyChange(ORIENTATION, oldValue, orientation);
	}

	public boolean isVerticalOrientation() {
		return orientation.equals(Orientation.VERTICAL);
	}

	@Override
	public boolean isBoundsToFit() {
		return boundsToFit;
	}

	@Override
	public void setBoundsToFit(boolean boundsToFit) {
		pcs.firePropertyChange(PROPERTY_DEFAULT_BOUNDS_FIT, this.boundsToFit, boundsToFit);
		this.boundsToFit = boundsToFit;
	}

	@Override
	public void updateFromPropertiesMap(Map<String, Object> properties) {
		super.updateFromPropertiesMap(properties);
		if (properties.containsKey(ORIENTATION)) {
			setOrientation(Orientation.valueOf(((String) properties.get(ORIENTATION)).toUpperCase()));
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (alternateBothAxes ? 1231 : 1237);
		result = prime * result + (boundsToFit ? 1231 : 1237);
		result = prime * result + ((orientation == null) ? 0 : orientation.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractTwoAxisGridModel other = (AbstractTwoAxisGridModel) obj;
		if (alternateBothAxes != other.alternateBothAxes)
			return false;
		if (boundsToFit != other.boundsToFit)
			return false;
		if (orientation != other.orientation)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "verticalOrientation=" + orientation
				+ ", " + super.toString();
	}

	/**
	 * Turns a StepModel into an equivalent PointsModel. Some scans (e.g XANES) perform similar scans but with small offsets,
	 * which can catch on floating point calculation errors, giving off-by-one in dataset shapes when reconstructing.
	 * This utility method prevents this situation by converting to a PointsModel, which will always have the same shape,
	 * and performing the division to find the number of points using BigDecimal.
	 * The BoundingBox of the returned model may differ from that of the input method, due to StepModels being
	 * able to leave a portion of a step towards their end too small for another step. PointsModel however use the entire region,
	 * so this partial box is equivalent in terms of point placement.
	 *
	 * @param model - a TwoAxisGridStepModel
	 * @return pointsModel - a TwoAxisGridPointsModel that will output IPositions that
	 * 						very closely match the IPositions generated by the input
	 */
	public static TwoAxisGridPointsModel enforceShape(TwoAxisGridStepModel model) {
		final TwoAxisGridPointsModel pointsModel = new TwoAxisGridPointsModel();
		if (!model.getName().equals("Raster")) {
			pointsModel.setName(model.getName());
		}
		pointsModel.setxAxisName(model.getxAxisName());
		pointsModel.setyAxisName(model.getyAxisName());
		pointsModel.setxAxisUnits(model.getxAxisUnits());
		pointsModel.setyAxisUnits(model.getyAxisUnits());
		pointsModel.setAlternating(model.isAlternating());
		pointsModel.setContinuous(model.isContinuous());
		pointsModel.setBoundsToFit(model.isBoundsToFit());
		pointsModel.setOrientation(model.getOrientation());

		final BoundingBox copy = new BoundingBox();
		final BoundingBox original = model.getBoundingBox();
		if (original != null) {
			copy.setxAxisName(original.getxAxisName());
			copy.setyAxisName(original.getyAxisName());
			copy.setxAxisStart(original.getxAxisStart());
			copy.setyAxisStart(original.getyAxisStart());
			copy.setxAxisLength(original.getxAxisLength());
			copy.setyAxisLength(original.getyAxisLength());
			copy.setRegionName(original.getRegionName());
		}
		final double xStep = model.getxAxisStep();
		final double yStep = model.getyAxisStep();

		final int xPoints = model.getPointsOnLine(copy.getxAxisLength(), xStep);
		final int yPoints = model.getPointsOnLine(copy.getyAxisLength(), yStep);

		pointsModel.setxAxisPoints(xPoints);
		pointsModel.setyAxisPoints(yPoints);

		// Trim region that would not have been stepped in (-1 point because of point at end)
		final int xSteps = model.isBoundsToFit() ? xPoints : (int) (xPoints - (Math.signum(xPoints)));
		final int ySteps = model.isBoundsToFit() ? yPoints : (int) (yPoints - (Math.signum(yPoints)));

		copy.setxAxisLength(xStep * xSteps);
		copy.setyAxisLength(yStep * ySteps);
		pointsModel.setBoundingBox(copy);

		return pointsModel;
	}

	public boolean isAlternateBothAxes() {
		return alternateBothAxes;
	}

	public void setAlternateBothAxes(boolean alternateBothAxes) {
		this.alternateBothAxes = alternateBothAxes;
	}

}
