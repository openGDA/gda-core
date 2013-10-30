/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.device.scannable;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.Finder;
import gda.util.converters.AutoRenameableConverter;

public class XasScannableDetectorWithHarmonics extends XasScannable {
	private int scanPointCounter = 0; 
	private AutoRenameableConverter energyHarmonicConverter = null;
	private String harmonicConverterName;

	public String getHarmonicConverterName() {
		return harmonicConverterName;
	}

	public void setHarmonicConverterName(String harmonicConverterName) {
		this.harmonicConverterName = harmonicConverterName;
	}

	public XasScannableDetectorWithHarmonics() {
		super();
	}
	
	@Override
	public void configure(){
		energyHarmonicConverter = Finder.getInstance().find(harmonicConverterName);		
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		Double[] positions = ScannableUtils.objectToArray(position);
		// this move must complete first
		energyScannable.moveTo(positions[0]);
		lastCollectionTimeUsed = positions[1];
		// ensure all detectors know the time for this point
		for (Scannable detector : theDetectors)
			if (detector instanceof Detector)
				((Detector) detector).setCollectionTime(positions[1]);
	}
	
	@Override
	public void atPointEnd() throws DeviceException{
		scanPointCounter++;
		if(scanPointCounter == 1){
			if(energyHarmonicConverter == null)
				configure();
			energyHarmonicConverter.disableAutoConversion();
		}
	}
	
	@Override
	public void atScanEnd() throws DeviceException{
		if(energyHarmonicConverter == null)
				configure();
		energyHarmonicConverter.enableAutoConversion();
	}
	
	@Override
	public void atCommandFailure() throws DeviceException{
		if(energyHarmonicConverter == null)
			configure();
		energyHarmonicConverter.enableAutoConversion();
	}

}
