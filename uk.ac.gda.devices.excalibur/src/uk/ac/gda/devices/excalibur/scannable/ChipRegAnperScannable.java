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

package uk.ac.gda.devices.excalibur.scannable;

import java.lang.reflect.Method;
import java.util.List;

import org.springframework.util.StringUtils;

import uk.ac.gda.devices.excalibur.ChipAnper;
import uk.ac.gda.devices.excalibur.ExcaliburReadoutNodeFem;
import uk.ac.gda.devices.excalibur.MpxiiiChipReg;

public class ChipRegAnperScannable extends BaseChipRegScannable{
	final String getmethod, setmethod;	
	
	public ChipRegAnperScannable(List<ExcaliburReadoutNodeFem> fems, String parameter) {
		super(fems);
		String cap = StringUtils.capitalize(parameter);
		getmethod = "get" + cap;
		setmethod = "set" + cap;	
	}

	@Override
	protected void doAsynchronousMoveTo(MpxiiiChipReg chipReg, int intValue) throws Exception {
		if( setmethod == null)
			throw new IllegalArgumentException("setmethod not defined");
		ChipAnper anper = chipReg.getAnper();
		Class<? extends ChipAnper> class1 = anper.getClass();
		Class<?> partypes[] = new Class[1];
		partypes[0] = Integer.TYPE;
		Method method = class1.getMethod(setmethod, partypes);
		if( method != null){
			Object arglist[] = new Object[1];
            arglist[0] = intValue;			
			method.invoke(anper, intValue);
		}
	}

	@Override
	protected Object doGetPosition() throws Exception {
		if( getmethod == null)
			throw new IllegalArgumentException("getmethod not defined");
		ChipAnper anper = fems.get(0).getMpxiiiChipReg1().getAnper();
		Class<? extends ChipAnper> class1 = anper.getClass();
		Class partypes[] = new Class[1];
		partypes[0] = Integer.TYPE;
		Method method = class1.getMethod(getmethod, (Class[])null);
		Object invoke=null;
		if( method != null){
			invoke = method.invoke(anper, (Object[]) null);
		}
		return invoke;
	}
}
