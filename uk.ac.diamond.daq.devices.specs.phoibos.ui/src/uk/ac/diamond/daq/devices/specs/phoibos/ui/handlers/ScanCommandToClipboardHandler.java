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

package uk.ac.diamond.daq.devices.specs.phoibos.ui.handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

public class ScanCommandToClipboardHandler {

	private final ScanCommandBuilder scanCommandBuilder = new ScanCommandBuilder();

	@Execute
	public void execute(Display display) {
		// Get the scan command
		final String scanCommand = scanCommandBuilder.buildScanCommand();

		// Get the clipboard
		final Clipboard clipboard = new Clipboard(display);

		// Put the scanCommand on the clipboard
		clipboard.setContents(new Object[] { scanCommand }, new Transfer[] { TextTransfer.getInstance() });

		// Cleanup
		clipboard.dispose();
	}
}

