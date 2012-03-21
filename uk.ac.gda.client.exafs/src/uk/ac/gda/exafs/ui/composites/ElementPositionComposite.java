/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.composites;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.gda.richbeans.components.wrappers.SpinnerWrapper;
import uk.ac.gda.richbeans.components.wrappers.TextWrapper;

/**
 *
 */
public class ElementPositionComposite extends Composite {

	private TextWrapper name;
	private SpinnerWrapper wheelPosition;
	private TextWrapper principleElement;
	/**
	 * @param parent
	 * @param style
	 */
	public ElementPositionComposite(Composite parent, int style) {
		super(parent, style);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		setLayout(gridLayout);

		final Label elementNameLabel = new Label(this, SWT.NONE);
		elementNameLabel.setText("Reference Name");

		name = new TextWrapper(this, SWT.BORDER);
		name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		Label lblPrincipleElement = new Label(this, SWT.NONE);
		lblPrincipleElement.setText("Principle Element");
		
		principleElement = new TextWrapper(this, SWT.BORDER);
		principleElement.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		final Label wheelPositionLabel_1 = new Label(this, SWT.NONE);
		wheelPositionLabel_1.setText("Wheel Position");

		this.wheelPosition = new SpinnerWrapper(this, SWT.BORDER);
	}

	/**
	 * @return Returns the wheelPosition.
	 */
	public SpinnerWrapper getWheelPosition() {
		return wheelPosition;
	}

	/**
	 * @return Returns the name.
	 */
	@SuppressWarnings("all")
	public TextWrapper getName() {
		return name;
	}

	/**
	 * @return Returns the principleElement.
	 */
	public TextWrapper getPrincipleElement() {
		return principleElement;
	}

}
