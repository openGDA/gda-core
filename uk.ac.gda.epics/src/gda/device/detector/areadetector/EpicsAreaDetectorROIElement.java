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
public interface EpicsAreaDetectorROIElement extends Configurable, Localizable {

	// Getters and setters for Spring
	public String getBasePVName();

	public void setBasePVName(String basePVName);

	public Integer getInitialMinX();

	public void setInitialMinX(Integer initialMinX);

	public Integer getInitialMinY();

	public void setInitialMinY(Integer initialMinY);

	public Integer getInitialSizeX();

	public void setInitialSizeX(Integer initialSizeX);

	public Integer getInitialSizeY();

	public void setInitialSizeY(Integer initialSizeY);

	public Integer getInitialBinX();

	public void setInitialBinX(Integer initialBinX);

	public Integer getInitialBinY();

	public void setInitialBinY(Integer initialBinY);

	public String getInitialDataType();

	public void setInitialDataType(String initialDataType);

	public Boolean getInitialUseROI();

	public void setInitialUseROI(Boolean initialUseROI);

	public void reset() throws CAException, InterruptedException;

	// Methods for manipulating the underlying channels
	public void setUse(boolean enable) throws CAException, InterruptedException;

	public void setROI(int minx, int miny, int sizex, int sizey) throws CAException, InterruptedException;

	public void setROI(AreaDetectorROI roi) throws CAException, InterruptedException;

	public AreaDetectorROI getROI() throws TimeoutException, CAException, InterruptedException;

	public void setBinning(int binx, int biny) throws CAException, InterruptedException;

	public AreaDetectorBin getBinning() throws TimeoutException, CAException, InterruptedException;

	public void setDataType(String dataType) throws CAException, InterruptedException;

}
