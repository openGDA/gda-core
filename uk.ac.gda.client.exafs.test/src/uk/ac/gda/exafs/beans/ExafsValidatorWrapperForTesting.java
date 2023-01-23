/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

import java.util.Collections;
import java.util.List;

import gda.exafs.scan.ExafsValidator;
import uk.ac.gda.beans.exafs.IDetectorParameters;
import uk.ac.gda.beans.exafs.IOutputParameters;
import uk.ac.gda.beans.exafs.ISampleParameters;
import uk.ac.gda.beans.exafs.QEXAFSParameters;
import uk.ac.gda.beans.exafs.XanesScanParameters;
import uk.ac.gda.beans.exafs.XasScanParameters;
import uk.ac.gda.beans.exafs.XesScanParameters;
import uk.ac.gda.beans.validation.InvalidBeanMessage;

/**
 * Class to give access to protected methods in ExafsValidator
 */
public class ExafsValidatorWrapperForTesting extends ExafsValidator {

	public List<InvalidBeanMessage> validateXasScanParametersForTest(XasScanParameters x, double beamlineMinEnergy, double beamlineMaxEnergy) {
		return validateXasScanParameters(x, beamlineMinEnergy, beamlineMaxEnergy);
	}

	public List<InvalidBeanMessage> validateIOutputParametersForTest(IOutputParameters iOutputParams) {
		return validateIOutputParameters(iOutputParams);
	}

	public List<InvalidBeanMessage> validateXanesScanParametersForTest(XanesScanParameters x) {
		return validateXanesScanParameters(x);
	}

	public List<InvalidBeanMessage> validateQEXAFSParametersForTest(QEXAFSParameters x) {
		return validateQEXAFSParameters(x);
	}

	public List<InvalidBeanMessage> validateXesScanParametersForTest(XesScanParameters x, IDetectorParameters detParams) {
		return validateXesScanParameters(x, detParams);
	}

	@Override
	protected List<InvalidBeanMessage> validateISampleParameters(ISampleParameters sampleParameters) {
		return Collections.emptyList();
	}
}
