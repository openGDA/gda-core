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

package gda.util;

import gda.configuration.properties.LocalProperties;
import gda.data.PathConstructor;

/**
 * VisitPath Class
 */
public class VisitPath {
	/**
	 * @return visit path
	 */
	public static String getVisitPath() {
		/*
		 * in java.properties add a definition for the template e.g. gda.data.scan.datawriter.datadir =
		 * /dls/$instrument$/data/$year$/$visit$
		 */
		return PathConstructor.createFromProperty(LocalProperties.GDA_DATAWRITER_DIR);
	}
}
