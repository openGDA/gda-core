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
 *
 * This is the default tooltip text provider for a client.  A beamline can use
 * their own implementation of this by subclassing this class and adding the following
 * into the  appropriate Spring configuration file:
 *
 *     <bean class="gda.util.osgi.OSGiServiceRegister">
 *		   <property name="class" value="org.eclipse.scanning.api.event.scan.ToolTipTextProvider" />
 *		   <property name="service" ref="tooltipProvider" />
 *	   </bean>
 *
 *	   <bean id="tooltipProvider" class="org.eclipse.scanning.api.event.scan.<subclass>"/>
 *
 */

package org.eclipse.scanning.api.event.scan;

import java.lang.reflect.Method;
import java.util.Optional;

import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.EllipticalROI;
import org.eclipse.dawnsci.analysis.dataset.roi.HyperbolicROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.ParabolicROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PointROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolygonalROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolylineROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RingROI;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.api.points.models.AbstractBoundingLineModel;
import org.eclipse.scanning.api.points.models.AbstractTwoAxisGridModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.BoundingLine;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.points.models.ScanRegion;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsRandomOffsetModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridStepModel;
import org.eclipse.scanning.api.points.models.TwoAxisLinePointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisLineStepModel;
import org.eclipse.scanning.api.points.models.TwoAxisLissajousModel;
import org.eclipse.scanning.api.points.models.TwoAxisPointSingleModel;
import org.eclipse.scanning.api.points.models.TwoAxisSpiralModel;

public class ScanBeanSummariser extends StatusBeanSummariser {

	@Override
	public String summarise(StatusBean statusBean)
	 {
		StringBuilder toolTipText = new StringBuilder();

		if (statusBean instanceof ScanBean)
		{
			CompoundModel compoundModel = ((ScanBean)statusBean).getScanRequest().getCompoundModel();

			Optional<ScanRegion> region = compoundModel.getRegions().stream().findFirst();
			if (region.isPresent()) {
				toolTipText.append(getToolTipText(region.get()));
			}

			for (int i = 0; i < compoundModel.getModels().size() && i <= 2 ; i++) {

				IScanPointGeneratorModel model = compoundModel.getModels().get(i);

				Method methodToCall = null;
				Class<?> modelClass = model.getClass();

				while (modelClass != null && methodToCall == null) {
					Method[] methods = this.getClass().getMethods();

					for (Method method : methods) {
						if ("getToolTipText".equals(method.getName()) &&
							method.getParameterTypes().length == 1 &&
							method.getParameterTypes()[0].equals(modelClass)) {
							methodToCall = method;
						}
					}

					modelClass = modelClass.getSuperclass();
				}

				if (methodToCall != null) {
					try {
						toolTipText.append((String)methodToCall.invoke(this, model));
					} catch (Exception e) {
						toolTipText.append(e);
					}
				} else {
					toolTipText.append("\n"+super.summarise(statusBean));
				}
			}
			if (compoundModel.getModels().size() > 2) {
				toolTipText.append("...");
			}
		}
		else
		{
			toolTipText.append(super.summarise(statusBean));
		}
		return toolTipText.toString();
	}


	public String getToolTipText(TwoAxisPointSingleModel singlePointModel) {
		return String.format("%nat (%.1f, %.1f)",
				singlePointModel.getX(),
				singlePointModel.getY());
	}

	public String getToolTipText(AbstractTwoAxisGridModel gridModel) {

		StringBuilder toolTipText = new StringBuilder();

		double xStart = gridModel.getBoundingBox().getxAxisStart();
		double yStart = gridModel.getBoundingBox().getyAxisStart();
		double xLength = gridModel.getBoundingBox().getxAxisLength();
		double yLength = gridModel.getBoundingBox().getyAxisLength();

		toolTipText.append(String.format("%n%.1f x %.1f, centre (%.1f, %.1f)%n",
				xLength, yLength, xStart + (xLength/2), yStart + (yLength/2)));

		if (gridModel instanceof TwoAxisGridPointsModel) {
			int xPoints = ((TwoAxisGridPointsModel)gridModel).getxAxisPoints();
			int yPoints = ((TwoAxisGridPointsModel)gridModel).getyAxisPoints();

			if (xPoints == yPoints) {
				toolTipText.append(String.format("%d points per side (%d total)%n",xPoints, xPoints * yPoints));
			}
			else {
				toolTipText.append(String.format("%d points x %d points (%d total)%n",xPoints, yPoints, xPoints * yPoints));
			}
		}
		else if (gridModel instanceof TwoAxisGridStepModel)
		{
			double xStep = ((TwoAxisGridStepModel)gridModel).getxAxisStep();
			double yStep = ((TwoAxisGridStepModel)gridModel).getyAxisStep();

			if (xStep == yStep) {
				toolTipText.append(String.format("Steps of %.1f per side%n",xStep));
			}
			else {
				toolTipText.append(String.format("Steps of %.1f and %.1f%n",xStep, yStep));
			}
		}

		toolTipText.append(String.format("%s  %s%s",
			gridModel.isContinuous() ? "Continuous" : "Stepped",
			gridModel instanceof TwoAxisGridPointsRandomOffsetModel ? "Randomised  " : "",
			gridModel.isAlternating() ? "Alternating" : ""));

		return toolTipText.toString();
	}

