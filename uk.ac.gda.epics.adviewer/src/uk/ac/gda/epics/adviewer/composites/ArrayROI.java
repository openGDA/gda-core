/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.epics.adviewer.composites;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

public class ArrayROI extends Composite {
	private Canvas canvas;
	Figure fig1;
	private RectangleFigure fig2;
	private RectangleFigure topFig;

	public ArrayROI(Composite parent, int style) {
		super(parent, style);
		canvas = new Canvas(parent, SWT.NONE);
		LightweightSystem lws = new LightweightSystem(canvas); 

		fig1 = new Figure();
		GridDataFactory.fillDefaults().grab(true, true).applyTo(canvas);
		lws.setContents(fig1);

		
		fig2 = new RectangleFigure();
		fig2.setFill(true);
		fig2.setSize(400, 50);
		fig2.setLineWidth(5);
		fig2.setForegroundColor(ColorConstants.yellow);
		fig2.setBackgroundColor(ColorConstants.yellow);
		fig2.setAlpha(50);

		fig1.setLayoutManager(new XYLayout());
		fig1.add(fig2, new Rectangle(0, 0, -1, -1));
		
		
		topFig = new RectangleFigure();
		topFig.setFill(true);
		topFig.setSize(80, 30);
		topFig.setLineWidth(5);
		topFig.setForegroundColor(ColorConstants.red);
		topFig.setAlpha(50);

		fig1.add(topFig, new Rectangle(10, 10, -1, -1));
	}

	public static void main(String[] args) {
		final Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new org.eclipse.swt.layout.GridLayout(1, false));
		shell.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));

		Group c = new Group(shell, SWT.SHADOW_ETCHED_IN);
		c.setLayout(new GridLayout());
		GridDataFactory.fillDefaults().grab(true, true).applyTo(c);
		final ArrayROI imageViewer = new ArrayROI(c, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(imageViewer);

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
}
