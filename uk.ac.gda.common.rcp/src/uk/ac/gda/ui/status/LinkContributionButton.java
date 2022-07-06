/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.status;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class LinkContributionButton implements LinkContributionWidget {
	private Button button;

	@Override
	public void create(Composite parent) {
		button = new Button(parent, SWT.PUSH);
	}

	@Override
	public boolean isCreated() {
		return button != null;
	}

	@Override
	public void addMouseListener(MouseListener listener) {
		button.addMouseListener(listener);
	}

	@Override
	public void setImage(Image image) {
		button.setImage(image);
	}

	@Override
	public void setToolTipText(String string) {
		button.setToolTipText(string);
	}

	@Override
	public void setLayoutData(Object layoutData) {
		button.setLayoutData(layoutData);
	}

	@Override
	public void setText(String text) {
		button.setText(text);
	}

	@Override
	public Point getLocation() {
		return button.getLocation();
	}

	@Override
	public boolean isDisposed() {
		return button.isDisposed();
	}

	@Override
	public void setBackground(Color colour) {
		button.setBackground(colour);
	}
}