	public String getToolTipText(AbstractBoundingLineModel boundingLineModel) {
		StringBuilder toolTipText = new StringBuilder();

		double xStart = boundingLineModel.getBoundingLine().getxStart();
		double yStart = boundingLineModel.getBoundingLine().getyStart();

		BoundingLine line = boundingLineModel.getBoundingLine();
		double xStop = line.getxStart() + (line.getLength() * Math.cos(line.getAngle()));
		double yStop = line.getxStart() + (line.getLength() * Math.sin(line.getAngle()));

		toolTipText.append(String.format("%n(%.1f, %.1f) to (%.1f, %.1f)%n",
				xStart, yStart, xStop, yStop));

		if (boundingLineModel instanceof TwoAxisLinePointsModel) {
			toolTipText.append(String.format("%d points%n", ((TwoAxisLinePointsModel)boundingLineModel).getPoints()));
		}
		else if (boundingLineModel instanceof TwoAxisLineStepModel) {
			toolTipText.append(String.format("%.1f step%n", ((TwoAxisLineStepModel)boundingLineModel).getStep()));

		}

		toolTipText.append(String.format("%s",	boundingLineModel.isContinuous() ? "Continuous" : "Stepped"));
		toolTipText.append(String.format("%s",	boundingLineModel.isAlternating() ? " Alternating" : ""));

		return toolTipText.toString();
	}

	public String getToolTipText(TwoAxisSpiralModel spiralModel) {
		StringBuilder toolTipText = new StringBuilder();
		BoundingBox box = spiralModel.getBoundingBox();

		toolTipText.append(String.format("%nCentre (%.1f, %.1f), radius %.1f%n",
				box.getxAxisStart() + box.getxAxisLength() / 2,
				box.getyAxisStart() + box.getyAxisLength() / 2,
				Math.hypot(box.getxAxisLength() / 2, box.getyAxisLength() / 2)));
		toolTipText.append(String.format("With scale %.1f%n", spiralModel.getScale()));

		toolTipText.append(String.format("%s",	spiralModel.isContinuous() ? "Continuous" : "Stepped"));
		toolTipText.append(String.format("%s",	spiralModel.isAlternating() ? " Alternating" : ""));

		return toolTipText.toString();
	}

	public String getToolTipText(TwoAxisLissajousModel lissajousModel) {
		StringBuilder toolTipText = new StringBuilder();

		toolTipText.append(String.format("%nxLobes = %d, yLobes = %d%s%n(%d points)%n",
				lissajousModel.getLobes(), lissajousModel.getLobes() + 1,
				lissajousModel.getPhaseDifference() == 0 ? "" : ", π/2 phase difference",
				lissajousModel.getPoints()));

		toolTipText.append(String.format("%s",	lissajousModel.isContinuous() ? "Continuous" : "Stepped"));
		toolTipText.append(String.format("%s",	lissajousModel.isAlternating() ? " Alternating" : ""));

		return toolTipText.toString();
	}

	public String getToolTipText(ScanRegion region) {
		if (region.getRoi() instanceof LinearROI) {
			return "Line";
		} else if (region.getRoi() instanceof RectangularROI) {
			return "Rectangle";
		} else if (region.getRoi() instanceof PointROI) {
			return "Point";
		} else if (region.getRoi() instanceof PolygonalROI) {
			return "Polygon";
		} else if (region.getRoi() instanceof CircularROI) {
			return "Circle: radius " + ((CircularROI) region.getRoi()).getRadius();
		} else if (region.getRoi() instanceof HyperbolicROI) {
			return "Hyperbola";
		} else if (region.getRoi() instanceof ParabolicROI) {
			return "Parabola";
		} else if (region.getRoi() instanceof EllipticalROI) {
			return "Ellipse";
		} else if (region.getRoi() instanceof PolylineROI) {
			return "Polyline";
		} else if (region.getRoi() instanceof RingROI) {
			return "Ring";
		} else {
			return "";
		}
	}



}
