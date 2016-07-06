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

package gda.device.temperature;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableBase;
import gda.epics.CAClient;
import gda.jython.InterfaceProvider;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;

/**
 * This class controls a Eurotherm903 furnace using the EPICS interface.
 * It was originally written for use with the B18 Eurotherm (in a B18 specific plugin).
 * This is a cleaned up version of that class developed when using the same furnace on I20,
 * with 'moveAndWait' function added, and moved to a non beamline specific package.
 *
 * TODO: Use this class on B18 and use new 'moveAndWait' method to replace equivalent functionality
 * currently in B18SampleEnvironmentIterator; implement this class by extending TemperatureBase
 *
 * @author Iain Hall
 */
public class EpicsEurotherm903 extends ScannableBase implements Scannable {

	private CAClient caClient = new CAClient();

	private String eurothermPv;

	private String setpointPv;
	private String actualTemperatureRbvPv;
	private String upperLimitPv;

	private double requiredSetpointTemperature;
	private double temperatureTolerance = 0.1;

	private double pollTimeIntervalSecs = 2;
	private double setpointTemperatureWaitTime = 30;

	/**
	 * Returns {@code true} if current and demand temperature is outside of tolerance range.
	 */
	@Override
	public boolean isBusy() throws DeviceException {
		double temperatureDiff = Math.abs( (Double)rawGetPosition() -  requiredSetpointTemperature );
		return temperatureDiff > temperatureTolerance;
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		try {
			requiredSetpointTemperature = (Double)position;
			caClient.caput(setpointPv, String.valueOf(position));
		} catch (Exception e) {
			if( e instanceof DeviceException)
				throw (DeviceException)e;
			throw new DeviceException(getName() +" exception in rawAsynchronousMoveTo", e);
		}
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		try {
			return Double.parseDouble(caClient.caget(actualTemperatureRbvPv));
		} catch (Exception e) {
			if( e instanceof DeviceException)
				throw (DeviceException)e;
			throw new DeviceException(getName() +" exception in rawGetPosition", e);
		}
	}

	public String getEurothermPv() {
		return eurothermPv;
	}

	public void setEurothermPv(String eurothermPv) {
		this.eurothermPv = eurothermPv;

		// set values of the other PVs here as well; we can set the values explicitly later if needed
		setpointPv = eurothermPv+":SL";
		actualTemperatureRbvPv = eurothermPv+":PV:RBV";
		upperLimitPv = eurothermPv+":SL.DRVH";
	}

	public String getSetpointPv() {
		return setpointPv;
	}

	public void setSetpointPv(String setpointPv) {
		this.setpointPv = setpointPv;
	}


	public String getUpperLimit() throws CAException, TimeoutException, InterruptedException{
		return caClient.caget(upperLimitPv);
	}

	public void setUpperLimitPv(String upperLimitPv) {
		this.upperLimitPv = upperLimitPv;
	}

	public String getLowerLimit() {
		return "0.0";
	}

	public String getActualTemperatureRbvPv() {
		return actualTemperatureRbvPv;
	}

	public void setActualTemperatureRbvPv(String actualTemperatureRbvPv) {
		this.actualTemperatureRbvPv = actualTemperatureRbvPv;
	}

	public double getPollTimeInterval() {
		return pollTimeIntervalSecs;
	}

	public void setPollTimeInterval(double pollTimeInterval) {
		this.pollTimeIntervalSecs = pollTimeInterval;
	}

	public double getSetpointTemperatureWaitTime() {
		return setpointTemperatureWaitTime;
	}

	public void setSetpointTemperatureWaitTime(double setpointTemperatureWaitTime) {
		this.setpointTemperatureWaitTime = setpointTemperatureWaitTime;
	}

	public double getTemperatureTolerance() {
		return temperatureTolerance;
	}

	public void setTemperatureTolerance(double temperatureTolerance) {
		this.temperatureTolerance = temperatureTolerance;
	}

	/**
	 * Adjust setpoint temperature and wait until {@link #getSetpointTemperatureWaitTime()} seconds have passed at {@code finalTemperature}
	 *
	 * @param finalTemperature
	 * @throws DeviceException
	 * @throws InterruptedException
	 */
	public void moveAndWait( double finalTemperature ) throws DeviceException, InterruptedException {
		InterfaceProvider.getTerminalPrinter().print("MoveAndWait to "+finalTemperature+" C \n\tpollInterval = "+pollTimeIntervalSecs+" secs, setpointTemperatureWaitTime = "+setpointTemperatureWaitTime+" secs");
		InterfaceProvider.getTerminalPrinter().print("Current temperature [C], Time at required setpoint [secs]");

		asynchronousMoveTo(finalTemperature);

		double timeAtSetpointTemperature = 0.0;

		while( timeAtSetpointTemperature < setpointTemperatureWaitTime ) {

			InterfaceProvider.getTerminalPrinter().print( rawGetPosition() + "\t\t\t\t\t" + timeAtSetpointTemperature );

			Thread.sleep( (long) pollTimeIntervalSecs*1000 );

			if ( isBusy() == false )
				timeAtSetpointTemperature += pollTimeIntervalSecs;
			else
				timeAtSetpointTemperature = 0;
		}

		InterfaceProvider.getTerminalPrinter().print( "moveAndWait finished" );

	}
}
