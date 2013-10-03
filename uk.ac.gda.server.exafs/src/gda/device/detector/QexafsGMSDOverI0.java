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

package gda.device.detector;

import gda.device.ContinuousParameters;
import gda.device.DeviceException;
import gda.device.detector.countertimer.BufferedScaler;

public class QexafsGMSDOverI0 extends DetectorBase implements BufferedDetector {

	protected ContinuousParameters continuousParameters = null;
	protected boolean isContinuousMode = true;
	private BufferedScaler qscaler = null;
	
	public BufferedScaler getQexafsScaler() {
		return qscaler;
	}

	public void setQexafsScaler(BufferedScaler scaler) {
		this.qscaler = scaler;
	}
	
	@Override
	public void configure() {
		this.setExtraNames(new String[] { "QexafsGMSDI0" });
		this.setInputNames(new String[0]);
		this.setOutputFormat(new String[] { "%.4f" });
	}
	
	@Override
	public void collectData() throws DeviceException {
	}

	@Override
	public int getStatus() throws DeviceException {
		return 0;
	}

	@Override
	public Object readout() throws DeviceException {
		return null;
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public void clearMemory() throws DeviceException {
		
	}

	@Override
	public void setContinuousMode(boolean on) throws DeviceException {
	}

	@Override
	public boolean isContinuousMode() throws DeviceException {
		return false;
	}

	@Override
	public void setContinuousParameters(ContinuousParameters parameters) throws DeviceException {
		continuousParameters = parameters;
	}

	@Override
	public ContinuousParameters getContinuousParameters() throws DeviceException {
		return continuousParameters;
	}

	@Override
	public int getNumberFrames() throws DeviceException {
		if (!isContinuousMode) {
			return 0;
		}
		return continuousParameters.getNumberDataPoints();
	}

	@Override
	public Object[] readFrames(int startFrame, int finalFrame) throws DeviceException {
		// do lots of stuff here
		double[][] scalerFrames = (double[][])qscaler.readFrames(startFrame, finalFrame);
		Double[] gmsdio = new Double[finalFrame-startFrame+1];
		
		for(int i=0;i<finalFrame-startFrame+1;i++){

//			time
//			I0
//			tey
//			gmsd1
//			gmsd2
//			gmsd3
//			gmsd4
//			gmsd5
//			gmsd6
			
			String[] exteraNames = qscaler.getExtraNames();
			int I0Pos =0;
			for(int j=0;j<exteraNames.length;j++){
				if(exteraNames[j].equals("I0"))
					I0Pos=j;
			}
			
			double[] scalarFrame = scalerFrames[i];

			// add gmsd1:gmsd6
			double gmsdTotal=0;
			for(int j=I0Pos+2;j<I0Pos+6;j++){
				gmsdTotal+=scalarFrame[j];
			}
			
			double io = scalarFrame[I0Pos];
			double result = 0;
			if(gmsdTotal!=0 && io!=0)
				result = gmsdTotal/io;
			gmsdio[i] =  result;
		}
		return gmsdio;
	}

	@Override
	public Object[] readAllFrames() throws DeviceException {
		Integer lastFrame = getNumberFrames() - 1;
		return readFrames(0, lastFrame);
	}

	@Override
	public int maximumReadFrames() throws DeviceException {
		// return gmsd maximumReadFrames()
		// it's not cleat how big this should be. Will try 20 as that is the typical size of a typical QEXAFS scan.
		return 20;
	}

}
