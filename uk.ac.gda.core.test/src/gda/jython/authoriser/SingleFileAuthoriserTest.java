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

package gda.jython.authoriser;

import static gda.jython.authoriser.Authoriser.DEFAULT_LEVEL_PROPERTY;
import static gda.jython.authoriser.Authoriser.DEFAULT_STAFF_LEVEL_PROPERTY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;

import gda.configuration.properties.LocalProperties;

public class SingleFileAuthoriserTest {
	private static final String TEST_DIRECTORY = "testfiles/gda/jython/authoriser";
	@Test
	public void validFileIsParsedCorrectly() throws ConfigurationException, IOException {
		var sfa = new SingleFileAuthoriser(TEST_DIRECTORY, "sfa_valid_permissions");
		// both level and staff defined
		assertThat(sfa.isLocalStaff("staff-level3"), is(true));
		assertThat(sfa.getAuthorisationLevel("staff-level3"), is(3));

		assertThat(sfa.isLocalStaff("not-staff-level2"), is(false));
		assertThat(sfa.getAuthorisationLevel("not-staff-level2"), is(2));

		// just staff defined
		assertThat(sfa.isLocalStaff("staff"), is(true));
		assertThat(sfa.getAuthorisationLevel("staff"), is(3));
		assertThat(sfa.isLocalStaff("not-staff"), is(false));
		assertThat(sfa.getAuthorisationLevel("not-staff"), is(1));

		// just level
		assertThat(sfa.isLocalStaff("level2"), is(false));
		assertThat(sfa.getAuthorisationLevel("level2"), is(2));

		// unspecified
		assertThat(sfa.isLocalStaff("not-in-file-staff"), is(false));
		assertThat(sfa.getAuthorisationLevel("not-in-file-level"), is(1));

		// invalid properties
		assertThat(sfa.isLocalStaff("invalid-staff"), is(false));
		assertThat(sfa.getAuthorisationLevel("invalid-level"), is(1));
	}

	@Test(expected = ConfigurationException.class)
	public void invalidFileIsNotIgnored() throws ConfigurationException, IOException {
		@SuppressWarnings("unused")
		var sfa = new SingleFileAuthoriser(TEST_DIRECTORY, "sfa_invalid_permissions");
	}

	@Test
	public void defaultsUseProperties() throws ConfigurationException, IOException {
		try {
			LocalProperties.set(DEFAULT_LEVEL_PROPERTY, "2");
			LocalProperties.set(DEFAULT_STAFF_LEVEL_PROPERTY, "4");
			var sfa = new SingleFileAuthoriser(TEST_DIRECTORY, "sfa_valid_permissions");
			assertThat(sfa.getAuthorisationLevel("staff"), is(4));
			assertThat(sfa.getAuthorisationLevel("not-staff"), is(2));
		} finally {
			LocalProperties.clearProperty(DEFAULT_LEVEL_PROPERTY);
			LocalProperties.clearProperty(DEFAULT_STAFF_LEVEL_PROPERTY);
		}
	}
}
