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

package org.eclipse.scanning.test.malcolm.real;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.eclipse.scanning.api.malcolm.MalcolmVersion;
import org.junit.Test;

public class MalcolmVersionTest {

	@Test
	public void testMalcolmVersion() {
		assertMalcolmVersion("version:pymalcolm:4.2", MalcolmVersion.VERSION_4_2);
		assertMalcolmVersion("version:pymalcolm:4.2.abc", MalcolmVersion.VERSION_4_2);

		// if we can't parse the version we use a fallback, note: malcolm should never
		// return a version we can't parse, see DAQ-3027
		assertMalcolmVersion("version:pymalcolm:kinematics.working+6.43e62647",
				MalcolmVersion.FALLBACK_VERSION);
	}

	public void assertMalcolmVersion(String versionString, MalcolmVersion expectedVersion) {
		final MalcolmVersion version = MalcolmVersion.fromVersionString(versionString);
		assertThat(version, is(equalTo(expectedVersion)));
	}

}
