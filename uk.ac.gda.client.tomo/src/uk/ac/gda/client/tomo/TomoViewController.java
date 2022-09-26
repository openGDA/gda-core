/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo;

import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.client.tomo.configuration.view.handlers.IScanControllerUpdateListener;
import uk.ac.gda.client.tomo.configuration.view.handlers.ITomoScanController;

public abstract class TomoViewController implements InitializingBean {

	private ITomoScanController scanController;

	public ITomoScanController getScanController() {
		return scanController;
	}

	public void setScanController(ITomoScanController scanController) {
		this.scanController = scanController;
	}

	public void addScanControllerUpdateListener(IScanControllerUpdateListener scanControllerUpdateListener) {
		getScanController().addControllerUpdateListener(scanControllerUpdateListener);
	}

	public void removeScanControllerUpdateListener(IScanControllerUpdateListener scanControllerUpdateListener) {
		getScanController().removeControllerUpdateListener(scanControllerUpdateListener);
	}

	public void isScanRunning() {
		getScanController().isScanRunning();
	}

	public void dispose() {
		getScanController().dispose();
	}

	public void initialize() {
		scanController.initialize();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (scanController == null) {
			throw new IllegalArgumentException("'scanController' must be provided");
		}
	}
}
