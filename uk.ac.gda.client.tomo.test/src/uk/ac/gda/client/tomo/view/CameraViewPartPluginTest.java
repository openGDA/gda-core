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

package uk.ac.gda.client.tomo.view;

import gda.TestHelpers;
import gda.commandqueue.CommandQueue;
import gda.commandqueue.FindableProcessorQueue;
import gda.commandqueue.Processor;
import gda.commandqueue.Queue;
import gda.rcp.util.OSGIServiceRegister;

import java.io.File;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.gda.client.CommandQueueView;
import uk.ac.gda.client.tomo.test.PluginTestHelpers;

/**
 *
 */
public class CameraViewPartPluginTest {

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

	private String scratchFolder;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		scratchFolder=TestHelpers.setUpTest(CameraViewPartPluginTest.class, "setUp", true);
/*		FindableProcessorQueue simpleProcessor = new FindableProcessorQueue();
		simpleProcessor.setLogFilePath(scratchFolder + File.separator + "commandQueue.log");
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
*/		
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
		IViewPart part = window.getActivePage().showView(CameraViewPart.ID);
		if( !(part instanceof CameraViewPart)){
			throw new PartInitException("View is not a CameraViewPart");
		}
		PluginTestHelpers.delay(300000); //time to 'play with the graph if wanted
	}

}
