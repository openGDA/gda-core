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
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * FindableProcessorQueue implements Processor and Queue
 * The implementation of Queue is done using delegation to the Queue
 * object set using the setQueue method
 *
 */
public class FindableProcessorQueue implements IFindableQueueProcessor, Runnable, IObserver, InitializingBean {
	ObservableComponent obsComp = new ObservableComponent();

	private static final Logger logger = LoggerFactory.getLogger(FindableProcessorQueue.class);

	enum COMMAND { NONE, START, STOP, PAUSE}

	/*
	 * Thread that actually calls the run method of the indivudal Commands
	 * retrieve from the Queue
	 */
	Thread managerThread;

	/*
	 * The Queue holding the commands
	 */
	Queue queue;

	Boolean running=false;
	private boolean waitingForCommandOrQueue=false;

	Processor.STATE state=Processor.STATE.WAITING_START;

	/*
	 * As the object can be called in many threads we need to synchronize access
	 * to fields used to communicate with the managerThread
	 */
	Object lock=new Object();

	/*
	 * The last Command removed from the Queue for processing and has not
	 * yet been fully processed
	 */
	private Command cmdBeingProcessed;

	/*
	 * COMMAND received outside of the managerThread
	 */
	COMMAND commandToBeProcessed=COMMAND.NONE;

	/*
	 * Flag to signal to managerThread that a queueChange event has occurred
	 */
	Boolean queueChanged=false;

	/*
	 * Flag to signal to managerThread that the current Command has had its skip method
	 * call
	 */
	Boolean skip=false;

	/*
	 * Allows a mode where the users expect to always have to manually start the queue
	 */
	boolean pauseWhenQueueEmpty = false;

	boolean startImmediately=false;

	public void setQueue(Queue queue) {
		if(this.queue != null)
			this.queue.deleteIObserver(this);
		this.queue = queue;
		queue.addIObserver(this);
	}

	void sendCommandToManagerThread(long timeout_ms, COMMAND cmd) throws Exception {
		synchronized(lock){
			if(cmd.equals(COMMAND.PAUSE) ){
				if( cmdBeingProcessed != null)
					cmdBeingProcessed.pause();
				/**
				 * pause the current task and the queue
				 */
				commandToBeProcessed = cmd;
				lock.notifyAll();
				return;
			}

			if(cmd.equals(COMMAND.STOP) ){
				if( cmdBeingProcessed != null)
					cmdBeingProcessed.abort();
				/**
				 * stop the current task and the queue
				 */
				commandToBeProcessed = cmd;
				lock.notifyAll();
				return;
			}

			if( cmd.equals(COMMAND.START)){
				if( cmdBeingProcessed != null){
					if(cmdBeingProcessed.getState().equals(Command.STATE.PAUSED ))
						cmdBeingProcessed.resume();
				} else {
					commandToBeProcessed = COMMAND.START;
					lock.notifyAll();
				}
			}

		}
		if(cmd.equals(COMMAND.START) ){
			long time_ms=0;
			while((commandToBeProcessed.equals(cmd)) && time_ms<=timeout_ms){
				try {
					Thread.sleep(50);
					time_ms+=50;
				} catch (InterruptedException e) {
					logger.error("Interrupted",e);
				}
			}
			if (time_ms > timeout_ms){
				synchronized(lock){
					if( ( ( cmdBeingProcessed != null) && !getCmdBeingProcessedState().equals(Command.STATE.RUNNING))){
						throw new TimeoutException();
					}
				}
			}
		}
	}

	@Override
	public void pause(long timeout_ms) throws Exception {
		sendCommandToManagerThread(timeout_ms, COMMAND.PAUSE);
	}

	@Override
	public void skip(long timeout_ms) throws Exception {
		synchronized(lock){
			if(cmdBeingProcessed != null)
				cmdBeingProcessed.abort();
			skip = true;
			lock.notifyAll();
		}

	}

	@Override
	public void start(long timeout_ms) throws Exception {
		//if manager thread does not exist then create
		//if it does exist set volatile started flag and notify watchers ( which could be the manager thread
		if(managerThread == null ){
			managerThread = new Thread(this, "SimpleProcessor-ManagerThread");
			managerThread.start();
		}
		sendCommandToManagerThread(timeout_ms, COMMAND.START);
	}

	@Override
	public void stop(long timeout_ms) throws Exception {
		sendCommandToManagerThread(timeout_ms, COMMAND.STOP);
	}

