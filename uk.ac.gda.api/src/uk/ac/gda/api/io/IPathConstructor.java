/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.api.io;

import java.util.Map;

/**
 * Used to expose PathConstructor to projects not explicitly importing core.
 */
public interface IPathConstructor {

	String getDefaultDataDir();

	String getFromTemplate(String template);

	String createFromDefaultProperty();

	String createFromProperty(String property);

	String getVisitSubdirectory(String subdirectory);

	String getClientVisitSubdirectory(String subdirectory);

	@Deprecated(since="GDA 9.3")
	String createFromRCPProperties();

	String getVisitDirectory();

	String createFromTemplate(String template);

	String getClientVisitDirectory();

	String createFromProperty(String property, Map<String, String> overrides);

	String getDefaultPropertyName();

}
