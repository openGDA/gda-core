/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.data.fileregistrar;

import gda.data.PathConstructor;
import gda.data.scan.datawriter.DataWriterExtenderBase;
import gda.data.scan.datawriter.IDataWriterExtender;
import gda.device.Detector;
import gda.device.DeviceBase;
import gda.factory.Localizable;
import gda.jython.Jython;
import gda.jython.JythonServerFacade;
import gda.scan.IScanDataPoint;

import java.io.File;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File registration service that listens to scans (via DataWriterExtender) and can be used directly by detectors. Files
 * will be archived and listed in icat and possibly post-processed. Whatever the pipeline is configured to do.
 */
public class FileRegistrar extends DataWriterExtenderBase implements IFileRegistrar, Localizable {

	private static final Logger logger = LoggerFactory.getLogger(FileRegistrar.class);

	private IScanDataPoint lastScanDataPoint = null;

	private IcatXMLCreator icatXMLCreator = new IcatXMLCreator();

	private DeviceBase sockPuppet;

	private Vector<String> files = new Vector<String>();

	private String name;

	private boolean local = false;

	private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 10, 1, TimeUnit.SECONDS,
			new LinkedBlockingQueue<Runnable>());

	@Override
	public void registerFile(String fileName) {
		registerFiles(new String[] { fileName });
	}

	@Override
	public void registerFiles(String[] fileNames) {
		for (String fileName : fileNames) {
			addFile(fileName);
		}
		// if we are not in a scan, send out immediately, otherwise wait for next ScanDataPoint to batch process
		if (JythonServerFacade.getInstance().getScanStatus() != Jython.RUNNING) {
			kickOff();
		}
	}

	private void addFile(String fileName) {
		if (fileName == null || fileName.isEmpty()) {
			return;
		}

		logger.debug("adding " + fileName);
		if (fileName.charAt(0) != '/') {
			fileName = PathConstructor.createFromDefaultProperty() + "/" + fileName;
			logger.debug("changed filename to " + fileName);
		}
		synchronized (files) {
			if (!files.contains(fileName)){
				files.add(fileName);
			}
		}
	}

	@Override
	public void addData(IDataWriterExtender parent, IScanDataPoint dataPoint) throws Exception {
		lastScanDataPoint = dataPoint;
		for (Detector detector : dataPoint.getDetectors()) {
			if (detector.createsOwnFiles()) {
				int index = dataPoint.getDetectorNames().indexOf(detector.getName());
				String fileName = (String) dataPoint.getDetectorData().get(index);
				addFile(fileName);
			}
		}
		super.addData(parent, dataPoint);
	}

	private void addCurrentScanFile() {
		if (lastScanDataPoint != null) {
			addFile(lastScanDataPoint.getCurrentFilename());
		}
	}

	@Override
	public void completeCollection(IDataWriterExtender parent) {
		addCurrentScanFile();
		kickOff();
		lastScanDataPoint = null; // to prevent this method doing its work twice in the same scan.
		super.completeCollection(parent);
	}

	private void kickOff() {
		final String[] fileArr;
		final String datasetId;

		synchronized (files) {
			if (files.isEmpty()) {
				return;
			}

			if (lastScanDataPoint != null) {
				datasetId = "scan-" + lastScanDataPoint.getScanIdentifier();
			} else {
				datasetId = new File(files.iterator().next()).getName();
			}
			fileArr = files.toArray(new String[0]);
			files.clear();
		}

		threadPoolExecutor.submit(new Runnable() {
			@Override
			public void run() {
				try {
					logger.info("icatXMLCreator.registerFiles started");
					icatXMLCreator.registerFiles(datasetId, fileArr);
					if (sockPuppet != null)
						sockPuppet.notifyIObservers(sockPuppet, fileArr);
				} catch (Exception e) {
					logger.error("Catching " + e.getClass() + " thrown in the XML generation step", e);
				}
				logger.info("icatXMLCreator.registerFiles completed");
			}
		});

		logger.debug("kicked off for datasetId " + datasetId + " registering "
				+ ((fileArr.length == 1) ? "one file" : (fileArr.length + " files")));
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * mainly for testing
	 *
	 * @return the xml creator instance
	 */
	protected IcatXMLCreator getIcatXMLCreator() {
		return icatXMLCreator;
	}

	/**
	 * used for testing
	 *
	 * @param icatXMLCreator
	 */
	protected void setIcatXMLCreator(IcatXMLCreator icatXMLCreator) {
		this.icatXMLCreator = icatXMLCreator;
	}

	/**
	 * the directory to create the XML in
	 *
	 * @param directory
	 */
	public void setDirectory(String directory) {
		icatXMLCreator.setDirectory(directory);
	}

	/**
	 * @return the configured directory
	 */
	public String getDirectory() {
		return icatXMLCreator.getDirectory();
	}

	@Override
	public boolean isLocal() {
		return local;
	}

	@Override
	public void setLocal(boolean local) {
		this.local = local;
	}

	public DeviceBase getClientFileAnnouncer() {
		return sockPuppet;
	}

	/**
	 * Clients can listen to this DeviceBase object for
	 * String arrays with filenames of recently created files
	 * to update their data projects.
	 * @param clientFileAnnouncer
	 */
	public void setClientFileAnnouncer(DeviceBase clientFileAnnouncer) {
		this.sockPuppet = clientFileAnnouncer;
	}
}