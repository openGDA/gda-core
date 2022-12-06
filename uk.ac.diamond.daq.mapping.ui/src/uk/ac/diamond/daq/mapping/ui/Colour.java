/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui;

import org.eclipse.swt.graphics.RGB;

public enum Colour {
	BLUE(new RGB(179, 204, 255)),
	PURPLE(new RGB(221,221,248)),
	YELLOW(new RGB(255, 204, 128)),
	GREEN(new RGB(140, 217, 179)),
	RED(new RGB(244,166,152)),
	WHITE(new RGB(255, 255, 255));

	private final RGB rgb;

	private Colour(final RGB rgb) {
		this.rgb = rgb;
	}

	public RGB getRGB() {
		return rgb;
	}
}
