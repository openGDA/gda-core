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

import java.util.Collection;
import java.util.List;
import java.util.Vector;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class CommandQueueTest {

	private CommandQueue queue;

	/**
	 */
	@BeforeClass
	public static void setUpBeforeClass() {
	}

	/**
	 */
	@AfterClass
	public static void tearDownAfterClass() {
	}

	/**
	 */
	@Before
	public void setUp() {
		queue = new CommandQueue();
	}

	/**
	 */
	@After
	public void tearDown() {
	}

	/**
	 * adds an item to the queue
	 * get list of descriptions
	 * test list has 1 item and its description is that of the 1 added
	 * @throws Exception 
	 */
	@Test
	public void testAddToEnd() throws Exception{
		JythonScriptFileRunnerCommand jythonCommand = new JythonScriptFileRunnerCommand();
		jythonCommand.setDescription("JythonCommand");
		queue.addToTail(jythonCommand);
		List<QueuedCommandSummary> summaryList = queue.getSummaryList();
		Assert.assertEquals(jythonCommand.getDescription(), 
				summaryList.get(summaryList.size()-1).getDescription());
	}

	/**
	 * adds an item to the queue
	 * get list of descriptions
	 * test list has 1 item and its description is that of the 1 added
	 * @throws Exception 
	 */
	@Test
	public void testOrder() throws Exception{
		JythonScriptFileRunnerCommand jythonCommand1 = new JythonScriptFileRunnerCommand();
		jythonCommand1.setDescription("JythonCommand1");
		queue.addToTail(jythonCommand1);
		JythonScriptFileRunnerCommand jythonCommand2 = new JythonScriptFileRunnerCommand();
		jythonCommand2.setDescription("JythonCommand2");
		queue.addToTail(jythonCommand2);
		List<QueuedCommandSummary> summaryList = queue.getSummaryList();
		Assert.assertEquals(jythonCommand1.getDescription(), 
				summaryList.get(0).getDescription());
		Assert.assertEquals(jythonCommand2.getDescription(), 
				summaryList.get(summaryList.size()-1).getDescription());
	}

	/**
	 * adds an item to the queue
	 * get list of descriptions
	 * test list has 1 item and its description is that of the 1 added
	 */
	@Test
	public void testRemoveHead(){
		JythonScriptFileRunnerCommand jythonCommand1 = new JythonScriptFileRunnerCommand();
		jythonCommand1.setDescription("JythonCommand1");
		queue.addToTail(jythonCommand1);
		JythonScriptFileRunnerCommand jythonCommand2 = new JythonScriptFileRunnerCommand();
		jythonCommand2.setDescription("JythonCommand2");
		queue.addToTail(jythonCommand2);
		Command head = queue.removeHead();
		Assert.assertEquals(jythonCommand1, head);
	}
	
	@Test
	public void testMoveToBeforeLastTwoToBeforeFirst() throws Exception{
		Vector<CommandPlusId> addQueueItems = addQueueItems();

		List<QueuedCommandSummary> summaryListBefore = queue.getSummaryList();

		/*
		 * get ids for 1 before last and last item in queue
		 */
		Collection<CommandId> cmdIds = new Vector<CommandId>();
		cmdIds.add(addQueueItems.get(addQueueItems.size()-2).id);
		cmdIds.add(addQueueItems.get(addQueueItems.size()-1).id);
		
		/*
		 * move the 2 items extracted to before the first item
		 */
		queue.moveToBefore(addQueueItems.firstElement().id, cmdIds);
		
		/*
		 * get summary add to the end the 2 first items - which should now be the 2 extracted and move to the front
		 */
		List<QueuedCommandSummary> summaryListAfter = queue.getSummaryList();
		summaryListAfter.add(summaryListAfter.remove(0));
		summaryListAfter.add(summaryListAfter.remove(0));

		Assert.assertEquals(summaryListBefore, summaryListAfter);

	}

	@Test
	public void testMoveToTailFirstTwo() throws Exception{
		Vector<CommandPlusId> addQueueItems = addQueueItems();

		List<QueuedCommandSummary> summaryListBefore = queue.getSummaryList();

		Collection<CommandId> cmdIds = new Vector<CommandId>();
		cmdIds.add(addQueueItems.get(0).id);
		cmdIds.add(addQueueItems.get(1).id);
		queue.moveToTail(cmdIds);

		List<QueuedCommandSummary> summaryListAfter = queue.getSummaryList();
		summaryListAfter.add(0,summaryListAfter.remove(summaryListAfter.size()-2));
		summaryListAfter.add(1,summaryListAfter.remove(summaryListAfter.size()-1));
		Assert.assertEquals(summaryListBefore, summaryListAfter);

	}
	
	
	@Test
	public void testRemoveByIndex0() throws Exception{
		Vector<CommandPlusId> addQueueItems = addQueueItems();
		List<QueuedCommandSummary> summaryListBefore = queue.getSummaryList();
		queue.remove(addQueueItems.get(0).id);
		List<QueuedCommandSummary> summaryListAfter = queue.getSummaryList();
		summaryListBefore.remove(0);
		Assert.assertEquals(summaryListBefore, summaryListAfter);
		
	}
	
	
	
	@Test
	public void testReplaceFirst() throws Exception{
		Vector<CommandPlusId> addQueueItems = addQueueItems();

		List<QueuedCommandSummary> summaryListBefore = queue.getSummaryList();

		
		JythonScriptFileRunnerCommand cmd = new JythonScriptFileRunnerCommand();
		cmd.setDescription("replacement JythonCommand");
		
		queue.replace(addQueueItems.get(0).id, cmd);

		List<QueuedCommandSummary> summaryListAfter = queue.getSummaryList();
		QueuedCommandSummary remove = summaryListBefore.remove(0);
		summaryListBefore.add(0, new QueuedCommandSummary(remove.id, cmd.getCommandSummary()));

		Assert.assertEquals(summaryListBefore, summaryListAfter);
		
	}

	@Test
	public void testReplaceLast() throws Exception{
		Vector<CommandPlusId> addQueueItems = addQueueItems();

		List<QueuedCommandSummary> summaryListBefore = queue.getSummaryList();

		
		JythonScriptFileRunnerCommand cmd = new JythonScriptFileRunnerCommand();
		cmd.setDescription("replacement JythonCommand");
		
		queue.replace(addQueueItems.get(addQueueItems.size()-1).id, cmd);

		List<QueuedCommandSummary> summaryListAfter = queue.getSummaryList();
		QueuedCommandSummary remove = summaryListBefore.remove(summaryListBefore.size()-1);
		summaryListBefore.add(new QueuedCommandSummary(remove.id, cmd.getCommandSummary()));

		Assert.assertEquals(summaryListBefore, summaryListAfter);
		
	}
	
	private Vector<CommandPlusId> addQueueItems() {
		Vector<CommandPlusId> cmds = new Vector<CommandPlusId>();
		for (int i = 0; i < 20; i++) {
			JythonScriptFileRunnerCommand cmd = new JythonScriptFileRunnerCommand();
			cmd.setDescription("JythonCommand" + i);
			cmds.add(new CommandPlusId(queue.addToTail(cmd), cmd));
		}
		return cmds;
	}


	Integer numQueueEvents=0;
	@Test
	public void testAddListener(){
		numQueueEvents=0;
		IObserver ql = new IObserver(){

			@Override
			public void update(Object source, Object arg) {
				numQueueEvents++;
			}
			
		};
		queue.addIObserver(ql);
		Vector<CommandPlusId> addQueueItems = addQueueItems();
		Assert.assertEquals(numQueueEvents, (Integer)addQueueItems.size());
	}

	@Test
	public void testRemoveListener(){
		numQueueEvents=0;
		IObserver ql = new IObserver(){

			@Override
			public void update(Object source, Object arg) {
				numQueueEvents++;
			}
			
		};
		queue.addIObserver(ql);
		Vector<CommandPlusId> addQueueItems = addQueueItems();
		queue.deleteIObserver(ql);
		addQueueItems();
		Assert.assertEquals(numQueueEvents, (Integer)addQueueItems.size());
	}

}

class CommandPlusId{
	CommandId id;
	Command cmd;
	public CommandPlusId(CommandId id, Command cmd) {
		super();
		this.id = id;
		this.cmd = cmd;
	}

}