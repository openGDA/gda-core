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
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gov.aps.jca.event.PutEvent;
import gov.aps.jca.event.PutListener;

/*
 * Class of detector  to drive the PCO4000 camera using a DIOTrigger
 * 
 */
public class PCODIOTrigger extends PCOHWTriggerBase {
//	private static Logger logger = LoggerFactory.getLogger(PCODIOTrigger.class);

	public PCODIOTrigger(ADBase adBase, ADDriverPco adDriverPco) {
		super(adBase, adDriverPco);

	}

	PV<Integer> dioTrigger = LazyPVFactory.newIntegerPV("BL12I-EA-DET-02:DIO:CAPTURE");

	private PutListener dioTriggerPutListener = new PutListener() {
		
		@Override
		public void putCompleted(PutEvent arg0) {
			setCollectingData(false);
		}
	};



	@Override
	public void collectData() throws Exception {
		super.collectData();
		setCollectingData(true);
		try{
			dioTrigger.putNoWait(1, dioTriggerPutListener);
		} catch(Exception ex){
			setCollectingData(false);
			throw ex;
		}
		
	}

}
