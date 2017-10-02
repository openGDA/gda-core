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

package uk.ac.gda.devices.cirrus;

import gda.device.scannable.ScannableStatus;
import mksAsciiComms.JAnalogInput;
import mksAsciiComms.JCirrus;
import mksAsciiComms.JDegas;
import mksAsciiComms.JDiagnostics;
import mksAsciiComms.JDigitalPort;
import mksAsciiComms.JFilaments;
import mksAsciiComms.JInlet;
import mksAsciiComms.JMeasurement;
import mksAsciiComms.JMultiplier;
import mksAsciiComms.JRF;
import mksAsciiComms.JRVC;
import mksAsciiComms.JScan;
import mksAsciiComms.JTotalPressure;

/**
 * Bean holding the current state of the Cirrus hardware as reported through the IEvents interface.
 *
 * @author rjw82
 *
 */
public class CirrusState {

	private Boolean controlResponseRecieved = false;
	private Boolean isConnected = false;
	private Boolean hasControl = false;
	private Boolean scanStarted = false;
	private Boolean pumpPressureResponseRecieved = false;
	private Boolean filamentChangeResponseRecieved = false;
	private Boolean heaterToWarmResponseRecieved = false;
	private Boolean pumpPressureOffResponseRecieved = false;
	private Boolean heaterToOffResponseRecieved = false;
	private Boolean capillaryHeaterOn = false;
	private Boolean cirrusHeaterOn = false;
	private Boolean measurementCreationResponseRecieved;
	private Boolean measurementCreationResult;

	private JScan runningScan;
	private JDegas lastDegasReading;
	private JMeasurement lastMeasurement;
	private JRVC lastRVCState;
	private JFilaments lastFilamentState;
	private JDiagnostics lastDiagnosticsReading;
	private JMultiplier lastMultiplierState;
	private JTotalPressure lastTotalPressureReading;
	private JRF lastRFTripState;
	private JInlet lastInletState;
	private JDigitalPort lastDigitalPortState;
	private ScannableStatus status = ScannableStatus.IDLE;
	private String statusString = "";
	private JCirrus lastState;
	private JAnalogInput lastAnalogInputReading;

	public void setIsConnected(Boolean isConnected) {
		this.isConnected = isConnected;
	}

	public Boolean getIsConnected() {
		return isConnected;
	}

	public void setHasControl(Boolean hasControl) {
		this.hasControl = hasControl;
	}

	public Boolean getHasControl() {
		return hasControl;
	}

	public void setLastDegasReading(JDegas lastDegasReading) {
		this.lastDegasReading = lastDegasReading;
	}

	public JDegas getLastDegasReading() {
		return lastDegasReading;
	}

	public void setLastMeasurement(JMeasurement jMeasurement) {
		this.lastMeasurement = jMeasurement;
	}

	public JMeasurement getLastMeasurement() {
		return lastMeasurement;
	}

	public void setLastRVCState(JRVC lastRVCState) {
		this.lastRVCState = lastRVCState;
	}

	public JRVC getLastRVCState() {
		return lastRVCState;
	}

	public void setLastFilamentState(JFilaments lastFilamentState) {
		this.lastFilamentState = lastFilamentState;
	}

	public JFilaments getLastFilamentState() {
		return lastFilamentState;
	}

	public void setLastDiagnosticsReading(JDiagnostics lastDiagnosticsReading) {
		this.lastDiagnosticsReading = lastDiagnosticsReading;
	}

	public JDiagnostics getLastDiagnosticsReading() {
		return lastDiagnosticsReading;
	}

	public void setLastMultiplierState(JMultiplier lastMultiplierState) {
		this.lastMultiplierState = lastMultiplierState;
	}

	public JMultiplier getLastMultiplierState() {
		return lastMultiplierState;
	}

	public void setLastTotalPressureReading(JTotalPressure lastTotalPresuureReading) {
		this.lastTotalPressureReading = lastTotalPresuureReading;
	}

	public JTotalPressure getLastTotalPressureReading() {
		return lastTotalPressureReading;
	}

	public void setLastRFTripState(JRF lastRFTripState) {
		this.lastRFTripState = lastRFTripState;
	}

	public JRF getLastRFTripState() {
		return lastRFTripState;
	}

