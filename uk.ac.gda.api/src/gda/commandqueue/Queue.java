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

import java.util.Collection;
import java.util.List;

import gda.observable.IObservable;

/**
 * Queue is used to handle a queue of Command objects
 *
 *
 * As well as typical Queue methods such as addToTail,
 * removeHead there are methods to move items around within the
 * Queue referenced by unique CommandIds.
 *
 * Objects that implement QueueListener can register for events on the queue
 * using the  addQueueListener method.
 */
public interface Queue extends IObservable{

	CommandId addToTail(Command command) throws Exception;

	CommandId addToTail(CommandProvider provider) throws Exception;

	Command removeHead() throws Exception;

	Command remove(CommandId id)  throws Exception;

	void remove(Collection<CommandId> cmdIds)  throws Exception;

	Collection<Command> removeAll()  throws Exception;

	List<QueuedCommandSummary> getSummaryList()  throws Exception;

	void moveToHead( Collection<CommandId> cmdIds)  throws Exception;

	void moveToTail( Collection<CommandId> cmdIds)  throws Exception;

	void moveToBefore( CommandId id, Collection<CommandId> cmdIds)  throws Exception;

	void replace(CommandId id, Command cmd)  throws Exception;

	CommandDetails getCommandDetails(CommandId id) throws Exception;

	void setCommandDetails(CommandId id, String details) throws Exception;

	CommandSummary getCommandSummary(CommandId id)  throws Exception;

	CommandId getRemovedHeadID();
}
