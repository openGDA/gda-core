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

package gda.device.detector.xmap;

import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.ContinuousParameters;
import gda.device.DeviceException;
import gda.device.detector.BufferedDetector;
import gda.device.detector.DetectorBase;
import gda.device.detector.NXDetectorData;
import gda.device.detector.countertimer.BufferedScaler;

public class VortexQexafsFFIO extends DetectorBase implements BufferedDetector{
	private static final long serialVersionUID = 4571439036961728662L;
	private String scalerName;
	private BufferedDetector qxmap = null;
	private BufferedScaler qscaler = null;
	protected ContinuousParameters continuousParameters = null;
	protected boolean isContinuousMode = true;
	@Override
	public Double readout() throws DeviceException {
		return 1.0;
	}

	public String getscalerListName() {
		return scalerName;
	}

	public void setscalerListName(String[] scalerName) {
		this.scalerName = scalerName[0];
	}

	public BufferedDetector getQexafsXmap() {
		return qxmap;
	}

	public void setQexafsXmap(BufferedDetector qxmap) {
		this.qxmap = qxmap;
	}


	public BufferedScaler getQexafsScaler() {
		return qscaler;
	}

	public void setQexafsScaler(BufferedScaler scaler) {
		this.qscaler = scaler;
	}

	@Override
	public void configure() {
		if (isConfigured()) {
			return;
		}
		this.setExtraNames(new String[] { "VortexQexafsFFI0" });
		this.setInputNames(new String[0]);
		this.setOutputFormat(new String[] { "%.9f" });
		setConfigured(true);
	}

	@Override
	public void clearMemory() throws DeviceException {

	}

	@Override
	public Object[] readAllFrames() throws DeviceException {
		Integer lastFrame = getNumberFrames() - 1;
		return readFrames(0, lastFrame);
	}

	@Override
	public Object[] readFrames(int startFrame, int finalFrame) throws DeviceException {

		double[][] scalerFrames = (double[][])qscaler.readFrames(startFrame, finalFrame);
		Object[] xmapData = qxmap.readFrames(startFrame, finalFrame);
		NexusTreeProvider[] xmapFrames =  (NexusTreeProvider[])xmapData;

		Double[] ffio = new Double[finalFrame-startFrame+1];

		//for(int i=0;i<finalFrame-startFrame+1;i++){
		for(int i=0;i<xmapData.length;i++){
			NXDetectorData expressFrameData;
			if(startFrame==finalFrame)
				expressFrameData = (NXDetectorData)xmapFrames[finalFrame-1];
			else
				expressFrameData = (NXDetectorData)xmapFrames[i];

			Double[] expressFrameDoubles = expressFrameData.getDoubleVals();
			int col=0;
			String[] names = expressFrameData.getExtraNames();
			for(int name=0;name<expressFrameDoubles.length;name++)
				if(names[name].equals("FF"))
					col=name;

			String[] exteraNames = qscaler.getExtraNames();
			int I0Pos =0;
			for(int j=0;j<exteraNames.length;j++){
				if(exteraNames[j].equals("I0"))
					I0Pos=j;
			}

			double[] scalarFrame = scalerFrames[i];
			double io = scalarFrame[I0Pos];
			double ff = expressFrameDoubles[col];
			double result = 0;
			if(ff!=0 && io!=0)
				result = ff/io;
			ffio[i] =  result;
		}

		return ffio;

	}

	@Override
	public ContinuousParameters getContinuousParameters() throws DeviceException {
		return null;
	}

	@Override
	public int getNumberFrames() throws DeviceException {
		if (!isContinuousMode) {
			return 0;
		}
		return continuousParameters.getNumberDataPoints();
	}

	@Override
	public boolean isContinuousMode() throws DeviceException {
		return false;
	}

	@Override
	public int maximumReadFrames() throws DeviceException {
		return qxmap.maximumReadFrames();
	}

	@Override
	public void setContinuousMode(boolean on) throws DeviceException {

	}

	@Override
	public void setContinuousParameters(ContinuousParameters parameters) throws DeviceException {
		continuousParameters = parameters;
	}

	@Override
	public void collectData() throws DeviceException {

	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "Vortex QEXAFS FF over I0";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "Version 1";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "Vortex QexafsFFoverIO";
	}

	@Override
	public int getStatus() throws DeviceException {
		return 0;
	}
}
