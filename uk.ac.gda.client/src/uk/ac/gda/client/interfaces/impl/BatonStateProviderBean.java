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

package uk.ac.gda.client.interfaces.impl;

import java.util.List;

import gda.jython.IBatonStateProvider;
import gda.jython.InterfaceProvider;
import gda.jython.UserMessage;
import gda.jython.batoncontrol.ClientDetails;
import gda.observable.IObserver;

/*
 * Call InterfaceProvider.getCommandRunner() only when needed rather than in constructor when Factories may not be created
 */
public class BatonStateProviderBean implements IBatonStateProvider{

	private IBatonStateProvider runner;

	private IBatonStateProvider getRunner() {
		if( runner == null){
			runner = InterfaceProvider.getBatonStateProvider();
		}
		return runner;
	}
	BatonStateProviderBean(){
	}
	@Override
	public boolean isBatonHeld() {
		return getRunner().isBatonHeld();
	}
	@Override
	public void addBatonChangedObserver(IObserver anObserver) {
		getRunner().addBatonChangedObserver(anObserver);
	}
	@Override
	public void deleteBatonChangedObserver(IObserver anObserver) {
		getRunner().deleteBatonChangedObserver(anObserver);
	}
	@Override
	public boolean amIBatonHolder() {
		return getRunner().amIBatonHolder();
	}
	@Override
	public ClientDetails getBatonHolder() {
		return getRunner().getBatonHolder();
	}
	@Override
	public void returnBaton() {
		getRunner().returnBaton();
	}
	@Override
	public boolean requestBaton() {
		return getRunner().requestBaton();
	}
	@Override
	public void assignBaton(int receiverIndex, int holderIndex) {
		getRunner().assignBaton(receiverIndex, holderIndex);
	}
	@Override
	public ClientDetails[] getOtherClientInformation() {
		return getRunner().getOtherClientInformation();
	}
	@Override
	public ClientDetails getMyDetails() {
		return getRunner().getMyDetails();
	}
	@Override
	public void update(Object dataSource, Object data) {
		getRunner().update(dataSource, data);
	}
	@Override
	public void revertToOriginalUser() {
		getRunner().revertToOriginalUser();
	}
	@Override
	public void changeVisitID(String visitID) {
		getRunner().changeVisitID(visitID);
	}
	@Override
	public void sendMessage(String message) {
		getRunner().sendMessage(message);
	}

	@Override
	public List<UserMessage> getMessageHistory() {
		return getRunner().getMessageHistory();
	}

	@Override
	public boolean switchUser(String username, String password) {
		return getRunner().switchUser(username, password);
	}
}
