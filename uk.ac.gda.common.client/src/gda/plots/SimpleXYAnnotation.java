/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.plots;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.text.TextUtilities;
import org.jfree.ui.RectangleEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a clickable TextAnnotation by extending the standard XYTextAnnotation. Note that it is called 'Simple' for
 * the usual 'related to SimplePlot' reason. It is actually more complicated than XYTextAnnotation.
 */
public class SimpleXYAnnotation extends XYTextAnnotation implements ChartMouseListener {
	private static final Logger logger = LoggerFactory.getLogger(SimpleXYAnnotation.class);

	private boolean clickable = true;

	private Shape hotspot;

	/**
	 * Same as the super constructor, creates an XYAnnotation with the given string and position.
	 * 
	 * @param string
	 *            the text of the XYAnnotation
	 * @param x
	 *            the x position
	 * @param y
	 *            the y position
	 */
	public SimpleXYAnnotation(String string, double x, double y) {
		super(string, x, y);
	}

	/**
	 * Same as the super constructor, but adds a value for clickable.
	 * 
	 * @param string
	 *            the text of the XYAnnotation
	 * @param x
	 *            the x position
	 * @param y
	 *            the y position
	 * @param clickable
	 *            true if the SimpleXYAnnotation accepts mouse clicks
	 */
	public SimpleXYAnnotation(String string, double x, double y, boolean clickable) {
		super(string, x, y);
		this.clickable = clickable;
	}

	/**
	 * Implements the XYAnnotation interface. This is actually a copy of the superclass method EXCEPT: it adds the shape
	 * to the entity list based on the value of clickable and not whether or not there is a ToolTip; and it save the
	 * hotpsot so that it can recognize when it is the clicked entity.
	 * 
	 * @param g2
	 *            the graphics device.
	 * @param plot
	 *            the plot.
	 * @param dataArea
	 *            the data area.
	 * @param domainAxis
	 *            the domain axis.
	 * @param rangeAxis
	 *            the range axis.
	 * @param rendererIndex
	 *            the renderer index.
	 * @param info
	 *            an optional info object that will be populated with entity information.
	 */
	@Override
	public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis,
			int rendererIndex, PlotRenderingInfo info) {
		PlotOrientation orientation = plot.getOrientation();
		RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(plot.getDomainAxisLocation(), orientation);
		RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(plot.getRangeAxisLocation(), orientation);

		float anchorX = (float) domainAxis.valueToJava2D(getX(), dataArea, domainEdge);
		float anchorY = (float) rangeAxis.valueToJava2D(getY(), dataArea, rangeEdge);

		if (orientation == PlotOrientation.HORIZONTAL) {
			float tempAnchor = anchorX;
			anchorX = anchorY;
			anchorY = tempAnchor;
		}

		g2.setFont(getFont());
		g2.setPaint(getPaint());
		TextUtilities.drawRotatedString(getText(), g2, anchorX, anchorY, getTextAnchor(), getRotationAngle(),
				getRotationAnchor());
		hotspot = TextUtilities.calculateRotatedStringBounds(getText(), g2, anchorX, anchorY, getTextAnchor(),
				getRotationAngle(), getRotationAnchor());

		String toolTip = getToolTipText();
		String url = getURL();

		if (clickable || toolTip != null || url != null) {
			addEntity(info, hotspot, rendererIndex, toolTip, url);
		}
	}

	/**
	 * Implements the ChartMouseListener interface, called if there is a mouse click event over the annotation.
	 * 
	 * @param event
	 *            the ChartMouseEvent
	 */
	@Override
	public void chartMouseClicked(ChartMouseEvent event) {
		ChartEntity entity = event.getEntity();

		if (entity != null)
			logger.debug("And Entity was: " + entity);
	}

	/**
	 * Implements the ChartMouseListener interface, called if there is a mouse moved event over the annotation.
	 * 
	 * @param event
	 *            the ChartMouseEvent
	 */
	@Override
	public void chartMouseMoved(ChartMouseEvent event) {
		// Does nothing on purpose
	}

	/**
	 * Returns whether or not the SimpleXYAnnotation responds to mouse clicks.
	 * 
	 * @return true if clickable
	 */
	public boolean isClickable() {
		return clickable;
	}

	/**
	 * Sets whether or not the SimpleXYAnnotation responds to mouse clicks.
	 * 
	 * @param clickable
	 *            true if this should respond to clicks
	 */
	public void setClickable(boolean clickable) {
		this.clickable = clickable;
	}

}
