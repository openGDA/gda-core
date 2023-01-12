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

package gda.device.detector;

import gda.device.detector.pco.PCOADCMode;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;

public interface IPCODiverController {

	int getADCMode() throws TimeoutException, CAException, InterruptedException, Exception;

	void setADCMode(int value) throws Exception;

	void setADCMode(PCOADCMode value) throws Exception;

	int getPixRate() throws TimeoutException, CAException, InterruptedException, Exception;

	void setPixRate(int value) throws Exception;

	double getCamRamUsage() throws Exception;

	double getElectronicTemperature() throws Exception;

	double getPowerSupplyTemperature() throws Exception;

	int getStorageMode() throws Exception;

	void setStorageMode(int value) throws Exception;

	int getRecorderMode() throws Exception;

	void setRecorderMode(int value) throws Exception;

	int getTimestampMode() throws Exception;

	void setTimestampMode(int value) throws Exception;

	int getAcquireMode() throws Exception;

	void setAcquireMode(int value) throws Exception;

	int getArmMode() throws Exception;

	void armCamera() throws Exception;

	void disarmCamera() throws Exception;

	void setArmMode(int value) throws Exception;

	double getDelayTime() throws Exception;

	void setDelayTime(double value) throws Exception;

	void afterPropertiesSet() throws Exception;

	void setName(String name);

	String getName();

}