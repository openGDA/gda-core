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

package gda.device.detector;

import java.util.List;
import java.util.Map;

import gda.device.DeviceException;
import gda.device.Scannable;

public interface DetectorMonitorDataProviderInterface extends Scannable {

	public double getCollectionTime();

	public void setCollectionTime(double collectionTime);

	boolean getCollectionAllowed();

	void setCollectionAllowed(boolean collectionAllowed);

	public boolean getCollectionIsRunning();

	public List<String> getOutputFields(List<String> detectors);

	public List<String> collectData(List<String> detectors) throws DeviceException, InterruptedException;

	boolean isScriptOrScanIsRunning();

	/**
	 * Set a map containing the output format to be used when displaying quanties in the GUI.
	 * (i.e. key=field name in the 'extra name' of a scannable, value=output format to use)
	 * @param numberFormat
	 */
	void setNumberFormat(Map<String, String> numberFormat);

}
