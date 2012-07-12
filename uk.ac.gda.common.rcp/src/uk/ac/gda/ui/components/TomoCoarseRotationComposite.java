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

import java.util.List;

import org.eclipse.draw2d.Button;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.InputEvent;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
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
 * Class that inherits its functionality from the {@link TomoFineRotationComposite} but display buttons instead of
 * labels along the slider.
 */
public class TomoCoarseRotationComposite extends RotationSliderComposite {

	private Button btnCenter;
	private Button btnLeft;
	private Button btnRight;
	private Button btnLeftCenter;
	private Button btnRightCenter;

	private SliderButtonMouseListener ml;
	private final static double DEGREE_FULL = 360.0;
	private final static double DEGREE_BASE = 180.0;
	protected static final int SLIDER_START_TOLERANCE = 1;

	public TomoCoarseRotationComposite(Composite parent, int style, String[] labels, boolean ctrlPressRequired) {
		super(parent, style, labels, ctrlPressRequired);
	}

	@Override
	protected void initialize() {

		ml = new SliderButtonMouseListener();
	}

	@Override
	protected double getTotalSliderDegree() {
		return DEGREE_FULL;
	}

	@Override
	protected double getDegreeBase() {
		return DEGREE_BASE;
	}

	@Override
	protected Dimension getSliderTriangleDimension() {
		return new Dimension(60, 30);
	}

	@Override
	protected void addSliderMarkers(IFigure panel) {
		String[] labels = getLabels();

		if (labels != null && labels.length > 0) {
			switch (labels.length) {
			case 1:
				btnCenter = addButton(panel, labels[0]);
				setButtonProperties(btnCenter);
				break;
			case 2:
				btnLeft = addButton(panel, labels[0]);
				setButtonProperties(btnLeft);
				//
				btnRight = addButton(panel, labels[1]);
				setButtonProperties(btnRight);
				break;
			case 3:
				btnLeft = addButton(panel, labels[0]);
				setButtonProperties(btnLeft);
				//
				btnCenter = addButton(panel, labels[1]);
				setButtonProperties(btnCenter);
				//
				btnRight = addButton(panel, labels[2]);
				setButtonProperties(btnRight);
				break;
			case 4:
				btnLeft = addButton(panel, labels[0]);
				setButtonProperties(btnLeft);
				//
				btnLeftCenter = addButton(panel, labels[1]);
				setButtonProperties(btnLeftCenter);
				//
				btnRightCenter = addButton(panel, labels[2]);
				setButtonProperties(btnRightCenter);
				//
				btnRight = addButton(panel, labels[3]);
				setButtonProperties(btnRight);
				break;
			case 5:
				btnLeft = addButton(panel, labels[0]);
				setButtonProperties(btnLeft);
				//
				btnLeftCenter = addButton(panel, labels[1]);
				setButtonProperties(btnLeftCenter);
				//
				btnCenter = addButton(panel, labels[2]);
				setButtonProperties(btnCenter);
				//
				btnRightCenter = addButton(panel, labels[3]);
				setButtonProperties(btnRightCenter);
				//
				btnRight = addButton(panel, labels[4]);
				setButtonProperties(btnRight);
				break;
			}
		}
	}

	private Button addButton(IFigure panel, String btnLabel) {
		Button btn = new Button(btnLabel);
		btn.setBorder(new LineBorder(1));
		panel.add(btn);
		return btn;
	}

