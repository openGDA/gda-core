/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.gda.client.live.stream.view.customui;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

/**
 * This is class calculates the line coordinates a cross and grid
 * on top of video stream.
 */
public class EpicsCameraViewerGridModel {

	private int centreX;
	private int centreY;
	private int spacing;
	private int imageSizeX;
	private int imageSizeY;
	private int[] verticalLineOfCross;
	private int[] horizontalLineOfCross;
	private List<Line2D.Double> gridLines = new ArrayList<>();


	public EpicsCameraViewerGridModel(int centreX, int centreY, int spacing,
			int imageSizeX, int imageSizeY) {
		this.centreX = centreX;
		this.centreY = centreY;
		this.spacing = spacing;
		this.imageSizeX = imageSizeX;
		this.imageSizeY = imageSizeY;
	}

	/**
	 * Calculates the X,Y coordinates for each lines
	 */
	public void calculateGridLines() {
		if (centreX > spacing) {
			int numberOfLinesToDraw = centreX/spacing;
			for (int i = 1; i <= numberOfLinesToDraw; i++) {
				gridLines.add(new Line2D.Double(centreX - spacing*i, 0, centreX - spacing*i, imageSizeY));

			}
		}
		if (imageSizeX-centreX > spacing) {
			int numberOfLinesToDraw = ((imageSizeX-centreX)/spacing);
			for (int i = 1; i <= numberOfLinesToDraw; i++) {
				gridLines.add(new Line2D.Double(centreX + spacing*i, 0, centreX + spacing*i, imageSizeY));
			}
		}
		if (centreY > spacing) {
			int numberOfLinesToDraw = centreY/spacing;
			for (int i = 1; i <= numberOfLinesToDraw; i++) {
				gridLines.add(new Line2D.Double(0, centreY - spacing*i, imageSizeX, centreY - spacing*i));
			}
		}
		if (imageSizeY-centreY > spacing) {
			int numberOfLinesToDraw = ((imageSizeY-centreY)/spacing);
			for (int i = 1; i<= numberOfLinesToDraw; i++) {
				gridLines.add(new Line2D.Double(0, centreY + spacing*i, imageSizeX, centreY + spacing*i));

			}
		}
	}

	public double[] getLineStart(Line2D.Double line) {
		return new double[] {line.getP1().getX(), line.getP1().getY()};
	}

	public double[] getLineEnd(Line2D.Double line) {
		return new double[] {line.getP2().getX(), line.getP2().getY()};
	}

	public int[] getVerticalLineOfCross() {
		return verticalLineOfCross;
	}

	public int[] getHorizontalLineOfCross() {
		return horizontalLineOfCross;
	}

	public List<Line2D.Double> getGridLines() {
		return gridLines;
	}

}
