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

import gda.observable.IObservable;

import java.io.Serializable;

/**
 * The Command interface for commands to be stored in CommandQueue
 * and processed by FindableProcessorQueue
 * 
 * A Command can be run, paused, aborted and has a state.
 * 
 * Observers receive CommandProgress objects
 */
public interface Command  extends IObservable, Serializable{
	String getDescription() throws Exception;
	CommandDetails getDetails() throws Exception;
	void setDetails(String details) throws Exception;
	void run() throws Exception;
	void pause() throws Exception;
	void abort() throws Exception;
	void resume() throws Exception; //resume after a pause

	enum STATE {NOT_STARTED, RUNNING, PAUSED, ABORTED, COMPLETED, ERROR}
	
	public STATE getState() throws Exception;
	
	public CommandSummary getCommandSummary() throws Exception;
}