	@Override
	public void update(Object source, Object arg) {
		if(source == queue){
			synchronized (lock) {
				queueChanged = true;
				lock.notifyAll();
			}
		}
		if( source == cmdBeingProcessed){
			if( arg instanceof CommandProgress){
				log((CommandProgress)arg);
			} else if (arg instanceof Command.STATE){
				Command.STATE argState = (Command.STATE) arg;
				/*
				 * If command enters paused state then set processor state to waiting start
				 */
				if (argState.equals(Command.STATE.PAUSED)) {
					this.state = STATE.WAITING_START;
					notifyListeners();
				}
				/*
				 * If the command has come out of paused state indicated by the processor being in Waiting_start state
				 * and command state is now running then set processor state to running
				 */
				else if (this.state.equals(Processor.STATE.WAITING_START) && argState.equals(Command.STATE.RUNNING)) {
					this.state = STATE.PROCESSING_ITEMS;
					notifyListeners();
				}
				// if the queue has just completed and the pauseWhenQueueEmpty flag is set, then send a pause to the
				// manager thread
				else if (this.state.equals(Processor.STATE.PROCESSING_ITEMS)
						&& (argState.equals(Command.STATE.COMPLETED) || argState.equals(Command.STATE.ABORTED))) {
					try {
						int size = queue.getSummaryList().size();
						if (size == 0){
							// if empty then the behaviour is governed by the pauseWhenQueueEmpty attribute
							if (pauseWhenQueueEmpty) {
								this.state = STATE.WAITING_START;
								commandToBeProcessed = COMMAND.PAUSE;
							} else {
								this.state = STATE.WAITING_START;
								commandToBeProcessed = COMMAND.START;
							}
						} else {
							// if normal completion or skipped then continue running the queue
							if (skip || argState.equals(Command.STATE.COMPLETED)){
								this.state = STATE.WAITING_START;
								commandToBeProcessed = COMMAND.START;
								skip = false;
							} else { // the red 'abort and pause' button pressed
								this.state = STATE.WAITING_START;
								commandToBeProcessed = COMMAND.PAUSE;
							}
						}
					} catch (Exception e) {
						// should never be a timeout when pausing an empty queue
					}
					notifyListeners();
				}
			}

		}
		obsComp.notifyIObservers(this, arg);
	}

	private void notifyObserversOfProgress(float percentDone, String msg){
		obsComp.notifyIObservers(this, new SimpleCommandProgress(percentDone, msg));
	}

	@Override
	public void run() {
		cmdBeingProcessed = null;
		while(true){

			//enter region of waiting for a change to the queue or a change of started
			boolean notifyListenersNeeded=false;
			synchronized (lock) {
				if(commandToBeProcessed.compareTo(COMMAND.START)==0){
					running=true;
					commandToBeProcessed=COMMAND.NONE;
				}
				else if( (commandToBeProcessed.compareTo(COMMAND.STOP)==0) ||
						(commandToBeProcessed.compareTo(COMMAND.PAUSE)==0)){
					running=false;
					commandToBeProcessed=COMMAND.NONE;
				}
				waitingForCommandOrQueue=true;
				notifyListenersNeeded=setState();
			}

			if(notifyListenersNeeded){
				notifyListeners();
				notifyListenersNeeded = false;
			}

			synchronized (lock) {
				if(!queueChanged && !skip && commandToBeProcessed.compareTo(COMMAND.NONE)==0){
					try {
						lock.wait();
					} catch (InterruptedException e) {
						//
					}
				}
				skip = false; // acknowledge
				queueChanged = false; //acknowledge the queueChanged event

				if(commandToBeProcessed.compareTo(COMMAND.START)==0){
					running=true;
				}
				else if( (commandToBeProcessed.compareTo(COMMAND.STOP)==0) ||
						(commandToBeProcessed.compareTo(COMMAND.PAUSE)==0)){
					running=false;
				}

				commandToBeProcessed=COMMAND.NONE; //acknowledge command

				waitingForCommandOrQueue=false;
				notifyListenersNeeded=setState();
			}

			if(notifyListenersNeeded){
				notifyListeners();
				notifyListenersNeeded = false;
			}

			while(running && ((cmdBeingProcessed = removeQueueHead()) != null)){

				cmdBeingProcessed.addIObserver(this);
				notifyListeners();
				String description =  "Unknown command";
				try {
					description = cmdBeingProcessed.getDescription();
				} catch (Exception e1) {
					logger.error("Error getting description of command");
				}
				try {
					String msg = "Started: "+description;
					log(msg);
					notifyObserversOfProgress(0, msg);
					cmdBeingProcessed.run();
					msg = String.format((cmdBeingProcessed.getState().equals(Command.STATE.COMPLETED)? "Completed: %s" : "Aborted: %s"),description);
					log(msg);
					notifyObserversOfProgress(100, msg);
				} catch (Exception e) {
					logger.error("Error in run of current command: "+description,e);
					String msg = "Error in: "+description + e.getMessage();
					notifyObserversOfProgress(100, msg);
					log(msg);
				}
				cmdBeingProcessed.deleteIObserver(this); // TODO repeat above in catch?
				cmdBeingProcessed = null;

				synchronized(lock){
					if(commandToBeProcessed.compareTo(COMMAND.START)==0){
						running=true;
					}
					else if( (commandToBeProcessed.compareTo(COMMAND.STOP)==0) ||
							(commandToBeProcessed.compareTo(COMMAND.PAUSE)==0)){
						running=false;
					}
				}
				commandToBeProcessed=COMMAND.NONE;
				/*
				 * there is always a state change as we have just processed a command
				 */
				setState();
				notifyListeners();

			}
		}
	}

