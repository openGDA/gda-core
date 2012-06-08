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

import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;

/**
 *
 */
public interface AreaDetectorLiveView {

	EPICSAreaDetectorImage getImage();

	void setImage(EPICSAreaDetectorImage image);

	int getRefreshTime();

	void setRefreshTime(int refreshTime);

	String getPlotName();

	void setPlotName(String plotName);

	EpicsAreaDetectorROIElement getImageROI();

	void setImageROI(EpicsAreaDetectorROIElement imageROI);

	/**
	 * Invoke the AreaDetector live view to start
	 * 
	 * @throws CAException
	 * @throws TimeoutException
	 * @throws InterruptedException 
	 */
	public void start() throws CAException, TimeoutException, InterruptedException;

	void stop() throws CAException, InterruptedException, TimeoutException;

}
