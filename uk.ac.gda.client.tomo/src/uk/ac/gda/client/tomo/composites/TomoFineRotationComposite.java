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
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
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
 * This composite shows a slider - unlike the slider or scale SWT provides, this slider is in the form of a triangle.<br>
 * Listeners can be attached to the slider to to relate its position. <br>
 * <br>
 * Run this as a Java application to see the slider in action.
 */
public class TomoFineRotationComposite extends RotationSliderComposite {

	private Label lblLeft;
	private Label lblLeftCenter;
	private Label lblCenter;
	private Label lblRightCenter;
	private Label lblRight;

	public TomoFineRotationComposite(Composite parent, int style) {
		this(parent, style, null);
	}

	/**
	 * Constructor that labels as a parameter. There can be upto 5 labels.<br>
	 * If there is 1 - label will be placed in the center.<br>
	 * If there are 2 labels - the labels will be placed - left end and right end<br>
	 * if there are 3 labels - the labels will be placed - left, center, and right<br>
	 * If there are 4 labels, - the labels will be placed left, between center and left, between center and right, and
	 * right<br>
	 * if there are 5 labels - the lab els will be placed left, between center and left, center, between center and
	 * right, and right<br>
	 * 
	 * @param parent
	 * @param style
	 * @param labels
	 */
	public TomoFineRotationComposite(Composite parent, int style, String[] labels, boolean ctrlPressRequired) {
		super(parent, style, labels, ctrlPressRequired);

	}

	/**
	 * Constructor that labels as a parameter. There can be upto 5 labels.<br>
	 * If there is 1 - label will be placed in the center.<br>
	 * If there are 2 labels - the labels will be placed - left end and right end<br>
	 * if there are 3 labels - the labels will be placed - left, center, and right<br>
	 * If there are 4 labels, - the labels will be placed left, between center and left, between center and right, and
	 * right<br>
	 * if there are 5 labels - the labels will be placed left, between center and left, center, between center and
	 * right, and right<br>
	 * 
	 * @param parent
	 * @param style
	 * @param labels
	 */
	public TomoFineRotationComposite(Composite parent, int style, String[] labels) {
		this(parent, style, labels, false);
	}

	/**
	 * Place holder to add the markers along the slider
	 * 
	 * @param panel
	 */
	@Override
	protected void addSliderMarkers(IFigure panel) {
		String[] labels = getLabels();
		if (labels != null && labels.length > 0) {
			switch (labels.length) {
			case 1:
				lblCenter = addLabel(panel, labels[0]);
				break;
			case 2:
				/**/
				lblLeft = addLabel(panel, labels[0]);
				/**/
				lblRight = addLabel(panel, labels[1]);
				break;
			case 3:
				lblLeft = addLabel(panel, labels[0]);
				/**/
				lblCenter = addLabel(panel, labels[1]);
				/**/
				lblRight = addLabel(panel, labels[2]);
				break;
			case 4:
				lblLeft = addLabel(panel, labels[0]);
				/**/
				lblLeftCenter = addLabel(panel, labels[1]);
				/**/
				lblRightCenter = addLabel(panel, labels[2]);
				/**/
				lblRight = addLabel(panel, labels[3]);
				break;
			case 5:
				lblLeft = addLabel(panel, labels[0]);
				/**/
				lblLeftCenter = addLabel(panel, labels[1]);
				/**/
				lblCenter = addLabel(panel, labels[2]);
				/**/
				lblRightCenter = addLabel(panel, labels[3]);
				/**/
				lblRight = addLabel(panel, labels[4]);
				break;
			}
		}
	}

	private Label addLabel(IFigure panel, String lblText) {
		Label lbl = new Label(lblText);
		lbl.setFont(fontRegistry.get(TEXT_SMALL_7));
		panel.add(lbl);
		return lbl;
	}

