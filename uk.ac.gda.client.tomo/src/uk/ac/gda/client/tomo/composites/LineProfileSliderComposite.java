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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.Triangle;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 *
 */
public class LineProfileSliderComposite extends Composite {

	public interface SliderMidpointListener {
		public void handleSliderMidpointMoved(int oldMidpoint, int newMidpoint);
	}

	private List<SliderMidpointListener> sliderMidpointListeners = new ArrayList<SliderMidpointListener>();
	private FigureCanvas figCanvas;
	private Triangle triangleFigure;
	private Dragger dragger;
	private int minY;
	private int maxY;

	public boolean addSliderMidpointListener(SliderMidpointListener sliderMidpointListener) {
		return sliderMidpointListeners.add(sliderMidpointListener);
	}

	public boolean removeSliderMidpointListener(SliderMidpointListener sliderMidpointListener) {
		return sliderMidpointListeners.remove(sliderMidpointListener);
	}

	public LineProfileSliderComposite(Composite parent, int style) {
		super(parent, style);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		setLayout(layout);
		figCanvas = new FigureCanvas(this);
		figCanvas.setContents(getContents());
		figCanvas.getViewport().setContentsTracksHeight(true);
		figCanvas.getViewport().setContentsTracksWidth(true);
		figCanvas.setLayoutData(new GridData(GridData.FILL_BOTH));

	}

	private IFigure getContents() {
		Panel panel = new Panel();
		panel.setSize(10, 10);
		XYLayout manager = new XYLayout();
		panel.setLayoutManager(manager);
		triangleFigure = new Triangle();
		triangleFigure.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_SIZEN));
		triangleFigure.setPreferredSize(12, 12);
		triangleFigure.setDirection(PositionConstants.EAST);
		triangleFigure.setFill(true);
		triangleFigure.setBackgroundColor(ColorConstants.black);
		panel.add(triangleFigure);
		manager.setConstraint(triangleFigure, new Rectangle(0, 5, -1, -1));
		dragger = new Dragger(triangleFigure);

		return panel;
	}

	public void setDraggerInitialLocation(int initialY) {
		triangleFigure.setLocation(new Point(0, initialY));
	}

	class Dragger extends MouseMotionListener.Stub implements MouseListener {
		private final IFigure figure;

		public Dragger(IFigure figure) {
			this.figure = figure;
			figure.addMouseMotionListener(this);
			figure.addMouseListener(this);
		}

		Point last;

		@Override
		public void mouseReleased(MouseEvent e) {
			// FIXME - Add listener updates.

		}

		private void updateListeners() {
			for (SliderMidpointListener sliderMidpointListener : sliderMidpointListeners) {
				sliderMidpointListener.handleSliderMidpointMoved(last.y + 6, triangleFigure.getBounds().y + 5);
			}
		}

		@SuppressWarnings("unused")
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mouseDoubleClicked(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
			last = e.getLocation();
			e.consume();
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			Point p = e.getLocation();
			Dimension delta = p.getDifference(last);
			// Restricted drag movement for the triangle
			Rectangle triangleBounds = triangleFigure.getBounds();
			if (triangleBounds.y + delta.height > minY
					&& triangleBounds.y + triangleBounds.height + delta.height < maxY) {
				triangleFigure.setBounds(triangleBounds.getTranslated(0, delta.height));
				updateListeners();
			}
			last = p;
		}

		protected void dispose() {
			figure.removeMouseListener(this);
			figure.removeMouseMotionListener(this);
		}
	}

	public void setDraggerLimits(int minY, int maxY) {
		this.minY = minY;
		this.maxY = maxY;
	}

	public int getSliderMidpointYLocation() {
		return triangleFigure.getBounds().y + 5;
	}

	public static void main(String[] args) {
		final Display display = new Display();
		final Shell shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setBounds(new org.eclipse.swt.graphics.Rectangle(0, 0, 100, 400));
		shell.setLayout(new GridLayout());
		shell.setBackground(ColorConstants.black);
		LineProfileSliderComposite sliderComposite = new LineProfileSliderComposite(shell, SWT.DOWN);
		shell.setText(sliderComposite.getClass().getName());
		sliderComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		shell.pack();
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	@Override
	public void setEnabled(final boolean enabled) {
		super.setEnabled(enabled);
		if (getDisplay() != null && !getDisplay().isDisposed()) {
			getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					if (!enabled) {
						triangleFigure.setBackgroundColor(ColorConstants.gray);
					} else {
						triangleFigure.setBackgroundColor(ColorConstants.black);
					}

				}
			});
		}
	}

	@Override
	public void dispose() {
		if (dragger != null) {
			dragger.dispose();
		}
		figCanvas.dispose();
		super.dispose();
	}
}
