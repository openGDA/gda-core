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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.event.ui.view.EventConnectionView;
import org.eclipse.scanning.event.ui.view.StatusQueueView;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.osgi.util.tracker.ServiceTracker;

import gda.commandqueue.Processor;
import gda.commandqueue.Queue;
import gda.configuration.properties.LocalProperties;
import gda.rcp.GDAClientActivator;

public class CommandQueueViewFactory implements IExecutableExtensionFactory {

	public static final String ID = "uk.ac.gda.client.CommandQueueViewFactory";

	public static final String GDA_USE_STATUS_QUEUE_VIEW = "gda.client.useStatusQueueView";

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
		if (LocalProperties.check(GDA_USE_STATUS_QUEUE_VIEW, false)) {
			// use the new GDA9 StatusQueueView
			String queueViewPropertiesId = createStatusQueuePropertiesString();
			StatusQueueView statusQueueView = new StatusQueueView();
			statusQueueView.setIdProperties(queueViewPropertiesId);
			return statusQueueView;
		} else {
			// use the old CommandQueueView
			CommandQueueView commandQueueView = new CommandQueueView();
			commandQueueView.setProcessor(getProcessor());
			commandQueueView.setQueue(getQueue());
			return commandQueueView;
		}
	}

	private String createStatusQueuePropertiesString() {
		String activeMqUri = LocalProperties.get(LocalProperties.GDA_ACTIVEMQ_BROKER_URI, "");
		String queueViewPropertiesId = EventConnectionView.createSecondaryId(activeMqUri,
				"org.eclipse.scanning.api",
				"org.eclipse.scanning.api.event.status.StatusBean",
				EventConstants.STATUS_SET,
				EventConstants.STATUS_TOPIC,
				EventConstants.SUBMISSION_QUEUE);
		queueViewPropertiesId = queueViewPropertiesId + "partName=Queue";
		return queueViewPropertiesId;
	}


}
