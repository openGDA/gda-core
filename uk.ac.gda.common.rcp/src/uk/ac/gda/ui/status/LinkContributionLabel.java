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
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

/**
 * Wraps a {@link CLabel} for use in a {@link LinkContributionItem}
 */
public class LinkContributionLabel implements LinkContributionWidget {
	private CLabel label;

	@Override
	public void create(Composite parent) {
		label = new CLabel(parent, SWT.SHADOW_NONE);
	}

	@Override
	public boolean isCreated() {
		return label != null;
	}

	@Override
	public void addMouseListener(MouseListener listener) {
		label.addMouseListener(listener);
	}

	@Override
	public void setImage(Image image) {
		label.setImage(image);
	}

	@Override
	public void setToolTipText(String string) {
		label.setToolTipText(string);
	}

	@Override
	public void setLayoutData(Object layoutData) {
		label.setLayoutData(layoutData);
	}

	@Override
	public void setText(String text) {
		label.setText(text);
	}

	@Override
	public Point getLocation() {
		return label.getLocation();
	}

	@Override
	public boolean isDisposed() {
		return label.isDisposed();
	}

	@Override
	public void setBackground(Color colour) {
		label.setBackground(colour);
	}
}
