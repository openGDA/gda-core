/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.device.continuouscontroller;

import gda.device.DeviceException;
import gda.device.scannable.ContinuouslyScannableViaController;
import gda.device.scannable.PositionCallableProvider;
import gda.device.scannable.PositionConvertorFunctions;
import gda.device.scannable.SimpleScannable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CSViacontrollerScannable extends SimpleScannable implements ContinuouslyScannableViaController, PositionCallableProvider<Double>{
	private static final Logger logger = LoggerFactory.getLogger(CSViacontrollerScannable.class);
	ContinuousMoveController cmc;
	

	public ContinuousMoveController getCmc() {
		return cmc;
	}

	public void setCmc(ContinuousMoveController cmc) {
		this.cmc = cmc;
	}

	@Override
	public Callable<Double> getPositionCallable() throws DeviceException {
		return new Callable<Double>(){

			@Override
			public Double call() throws Exception {
				return 0.;
			}};
	}

	@Override
	public void setOperatingContinuously(boolean b) throws DeviceException {
	}

	@Override
	public boolean isOperatingContinously() {
		return true;
	}

	@Override
	public ContinuousMoveController getContinuousMoveController() {
		return cmc;
	}

	@Override
	public void setContinuousMoveController(ContinuousMoveController controller) {
		throw new IllegalArgumentException("setContinuousMoveController("+controller.getName()+") not supported on "+this.getName());
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		addPoint(PositionConvertorFunctions.toDouble(externalToInternal(position)));
	}

	@Override
	public Object getPosition() throws DeviceException {
		if (isOperatingContinously()) {
			Object[] pos = (Object[]) internalToExternal(new Double[]{getLastPointAdded()});
			if (pos == null) {
				// First point is in process of being added
				return super.getPosition();
			}
			return pos[0];
		}
		return super.getPosition();
	}	
	
	List<Double> points = null;


	public void addPoint(Double point) {
		if(points == null){
			points = new ArrayList<Double>();
		}
		points.add(point);
	}

	public Double getLastPointAdded() {
		if (points == null || points.size() == 0) {
			logger.info(getName() + ".getLastPointAdded() returning null, as no points have yet been added");
			return null;
		}
		return points.get(points.size() - 1);
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

}