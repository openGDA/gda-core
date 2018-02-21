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

package uk.ac.gda.exafs.beans;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import uk.ac.gda.beans.exafs.b18.B18SampleParameters;
import uk.ac.gda.beans.exafs.b18.SampleParameterMotorPosition;
import uk.ac.gda.exafs.ui.dialogs.ParameterValuesForBean;
import uk.ac.gda.exafs.ui.dialogs.SpreadsheetViewHelperClasses;

public class SpreadsheetViewHelperClassesTest {

	public B18SampleParameters getSampleParameters() {
		SampleParameterMotorPosition sampleMotorPosition = new SampleParameterMotorPosition("nameOfScannable", 10.01, true);

		B18SampleParameters sampleParameters = new B18SampleParameters();
		sampleParameters.setName("sample_name");
		sampleParameters.addSampleParameterMotorPosition(sampleMotorPosition);
		// set some non zero values for some of the motor positions
		sampleParameters.getUserStageParameters().setAxis2(1.0);
		sampleParameters.getUserStageParameters().setAxis4(112.0);
		sampleParameters.getSXCryoStageParameters().setHeight(15.6);
		sampleParameters.getSXCryoStageParameters().setRot(210.2);

		return sampleParameters;
	}

	@Test
	public void testInvokeMethodFromName() throws Exception {

		B18SampleParameters sampleParameters = getSampleParameters();
		SampleParameterMotorPosition sampleMotorPosition = sampleParameters.getSampleParameterMotorPositions().get(0);
		String scannableName = sampleMotorPosition.getScannableName();

		Object result = SpreadsheetViewHelperClasses.invokeMethodFromName(sampleParameters, "getName", null);
		assertEquals(sampleParameters.getName(), result);

		result = SpreadsheetViewHelperClasses.invokeMethodFromName(sampleParameters, B18SampleParameters.MOTOR_POSITION_GETTER_NAME+"("+scannableName+")", null);
		assertEquals(sampleMotorPosition, result);

		result = SpreadsheetViewHelperClasses.invokeMethodFromName(sampleParameters, B18SampleParameters.MOTOR_POSITION_GETTER_NAME+"("+scannableName+")."+SampleParameterMotorPosition.DO_MOVE_GETTER_NAME, null);
		assertEquals(sampleMotorPosition.getDoMove(), result);

		result = SpreadsheetViewHelperClasses.invokeMethodFromName(sampleParameters, B18SampleParameters.MOTOR_POSITION_GETTER_NAME+"("+scannableName+")."+SampleParameterMotorPosition.DEMAND_POSITION_GETTER_NAME, null);
		assertEquals(sampleMotorPosition.getDemandPosition(), result);

		result = SpreadsheetViewHelperClasses.invokeMethodFromName(sampleParameters, "getUserStageParameters.getAxis2", null);
		assertEquals(sampleParameters.getUserStageParameters().getAxis2(), result);

		result = SpreadsheetViewHelperClasses.invokeMethodFromName(sampleParameters, "getUserStageParameters.getAxis4", null);
		assertEquals(sampleParameters.getUserStageParameters().getAxis4(), result);

		result = SpreadsheetViewHelperClasses.invokeMethodFromName(sampleParameters, "getSXCryoStageParameters.getHeight", null);
		assertEquals(sampleParameters.getSXCryoStageParameters().getHeight(), result);

		result = SpreadsheetViewHelperClasses.invokeMethodFromName(sampleParameters, "getSXCryoStageParameters.getRot", null);
		assertEquals(sampleParameters.getSXCryoStageParameters().getRot(), result);
	}

	@Test
	public void testUpdateBeanWIthOverrides() {
		B18SampleParameters sampleParameters = getSampleParameters();
		SampleParameterMotorPosition sampleMotorPosition = sampleParameters.getSampleParameterMotorPositions().get(0);
		String scannableName = sampleMotorPosition.getScannableName();

		String newSampleName = "new sample name";
		double newAxisPosition = 15.7;
		double newDemandPosition = 50.56;
		boolean newDoMove = false;

		ParameterValuesForBean newParameterValues = new ParameterValuesForBean();
		newParameterValues.addParameterValue("getName", newSampleName);
		newParameterValues.addParameterValue("getUserStageParameters.getAxis2", newAxisPosition);
		newParameterValues.addParameterValue("getSampleParameterMotorPosition("+scannableName+").getDemandPosition", newDemandPosition);
		newParameterValues.addParameterValue("getSampleParameterMotorPosition("+scannableName+").getDoMove", newDoMove);

		SpreadsheetViewHelperClasses.updateBeanWIthOverrides(sampleParameters, newParameterValues);

		assertEquals(newSampleName, sampleParameters.getName());
		assertEquals(newAxisPosition, sampleParameters.getUserStageParameters().getAxis2(), 0.0001);
		assertEquals(newDemandPosition, sampleParameters.getSampleParameterMotorPosition(scannableName).getDemandPosition(), 0.0001);
		assertEquals(newDoMove, sampleParameters.getSampleParameterMotorPosition(scannableName).getDoMove());
	}
}
