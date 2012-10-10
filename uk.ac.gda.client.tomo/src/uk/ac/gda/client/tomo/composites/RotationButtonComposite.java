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

import org.eclipse.draw2d.ArrowButton;
import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.InputEvent;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Triangle buttons used in the Tomography view to represent 90° rotations.
 */
public class RotationButtonComposite extends Composite {
	private static final Color BUSY_COLOUR = ColorConstants.yellow;
	private static final Color DIABLED_COLOUR = ColorConstants.gray;
	private static final Color READY_COLOUR = ColorConstants.darkGreen;
	private final int direction;
	private FigureCanvas figCanvas;
	private ArrowButton arrowButton;
	private final String lblText;
	private final boolean ctrlPressRequired;
	private List<RotationButtonSelectionListener> listeners;

	/**
	 * Button selection listener
	 */
	public interface RotationButtonSelectionListener {
		/**
		 * 
		 */
		public void handleButtonClicked(RotationButtonComposite button);
	}

	private MouseListener mousePressListener = new MouseListener() {

		@Override
		public void mousePressed(MouseEvent me) {

		}

		@Override
		public void mouseReleased(MouseEvent me) {
			if (ctrlPressRequired) {
				if ((me.getState() & InputEvent.CONTROL) == 0) {
					return;
				}
			}
			showButtonSelected();
			fireButtonClicked();
		}

		private void fireButtonClicked() {
			for (RotationButtonSelectionListener selListener : listeners) {
				selListener.handleButtonClicked(RotationButtonComposite.this);
			}
		}

		@Override
		public void mouseDoubleClicked(MouseEvent me) {
			// Do nothing
		}

	};

	public boolean addSelectionListener(RotationButtonSelectionListener listener) {
		if (listeners == null) {
			listeners = new ArrayList<RotationButtonComposite.RotationButtonSelectionListener>();
		}
		return listeners.add(listener);
	}

	protected void showButtonSelected() {
		setTriangleColor(BUSY_COLOUR);
	}

	protected void showButtonDeSelected() {
		setTriangleColor(READY_COLOUR);
	}

	public boolean removeSelectionListener(RotationButtonSelectionListener listener) {
		return listeners.remove(listener);
	}

	public RotationButtonComposite(Composite parent, int direction, String lblText) {
		this(parent, direction, lblText, false);
	}

	public RotationButtonComposite(Composite parent, int direction, String lblText, boolean ctrlPressRequired) {
		super(parent, SWT.None);
		this.direction = direction;
		this.lblText = lblText;
		this.ctrlPressRequired = ctrlPressRequired;
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		setLayout(layout);
		this.setBackground(ColorConstants.white);
		figCanvas = new FigureCanvas(this);
		figCanvas.setContents(getContents());
		figCanvas.getViewport().setContentsTracksHeight(true);
		figCanvas.getViewport().setContentsTracksWidth(true);
		figCanvas.setLayoutData(new GridData(GridData.FILL_BOTH));
		figCanvas.setBorder(new LineBorder(1));
	}

	private IFigure getContents() {
		IFigure panel = new Panel();
		BorderLayout lm = new BorderLayout();
		
		panel.setLayoutManager(lm);
		arrowButton = new ArrowButton(direction);
		arrowButton.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_HAND));
		arrowButton.setBackgroundColor(ColorConstants.white);
		Label lbl = new Label(lblText);
		lbl.setBackgroundColor(ColorConstants.black);
		lbl.setForegroundColor(ColorConstants.black);
		setTriangleColor(READY_COLOUR);
		
		arrowButton.add(lbl);
		panel.add(arrowButton);
		panel.setConstraint(arrowButton, BorderLayout.CENTER);
		lbl.addMouseListener(mousePressListener);
		return panel;
	}

	protected void setTriangleColor(Color color) {
		Object object = arrowButton.getChildren().get(0);
		if (object instanceof IFigure) {
			((IFigure) object).setBackgroundColor(color);
		}
	}


	public static void main(String[] args) {
		final Display display = new Display();
		final Shell shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setBounds(0, 0, 25, 25);
		shell.setLayout(new GridLayout());
		shell.setBackground(READY_COLOUR);
		RotationButtonComposite sliderComposite = new RotationButtonComposite(shell, SWT.UP, "Test");
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
	public void setEnabled(boolean enabled) {
		if (enabled) {
			setTriangleColor(READY_COLOUR);
		} else {
			setTriangleColor(DIABLED_COLOUR);
		}
		super.setEnabled(enabled);
	}
}
