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

	private static Processor processor;
	private static Queue queue;
	private static Boolean openProcessorServiceAlreadyAttempted = false;
	private static Boolean openQueueServiceAlreadyAttempted = false;
	private static Boolean usingNewQueue = null;

	/**
	 * @return Returns the processor.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Processor getProcessor() {
		if (processor == null && !openProcessorServiceAlreadyAttempted) {
			openProcessorServiceAlreadyAttempted = true;
			final ServiceTracker processorTracker = new ServiceTracker(GDAClientActivator.getBundleContext(), Processor.class.getName(), null);
			processorTracker.open();
			processor = (Processor) processorTracker.getService();
		}
		return processor;
	}

	/**
	 * @return Returns the queue.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Queue getQueue() {
		if (usingNewQueue()) {
			return null;
		}

		if (queue == null && !openQueueServiceAlreadyAttempted) {
			openQueueServiceAlreadyAttempted = true;
			final ServiceTracker queueTracker = new ServiceTracker(GDAClientActivator.getBundleContext(), Queue.class.getName(), null);
			queueTracker.open();
			queue = (Queue) queueTracker.getService();
		}
		return queue;
	}

	public static void showView(){
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(CommandQueueViewFactory.ID);
		} catch (PartInitException e1) {
			// Logger may not be initialised
			System.err.println("Error initialising CommandQueueViewFactory");
			e1.printStackTrace();
		}

	}
	public static boolean usingNewQueue() {
		if (usingNewQueue == null) {
			usingNewQueue = LocalProperties.check(GDA_USE_STATUS_QUEUE_VIEW);
		}
		return usingNewQueue;
	}

	public CommandQueueViewFactory() {
		if (!usingNewQueue() && (getProcessor() == null || getQueue() == null)) {
			throw new IllegalStateException("Unable to find processor or queue");
		}
	}

	@Override
	public Object create() throws CoreException {
		if (usingNewQueue()) {
			// use the new GDA9 StatusQueueView
			final String queueViewPropertiesId = createStatusQueuePropertiesString();
			final StatusQueueView statusQueueView = new StatusQueueView();
			statusQueueView.setIdProperties(queueViewPropertiesId);
			return statusQueueView;
		} else {
			// use the old CommandQueueView
			final CommandQueueView commandQueueView = new CommandQueueView();
			commandQueueView.setProcessor(getProcessor());
			commandQueueView.setQueue(getQueue());
			return commandQueueView;
		}
	}

	private String createStatusQueuePropertiesString() {
		final String activeMqUri = LocalProperties.get(LocalProperties.GDA_ACTIVEMQ_BROKER_URI, "");
		String queueViewPropertiesId = EventConnectionView.createSecondaryId(activeMqUri,
				"org.eclipse.scanning.api",
				"org.eclipse.scanning.api.event.status.StatusBean",
				EventConstants.STATUS_TOPIC,
				EventConstants.SUBMISSION_QUEUE);
		queueViewPropertiesId = queueViewPropertiesId + "partName=Queue";
		return queueViewPropertiesId;
	}

}
