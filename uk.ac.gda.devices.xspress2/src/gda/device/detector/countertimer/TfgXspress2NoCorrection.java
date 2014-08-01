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

package gda.device.detector.countertimer;

import org.apache.commons.lang.ArrayUtils;

import gda.device.DeviceException;
import gda.device.detector.xspress.Xspress2System;

/**
 * Version of TFGXspress2 which does not perform deadtime corrections on the values it returns
 */
public class TfgXspress2NoCorrection extends TfgXspress2 {
	
	String[] format;
	
	@Override
	public Object readout() throws DeviceException {
		Xspress2System xspress2system = (Xspress2System) xspress;
		double[] scalerDataNoCorrection = xspress2system.readoutScalerDataNoCorrection();
		if (xspress2system.isOnlyDisplayFF())
			return scalerDataNoCorrection[scalerDataNoCorrection.length-1];
		return scalerDataNoCorrection;
	}

	@Override
	public String[] getExtraNames() {
		String readoutMode="";
		String resGrade="";
		try {
			readoutMode = ((Xspress2System) xspress).getReadoutMode();
			resGrade = ((Xspress2System) xspress).getResGrade();
		} catch (DeviceException e) {
		}
		
		String[] names = (String[]) ArrayUtils.clone(super.getExtraNames());
		
		String[] newNames = null;
		
		if(readoutMode.equals("Scalers only")||readoutMode.equals("Scalers and MCA")){
			//Element0 - Element8, FF
			int counter = 0;
			newNames = new String[names.length];
			for (int i = 0; i < names.length; i++) {
				if((names[i].contains("Element")||names[i].contains("FF"))&&!names[i].contains("_")){
					newNames[counter] = names[i] + "_nocorr";
					counter++;
				}
			}
		}
		
		else if(readoutMode.equals("Regions Of Interest")){
			if(resGrade.contains("res-thres")){
				//Element0 - Element8, FF, FF_bad
				int counter = 0;
				newNames = new String[names.length];
				for (int i = 0; i < names.length; i++) {
					if((names[i].contains("Element")||names[i].contains("FF"))&&(!names[i].contains("_")||names[i].contains("FF")||names[i].contains("peak"))){
						newNames[counter] = names[i] + "_nocorr";
						counter++;
					}
				}
			}
			else if(resGrade.contains("res-min-div")){
				//Element0_best8 - Element8_best8, FF
				int counter = 0;
				newNames = new String[names.length];
				for (int i = 0; i < names.length; i++) {
					if((names[i].contains("Element")||names[i].contains("FF")||names[i].contains("res"))&&(!names[i].contains("_")||names[i].contains("best8")||names[i].contains("norm"))){
						newNames[counter] = names[i] + "_nocorr";
						counter++;
					}
				}
			}
			else if(resGrade.equals("res-none")){
				//Element0_peak - Element8_peak, FF
				int counter = 0;
				newNames = new String[names.length];
				for (int i = 0; i < names.length; i++) {
					if((names[i].contains("Element")||names[i].contains("FF"))&&(!names[i].contains("_")||names[i].contains("peak"))){
						newNames[counter] = names[i] + "_nocorr";
						counter++;
					}
				}

			}
		}
		
		if (newNames == null){
			format = new String[]{};
			return new String[]{};
		}
		
		format = new String[newNames.length];
		for (int i = 0; i < newNames.length; i++)
			format[i]="%.6g";

		return newNames;
	}
	
	@Override
	public String[] getOutputFormat() {
		return format;
	}
}
