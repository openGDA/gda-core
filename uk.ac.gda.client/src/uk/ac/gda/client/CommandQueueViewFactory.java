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

import gda.commandqueue.IFindableQueueProcessor;
import gda.commandqueue.Processor;
import gda.commandqueue.Queue;
import gda.configuration.properties.LocalProperties;
import gda.factory.Finder;

public class CommandQueueViewFactory implements IExecutableExtensionFactory {

	public static final String ID = "uk.ac.gda.client.CommandQueueViewFactory";

	public static final String GDA_USE_STATUS_QUEUE_VIEW = "gda.client.useStatusQueueView";

	private static IFindableQueueProcessor queueProcessor;
	private static boolean openQueueProcessorAlreadyAttempted = false;
	private static Boolean usingNewQueue = null;

	/**
	 * @return Returns the processor.
	 */
	public static Processor getProcessor() {
		return getQueueProcessor();
	}

	/**
	 * @return Returns the queue.
	 */
	public static Queue getQueue() {
		if (usingNewQueue()) {
			return null;
		}
		return getQueueProcessor();
	}

	private static IFindableQueueProcessor getQueueProcessor() {
		if (queueProcessor == null && !openQueueProcessorAlreadyAttempted) {
			queueProcessor = Finder.getInstance().listFindablesOfType(IFindableQueueProcessor.class).stream().findFirst().orElse(null);
		}
		return queueProcessor;
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
