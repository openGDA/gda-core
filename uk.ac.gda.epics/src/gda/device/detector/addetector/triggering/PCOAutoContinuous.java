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
import gda.device.detector.areadetector.v17.ADDriverPco;
import gda.device.detector.areadetector.v17.ADBase.ImageMode;
import gda.device.detector.areadetector.v17.ADDriverPco.PcoTriggerMode;

/*
 * Class to set PCO into Continuous acquire call prepareForCollection and then collectData to start
 */
public class PCOAutoContinuous extends SimpleAcquire {
	private Integer adcMode = 1;// 2 adcs

	// TF3_OUT5

	public Integer getAdcMode() {
		return adcMode;
	}

	public void setAdcMode(Integer adcMode) {
		this.adcMode = adcMode;
	}

	private final ADDriverPco adDriverPco;

	public PCOAutoContinuous(ADBase adBase, ADDriverPco adDriverPco) {
		super(adBase, -0.1); // force acquireTime to be 0.
		this.adDriverPco = adDriverPco;
	}

	public ADDriverPco getAdDriverPco() {
		return adDriverPco;
	}

	@Override
	public void prepareForCollection(double collectionTime, int numImagesIgnored) throws Exception {
		getAdBase().stopAcquiring(); // to get out of armed state

		super.prepareForCollection(collectionTime, numImagesIgnored);
		getAdBase().setImageMode(ImageMode.CONTINUOUS.ordinal());
		getAdBase().setTriggerMode(PcoTriggerMode.AUTO.ordinal());
		adDriverPco.getAdcModePV().put(adcMode); // 2 adcs
		adDriverPco.getTimeStampModePV().put(1); // BCD - if set to None then the image is blank. BCD means no timestamp
													// on image
	}

	@Override
	public void completeCollection() throws Exception {
		getAdBase().stopAcquiring();
		adDriverPco.getArmModePV().putCallback(false);

	}

	@Override
	public void collectData() throws Exception {
		adDriverPco.getArmModePV().putCallback(true);
	}

}
