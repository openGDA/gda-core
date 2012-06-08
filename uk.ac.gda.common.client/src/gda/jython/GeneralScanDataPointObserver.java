/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

import gda.jython.gui.JythonGuiConstants;
import gda.observable.IObserver;
import gda.scan.IScanDataPoint;

/**
 * Class to monitor ScanDataPoints with PanelName equal to JythonTerminal
 */
public class GeneralScanDataPointObserver implements INamedScanDataPointObserver{

	IObserver observer;
	/**
	 * @param observer
	 */
	public GeneralScanDataPointObserver(IObserver observer){
		this.observer = observer;
		InterfaceProvider.getScanDataPointProvider().addIScanDataPointObserver(this);		
	}
	
	@Override
	public String getName() {
		return JythonGuiConstants.TERMINALNAME;
	}

	@Override
	public void update(Object theObserved, Object changeCode) {
		if( changeCode instanceof IScanDataPoint){
			observer.update(theObserved, changeCode);
		}
	}

	/**
	 * Remove this terminal from the command server observer list. This method should be called to ensure new terminals
	 * can be registered with the command server.
	 */
	public void dispose() {
		InterfaceProvider.getScanDataPointProvider().deleteIScanDataPointObserver(this);		
	}
}