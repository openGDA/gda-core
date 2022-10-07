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

import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.LayoutManager;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.UpdateManager;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Canvas;

public class AxisDragFigure extends ImageFigure implements MouseListener, MouseMotionListener, KeyListener {

	private Point location;
	private DataCollectionMJPegViewInitialiser mjPegViewInitialiser;
	private Canvas canvas;
	private boolean x_axis;

	public AxisDragFigure(boolean x_axis, final DataCollectionMJPegViewInitialiser mjPegViewInitialiser, Canvas canvas) {
		super();
		this.x_axis = x_axis;
		this.mjPegViewInitialiser = mjPegViewInitialiser;
		this.canvas = canvas;
		setVisible(true);
		setOpaque(false);
		addMouseListener(this);
		addMouseMotionListener(this);
		canvas.addKeyListener(this);
	}

	public void stop() {
		removeMouseListener(this);
		removeMouseMotionListener(this);
		canvas.removeKeyListener(this);
		if (getImage() != null) {
			getImage().dispose();
		}
	}

	@Override
	public void keyReleased(KeyEvent ke) {
	}

	@Override
	public void keyPressed(KeyEvent ke) {
		if (ke.keyCode == SWT.ESC) {
			mjPegViewInitialiser.handleAxisDragCancel(x_axis);
		}
	}

	@Override
	public void mouseReleased(MouseEvent me) {
		if (location == null)
			return;
		LayoutManager layoutMgr = getParent().getLayoutManager();
		Rectangle constraint = (Rectangle) layoutMgr.getConstraint(AxisDragFigure.this);
		mjPegViewInitialiser.handleAxisDrag(x_axis, x_axis ? constraint.x : constraint.y);
		me.consume();
	}

	@Override
	public void mousePressed(MouseEvent me) {
		location = me.getLocation();
		me.consume();
	}

	@Override
	public void mouseDoubleClicked(MouseEvent me) {
		me.consume();
	}

	@Override
	public void mouseMoved(MouseEvent me) {
	}

	@Override
	public void mouseHover(MouseEvent me) {
		me.consume();
	}

	@Override
	public void mouseExited(MouseEvent me) {
	}

	@Override
	public void mouseEntered(MouseEvent me) {
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
		bounds = constraint.getCopy().translate(x_axis ? offset.width : 0, x_axis ? 0 : offset.height);
		layoutMgr.setConstraint(this, bounds);
		this.translate(x_axis ? offset.width : 0, x_axis ? 0 : offset.height);
		updateMgr.addDirtyRegion(getParent(), bounds);
		me.consume();
	}
}
