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

import gda.device.DeviceException;
import gda.device.detector.DetectorMonitorDataProvider.COLLECTION_TYPES;
import gda.factory.Findable;
import gda.observable.IObservable;

public interface DetectorMonitorDataProviderInterface extends Findable, IObservable {

	public double getCollectionTime();

	public void setCollectionTime(double collectionTime);

	public Double[] getIonChamberValues(COLLECTION_TYPES type) throws Exception;

	public Double[] getFluoDetectorCountRatesAndDeadTimes(COLLECTION_TYPES type) throws DeviceException;

	public int getNumElements(COLLECTION_TYPES type) throws DeviceException;

	boolean getCollectionAllowed();

	void setCollectionAllowed(boolean collectionAllowed);

	public boolean getCollectionIsRunning();

	public void setCollectionIsRunning(boolean collectionIsRunning);

}
