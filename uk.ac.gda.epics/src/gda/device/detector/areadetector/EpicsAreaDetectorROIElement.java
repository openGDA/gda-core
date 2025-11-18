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
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;

public interface EpicsAreaDetectorROIElement extends Configurable {

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

	Boolean getInitialUseROI();

	void setInitialUseROI(Boolean initialUseROI);

	void reset() throws CAException, InterruptedException;

	// Methods for manipulating the underlying channels
	void setUse(boolean enable) throws CAException, InterruptedException;

	void setROI(int minx, int miny, int sizex, int sizey) throws CAException, InterruptedException;

	void setROI(AreaDetectorROI roi) throws CAException, InterruptedException;

	AreaDetectorROI getROI() throws TimeoutException, CAException, InterruptedException;

	void setBinning(int binx, int biny) throws CAException, InterruptedException;

	AreaDetectorBin getBinning() throws TimeoutException, CAException, InterruptedException;

	void setDataType(String dataType) throws CAException, InterruptedException;
}
