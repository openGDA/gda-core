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
public class IcatTest {

	public static void main(String[] args) throws Exception {

		// You'll want to change these
		final String username = "qny31541";
		for (String instrument : new String[] {"i02", "i03", "i04", "i04-1", "i23", "i24"}) {

			System.setProperty(LocalProperties.GDA_PROPERTIES_FILE, "src/gda/data/metadata/icat/DLSdicatTest.properties");

			final IcatBase icat = (IcatBase) IcatProvider.getInstance();
			icat.setInstrumentName(instrument);

			final VisitEntry[] visits = icat.getMyValidVisits(username);

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
