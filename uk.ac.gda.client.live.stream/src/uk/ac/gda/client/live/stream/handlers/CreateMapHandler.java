/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.gda.client.live.stream.handlers;

import static uk.ac.gda.client.live.stream.Activator.getService;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistenceService;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistentFile;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.live.stream.Activator;
import uk.ac.gda.client.live.stream.view.LiveStreamView;

/**
 * A handler to take a snapshot of a live stream and send to the 'Mapped Data' view.
 * This is done by taking a snapshot and sending an {@link Event}.
 */
public class CreateMapHandler extends AbstractHandler {

	private static final String TOPIC_NAME_MAPPING_FILE_OPEN = "org/dawnsci/events/file/OPEN";
	private static final String PROPERTY_NAME_PATH = "path";

	private final Logger logger = LoggerFactory.getLogger(CreateMapHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final LiveStreamView liveStreamView = getLiveStreamView(event);
		final SnapshotData snapshot = liveStreamView.getSnapshot();

		final Job createMapJob = Job.create("Create map from live stream", monitor -> createMap(snapshot));
		createMapJob.schedule();
		return null;
	}

	private IStatus createMap(SnapshotData snapshot) {
		try {
			final String filePath = getFilePath();
			final IPersistenceService persistenceService = getService(IPersistenceService.class);

			// create a persistent file and add the data and axis.
			final IPersistentFile persistentFile = persistenceService.createPersistentFile(filePath);
			persistentFile.setData(snapshot.getDataset(), snapshot.getxAxis(), snapshot.getyAxis()); // handles null for axes

			// send an event to update the Mapped Data view
			updateMappedDataView(filePath);
		} catch (Exception e) {
			logger.error("Cannot save camera snapshot to file.", e);
			Display.getDefault().asyncExec(() -> MessageDialog.openError(getShell(),
					"Error", "Cannot save camera snapshot to file."));
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Cannot save camera snapshot to file.", e);
		}
		return Status.OK_STATUS;
	}

	/**
	 * Post an event to the mapping open file topic. This event will cause the file
	 * to be shown in the 'Mapped Data' view.
	 * @param filePath
	 */
	private void updateMappedDataView(String filePath) {
		final Map<String, Object> properties = new HashMap<>(2);
		properties.put(PROPERTY_NAME_PATH, filePath);

		final EventAdmin eventAdmin = getService(EventAdmin.class);
		eventAdmin.postEvent(new Event(TOPIC_NAME_MAPPING_FILE_OPEN, properties));
	}

	private Shell getShell() {
		return Display.getDefault().getActiveShell();
	}

	private LiveStreamView getLiveStreamView(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (!(part instanceof LiveStreamView)) {
			throw new ExecutionException("Active part should be " + LiveStreamView.class.getName());
		}
		return (LiveStreamView) part;
	}

	private String getFilePath() throws Exception {
		final IFilePathService filePathService = getService(IFilePathService.class);
		final String processedFilesDir = filePathService.getProcessingDir();

		return filePathService.getNextPath(processedFilesDir, "snapshot");
	}

}
