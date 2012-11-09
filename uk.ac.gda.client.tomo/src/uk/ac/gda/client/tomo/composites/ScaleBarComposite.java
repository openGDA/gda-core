/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo.composites;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

/**
 * ScaleBarComposite show the scale bar - horizontal bar used to display the scale of the image. The width of the scale
 * bar is determined dynamically and can be set on this composite.
 */
public class ScaleBarComposite extends Composite {
	private int scaleWidth;

	public ScaleBarComposite(Composite parent, int style) {
		super(parent, style);
		this.setBackground(ColorConstants.black);
	}

	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		return new Point(scaleWidth, 12);
	}

	/**
	 * Set the width of the scale bar - <code>pack(true)</code> is called to layout the composite to show the correct
	 * display of the scale bar.
	 * 
	 * @param scaleWidth
	 */
	public void setScaleWidth(int scaleWidth) {
		this.scaleWidth = scaleWidth;
		pack(true);
	}
}