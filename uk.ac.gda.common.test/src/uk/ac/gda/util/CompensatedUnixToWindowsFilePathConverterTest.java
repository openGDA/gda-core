/*-
 * Copyright © 2025 Diamond Light Source Ltd.
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

package uk.ac.gda.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Test;

public final class CompensatedUnixToWindowsFilePathConverterTest {

	private UnixToWindowsFilePathConverter converter;
	private String checkedWindowsSubString;

	@Before
	public void setUp() {
		converter = new CompensatedUnixToWindowsFilePathConverter();
		converter.setUnixSubString("/dls/i95/example/");
	}

	@Test
	public void testThatCompensationPreservesLowerCaseWindowsPath() {
		var expectedUnchangedExample = "t:\\example\\sec\\min\\hour";
		givenAWindowsSubString(expectedUnchangedExample);
		whenWindowsSubStringIsChecked();
		assertThat(checkedWindowsSubString, equalTo(expectedUnchangedExample));
	}

	@Test
	public void testThatCompensationPreservesLowerCaseDriveWindowsPath() {
		var expectedUnchangedExample = "c:\\cAmEl\\exAGGeraTion";
		givenAWindowsSubString(expectedUnchangedExample);
		whenWindowsSubStringIsChecked();
		assertThat(checkedWindowsSubString, equalTo(expectedUnchangedExample));
	}

	@Test
	public void testThatCompensationOnlyLowersDriveLetterCaseOfWindowsPath() {
		parametricallyTestCompensationUsing("M:\\PARIS\\peckham\\Rome", "m:\\PARIS\\peckham\\Rome");
	}

	@Test
	public void testThatCompensationCorrectsUnixifiedWindowsPath() {
		parametricallyTestCompensationUsing("y:/property/mANGLed/somehow", "y:\\property\\mANGLed\\somehow");
	}

	@Test
	public void testThatCompensationCorrectsUnixifiedWindowsPathWhenDriverLetterIsUpperCase() {
		parametricallyTestCompensationUsing("G:/zebRa/pandering12", "g:\\zebRa\\pandering12");
	}

	@Test
	public void testConverttoInternal() {
		converter.setUnixSubString("/dls/i13-1/data");
		converter.setWindowsSubString("y:\\data");

		var conversionResult = converter.converttoInternal("/dls/i13-1/data/2011/0-0/");
		assertThat(conversionResult, equalTo("y:\\data\\2011\\0-0\\"));
	}

	@Test
	public void testConverttoInternalWithLinuxFileSeparator() {
		converter.setUnixSubString("/dls/i13-1/data");
		converter.setWindowsSubString("R:/data");

		var conversionResult = converter.converttoInternal("/dls/i13-1/data/2011/0-0/");
		assertThat(conversionResult, equalTo("r:\\data\\2011\\0-0\\"));
	}

	@Test
	public void testConverttoExternal() {
		converter.setWindowsSubString("Z:\\data");
		converter.setUnixSubString("/dls/i13-1/data");

		var conversionResult = converter.converttoExternal("z:\\data\\2011\\0-0\\");
		assertThat(conversionResult, equalTo("/dls/i13-1/data/2011/0-0/") );
	}

	@Test
	public void testConverttoExternalWithLinuxFileSeparator() {
		converter.setUnixSubString("/dls/i13-1/data");
		converter.setWindowsSubString("v:/data");

		var conversionResult = converter.converttoExternal("v:\\data\\2011\\0-0\\");
		assertThat(conversionResult, equalTo("/dls/i13-1/data/2011/0-0/"));
	}

	@Test
	public void testConverttoExternalWithMixedFileSeparators() {
		converter.setUnixSubString("/dls/i13-1/data");
		converter.setWindowsSubString("q:/data\\subdir");

		var conversionResult = converter.converttoExternal("q:\\data\\subdir\\2011\\0-0\\");
		assertThat(conversionResult, equalTo("/dls/i13-1/data/2011/0-0/"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testThatSettingWindowsPathWithoutDriveLetterThrowsIllegalArgument() {
		var malformedWindowsPath = "..\\driveless\\path";
		givenAWindowsSubString(malformedWindowsPath);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testThatSettingWindowsPathWithDriveLettersThrowsIllegalArgument() {
		var malformedWindowsPath = "NO:\\inappropriate\\path";
		givenAWindowsSubString(malformedWindowsPath);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testThatSettingWindowsPathWithDrivePrefixesThrowsIllegalArgument() {
		var malformedWindowsPath = "C:d:\\inappropriate\\path";
		givenAWindowsSubString(malformedWindowsPath);
	}

	private void parametricallyTestCompensationUsing(String input, String expectedCompensatedResult) {
		givenAWindowsSubString(input);
		whenWindowsSubStringIsChecked();
		assertThat(checkedWindowsSubString, equalTo(expectedCompensatedResult));
	}

	private void givenAWindowsSubString(String wss) {
		converter.setWindowsSubString(wss);
	}

	private void whenWindowsSubStringIsChecked() {
		checkedWindowsSubString = converter.getWindowsSubString();
	}
}
