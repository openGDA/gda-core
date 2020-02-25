/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.browser;

import org.eclipse.jface.action.Action;

import gda.rcp.views.Browser;
import uk.ac.diamond.daq.mapping.ui.experiment.file.SavedScanMetaData;
import uk.ac.diamond.daq.mapping.ui.experiment.saver.ScanSaver;

/**
 * Defines a delete operation to be used by a {@link Browser} row.
 *
 * @author Maurizio Nagni
 */
public class DeleteAcquisitionConfigurationResource extends Action {
	private final SavedScanMetaData selectedScan;
	private final ScanSaver scanSaver;

	public DeleteAcquisitionConfigurationResource(SavedScanMetaData selectedScan, ScanSaver scanSaver) {
		super("Delete Scan");
		this.selectedScan = selectedScan;
		this.scanSaver = scanSaver;
	}

	@Override
	public void run() {
		scanSaver.delete(selectedScan);
	}
}
