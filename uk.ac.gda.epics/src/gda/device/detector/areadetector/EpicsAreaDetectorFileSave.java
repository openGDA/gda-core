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

package gda.device.detector.areadetector;

import gda.factory.Configurable;
import gda.factory.Localizable;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;

/**
 *
 */
public interface EpicsAreaDetectorFileSave extends Localizable, Configurable {

	// Getters and Setters for spring
	void setBasePVName(String basePVName);

	String getBasePVName();

	String getInitialFileName();

	void setInitialFileName(String initialFileName);

	String getInitialFileTemplate();

	void setInitialFileTemplate(String initialFileTemplate);

	String getInitialAutoIncrement();

	void setInitialAutoIncrement(String initialAutoIncrement);

	String getInitialAutoSave();

	void setInitialAutoSave(String initialAutoSave);

	String getInitialWriteMode();

	void setInitialWriteMode(String initialWriteMode);

	Integer getInitialNumCapture();

	void setInitialNumCapture(Integer initialNumCapture);

	String getInitialArrayPort();

	void setInitialArrayPort(String initialArrayPort);

	Integer getInitialArrayAddress();

	void setInitialArrayAddress(Integer initialArrayAddress);

	Boolean getInitialBlockingCallbacks();

	void setInitialBlockingCallbacks(Boolean initialBlockingCallback);

	void reset() throws CAException, InterruptedException;

	// Methods for manipulating the underlying channels
	void setEnable(boolean enable) throws CAException, InterruptedException;

	void setFilePath(String filePath) throws CAException, InterruptedException;

	void setFileName(String fileName) throws CAException, InterruptedException;

	String getFullFileName() throws TimeoutException, CAException, InterruptedException;

	String getFileName() throws TimeoutException, CAException, InterruptedException;

	String getFilePath() throws TimeoutException, CAException, InterruptedException;

	void setFileTemplate(String fileTemplate) throws CAException, InterruptedException;

	String getFileTemplate() throws TimeoutException, CAException, InterruptedException;

	double getTimeStamp() throws TimeoutException, CAException, InterruptedException;

	void setWriteMode(String writeMode) throws CAException, InterruptedException;

	void startCapture() throws CAException, InterruptedException;

	void setNumCapture(int numberOfFramesToCapture) throws CAException, InterruptedException;

	void setframeCounter(int numberOfFrameToSetCounterTo) throws CAException, InterruptedException;

	void setAutoIncrement(String increment) throws CAException, InterruptedException;

	void setAutoSave(String save) throws CAException, InterruptedException;

	int getFileNumber() throws TimeoutException, CAException, InterruptedException;

	void setArrayPort(String channelArrayPortName) throws CAException, InterruptedException;

	void setArrayAddress(int channelArrayAddressToUse) throws CAException, InterruptedException;

}
