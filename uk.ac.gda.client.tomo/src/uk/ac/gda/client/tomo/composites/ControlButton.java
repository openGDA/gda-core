/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class ControlButton extends Composite {

	private Button btn;

	public ControlButton(FormToolkit toolkit, Composite parent, String text) {
		this(toolkit, parent, text, SWT.PUSH);
	}

	public ControlButton(FormToolkit toolkit, Composite parent, String text, int style) {
		super(parent, style);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 2;
		layout.marginHeight = 2;
		setLayout(layout);
		setBackground(ColorConstants.cyan);

		btn = toolkit.createButton(this, text, SWT.PUSH);
		btn.setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	@Override
	public void addListener(int eventType, Listener listener) {
		if (btn != null) {
			btn.addListener(eventType, listener);
		} else {
			super.addListener(eventType, listener);
		}
	}

	@Override
	public void setFont(Font font) {
		if (btn != null) {
			btn.setFont(font);
		} else {
			super.setFont(font);
		}
	}

	public void setText(String btnText) {
		btn.setText(btnText);
	}

	@Override
	public void setBackground(Color color) {
		if (btn != null) {
			btn.setBackground(color);
		} else {
			super.setBackground(color);
		}
	}

	@Override
	public void setForeground(Color color) {
		if (btn != null) {
			btn.setForeground(color);
		} else {
			super.setForeground(color);
		}
	}

	@Override
	public Color getForeground() {
		if (btn != null) {
			return btn.getForeground();
		}
		return super.getForeground();
	}

	@Override
	public Color getBackground() {
		if (btn != null) {
			return btn.getBackground();
		}
		return super.getBackground();
	}
}
