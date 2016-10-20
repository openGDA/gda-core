/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.tomography.datacollection.ui.adviewer;

import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.UpdateManager;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

public class VertexFigure extends RectangleFigure implements MouseListener, MouseMotionListener {

	private Point location;
	private ROIDragFigure roiFig;

	public VertexFigure(int i, ROIDragFigure roiFig) {
		this.roiFig = roiFig;
		setSize(i, i);
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	@Override
	public void mouseDragged(MouseEvent me) {
		if (location == null)
			return;
		Point newLocation = me.getLocation();
		if (newLocation == null)
			return;
		Dimension offset = newLocation.getDifference(location);
		if (offset.width == 0 && offset.height == 0)
			return;
		location = newLocation;

		UpdateManager updateMgr = getUpdateManager();
		Rectangle bounds = getBounds();
		updateMgr.addDirtyRegion(getParent(), bounds);

		roiFig.vertexDragged(offset);
		me.consume();
	}

	@Override
	public void mouseEntered(MouseEvent me) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseExited(MouseEvent me) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseHover(MouseEvent me) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseMoved(MouseEvent me) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mousePressed(MouseEvent me) {
		location = me.getLocation();
		me.consume();
	}

	@Override
	public void mouseReleased(MouseEvent me) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseDoubleClicked(MouseEvent me) {
		// TODO Auto-generated method stub
	}
}
