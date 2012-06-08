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

package gda.scan;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.RepScanScannable;
import gda.device.scannable.PositionCallableProvider;
import gda.device.scannable.ScannableBase;
import gda.jython.commands.ScannableCommands;

import java.util.Vector;

/**
 * RepeatScan contains the static command repscan originally written to allow a detector to be exposed 
 * multiple times and results written to a scan file by a command of the form:
 * 
 * repscan 10 det 0.1
 * 
 * hence removing the need for a dummy variable and use a command of the
 * form:
 * 		scan dummy 1 10 1 det 0.1
 * 
 *  repscan also handles detectors that can have their own internal frame advance mechanism by 
 *  sending the repeat number to all scannables/detectors that support the interface 
 *  gda.device.detector.DetectorFrameController to allow detectors/scannables to be configured correctly
 *  to generate the required number of frames.
 *  Note that detectors that support  gda.device.detector.DetectorFrameController would normally support
 *  gda.device.scannable.PositionCallableProvider as well as frame advance would not normally be controlled by the
 *  scan command.
 *  
 *  repscan can be used to repeated call pos on any scannable and get the results written to a scan file
 *  
 *  repscan CANNOT be used to repeat an inner scan e.g.
 *  
 *   	repscan 1000 pos1 1 10 1  - WILL THROW EXCEPTION
 */
public class RepeatScan {

	public static ConcurrentScan create_repscan(Object... args) throws Exception {
		//add enough items to build a up an extra loop that concurrent scan will recognise
		if( args == null)
			throw new IllegalArgumentException("args is null");
		if( args.length == 0)
			throw new IllegalArgumentException("No arguments given to createSnapScan");
		
		int repeatCount = 1;
		int offsetIntoArgs = 0;
		// try to get repeat number  - if not possible add repeat of 1
		if( !( args[0] instanceof Scannable)){
			//allow NumberFormatException to be visible to caller 
			repeatCount  = Integer.valueOf(args[0].toString());
			offsetIntoArgs = 1;
		}
		Object [] newargs = new Object[args.length - offsetIntoArgs + 2];

		NumberOfFramesScannable ns = new NumberOfFramesScannable();
		ns.setName("index");
		ns.configure();
		newargs[0] = ns;
		newargs[1] = new FrameProvider(repeatCount);
		for(int i= 0 ; i< args.length - offsetIntoArgs ;i++){
			newargs[i + 2 ] = args[i + offsetIntoArgs];
		}
		ConcurrentScan scan =  ScannableCommands.createConcurrentScan(newargs);
		if ( scan.getChild() != null)
			throw new IllegalArgumentException("repscan cannot be used to repeat an inner scan");
		
		ns.setRepeatCount(repeatCount);
		ns.setAllDetectors(scan.allDetectors);
		ns.setAllScannables(scan.allScannables);

		int numPositionCallableProvider=0;
		for(Detector det : scan.allDetectors){
			if( det instanceof PositionCallableProvider){
				numPositionCallableProvider++;
			}
		}		
		for(Scannable scannable : scan.allScannables){
			if( scannable instanceof PositionCallableProvider){
				numPositionCallableProvider++;
			}
		}
		if( numPositionCallableProvider > 0 ) {
			scan.setScanDataPointQueueLength(repeatCount * numPositionCallableProvider);
			scan.setPositionCallableThreadPoolSize(numPositionCallableProvider);
		}
		return scan;
	}

	public static void repscan(Object... args) throws Exception {
		ConcurrentScan scan = create_repscan(args);
		scan.runScan();
	}
	
}
class NumberOfFramesScannable extends ScannableBase {

	int repeatCount=0;
	Vector<Scannable> allScannables = null;
	Vector<Detector> allDetectors = null;
	
	public int getRepeatCount() {
		return repeatCount;
	}

	public void setRepeatCount(int repeatCount) {
		this.repeatCount = repeatCount;
	}


	public void setAllScannables(Vector<Scannable> allScannables) {
		this.allScannables = allScannables;
	}

	public void setAllDetectors(Vector<Detector> allDetectors) {
		this.allDetectors = allDetectors;
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

	private Object position;
	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		this.position = position;
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		return position;
	}
	@Override
	public void atScanLineStart() throws DeviceException {
		super.atScanLineStart();
		if( repeatCount== 0)
			return;
		if( allDetectors != null){
			for(Detector det : allDetectors){
				if( det instanceof RepScanScannable){
					((RepScanScannable)det).atRepScanStart(repeatCount);
				}
			}		
		}
		if( allScannables != null){
			for(Scannable scannable : allScannables){
				if( scannable instanceof RepScanScannable){
					((RepScanScannable)scannable).atRepScanStart(repeatCount);
				}
			}
		}
	}

	@Override
	public void atScanEnd() throws DeviceException {
		super.atScanEnd();
		//clear allDetectors and allScannables  as no longer needed
		allDetectors = null;
		allScannables = null;
	}
	
	

}

class FrameProvider implements ScanPositionProvider {

	int totalFrames=0;
	
	public FrameProvider(int totalFrames) {
		super();
		if( totalFrames <0)
			throw new IllegalArgumentException(" totalFrames must be > 0");
		this.totalFrames = totalFrames;
	}

	@Override
	public Object get(int index) {
		return new Integer(index);
	}

	@Override
	public int size() {
		return totalFrames;
	}

	@Override
	public String toString() {
		return "FrameProvider [totalFrames=" + totalFrames + "]";
	}

}