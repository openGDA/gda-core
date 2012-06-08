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

import gda.device.DeviceException;
import gda.factory.FactoryException;

import java.util.ArrayList;

public class EpicsTrajectoryScanRealPositionReader extends EpicsSingleTrajectoryScannable implements RealPositionReader {

	private int lastIndex = -1;
	private int numberOfScanPointsPerRow =-1;
	private ArrayList <double[]> positions = new ArrayList<double[]>() ;
	
	@Override
	public void atScanStart() throws DeviceException{
		positions = new ArrayList<double[]>() ;
	}
	@Override
	public void atScanLineStart() throws DeviceException{
		this.lastIndex = -1;
	}
	
	@Override
	public void atScanLineEnd() throws DeviceException{
		try {
			double tempPositions[]= tracController.getMActual(trajectoryIndex);
			//for the first scan line end get the total number of points per row
			if(numberOfScanPointsPerRow == -1)
			{				
				numberOfScanPointsPerRow = tempPositions.length;
			}
			int lineIndex = ((lastIndex )/numberOfScanPointsPerRow);
			positions.add(lineIndex ,tempPositions);
		} catch (Exception e) {
			throw new DeviceException(getName() + " exception in atScanLineEnd",e);
		}
	}
	
	@Override
	public Object getPosition()
	{
		return new RealPositionCallable(this, ++lastIndex);
	}
	
	@Override
	public Object get(int index) {
		try {
			int lineNumber = index / numberOfScanPointsPerRow;
			int pointNumber = index % numberOfScanPointsPerRow;
			if(lineNumber < 0)
				lineNumber = 0;
			while(lineNumber >= positions.size())
			{
				//InterfaceProvider.getTerminalPrinter().print("Waiting for traj to complete " + index);
				Thread.sleep(100);
				lineNumber = index / numberOfScanPointsPerRow;
				pointNumber = index % numberOfScanPointsPerRow;
			}
		
			return positions.get(lineNumber)[pointNumber];
	
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void configure() throws FactoryException {
		super.configure();
		this.setInputNames(new String[]{this.getName()});
	}
}