	@Override
	protected void layoutDownSliderMarkers(Rectangle parentBounds) {
		Dimension textExtents = null;

		int labelY = sliderBoundary.getLocation().y + 3;
		if (btnLeft != null) {
			Label lblLeft = getButtonLabel(btnLeft);
			textExtents = lblLeft.getTextUtilities().getTextExtents(lblLeft.getText(), getFont());
			lblLeft.setSize(textExtents);
			Dimension buttonSize = new Dimension(textExtents.width, textExtents.height);

			btnLeft.setSize(buttonSize);

			btnLeft.setLocation(new Point(3, labelY));
		}
		if (btnLeftCenter != null) {
			Label lblLeftCenter = getButtonLabel(btnLeftCenter);
			textExtents = lblLeftCenter.getTextUtilities().getTextExtents(lblLeftCenter.getText(), getFont());
			lblLeftCenter.setSize(textExtents);
			Dimension buttonSize = new Dimension(textExtents.width, textExtents.height);

			btnLeftCenter.setSize(buttonSize);
			double x = sliderBoundary.getBounds().width / 4.0 + sliderTriangle.getBounds().width / 2.0
					- buttonSize.width / 2.0;
			btnLeftCenter.setLocation(new Point(x, labelY));
		}
		if (btnCenter != null) {
			Label lblCenter = getButtonLabel(btnCenter);
			textExtents = lblCenter.getTextUtilities().getTextExtents(lblCenter.getText(), getFont());
			lblCenter.setSize(textExtents);
			Dimension buttonSize = new Dimension(textExtents.width, textExtents.height);

			btnCenter.setSize(buttonSize);
			int x = (parentBounds.width / 2) - buttonSize.width / 2;
			btnCenter.setLocation(new Point(x, labelY));
		}
		if (btnRightCenter != null) {
			Label lblRightCenter = getButtonLabel(btnRightCenter);
			textExtents = lblRightCenter.getTextUtilities().getTextExtents(lblRightCenter.getText(), getFont());
			lblRightCenter.setSize(textExtents);
			Dimension buttonSize = new Dimension(textExtents.width, textExtents.height);

			btnRightCenter.setSize(buttonSize);
			double x = (3.0 * sliderBoundary.getBounds().width) / 4.0 + sliderTriangle.getBounds().width / 2.0
					- buttonSize.width / 2.0;
			btnRightCenter.setLocation(new Point(x, labelY));
		}
		if (btnRight != null) {
			Label lblRight = getButtonLabel(btnRight);
			textExtents = lblRight.getTextUtilities().getTextExtents(lblRight.getText(), getFont());
			lblRight.setSize(textExtents);
			Dimension buttonSize = new Dimension(textExtents.width, textExtents.height);

			btnRight.setSize(buttonSize);
			btnRight.setLocation(new Point(parentBounds.width - (textExtents.width + 2), labelY));
		}
	}

