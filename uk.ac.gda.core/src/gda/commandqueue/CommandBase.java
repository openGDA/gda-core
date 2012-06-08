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

package gda.commandqueue;

import gda.observable.IObserver;
import gda.observable.ObservableComponent;

import java.io.Serializable;

/**
 * The class provides default implementations Command interface methods:
 * getDescription and getState
 * 
 * The developer need only subclass this class and define the <code>run</code> method.
 * It is assumed that the run method was take a while to execute. 
 * Wrap the code in the run method in calls to  beginRun and endRun to ensure the state
 * is set correctly
 * 
 */
public abstract class CommandBase implements Command, Serializable {
	protected ObservableComponent obsComp = new ObservableComponent();

	STATE state=STATE.NOT_STARTED;
	private String description;
	
	public CommandBase() {
		setDescription("CommandBase");
	}

	@Override
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public String toString() {
		return "CommandBase [description=" + description + "]";
	}

	@Override
	public STATE getState() {
		return state;
	}

	protected void setState(STATE newState){
		state = newState;
		/*
		 * always inform observers even if the state is the same as there may be new observers
		 */
		obsComp.notifyIObservers(this, state);
	}
	@Override
	public void abort() {
		setState(STATE.ABORTED);
	}

	@Override
	public void pause() {
		setState(STATE.PAUSED);
	}

	public void beginRun() {
		setState(STATE.RUNNING);
	}
	
	public void endRun() {
		setState(STATE.COMPLETED);
	}

	@Override
	public void addIObserver(IObserver anIObserver) {
		obsComp.addIObserver(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		obsComp.deleteIObserver(anIObserver);
	}

	@Override
	public void deleteIObservers() {
		obsComp.deleteIObservers();
	}	
	
	@Override
	public void resume() throws Exception {
		run();
	}
	
}
