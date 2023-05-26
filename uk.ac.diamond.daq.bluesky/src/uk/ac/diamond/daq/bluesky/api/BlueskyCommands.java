/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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
package uk.ac.diamond.daq.bluesky.api;

import java.util.List;
import java.util.concurrent.ExecutionException;

import gda.jython.GdaJythonBuiltin;
import io.blueskyproject.TaggedDocument;
import uk.ac.diamond.daq.bluesky.commands.BlueskyScan;
import uk.ac.diamond.daq.bluesky.commands.DataTerminalPrinter;
import uk.ac.gda.core.GDACoreActivator;

public final class BlueskyCommands {

	private BlueskyCommands() {
		throw new UnsupportedOperationException();
	}

	@GdaJythonBuiltin(docstring = "Submit a scan plan to bluesky")
	public static void bscan(String motor, double start, double stop, int points, List<String> detectors)
			throws InterruptedException, ExecutionException, BlueskyException {
		var scan = new BlueskyScan(motor, start, stop, points, detectors);
		var controller = GDACoreActivator.getService(BlueskyController.class).orElseThrow();
		var printer = new DataTerminalPrinter();
		controller.addEventListener(TaggedDocument.class, printer);
		try {
			var future = controller.runTask(scan.toPlan());
			future.get();
		} finally {
			controller.removeWorkerEventListener(printer);
		}
	}
}