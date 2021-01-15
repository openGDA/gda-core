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

package gda.gui;

import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * Implementation of RCPController to run on the server.
 *
 * To send a command to the client in a script
 *
 * from gda.gui import RCPController
 *
 * 	rcpController = finder.find("RCPController")
 *	if rcpController != None:
 *		rcpController.openView("gda.rcp.mx.views.AlignmentPlot")
 *
 */
@ServiceInterface(RCPController.class)
public class RCPControllerImpl implements RCPController {
	public static final String NAME = "RCPController";

	private ObservableComponent obs = new ObservableComponent();

	@Override
	public void openView(String id) {
		notifyIObservers(this, new RCPOpenViewCommand(id));
	}

	@Override
	public void setName(String name) {
		// do not allow the name to be changed
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void addIObserver(IObserver anIObserver) {
		obs.addIObserver(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		obs.deleteIObserver(anIObserver);
	}

	@Override
	public void deleteIObservers() {
		obs.deleteIObservers();
	}

	private void notifyIObservers(Object theObserved, Object changeCode) {
		obs.notifyIObservers(theObserved, changeCode);
	}

	@Override
	public void openPerspective(String id) {
		notifyIObservers(this, new RCPOpenPerspectiveCommand(id));

	}

	@Override
	public void setPreference(String id, Object value) {
		notifyIObservers(this, new RCPSetPreferenceCommand(id, value));
	}

	@Override
	public void resetPreference(String id) {
		notifyIObservers(this, new RCPResetPreferenceCommand(id));
	}
}
