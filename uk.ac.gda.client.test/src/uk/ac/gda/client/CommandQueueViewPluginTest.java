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

package uk.ac.gda.client;

import gda.commandqueue.CommandQueue;
import gda.commandqueue.FindableProcessorQueue;
import gda.commandqueue.Processor;
import gda.commandqueue.Queue;
import gda.commandqueue.TestCommand;
import gda.rcp.util.OSGIServiceRegister;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 */
public class CommandQueueViewPluginTest {

	static final long MAX_TIMEOUT_MS = 500;


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
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		FindableProcessorQueue simpleProcessor = new FindableProcessorQueue();
		simpleProcessor.setStartImmediately(false);
		simpleProcessor.afterPropertiesSet();

		Queue queue = new CommandQueue();
		simpleProcessor.setQueue(queue);
		Processor processor = simpleProcessor;

		OSGIServiceRegister processorReg = new OSGIServiceRegister();
		processorReg.setClass(Processor.class);
		processorReg.setService(processor);
		processorReg.afterPropertiesSet();

		OSGIServiceRegister queueReg = new OSGIServiceRegister();
		queueReg.setClass(Queue.class);
		queueReg.setService(queue);
		queueReg.afterPropertiesSet();

	}

	/**
	 */
	@After
	public void tearDown() {

	}

	/**
	 * Test method for {@link uk.ac.gda.client.CommandQueueView#createPartControl(org.eclipse.swt.widgets.Composite)}.
	 * @throws Exception
	 */
	@Test
	public final void testShowView() throws Exception {



		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IViewPart part = window.getActivePage().showView(CommandQueueViewFactory.ID);
		if( !(part instanceof CommandQueueView)){
			throw new PartInitException("View is not a CommandQueueView");
		}
		CommandQueueView view = (CommandQueueView) part;
		TestCommand pauseCommand = new TestCommand(view.getProcessor(),MAX_TIMEOUT_MS);
		pauseCommand.pause=true;
		pauseCommand.setDescription("Pause");
		view.getQueue().addToTail(pauseCommand);
		for( int i=0; i< 10; i++){
			TestCommand normalCommand = new TestCommand(view.getProcessor(),MAX_TIMEOUT_MS);
			normalCommand.setDescription("Normal"+i);
			view.getQueue().addToTail(normalCommand);
		}
		Thread.sleep(500);


		view.getProcessor().start(MAX_TIMEOUT_MS);
		Thread.sleep(500);

//		Assert.assertEquals(Command.STATE.PAUSED, pauseCommand.getState());
//		Assert.assertEquals(Command.STATE.NOT_STARTED, normalCommand.getState());

		PluginTestHelpers.delay(300000); //time to 'play with the graph if wanted
	}

}
