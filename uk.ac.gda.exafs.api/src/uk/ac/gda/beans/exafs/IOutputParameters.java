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

package uk.ac.gda.beans.exafs;

import java.util.List;

import uk.ac.gda.util.beans.xml.XMLRichBean;


/**
 * Common interface for all Output beans
 */
public interface IOutputParameters extends XMLRichBean{

	public String getAsciiFileName();

	public String getAsciiDirectory();

	public String getNexusDirectory();

	List<MetadataParameters> getMetadataList();

	public void setAsciiFileName(String name);

	public String getAfterScriptName();

	public String getBeforeScriptName();

	public String getBeforeFirstRepetition();

	public List<SignalParameters> getSignalList();

}
