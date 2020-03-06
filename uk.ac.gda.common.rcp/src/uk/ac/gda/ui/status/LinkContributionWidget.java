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

import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

/**
 * Interface for a widget in a {@link LinkContributionItem}
 * <p>
 * Implementing classes will typically wrap a GUI element such as a label or a button
 */
public interface LinkContributionWidget {

	void create(Composite parent);

	boolean isCreated();

	boolean isDisposed();

	void setImage(Image image);

	void setText(String text);

	void setToolTipText(String string);

	void setLayoutData(Object layoutData);

	void setBackground(Color colour);

	void addMouseListener(MouseListener listener);

	Point getLocation();
}
