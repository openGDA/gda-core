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

package gda.icons;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GdaIcons {

	private static final Logger logger = LoggerFactory.getLogger(GdaIcons.class);

	public static Image getWindowIcon() {
		return getLogo(16);
	}
	
	public static Image getLogo(int size) {
		final String filename = "GDALogo" + size + ".png";
		final URL url = GdaIcons.class.getResource(filename);
		if (url == null) {
			logger.warn(filename + " not found");
			return null;
		}
		Image image = Toolkit.getDefaultToolkit().getImage(url);
		return image;
	}

}
