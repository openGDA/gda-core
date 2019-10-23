/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.experiment.scans.mapping;

import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.IdBean;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;

import uk.ac.diamond.daq.experiment.api.TriggerableScan;
import uk.ac.diamond.daq.experiment.api.plan.ExperimentPlanException;
import uk.ac.diamond.daq.experiment.scans.Activator;

public class TriggerableMap implements TriggerableScan {

	/** not serialised */
	private QueuePreventingScanSubmitter scanSubmitter;

	private ScanRequest scanRequest;
	private boolean important;

	public TriggerableMap(ScanRequest scanRequest, boolean important) {
		this.scanRequest = scanRequest;
		this.important = important;
	}

	@Override
	public IdBean trigger() {
		if (scanSubmitter == null) initialise();
		try {
			final ScanBean scanBean = new ScanBean(scanRequest);
			if (important) {
				scanSubmitter.submitImportantScan(scanBean);
			} else {
				scanSubmitter.submitScan(scanBean);
			}
			return scanBean;
		} catch (Exception e) {
			throw new ExperimentPlanException(e);
		}
	}

	private void initialise() {
		scanSubmitter = new QueuePreventingScanSubmitter();
		scanSubmitter.setEventService(Activator.getService(IEventService.class));
	}

	/**
	 * Used by Marshaller
	 */
	public ScanRequest getScanRequest() {
		return scanRequest;
	}

	/**
	 * Used by Marshaller
	 */
	public boolean isImportant() {
		return important;
	}

}
