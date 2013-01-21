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

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.ImageFigure;

public class ImageWithTransparencyFigure extends ImageFigure {
	double transparency = 0;
	
	public void setTransparency(double transparency) {
		this.transparency = transparency;
		super.repaint();
	}
	
	@Override
	public void paintFigure(Graphics graphics) {
		int alpha = (int) ((1.0-transparency) * 255.0);
		graphics.setAlpha(alpha);
		super.paintFigure(graphics);
	}
}