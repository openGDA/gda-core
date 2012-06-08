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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

/**
 * CommandQueue is an implementation of Queue for handling Command objects 
 */
public class CommandQueue implements Queue {
	ObservableComponent obsComp = new ObservableComponent();
	HashMap<CommandId, Command> commands = new HashMap<CommandId, Command>();
	List<CommandId> queue = new LinkedList<CommandId>();
	
	@Override
	public CommandId addToTail(Command command) {
		CommandId generateCommandId = CommandId.generateCommandId();
		synchronized (queue){
			queue.add(generateCommandId);
			commands.put(generateCommandId, command);
		}
		notifyListeners();
		return generateCommandId;
	}
	
	@Override
	public List<QueuedCommandSummary> getSummaryList() throws Exception {
		synchronized (queue){
			Iterator<CommandId> iterator = queue.iterator();
			List<QueuedCommandSummary> summaries = new Vector<QueuedCommandSummary>();
			while(iterator.hasNext()){
				CommandId id = iterator.next();
				summaries.add(new QueuedCommandSummary(id, commands.get(id).getCommandSummary()));
			}
			return summaries;
		}
	}
	
	@Override
	public void moveToBefore(CommandId id, Collection<CommandId> cmdIds) {
		/*
		 * extract all cmds into a local collection 
		 * insert after id get index, extract and put back
		 * synchronize all actions
		 * 
		 */
		synchronized (queue) {
			//remove them all and then add back
			/*
			 * If id given then first check it is ok. If not then 
			 */
			int indexOfId=-1;
			if( id != null){
				indexOfId = queue.indexOf(id);
				if( indexOfId == -1)
					throw new IndexOutOfBoundsException("Item not in the list:"+id);
			}
			Iterator<CommandId> iterator = cmdIds.iterator();
			while(iterator.hasNext()){
				queue.remove(queue.indexOf(iterator.next()));
			}
			if(id == null)
				queue.addAll(cmdIds);
			else {
				queue.addAll(indexOfId, cmdIds);
			}
		}
		notifyListeners();
	}

	@Override
	public void moveToHead(Collection<CommandId> cmdIds) {
		moveToBefore(queue.get(0), cmdIds);
	}

	@Override
	public void moveToTail(Collection<CommandId> cmdIds) {
		moveToBefore(null, cmdIds);
	}

	
	@Override
	public Command removeHead() {
		Command cmd=null;
		synchronized (queue){
			if( queue.size()>0)
				cmd= commands.remove(queue.remove(0));
		}
		if(cmd!=null)	
			notifyListeners();
		return cmd;
	}
	@Override
	public Command remove(CommandId id) {
		Command cmd;
		synchronized (queue){
			cmd = commands.remove(queue.remove(id));
		}
		notifyListeners();
		return cmd;
	}

	@Override
	public Collection<Command> removeAll() {
		List<Command> cmds = new Vector<Command>();
		synchronized (queue){
			Iterator<CommandId> iterator = queue.iterator();
			while(iterator.hasNext()){
				CommandId id = iterator.next();
				cmds.add(commands.get(id));
			}
			queue.clear();
			commands.clear();
		}
		notifyListeners();
		return cmds;
	}
	
	@Override
	public void replace(CommandId id, Command cmd) {
		synchronized (queue){
			commands.put(id, cmd);
		}
		notifyListeners();
	}

	void notifyListeners(){
		obsComp.notifyIObservers(this, new QueueChangeEvent());
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
	public void remove(Collection<CommandId> cmdIds) {
		synchronized (queue){
			for(CommandId id : cmdIds){
				commands.remove(queue.remove(id));
			}
		}
		notifyListeners();
	}

	@Override
	public CommandDetails getCommandDetails(CommandId id) throws Exception {
		return commands.get(id).getDetails();
	}

	@Override
	public void setCommandDetails(CommandId id, String details) throws Exception {
		commands.get(id).setDetails(details);
		
	}

	@Override
	public CommandSummary getCommandSummary(CommandId id) throws Exception {
		return commands.get(id).getCommandSummary();
	}

	@Override
	public CommandId addToTail(CommandProvider provider) throws Exception {
		return addToTail(provider.getCommand());
	}


}
