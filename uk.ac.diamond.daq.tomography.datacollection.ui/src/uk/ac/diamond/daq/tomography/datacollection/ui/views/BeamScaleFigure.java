/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.tomography.datacollection.ui.views;

import java.text.DecimalFormat;

import javax.vecmath.Vector2d;

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;

public class BeamScaleFigure extends Figure {

	private static final DecimalFormat scaleFormat = new DecimalFormat("0.0"); // http://leepoint.net/nodes-java/data/strings/conversion/num2string.html
	private Vector2d micronsPerPixel = new Vector2d(1.0, 1.0);
	private Dimension scaleBoxSize = new Dimension(100, 100);

	private Label xScaleLabel;
	private Label yScaleLabel;
	private RectangleFigure rectangle;

	public BeamScaleFigure() {
		BorderLayout manager = new BorderLayout();
		setLayoutManager(manager);
		rectangle = new RectangleFigure();
		rectangle.setAlpha(50);
		rectangle.setForegroundColor(ColorConstants.yellow);
		rectangle.setBackgroundColor(ColorConstants.yellow);
		add(rectangle);
		manager.setConstraint(rectangle, BorderLayout.CENTER);
		xScaleLabel = new Label();
		xScaleLabel.setForegroundColor(ColorConstants.red);
		add(xScaleLabel);
		manager.setConstraint(xScaleLabel, BorderLayout.TOP);
		yScaleLabel = new Label();
		yScaleLabel.setForegroundColor(ColorConstants.red);
		add(yScaleLabel);
		manager.setConstraint(yScaleLabel, BorderLayout.RIGHT);
		update();
	}

	public double getXScale() {
		return micronsPerPixel.x;
	}

	public double getYScale() {
		return micronsPerPixel.y;
	}

	public void setXScale(double scale) {
		this.micronsPerPixel.x = scale;
		update();
	}

	public void setYScale(double scale) {
		this.micronsPerPixel.y = scale;
		update();
	}

	public void setBeamSize(int w, int h) {
		scaleBoxSize.width = w;
		scaleBoxSize.height = h;
		update();
	}

	@Override
	protected boolean useLocalCoordinates() {
		return true;
	}

	/**
	 * Updates label contents and box size. This needs to be called from UI thread
	 */
	private void update() {
		String xScaleFormat = scaleFormat.format(scaleBoxSize.width * micronsPerPixel.x / (1024.0 / 10));
		xScaleLabel.setText(xScaleFormat + " μm");
		String yScaleFormat = scaleFormat.format(scaleBoxSize.height * micronsPerPixel.y / (768.0 / 10));
		yScaleLabel.setText(yScaleFormat + " μm");
		rectangle.setBounds(new Rectangle(rectangle.getBounds().getTopLeft(), scaleBoxSize));
	}
}
