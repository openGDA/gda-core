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

package gda.device.scannable.scannablegroup;

import gda.device.DeviceException;

class DummyScannableFieldScannableMotion extends ScannableMotionWithScannableFieldsBase {
	
	Double[] inputPos;
	Double[] extraPos;
	
	public DummyScannableFieldScannableMotion(String name, String[] inputNames, String[] extraNames) {
		setName(name);
		setInputNames(inputNames);
		setExtraNames(extraNames);
		inputPos = new Double[inputNames.length];
		for (int i = 0; i < inputPos.length; i++) {
			inputPos[i] = new Double(i);
		}
		extraPos = new Double[extraNames.length];
		for (int i = 0; i < extraPos.length; i++) {
			extraPos[i] = new Double(i+100);
		}
		String[] formatArray = new String[inputNames.length+extraNames.length];
		for (int i = 0; i < formatArray.length; i++) {
			formatArray[i] = "%f";
		}
		setOutputFormat(formatArray);
	}
	
	@Override
	public void asynchronousMoveTo(Object position) throws gda.device.DeviceException {
		inputPos = gda.device.scannable.ScannableUtils.objectToArray(position);			
	}
	
	@Override
	public Object getPosition() throws DeviceException {
		Double[] pos = new Double[inputPos.length + extraPos.length];
		System.arraycopy(inputPos, 0, pos, 0, inputPos.length);
		System.arraycopy(extraPos, 0, pos, inputPos.length, inputPos.length);
		return pos;
	}
	
	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}
}