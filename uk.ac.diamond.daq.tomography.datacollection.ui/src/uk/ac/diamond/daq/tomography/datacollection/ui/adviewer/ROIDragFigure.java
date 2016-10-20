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

import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.UpdateManager;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Canvas;

public class ROIDragFigure extends RectangleFigure implements KeyListener, MouseListener, MouseMotionListener{

	private DataCollectionMJPegViewInitialiser mjPegViewInitialiser;
	private Canvas canvas;
	private RectangleFigure topLeft;//, topRight, bottomLeft, bottomRight;

	private Point location;

	public ROIDragFigure(final DataCollectionMJPegViewInitialiser mjPegViewInitialiser, Canvas canvas) {
		super();
		this.mjPegViewInitialiser = mjPegViewInitialiser;
		this.canvas = canvas;
		addMouseListener(this);
		addMouseMotionListener(this);
		canvas.addKeyListener(this);
		topLeft = new VertexFigure(4, this);
		add(topLeft, new Rectangle(-2,-2,4,4));
/*		topRight = new RectangleFigure();
		add(topRight);
		bottomLeft = new RectangleFigure();
		add(bottomLeft);
		bottomRight = new RectangleFigure();
		add(bottomRight);
*/	}

	@Override
	public void keyReleased(KeyEvent ke) {
	}

	@Override
	public void keyPressed(KeyEvent ke) {
		if (ke.keyCode == SWT.ESC) {
			mjPegViewInitialiser.handleROIDragCancel();
		}
		if (ke.keyCode == 13) {
			mjPegViewInitialiser.handleROIDrag();
		}
	}

	public void stop() {
		removeMouseListener(this);
		removeMouseMotionListener(this);
		canvas.removeKeyListener(this);
	}

	@Override
	public void mousePressed(MouseEvent me) {
		location = me.getLocation();
		me.consume();
	}

	@Override
	public void mouseReleased(MouseEvent me) {
	}

	@Override
	public void mouseDoubleClicked(MouseEvent me) {
		// TODO Auto-generated method stub
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
		LayoutManager layoutMgr = getParent().getLayoutManager();
		Rectangle bounds = getBounds();
		updateMgr.addDirtyRegion(getParent(), bounds);
		Rectangle constraint = (Rectangle) layoutMgr.getConstraint(this);
		bounds = constraint.getCopy().translate(offset.width, offset.height);
		// System.out.println("MouseDrag offset :" + offset.width + "," + offset.height);
		layoutMgr.setConstraint(this, bounds);
		ROIDragFigure.this.translate(offset.width, offset.height);
		updateMgr.addDirtyRegion(getParent(), bounds);
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

	public void vertexDragged(Dimension offset) {
		// for now topLeft dragged
		if (offset.width == 0 && offset.height == 0)
			return;
		UpdateManager updateMgr = getUpdateManager();
		LayoutManager layoutMgr = getParent().getLayoutManager();
		Rectangle bounds = getBounds();
		updateMgr.addDirtyRegion(getParent(), bounds);
		Rectangle constraint = (Rectangle) layoutMgr.getConstraint(this);
		bounds = new Rectangle(constraint.getTopLeft().translate(offset), constraint.getBottomRight().translate(-offset.width, -offset.height));
		// System.out.println("MouseDrag offset :" + offset.width + "," + offset.height);
		layoutMgr.setConstraint(this, bounds);
		this.translate(offset.width, offset.height);
		this.setSize(bounds.getSize());
		updateMgr.addDirtyRegion(getParent(), bounds);
	}
}
