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

package gda.jython;

import gda.device.Scannable;
import gda.scan.Scan;
import gda.scan.ScanDataPoint;
import gda.scan.ScanInformation;

import java.util.Vector;

/**
 * Mock implementation of interfaces usually provided by JythonServer  to be used when running tests outside of gda
 * This implementation is used if you set the property JythonServer.dummy to true
 *
 */
public class MockJythonServer implements IJythonServerNotifer, ICurrentScanInformationHolder, IDefaultScannableProvider {

	String scanObserverName="";

	@Override
	public Vector<Scannable> getDefaultScannables() {
		return new Vector<Scannable>();
	}

	@Override
	public void notifyServer(Object source, Object data) {
		if( data instanceof ScanDataPoint){
			((ScanDataPoint)data).setCreatorPanelName(scanObserverName);
			InterfaceProvider.getScanDataPointProvider().update(source, data);
		}
	}

	Scan currentScan=null;
	@Override
	public void setCurrentScan(Scan newScan) {
		currentScan = newScan;
	}

	@Override
	public ScanInformation getCurrentScanInformation() {
		return JythonServer.getScanInformation(currentScan);
	}

	/**
	 * @param scanObserver The name of the observer to return ScanDataPoints
	 */
	public void setScanObserver(String scanObserver) {
		this.scanObserverName = scanObserver;
	}
}