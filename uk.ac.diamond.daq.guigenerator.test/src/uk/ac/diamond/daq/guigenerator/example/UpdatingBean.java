/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.guigenerator.example;

import org.eclipse.richbeans.api.generator.RichbeansAnnotations.UiTooltip;
import org.metawidget.inspector.annotation.UiReadOnly;

public interface UpdatingBean {
	public static final String UPDATE_BUTTON_TOOLTIP = "Select to turn on automatic updating of the X and Y values by a background thread";

	@UiTooltip(UPDATE_BUTTON_TOOLTIP)
	public boolean isUpdate();
	public void setUpdate(boolean newValue);
	@UiReadOnly
	public double getX();
	public void setX(double newValue);
	@UiReadOnly
	public double getY();
	public void setY(double newValue);
}
