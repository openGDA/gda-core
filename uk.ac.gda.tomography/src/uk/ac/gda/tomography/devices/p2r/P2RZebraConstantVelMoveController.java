/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.tomography.devices.p2r;

import gda.device.DeviceException;
import gda.device.scannable.PositionInputStream;
import gda.device.scannable.PositionStreamIndexer;
import gda.device.zebra.ZebraConstantVelocityMoveController;
import gda.device.zebra.controller.Zebra;
import gda.io.BidiAsciiCommunicator;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class P2RZebraConstantVelMoveController extends ZebraConstantVelocityMoveController {

	
	protected static boolean started=false;
	BidiAsciiCommunicator comm;
	Double positionToReport=0.;
	Double step=0.;
	
	public BidiAsciiCommunicator getBidiAsciiCommunicator() {
		return comm;
	}

	public void setBidiAsciiCommunicator(BidiAsciiCommunicator comm) {
		this.comm = comm;
	}

	/**
	 * sets gate source to external
	 * configure P2R to give triggers at pcGateStart 
	 * arm p2R
	 * setup zebra to create a gate from the trigger pulse that lasts for long enough
	 */
	@Override
	protected void setupGateAndArm(double pcGateStart, double pcGateWidth, double step, double pcGateTimeInS) throws Exception {
		getZebra().pcDisarm();
		
		//comm.send("TR2,"+pcGateStart+","+pcGateStart+step);
		comm.send("TR1,"+pcGateStart);
		comm.send("TS");
		
		pcGateWidthRBV = pcGateWidth;
		pcGateStartRBV = pcGateStart;
		
		getZebra().setPCArmSource(Zebra.PC_ARM_SOURCE_EXTERNAL);
		getZebra().setPCArmInput(1);//signal from p2r goes into ttl 1 of zebra
		getZebra().setPCGateSource(Zebra.PC_GATE_SOURCE_TIME);
		getZebra().setPCGateStart(0.);
		getZebra().setPCGateWidth(pcGateTimeInS*1000.);
		
		positionToReport=pcGateStart;
		this.step=step;
		started = true;
		
	}
	
	@Override
	public void atScanLineStart() throws DeviceException {
		super.atScanLineStart();
		started=false;
	}

	@Override
	public void atScanLineEnd() throws DeviceException {
		super.atScanLineEnd();
		positionStreamIndexer=null;
	}

	@Override
	public void stop() throws DeviceException {
		super.stop();
		positionStreamIndexer=null;
	}

	PositionStreamIndexer<Double> positionStreamIndexer=null;
	@Override
	public PositionStreamIndexer<Double> getPositionSteamIndexer(int index) {
		if( index!=0)
			return super.getPositionSteamIndexer(index);
		if( positionStreamIndexer == null){
			PositionInputStream<Double> stream = new PositionInputStream<Double>(){

				@Override
				public List<Double> read(int maxToRead) throws NoSuchElementException, InterruptedException,
						DeviceException {
					while(!P2RZebraConstantVelMoveController.started){
						Thread.sleep(1000);
					}				
					List<Double> read = new ArrayList<Double>();
					read.add(positionToReport); 
					positionToReport += step;
					return read;
				}};
			positionStreamIndexer =  new PositionStreamIndexer<Double>(stream);
		}
		return positionStreamIndexer;
	}	
	
}
