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

import gda.commandqueue.Processor;
import gda.commandqueue.Queue;
import gda.rcp.GDAClientActivator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.osgi.util.tracker.ServiceTracker;

public class CommandQueueViewFactory implements IExecutableExtensionFactory {
	public static final String ID = "uk.ac.gda.client.CommandQueueViewFactory";

	static Processor processor;
	static Queue queue;
	static Boolean openProcessorServiceAlreadyAttempted = false;
	static Boolean openQueueServiceAlreadyAttempted = false;
	
	
	/**
	 * @return Returns the processor.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Processor getProcessor() {
		if( processor == null && !openProcessorServiceAlreadyAttempted ){
			openProcessorServiceAlreadyAttempted = true;
			ServiceTracker processorTracker;
			processorTracker = new ServiceTracker(GDAClientActivator.getBundleContext(), gda.commandqueue.Processor.class.getName(), null);
			processorTracker.open();
			processor = (gda.commandqueue.Processor)processorTracker.getService();
		}
		return processor;
	}

	/**
	 * @return Returns the queue.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Queue getQueue() {
		if( queue == null && !openQueueServiceAlreadyAttempted){
			openQueueServiceAlreadyAttempted = true;
			ServiceTracker queueTracker;
			queueTracker = new ServiceTracker(GDAClientActivator.getBundleContext(), gda.commandqueue.Queue.class.getName(), null);
			queueTracker.open();		
			queue = (gda.commandqueue.Queue)queueTracker.getService();
		}
		return queue;
	}

	static public void showView(){
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(CommandQueueViewFactory.ID);
		} catch (PartInitException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	public CommandQueueViewFactory() {
		if( getProcessor() == null || getQueue() == null){
			throw new IllegalStateException("Unable to find processor or queue");
		}
	}

	@Override
	public Object create() throws CoreException {
		CommandQueueView view = new CommandQueueView();
		view.setProcessor(getProcessor());
		view.setQueue(getQueue());
		return view;
	}


}
