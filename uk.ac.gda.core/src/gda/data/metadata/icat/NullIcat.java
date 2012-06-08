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

package gda.data.metadata.icat;

import gda.configuration.properties.LocalProperties;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.metadata.Metadata;
import gda.data.metadata.VisitEntry;


/**
 * If no Icat configured, then this should be used as the singleton returned by IcatProvider to return meaningful error
 * messages and responses.
 */
public class NullIcat extends IcatBase implements Icat {

	@Override
	public boolean icatInUse() {
		return false;
	}
	
	/**
	 * Return the default experiment defined by either the metadata or the java property
	 */
	@Override
	public VisitEntry[] getMyValidVisits(String username) throws Exception {
		Metadata metadata = GDAMetadataProvider.getInstance();
		String defVisit = metadata.getMetadataValue("defVisit");
		if (defVisit != null && !defVisit.equals("")) {
			defVisit = LocalProperties.get("gda.defVisit");
		}
		
		VisitEntry[] out = new VisitEntry[1];
		out[0] = new VisitEntry(defVisit, "");
		return out;
	}
	
	@Override
	public String getCurrentInformation(String accessName) throws Exception {
		return "";
	}

	@Override
	protected String getExperimentTitleAccessName() {
		return "";
	}

	@Override
	protected String getValue(String visitIDFilter, String userNameFilter, String accessName) throws Exception {
		return "";
	}

	@Override
	protected String getVisitIDAccessName() {
		return "";
	}

}
