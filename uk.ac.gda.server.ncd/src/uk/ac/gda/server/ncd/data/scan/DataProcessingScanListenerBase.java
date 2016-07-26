/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.data.scan;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.scan.datawriter.DataWriter;
import gda.data.scan.datawriter.DataWriterExtenderBase;
import gda.data.scan.datawriter.IDataWriterExtender;
import gda.device.Scannable;
import gda.factory.Findable;
import gda.jython.InterfaceProvider;
import gda.scan.IScanDataPoint;
import gda.scan.ScanInformation;
import uk.ac.gda.server.ncd.data.ProcessingRunner;

public abstract class DataProcessingScanListenerBase extends DataWriterExtenderBase implements Findable {

	protected static final Logger logger = LoggerFactory.getLogger(DataProcessingScanListenerBase.class);
	protected Scannable detector;
	protected ProcessingRunner runner;
	protected String filepath;
	private String name;
	boolean enabled = false;
	private ScanInformation scanInformation;

	public DataProcessingScanListenerBase() {
		super();
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enable) {
		this.enabled = enable;
	}

	public Scannable getDetector() {
		return detector;
	}

	public ProcessingRunner getRunner() {
		return runner;
	}

	@Override
	public void addData(IDataWriterExtender parent, IScanDataPoint dataPoint) {
		try {
			super.addData(parent, dataPoint);
			if (filepath == null) {
				if (parent instanceof DataWriter) {
					DataWriter writer = (DataWriter)parent;
					filepath = writer.getCurrentFileName();
				}
			}
		} catch (Exception e) {
			logger.error("Could not update scanInformation");
		}
	}

	public void setDetector(Scannable detector) {
		this.detector = detector;
	}

	public void setRunner(ProcessingRunner runner) {
		this.runner = runner;
	}

	@Override
	public void completeCollection(IDataWriterExtender parent) {
		try {
			super.completeCollection(parent);
			if (!enabled || filepath == null) {
				return;
			}
			scanInformation = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation();
			String[] detectors = scanInformation.getDetectorNames();

			if (detector == null || Arrays.asList(detectors).contains(detector.getName())) {
				doProcessing();
			}
		} catch (Exception e) { // catch them all - prevent scans failing
			logger.error("Failed to complete data processing", e);
		} finally {
			scanInformation = null;
			filepath = null;
		}
	}

	protected abstract void doProcessing();

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return String.format("%s: %s", name, enabled ? "enabled" : "disabled");
	}

	public void __call__(boolean enable) {
		setEnabled(enable);
	}
}