	/**
	 * layout for up slider markers - in case the triangle is pointing downwards, layout the slider markers along the
	 * slider in the this method.
	 * 
	 * @param parentBounds
	 */
	@Override
	protected void layoutUpSliderMarkers(Rectangle parentBounds) {
		if (lblLeft != null) {
			Dimension textExtents = lblLeft.getTextUtilities().getTextExtents(lblLeft.getText(), getFont());
			lblLeft.setSize(textExtents);
			lblLeft.setLocation(new Point(sliderBoundary.getLocation().x - 2, parentBounds.height
					- (22 + textExtents.height)));
		}
		if (lblLeftCenter != null) {
			Dimension textExtents = lblLeftCenter.getTextUtilities().getTextExtents(lblLeftCenter.getText(), getFont());
			lblLeftCenter.setSize(textExtents);
			lblLeftCenter
					.setLocation(new Point(parentBounds.width / 4, parentBounds.height - (22 + textExtents.height)));
		}
		if (lblCenter != null) {
			Dimension textExtents = lblCenter.getTextUtilities().getTextExtents(lblCenter.getText(), getFont());
			lblCenter.setSize(textExtents);
			lblCenter.setLocation(new Point(parentBounds.width / 2, parentBounds.height - (22 + textExtents.height)));
		}
		if (lblRightCenter != null) {
			Dimension textExtents = lblRightCenter.getTextUtilities().getTextExtents(lblRightCenter.getText(),
					getFont());
			lblRightCenter.setSize(textExtents);
			lblRightCenter.setLocation(new Point((3 * parentBounds.width) / 4, parentBounds.height
					- (22 + textExtents.height)));
		}
		if (lblRight != null) {
			Dimension textExtents = lblRight.getTextUtilities().getTextExtents(lblRight.getText(), getFont());
			lblRight.setSize(textExtents);
			lblRight.setLocation(new Point(sliderBoundary.getSize().width + 3, parentBounds.height
					- (22 + textExtents.height)));
		}
	}

	/**
	 * layout for down slider markers - in case the triangle is pointing downwards, layout the slider markers along the
	 * slider in the this method.
	 * 
	 * @param parentBounds
	 */
	@Override
	protected void layoutDownSliderMarkers(Rectangle parentBounds) {
		if (lblLeft != null) {
			lblLeft.setSize(lblLeft.getTextUtilities().getTextExtents(lblLeft.getText(), getFont()));
			lblLeft.setLocation(new Point(3, 20));
		}
		if (lblLeftCenter != null) {
			lblLeftCenter.setSize(lblLeftCenter.getTextUtilities().getTextExtents(lblLeftCenter.getText(), getFont()));
			lblLeftCenter.setLocation(new Point(parentBounds.width / 4, 20));
		}
		if (lblCenter != null) {
			lblCenter.setSize(lblCenter.getTextUtilities().getTextExtents(lblCenter.getText(), getFont()));
			lblCenter.setLocation(new Point(parentBounds.width / 2, 20));
		}
		if (lblRightCenter != null) {
			lblRightCenter.setSize(lblRightCenter.getTextUtilities()
					.getTextExtents(lblRightCenter.getText(), getFont()));
			lblRightCenter.setLocation(new Point((3 * parentBounds.width) / 4, 20));
		}
		if (lblRight != null) {
			Dimension textExtents = lblRight.getTextUtilities().getTextExtents(lblRight.getText(), getFont());
			lblRight.setSize(textExtents);
			lblRight.setLocation(new Point(parentBounds.width - (textExtents.width + 2), 20));
		}
	}

	public static void main(String[] args) {
		final Display display = new Display();
		final Shell shell = new Shell(display, SWT.SHELL_TRIM);

		shell.setLayout(new GridLayout());
		shell.setBackground(ColorConstants.black);
		TomoFineRotationComposite sliderComposite = new TomoFineRotationComposite(shell, SWT.UP, new String[] { "10",
				"90", "25", "35", "45" });
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
	protected Dimension getSliderDimension() {
		return new Dimension(50, 30);
	}

	@Override
	protected double getDegreeBase() {
		return 5.0;
	}

	@Override
	protected double getTotalSliderDegree() {
		return 10.0;
	}
}
