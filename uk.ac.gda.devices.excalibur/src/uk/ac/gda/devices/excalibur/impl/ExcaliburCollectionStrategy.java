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

package uk.ac.gda.devices.excalibur.impl;

import gda.device.detector.addetector.triggering.ADTriggeringStrategy;

import org.springframework.beans.factory.InitializingBean;

public class ExcaliburCollectionStrategy implements ADTriggeringStrategy, InitializingBean{

	private ExcaliburController controller;
	
	public ExcaliburController getController() {
		return controller;
	}

	public void setController(ExcaliburController controller) {
		this.controller = controller;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if( controller == null)
			throw new Exception("controller is null");
		
	}

	@Override
	public void prepareForCollection(double collectionTime, int numImages) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getAcquireTime() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getAcquirePeriod() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void collectData() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void endCollection() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void atCommandFailure() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getStatus() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void waitWhileBusy() throws InterruptedException, Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getNumberImagesPerCollection(double collectionTime) {
		return 1;
	}

}
