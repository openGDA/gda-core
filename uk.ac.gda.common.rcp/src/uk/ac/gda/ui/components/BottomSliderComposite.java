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

package uk.ac.gda.ui.components;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.Triangle;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Slider composite which shows on the tomography alignment view in the right window during profiling.
 */
public class BottomSliderComposite extends Composite {

	private Triangle sliderTriangle;

	public BottomSliderComposite(Composite parent, int style) {
		super(parent, style);
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		this.setLayout(gridLayout);

		Canvas canvas = new Canvas(this, SWT.BORDER);

		canvas.setLayoutData(new GridData(GridData.FILL_BOTH));
		canvas.setBackground(ColorConstants.black);

		/**/
		Composite sliderComposite = new Composite(this, SWT.None);
		sliderComposite.setLayout(new FillLayout());
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.heightHint = 12;
		sliderComposite.setLayoutData(layoutData);
		FigureCanvas sliderCanvas = new FigureCanvas(sliderComposite);
		sliderCanvas.setContents(getContents());
		sliderCanvas.getViewport().setContentsTracksHeight(true);
		sliderCanvas.getViewport().setContentsTracksWidth(true);

		/**/

	}

	private IFigure getContents() {
		Panel panel = new Panel();
		panel.setSize(10, 10);
		XYLayout manager = new XYLayout();
		panel.setLayoutManager(manager);
		sliderTriangle = new Triangle();
		sliderTriangle.setPreferredSize(12, 12);
		sliderTriangle.setDirection(PositionConstants.NORTH);
		sliderTriangle.setFill(true);
		sliderTriangle.setBackgroundColor(ColorConstants.black);
		sliderTriangle.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_SIZEWE));
		panel.add(sliderTriangle);
		manager.setConstraint(sliderTriangle, new Rectangle(0, 0, -1, -1));
		new Dragger(sliderTriangle);

		return panel;
	}

	class Dragger extends org.eclipse.draw2d.MouseMotionListener.Stub implements MouseListener {
		public Dragger(IFigure figure) {
			figure.addMouseMotionListener(this);
			figure.addMouseListener(this);
		}

		Point last;

		@Override
		public void mouseReleased(MouseEvent e) {
			// FIXME-Ravi - Add listener updates.

		}

		private void updateListeners() {
			// for (SliderMidpointListener sliderMidpointListener : sliderMidpointListeners) {
			// sliderMidpointListener.handleSliderMidpointMoved(last.y + 6, topTriangleFigure.getBounds().y + 5);
			// }
		}

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
			Rectangle sliderBounds = sliderTriangle.getBounds();
			if (sliderBounds.x + delta.width > 0
					&& sliderBounds.x + sliderBounds.width + delta.width < sliderTriangle.getParent().getBounds().width) {
				sliderTriangle.setBounds(sliderBounds.getTranslated(delta.width, 0));
				updateListeners();
			}
			last = p;
		}
	}

	public int getSliderMidpointYLocation() {
		return sliderTriangle.getBounds().y + 5;
	}

	public static void main(String[] args) {
		final Display display = new Display();
		final Shell shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setBounds(new org.eclipse.swt.graphics.Rectangle(0, 0, 100, 400));
		shell.setLayout(new GridLayout());
		shell.setBackground(ColorConstants.black);
		BottomSliderComposite zoomedImageComposite = new BottomSliderComposite(shell, SWT.None);
		shell.setText(zoomedImageComposite.getClass().getName());
		zoomedImageComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
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
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (!enabled) {
			sliderTriangle.setBackgroundColor(ColorConstants.gray);
		} else {
			sliderTriangle.setBackgroundColor(ColorConstants.black);
		}
	}

}
