/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HardwareTriggeredMedipix extends HardwareTriggeredStandard {

	static final Logger logger = LoggerFactory.getLogger(HardwareTriggeredMedipix.class);

	public HardwareTriggeredMedipix(ADBase adBase, double readoutTime) {
		super(adBase, readoutTime);
		setTriggerModeProvider(new MedipixTriggerModeProvider(MedipixTriggerModeProvider.TriggerStartRising));
		
	}

	
	
	
	@Override
	public void collectData() throws Exception {
		getAdBase().startAcquiring();
		logger.warn("Sleeping for 1s after starting medipix acquisition. Otherwise it may miss the first trigger.");
		Thread.sleep(2000);
	}
	
	@Override
	public void configureAcquireAndPeriodTimes(double collectionTime) throws Exception {
		if (getReadoutTime() < 0) {
			throw new IllegalStateException("This detector requires a (+ve) readout time to specified in hardware triggered mode."); 
		}
		
		getAdBase().setAcquireTime(collectionTime - getReadoutTime());  // strange medipix required due to its internal "gate time"
		getAdBase().setAcquirePeriod(0);  // This forces Epics to choose an appropriate one
		
		double resultingPeriod = getAdBase().getAcquirePeriod_RBV();
		String msg = MessageFormat.format("collection_time:{0} acquire_time:{1} acquire_period{2}",
				collectionTime, collectionTime - getReadoutTime(), resultingPeriod);
		logger.info(msg);
		
		if (resultingPeriod > collectionTime) {
			throw new IllegalArgumentException("The medimpix camera has chosen an acquire period longer than the request collection time. *Increase the readoutTime property*.\n" + msg);
		}
	}

	@Override
	public boolean requiresAsynchronousPlugins() {
		return true;
	}
}
