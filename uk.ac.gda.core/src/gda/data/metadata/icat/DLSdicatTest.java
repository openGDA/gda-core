/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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
import gda.data.metadata.VisitEntry;

/**
 * This isn't a unit test but is designed to let you interrogate DiCAT to find visits.
 */
public class DLSdicatTest {

	public static void main(String[] args) throws Exception {
		
		// You'll want to change these
		for (String instrument : new String[] {"i02", "i03", "i04", "i04-1", "i23", "i24"}) {
			
			final String username = String.format("%suser", instrument);
			
			final String gdaDirectory = String.format("/dls_sw/%s/software/gda", instrument);
			final String gdaConfig = String.format("%s/config", gdaDirectory);
			
			System.setProperty(LocalProperties.GDA_GIT_LOC, String.format("%s/workspace_git", gdaDirectory));
			System.setProperty(LocalProperties.GDA_CONFIG, gdaConfig);
			System.setProperty(LocalProperties.GDA_PROPERTIES_FILE, String.format("%s/properties/live/java.properties", gdaConfig));
			
			final DLSdicat dicat = new DLSdicat();
			dicat.setInstrumentName(instrument);
			
			final VisitEntry[] visits = dicat.getMyValidVisits(username);
			
			if (visits.length == 0) {
				System.out.println("No visits.");
			} else {
				System.out.printf("%d visit(s):%n", visits.length);
				for (VisitEntry visit : visits) {
					System.out.printf("\t%s\t%s%n", visit.getVisitID(), visit.getTitle());
				}
			}
		}
	}

}
