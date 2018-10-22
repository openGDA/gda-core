/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package gda.jython;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import gda.jython.GDAJythonInterpreter.OverwriteLock;
import uk.ac.gda.common.util.EclipseUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({EclipseUtils.class})
public class GDAJythonInterpreterTest {

	OverwriteLock overwriting;

	@Before
	public void setup() throws IOException {
		PowerMockito.mockStatic(EclipseUtils.class);
		when(EclipseUtils.resolveBundleFolderFile(anyString())).thenReturn(new File(""));
		overwriting = new GDAJythonInterpreter.OverwriteLock();
	}

	@Test
	public void testOverwritingDisabledByDefault() throws Exception {
		assertThat("Overwriting should be disabled by default", not(overwriting.enabled()));
	}

	@Test
	public void testOverwritingContextManager() throws Exception {
		assertThat("Overwriting should start disabled", not(overwriting.enabled()));
		overwriting.__enter__(null);
		assertThat("Overwriting should be enabled after __enter__", overwriting.enabled());
		overwriting.__exit__(null, null);
		assertThat("Overwriting should be disabled after __exit__", not(overwriting.enabled()));
	}

	@Test
	public void testNestedContextsDontAffectOuterContexts() throws Exception {
		assertThat("Overwriting should start disabled", not(overwriting.enabled()));
		overwriting.__enter__(null);
		assertThat("Overwriting should be enabled after __enter__", overwriting.enabled());
		overwriting.__enter__(null);
		assertThat("Overwriting should be enabled after entering nested context", overwriting.enabled());
		overwriting.__exit__(null, null);
		assertThat("Overwriting should be enabled leaving nested context", overwriting.enabled());
		overwriting.__exit__(null, null);
		assertThat("Overwriting should be disabled after leaving outer context", not(overwriting.enabled()));
	}

	@Test
	public void testExitingBeforeEnteringDoesntAffectNextContext() throws Exception {
		assertThat("Overwriting should start disabled", not(overwriting.enabled()));
		overwriting.__exit__(null, null);
		assertThat("Overwriting should be disabled after invalid exit", not(overwriting.enabled()));
		overwriting.__enter__(null);
		assertThat("Overwriting context should work after incorrectly calling exit", overwriting.enabled());
	}

	@Test
	public void testOverwritingString() throws Exception {
		assertThat("String format of overwrite should be constant", overwriting.toString(), equalTo("ScannableOverwritingBypass"));
	}
}
