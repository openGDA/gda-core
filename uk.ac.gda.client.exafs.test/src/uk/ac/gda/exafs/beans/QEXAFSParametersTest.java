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

package uk.ac.gda.exafs.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import org.eclipse.core.runtime.content.IContentDescriber;
import org.junit.Test;

import uk.ac.gda.beans.exafs.QEXAFSParameters;
import uk.ac.gda.beans.validation.InvalidBeanMessage;
import uk.ac.gda.exafs.ui.describers.QEXAFSParametersDescriber;
import uk.ac.gda.util.PackageUtils;

public class QEXAFSParametersTest {
	private final String validParametersFilePath = PackageUtils.getTestPath(getClass())	+ "QEXAFSParameters_Valid.xml";

	@Test
	public void testDescriber() {
		try {
			final InputStream contents = new FileInputStream(new File(validParametersFilePath));
			final QEXAFSParametersDescriber describer = new QEXAFSParametersDescriber();
			assertEquals(IContentDescriber.VALID, describer.describe(contents, null));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testCreateFromXML_FileDoesNotExist() {
		try {
			QEXAFSParameters.createFromXML("testfiles/DoesNotExist");
			fail("File does not exist");
		} catch (Exception ex) {
			if (!(ex instanceof FileNotFoundException)) {
				fail("Invalid exception thrown - " + ex.getMessage());
			}
		}
	}

	@Test
	public void testCreateFromXML() throws Exception {

		final QEXAFSParameters expectedValue = new QEXAFSParameters();
		expectedValue.setInitialEnergy(10000.0);
		expectedValue.setFinalEnergy(11000.0);
		expectedValue.setSpeed(10.0);
		expectedValue.setStepSize(1.0);
		expectedValue.setTime(104.89);
		//expectedValue.setNumberPoints(2000);
		//expectedValue.setTotalTime(1);
		//expectedValue.setChooseNumberPoints(true);

		final QEXAFSParameters s = QEXAFSParameters.createFromXML(validParametersFilePath);
		final List<InvalidBeanMessage> errors = new ExafsValidatorWrapperForTesting().validateQEXAFSParametersForTest(s);
		if (errors.size() > 0) {
			fail(errors.get(0).getPrimaryMessage());
		}
		if (!expectedValue.equals(s)) {
			fail("Values read are incorrect - " + s.toString());
		}

	}

}
