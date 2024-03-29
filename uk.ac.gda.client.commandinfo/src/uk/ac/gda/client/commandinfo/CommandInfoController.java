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

package uk.ac.gda.client.commandinfo;

import gda.jython.InterfaceProvider;
import gda.jython.commandinfo.CommandThreadEvent;
import gda.jython.commandinfo.CommandThreadEventType;
import gda.jython.commandinfo.ICommandThreadInfo;
import gda.jython.commandinfo.ICommandThreadInfoProvider;
import gda.jython.commandinfo.ICommandThreadObserver;

import java.util.ArrayList;
import java.util.List;

public class CommandInfoController implements ICommandThreadObserver, ICommandThreadInfoProvider {

	static private CommandInfoController self = null;

	static public CommandInfoController getInstance() {
		if (null==self) {
			self = new CommandInfoController();
		}
		return self;
	}

	private List<ICommandThreadObserver> localObservers = new ArrayList<ICommandThreadObserver>();
	private CommandInfoModel model = new CommandInfoModel();

	private CommandInfoController() {
		this.configure();
	}

	@Override
	public void addCommandThreadObserver(ICommandThreadObserver anObserver) {
		localObservers.add(anObserver);
	}

	public void clearCommandList() {
		CommandThreadEvent event = new CommandThreadEvent(CommandThreadEventType.CLEAR,null);
		this.updateModel(event);
		this.updateObservers(event);
	}

	public void configure() {
		InterfaceProvider.getCommandThreadInfoProvider().addCommandThreadObserver(this);
		this.initialiseModel();
	}

	@Override
	public void deleteCommandThreadObserver(ICommandThreadObserver anObserver) {
		localObservers.remove(localObservers);
	}

	public void disconnect() {
		InterfaceProvider.getCommandThreadInfoProvider().deleteCommandThreadObserver(this);
	}

	public List<ICommandThreadInfo> getCommandList() {
		return model.getCommandList();
	}

	@Override
	public ICommandThreadInfo[] getCommandThreadInfo() {
		return initialiseModel().getCommandElements();
	}

	public CommandInfoModel getModel() {
		return model;
	}

	private CommandInfoModel initialiseModel() {
		model.clear();
		ICommandThreadInfo[] infos = InterfaceProvider.getCommandThreadInfoProvider().getCommandThreadInfo();
		for (ICommandThreadInfo info : infos) {
			if (null!=info) {
				model.put(info.getId(),info);
			}
		}
		return model;
	}

	private void updateObservers(CommandThreadEvent event) {
		for (ICommandThreadObserver observer : localObservers) {
			observer.update(this,event);
		}
	}

	public int refreshCommandList() {
		CommandThreadEvent event = new CommandThreadEvent(CommandThreadEventType.REFRESH,null);
		this.updateModel(event);
		this.updateObservers(event);
		return model.size();
	}

	@Override
	public void update(Object source, Object arg) {
		if (arg instanceof CommandThreadEvent) {
			CommandThreadEvent event = (CommandThreadEvent) arg;
			updateModel(event);
			updateObservers(event);
		}
	}

	private void updateModel(CommandThreadEvent event) {
		switch (event.getEventType()) {
		case CLEAR :
			model.clear();
			break;
		case REFRESH :
			initialiseModel();
			break;
		case START :
		case UPDATE :
			ICommandThreadInfo info = event.getInfo();
			model.put(info.getId(),info);
			break;
		case TERMINATE :
			model.remove(event.getInfo().getId());
			break;
		default : break;
		}
	}
}