	public void setLastInletState(JInlet lastInletState) {
		this.lastInletState = lastInletState;
	}

	public JInlet getLastInletState() {
		return lastInletState;
	}

	public void setLastDigitalPortState(JDigitalPort lastDigitalPortState) {
		this.lastDigitalPortState = lastDigitalPortState;
	}

	public JDigitalPort getLastDigitalPortState() {
		return lastDigitalPortState;
	}

	public void setStatus(ScannableStatus status) {
		this.status = status;
	}

	public ScannableStatus getStatus() {
		return status;
	}

	public void setStatusString(String statusString) {
		this.statusString = statusString;
	}

	public String getStatusString() {
		return statusString;
	}

	public void setLastState(JCirrus lastState) {
		this.lastState = lastState;
	}

	public JCirrus getLastState() {
		return lastState;
	}

	public void setLastAnalogInputReading(JAnalogInput lastAnalogInputReading) {
		this.lastAnalogInputReading = lastAnalogInputReading;
	}

	public JAnalogInput getLastAnalogInputReading() {
		return lastAnalogInputReading;
	}

	public void setScanStarted(Boolean scanStarted) {
		this.scanStarted = scanStarted;
	}

	public Boolean getScanStarted() {
		return scanStarted;
	}

	public void setRunningJScan(JScan runningScan) {
		this.runningScan = runningScan;
	}

	public JScan getRunningScan() {
		return runningScan;
	}

	public void setControlResponseRecieved(Boolean controlResponseRecieved) {
		this.controlResponseRecieved = controlResponseRecieved;
	}

	public Boolean getControlResponseRecieved() {
		return controlResponseRecieved;
	}

	public Boolean getPumpPressureResponseRecieved() {
		return pumpPressureResponseRecieved;
	}

	public void setPumpPressureResponseRecieved(Boolean pumpPressureResponseRecieved) {
		this.pumpPressureResponseRecieved = pumpPressureResponseRecieved;
	}

	public Boolean getFilamentChangeResponseRecieved() {
		return filamentChangeResponseRecieved;
	}

	public void setFilamentChangeResponseRecieved(Boolean filamentChangeResponseRecieved) {
		this.filamentChangeResponseRecieved = filamentChangeResponseRecieved;
	}

	public Boolean getHeaterToWarmResponseRecieved() {
		return heaterToWarmResponseRecieved;
	}

	public void setHeaterToWarmResponseRecieved(Boolean heaterToWarmResponseRecieved) {
		this.heaterToWarmResponseRecieved = heaterToWarmResponseRecieved;
	}

	public Boolean getPumpPressureOffResponseRecieved() {
		return pumpPressureOffResponseRecieved;
	}

	public void setPumpPressureOffResponseRecieved(Boolean pumpPressureOffResponseRecieved) {
		this.pumpPressureOffResponseRecieved = pumpPressureOffResponseRecieved;
	}

	public Boolean getHeaterToOffResponseRecieved() {
		return heaterToOffResponseRecieved;
	}

	public void setHeaterToOffResponseRecieved(Boolean heaterToOffResponseRecieved) {
		this.heaterToOffResponseRecieved = heaterToOffResponseRecieved;
	}

	public Boolean getMeasurementCreationResponseRecieved() {
		return measurementCreationResponseRecieved;
	}

	public void setMeasurementCreationResponseRecieved(Boolean measurementCreationResponseRecieved) {
		this.measurementCreationResponseRecieved = measurementCreationResponseRecieved;
	}

	public Boolean getMeasurementCreationResult() {
		return measurementCreationResult;
	}

	public void setMeasurementCreationResult(Boolean measurementCreationResult) {
		this.measurementCreationResult = measurementCreationResult;
	}

	public Boolean getCapillaryHeaterOn() {
		return capillaryHeaterOn;
	}

	public void setCapillaryHeaterOn(Boolean capillaryHeaterOn) {
		this.capillaryHeaterOn = capillaryHeaterOn;
	}

	public Boolean getCirrusHeaterOn() {
		return cirrusHeaterOn;
	}

	public void setCirrusHeaterOn(Boolean cirrusHeaterOn) {
		this.cirrusHeaterOn = cirrusHeaterOn;
	}

}