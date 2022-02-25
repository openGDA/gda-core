/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.InterfaceProvider;
import uk.ac.gda.client.live.stream.view.SnapshotView;

public class SaveSnapshotHandler extends AbstractHandler {

	private static final Logger logger = LoggerFactory.getLogger(SaveSnapshotHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		String rootPath = InterfaceProvider.getPathConstructor().createFromProperty("gda.data.scan.datawriter.datadir");
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
		LocalDateTime dateTimeNow = LocalDateTime.now();
		String timestamp = dateTimeNow.format(dtf);
		Path path = Paths.get(rootPath, "processing", timestamp);

		SnapshotView snapshotView = (SnapshotView) HandlerUtil.getActivePart(event);
		IPlottingSystem<Composite> plottingSystem = snapshotView.getPlottingSystem();

		try {
			plottingSystem.savePlotting(path.toString());
		} catch (Exception e) {
			logger.error("Could not save file", e);
		}

		return null;
	}

}
