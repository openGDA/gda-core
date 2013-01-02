/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.device.detector.addetector.triggering;

import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.ADBase.ImageMode;
import gda.device.detector.areadetector.v17.ADBase.StandardTriggerMode;

/*
 * Class to run a camera continuously with software trigger
 * 
 * Use prepareForCollection and then collectData
 */
public class SoftwareContinuous extends SimpleAcquire {

	public SoftwareContinuous(ADBase adBase) {
		super(adBase,-0.1); //force acquireTime to be 0.
	}

	@Override
	public void prepareForCollection(double collectionTime, int numImagesIgnored) throws Exception {
		getAdBase().stopAcquiring(); //to get out of armed state
 
		super.prepareForCollection(collectionTime, numImagesIgnored); 
		getAdBase().setImageMode(ImageMode.CONTINUOUS.ordinal());
		getAdBase().setTriggerMode(StandardTriggerMode.INTERNAL.ordinal()); 
	}

	@Override
	public void completeCollection() throws Exception {
		getAdBase().stopAcquiring();

	}

}