	private Command.STATE getCmdBeingProcessedState() {
		try {
			return cmdBeingProcessed == null ? Command.STATE.COMPLETED: cmdBeingProcessed.getState();
		} catch (Exception e) {
			logger.error("Error getting state of current command being processed", e);
		}
		return Command.STATE.COMPLETED;
	}

	private Command removeQueueHead() {
		try {
			Command cmd = queue.removeHead();
			queueChanged = false; //queueChanged is to inform that an external event changed the queue - not an internal one
			return cmd;
		} catch (Exception e) {
			logger.error("Error in removeQueueHead", e);
		}
		return null;
	}

	void notifyListeners(){
		obsComp.notifyIObservers(this, state);
	}

	@Override
	public STATE getState() {
		return state;
	}

	private boolean setState(){
		STATE newState;
		if(!running){
			newState = STATE.WAITING_START;
		}else if (waitingForCommandOrQueue){
			if( (cmdBeingProcessed != null) && (getCmdBeingProcessedState() == Command.STATE.PAUSED))
				newState = STATE.WAITING_START;
			else
				newState = STATE.WAITING_QUEUE;
		}else {
			newState = STATE.PROCESSING_ITEMS;
		}
		if(newState.compareTo(state)!=0){
			state = newState;
			return true;
		}
		return false;
	}

	public String name;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public CommandId addToTail(Command command) throws Exception {
		return queue.addToTail(command);
	}

	@Override
	public List<QueuedCommandSummary> getSummaryList() throws Exception {
		return queue.getSummaryList();
	}


	@Override
	public void moveToBefore(CommandId id, Collection<CommandId> cmdIds) throws Exception {
		queue.moveToBefore(id, cmdIds);
	}

	@Override
	public void moveToHead(Collection<CommandId> cmdIds) throws Exception {
		queue.moveToHead(cmdIds);
	}

	@Override
	public void moveToTail(Collection<CommandId> cmdIds) throws Exception {
		queue.moveToTail(cmdIds);
	}

	@Override
	public Command remove(CommandId id) throws Exception {
		return queue.remove(id);
	}

	@Override
	public Collection<Command> removeAll() throws Exception {
		return queue.removeAll();
	}

	@Override
	public Command removeHead() throws Exception {
		return removeQueueHead();
	}

	@Override
	public void replace(CommandId id, Command cmd) throws Exception {
		queue.replace(id, cmd);
	}

	@Override
	public String toString() {
		return "SimpleProcessor [name=" + name + ", state=" + state + "]";
	}

	@Override
	public ProcessorCurrentItem getCurrentItem() {
		try{
			synchronized(lock){
				if( cmdBeingProcessed != null)
					return new ProcessorCurrentItem(cmdBeingProcessed.getDescription(), getRemovedHeadID());
			}
		} catch (Exception e){
			logger.error("Error getting current item",e);
		}
		return null;
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
	public void remove(Collection<CommandId> cmdIds) throws Exception {
		queue.remove(cmdIds);
	}

	@Override
	public CommandDetails getCommandDetails(CommandId id) throws Exception {
		return queue.getCommandDetails(id);
	}

	@Override
	public void setCommandDetails(CommandId id, String details) throws Exception {
		queue.setCommandDetails(id, details);
	}

	@Override
	public CommandSummary getCommandSummary(CommandId id) throws Exception {
		return queue.getCommandSummary(id);
	}

	@Override
	public CommandId addToTail(CommandProvider provider) throws Exception {
		return queue.addToTail(provider);
	}

	@Override
	public String getLogFilePath() {
		throw new UnsupportedOperationException(String.format("%s no longer writes its own file", getClass().getSimpleName()));
	}

	/**
	 * @param logFilePath The logFilePath to set.
	 */
	public void setLogFilePath(@SuppressWarnings("unused") String logFilePath) {
		final String className = getClass().getSimpleName();
		logger.warn(String.format("Please remove the 'logFilePath' property from your %s bean definition - it is no longer used", className));
		logger.warn(String.format("%s now logs using an SLF4J logger, instead of writing its own file", className));
	}

	public boolean isPauseWhenQueueEmpty() {
		return pauseWhenQueueEmpty;
	}

	/**
	 * Auto pause the queue when it empties
	 *
	 * @param pauseWhenQueueEmpty
	 */
	public void setPauseWhenQueueEmpty(boolean pauseWhenQueueEmpty) {
		this.pauseWhenQueueEmpty = pauseWhenQueueEmpty;
	}

	private void log(String msg){
		logger.debug(msg);
	}

	private void log(CommandProgress p){
		log(p.getPercentDone() + "%: " + p.getMsg());
	}
	/**
	 * @param startImmediately The startImmediately to set.
	 */
	public void setStartImmediately(boolean startImmediately) {
		this.startImmediately = startImmediately;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if( startImmediately)
			start(5000);
	}

	@Override
	public void stopAfterCurrent() throws Exception {
		//add Command to Top of queue that will stop the processor
		CommandId addToTail = queue.addToTail(new StopCommand(this, 50));
		List<CommandId> ids = new Vector<CommandId>();
		ids.add(addToTail);
		queue.moveToHead(ids);
	}

	@Override
	public CommandId getRemovedHeadID() {
		return queue.getRemovedHeadID();
	}
}
