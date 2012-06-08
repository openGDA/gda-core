/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device.enumpositioner;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.EnumPositionerStatus;
import gda.device.Scannable;
import gda.device.scannable.ScannablePositionChangeEvent;
import gda.factory.FactoryException;

import java.util.Vector;

/**
 * A dummy class for valves. If positions is not set then it has two possible positions: Open and Close.
 * Provide a list of positions to make it a general dummy positioner
 */
public class DummyValve extends ValveBase implements EnumPositioner, Scannable {
	
	String currentPosition;
	
	@Override
	public void configure() throws FactoryException{
		if(!configured){
			super.configure();
			if(positions == null || positions.size() ==0 ){
				this.positions = new Vector<String>();
				this.positions.add(OPEN);
				this.positions.add(CLOSE);
			}
			if (currentPosition==null) this.currentPosition = positions.get(0);
			configured = true;
		}
	}

	@Override
	public String getPosition() throws DeviceException {
		return currentPosition;
	}

	@Override
	public EnumPositionerStatus getStatus() throws DeviceException {
		return EnumPositionerStatus.IDLE;
	}
	
	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		
		String positionString = position.toString();
		if( (currentPosition == null || !currentPosition.equals(positionString)) && positions.contains(positionString)){
			currentPosition = positionString;
			this.notifyIObservers(this, this.currentPosition); 
			this.notifyIObservers(this, new ScannablePositionChangeEvent(this.currentPosition));
		}
	}

	@Override
	public void stop() throws DeviceException {
		// do nothing
	}

	/**
	 * @param position The position to set.
	 */
	public void setPosition(String position) {
		if(!configured)
			this.currentPosition = position;
	}

}
