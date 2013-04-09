/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.epics.adviewer.composites.imageviewer;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;

public class CrossHairFigure  extends Figure {

	private RectangleFigure lineAcross;
	private RectangleFigure lineDown;
	Color color=ColorConstants.red;
	int lineWidth=3;
	int alpha=50;
	int height=100;
	int width=100;
	
	
	public CrossHairFigure() {
		setLayoutManager(new XYLayout());
		lineAcross = new RectangleFigure();
		lineDown = new RectangleFigure();
		update();
	}
	@Override
	protected boolean useLocalCoordinates() {
		return true;
	}
	void update(){
		lineAcross.setAlpha(alpha);
		lineAcross.setForegroundColor(color);
		lineAcross.setBackgroundColor(color);
		lineAcross.setSize(width,lineWidth);
		lineDown.setAlpha(alpha);
		lineDown.setForegroundColor(color);
		lineDown.setBackgroundColor(color);
		lineDown.setSize(lineWidth,height);
		if( lineAcross.getParent() == this)
			remove(lineAcross);
		add(lineAcross, new Rectangle(0,height/2,-1,-1));
		if( lineDown.getParent() == this)
			remove(lineDown);
		add(lineDown, new Rectangle(width/2,0,-1,-1));
		setPreferredSize(width, height);
		setBounds(new Rectangle(0,0,width, height));
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
		update();
	}

	public int getLineWidth() {
		return lineWidth;
	}

	public void setLineWidth(int lineWidth) {
		this.lineWidth = lineWidth;
		update();
	}

	public int getAlpha() {
		return alpha;
	}

	public void setAlpha(int alpha) {
		this.alpha = alpha;
		update();
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
		update();
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
		update();
	}

	@Override
	public void setSize(int w, int h) {
		height=h;
		width=w;
		update();
	}
	
}