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
public interface EpicsAreaDetector extends Localizable, Configurable {

	// Getters and setters for Spring
	String getBasePVName();

	void setBasePVName(String basePVName);

	Integer getInitialMinX();

	void setInitialMinX(Integer initialMinX);

	Integer getInitialMinY();

	void setInitialMinY(Integer initialMinY);

	Integer getInitialSizeX();

	void setInitialSizeX(Integer initialSizeX);

	Integer getInitialSizeY();

	void setInitialSizeY(Integer initialSizeY);

	Integer getInitialBinX();

	void setInitialBinX(Integer initialBinX);

	Integer getInitialBinY();

	void setInitialBinY(Integer initialBinY);

	String getInitialDataType();

	void setInitialDataType(String initialDataType);

	void reset() throws CAException, InterruptedException;

	// Methods for manipulating the underlying channels
	void setROI(int minx, int miny, int sizex, int sizey) throws CAException, InterruptedException;

	AreaDetectorROI getROI() throws TimeoutException, CAException, InterruptedException;

	void setBinning(int binx, int biny) throws CAException, InterruptedException;

	AreaDetectorBin getBinning() throws TimeoutException, CAException, InterruptedException;

	void setExpTime(double expTime) throws CAException, InterruptedException;

	double getExpTime() throws TimeoutException, CAException, InterruptedException;

	void setAcquirePeriod(double acquirePeriod) throws CAException, InterruptedException;

	double getAquirePeriod() throws TimeoutException, CAException, InterruptedException;

	String getPortName() throws TimeoutException, CAException, InterruptedException;

	void acquire() throws CAException, InterruptedException;

	void stop() throws CAException, InterruptedException;

	String getState() throws TimeoutException, CAException, InterruptedException;

	void setImageMode(int imageMode) throws CAException, InterruptedException;

	void setArrayCounter(int imageNumber) throws CAException, InterruptedException;

	int getArrayCounter() throws NumberFormatException, TimeoutException, CAException, InterruptedException;

	void setNumExposures(int NumberExposures) throws CAException, InterruptedException;

	int getNumExposures() throws NumberFormatException, TimeoutException, CAException, InterruptedException;

	void setNumImages(int NumberImages) throws CAException, InterruptedException;

	int getNumImages() throws NumberFormatException, TimeoutException, CAException, InterruptedException;

	void setDataType(String dataType) throws CAException, InterruptedException;

	int getAquireState() throws NumberFormatException, TimeoutException, CAException, InterruptedException;

	void setTriggerMode(int triggerMode) throws CAException, InterruptedException;
}