	/**
	 * Sets the default button properties of font, bg color, fg color, and cursor
	 * 
	 * @param button
	 */
	private void setButtonProperties(Button button) {
		getButtonLabel(button).setFont(fontRegistry.get(TEXT_SMALL_7));
		button.setBackgroundColor(ColorConstants.black);
		button.setForegroundColor(ColorConstants.white);
		button.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_HAND));
		getButtonLabel(button).addMouseListener(ml);
	}

	private class SliderButtonMouseListener implements MouseListener {

		@Override
		public void mousePressed(org.eclipse.draw2d.MouseEvent mouseEvent) {
			// do nothing
		}

		private void performButtonPressedAction(Button btn) {
			double initialSliderDegree = getCurrentSliderDegree();
			double degreeMovedTo = 0;
			if (btnCenter.equals(btn)) {
				// Button Center is pressed. Move the slider to the center position
				degreeMovedTo = 0;
			} else if (btnLeft.equals(btn)) {
				// Button Left is pressed. Move the slider to the left end.
				degreeMovedTo = -180;
			} else if (btnLeftCenter.equals(btn)) {
				// Button left center is pressed. Move the slider to the center of left and center
				degreeMovedTo = -90;
			} else if (btnRight.equals(btn)) {
				// Button right is pressed. Move the slider to the right end.
				degreeMovedTo = 180;
			} else if (btnRightCenter.equals(btn)) {
				// Button right center is pressed. Move the slider to the center of right and center.
				degreeMovedTo = 90;
			}
			moveSliderTo(degreeMovedTo);
			updateDegreeMovedTo(degreeMovedTo);
		}

		@Override
		public void mouseReleased(MouseEvent mouseEvent) {
			if ((mouseEvent.getState() & InputEvent.CONTROL) == 0) {
				return;
			}
			Label lbl = (Label) mouseEvent.getSource();
			Button btn = (Button) lbl.getParent();
			showButtonSelected(btn);
			performButtonPressedAction(btn);
			mouseEvent.consume();
		}

		@Override
		public void mouseDoubleClicked(MouseEvent me) {
			// Do nothing when mouse is double clicked.

		}
	}

	@Override
	protected void layoutUpSliderMarkers(Rectangle parentBounds) {
		Dimension textExtents = null;
		if (btnLeft != null) {
			Label lblLeft = getButtonLabel(btnLeft);
			textExtents = lblLeft.getTextUtilities().getTextExtents(lblLeft.getText(), getFont());
			lblLeft.setSize(textExtents);
			Dimension buttonSize = new Dimension(textExtents.width + 5, textExtents.height);
			btnLeft.setSize(buttonSize);
			btnLeft.setLocation(new Point(3, parentBounds.height - (20 + textExtents.height)));
		}
		if (btnLeftCenter != null) {
			Label lblLeftCenter = getButtonLabel(btnLeftCenter);
			textExtents = lblLeftCenter.getTextUtilities().getTextExtents(lblLeftCenter.getText(), getFont());
			lblLeftCenter.setSize(textExtents);
			Dimension buttonSize = new Dimension(textExtents.width + 5, textExtents.height);

			btnLeftCenter.setSize(buttonSize);
			btnLeftCenter
					.setLocation(new Point(parentBounds.width / 4, parentBounds.height - (20 + textExtents.height)));
		}
		if (btnCenter != null) {
			Label lblCenter = getButtonLabel(btnCenter);
			textExtents = lblCenter.getTextUtilities().getTextExtents(lblCenter.getText(), getFont());
			lblCenter.setSize(textExtents);
			Dimension buttonSize = new Dimension(textExtents.width + 5, textExtents.height);

			btnCenter.setSize(buttonSize);
			btnCenter.setLocation(new Point(parentBounds.width / 2, parentBounds.height - (20 + textExtents.height)));
		}
		if (btnRightCenter != null) {
			Label lblRightCenter = getButtonLabel(btnRightCenter);
			textExtents = lblRightCenter.getTextUtilities().getTextExtents(lblRightCenter.getText(), getFont());
			lblRightCenter.setSize(textExtents);
			Dimension buttonSize = new Dimension(textExtents.width + 5, textExtents.height);

			btnRightCenter.setSize(buttonSize);
			btnRightCenter.setLocation(new Point((3 * parentBounds.width) / 4, parentBounds.height
					- (20 + textExtents.height)));
		}
		if (btnRight != null) {
			Label lblRight = getButtonLabel(btnRight);
			textExtents = lblRight.getTextUtilities().getTextExtents(lblRight.getText(), getFont());
			lblRight.setSize(textExtents);
			Dimension buttonSize = new Dimension(textExtents.width + 5, textExtents.height);

			btnRight.setSize(buttonSize);
			btnRight.setLocation(new Point(parentBounds.width - (textExtents.width + 2), parentBounds.height
					- (20 + textExtents.height)));
		}
	}

	protected void showButtonSelected(Button btn) {
		btn.setBackgroundColor(ColorConstants.red);
	}

	public void showButtonDeSelected() {
		getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				if (btnCenter != null) {
					setButtonProperties(btnCenter);
				}
				if (btnLeft != null) {
					setButtonProperties(btnLeft);
				}
				if (btnLeftCenter != null) {
					setButtonProperties(btnLeftCenter);
				}
				if (btnRight != null) {
					setButtonProperties(btnRight);
				}
				if (btnRightCenter != null) {
					setButtonProperties(btnRightCenter);
				}
			}
		});
	}

	@SuppressWarnings("rawtypes")
	private Label getButtonLabel(Button btn) {
		List children = btn.getChildren();
		for (Object child : children) {
			if (child instanceof Label) {
				return (Label) child;
			}
		}
		return null;
	}

	/* For testing as Java application */
	public static void main(String[] args) {
		final Display display = new Display();
		final Shell shell = new Shell(display, SWT.SHELL_TRIM);

		shell.setLayout(new GridLayout());
		shell.setBackground(ColorConstants.black);
		TomoCoarseRotationComposite sliderComposite = new TomoCoarseRotationComposite(shell, SWT.DOWN, new String[] {
				"10", "90", "25", "35", "45" }, true);
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

	public void setMotorBusy(final boolean isBusy) {
		if (!isDisposed()) {
			getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					if (isBusy) {
						sliderTriangle.setBackgroundColor(ColorConstants.yellow);
					} else {
						sliderTriangle.setBackgroundColor(ColorConstants.darkGreen);
					}
				}
			});

		}

	}

}
