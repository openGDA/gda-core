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

import java.io.File;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.annotation.scan.FileDeclared;
import org.eclipse.scanning.api.annotation.scan.ScanEnd;
import org.eclipse.scanning.api.annotation.scan.ScanFinally;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.scanning.api.scan.IScanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.PathConstructor;
import gda.data.scan.datawriter.DataWriterExtenderBase;
import gda.data.scan.datawriter.IDataWriterExtender;
import gda.device.Detector;
import gda.device.DeviceBase;
import gda.factory.Localizable;
import gda.jython.Jython;
import gda.jython.JythonServerFacade;
import gda.scan.IScanDataPoint;

/**
 * <p>
 * The FileRegistrar deals with archiving in both the GDA8 and the GDA9 scanning systems.
 *
 * <h3>To be used in 9</h3> the init-method "register" must be called. This ensures that the FileRegistrar is registered
 * as a scanning participant. Now whenever a scan is run its @FileDeclared and @ScanEnd annotations will be run.
 * <p>
 *
 * <pre>
 * {@code <bean id="FileRegistrar" class="gda.data.fileregistrar.FileRegistrar"} <b>init-method="register"></b>
 * {@code     <property name="name" value="FileRegistrar"/> }
 * {@code     <property name="directory" value="/dls/bl-misc/dropfiles2/icat/dropZone/}${gda.instrument}-" />
 * {@code </bean> }
 * </pre>
 *
 * <h3>To be used in 8</h3> In GDA8 File registration listens to scans (via DataWriterExtender) and can be used directly
 * by detectors. Files will be archived and listed in icat and possibly post-processed. Whatever the pipeline is
 * configured to do.
 *
 * <pre>
 * {@code <bean id="FileRegistrar" class="gda.data.fileregistrar.FileRegistrar"}
 * {@code     <property name="name" value="FileRegistrar"/> }
 * {@code     <property name="directory" value="/dls/bl-misc/dropfiles2/icat/dropZone/}${gda.instrument}-" />
 * {@code </bean> }
 * </pre>
 *
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

	/**
	 * Entry point in GDA8 scanning to register a file
	 */
	@Override
	public void registerFile(String fileName) {
		registerFiles(new String[] { fileName });
	}

	/**
	 * Entry point in GDA8 scanning to register files
	 */
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

	/**
	 * Normally in GDA8 the IFileRegistrar interface is made available to register files.
	 * That interface should be used rather than this method.
	 *
	 * In GDA9 this is redundant because annotations are used for any object participating
	 * in a scan. The @FileDeclared injects the filenames to be used in the scan. You
	 * may also inject the first position of the scan or any OSGi service or the ScanInformation
	 * when using this annotation.
	 *
	 * @param filePath - the full path to the file
	 */
	@FileDeclared
	public void addScanFile(String filePath) {
		addFile(filePath);
	}

	/**
	 *
	 * @param fileNameOrPath - If this starts with / it is considered a path, otherwise a name.
	 */
	private void addFile(final String fileNameOrPath) {

		if (fileNameOrPath == null || fileNameOrPath.isEmpty()) {
			return; // TODO Should this be an exception? Perhaps it should be an error condition not an ignored one.
		}

		logger.debug("Adding " + fileNameOrPath);
		String filePath = fileNameOrPath;
		if (fileNameOrPath.charAt(0) != '/') {
			filePath = PathConstructor.createFromDefaultProperty() + "/" + fileNameOrPath;
			logger.debug("Changed file path to " + filePath);
		}

		synchronized (files) { // TODO files is already a Vector which is synchronized.
			if (!files.contains(filePath)){
				files.add(filePath);
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

	@ScanEnd
	public void scanEnd() {
		addCurrentScanFile();
		kickOff();
	}

	@ScanFinally
	public void scanFinally() {
		if (files!=null) files.clear(); // Do not archive failed scans?
	}

	private void kickOff() {
		final String[] fileArr;
		final String datasetId;

		synchronized (files) {  // TODO files is already a Vector which is synchronized.
			if (files.isEmpty()) {
				return;
			}

			if (lastScanDataPoint != null) {
				datasetId = "scan-" + lastScanDataPoint.getScanIdentifier();
			} else {
				final IFilePathService pathService = FileRegistrarServiceHolder.getFilePathService();
				String id; // to get over the fact the datasetId is final
				try {
					int scanNumber = pathService.getScanNumber();
					id = "scan-" + scanNumber;
				} catch (Exception e) {
					logger.warn("Cannot get scan number from FilePathService, using file name instead");
					id = new File(files.iterator().next()).getName();
				}
				datasetId = id;
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
	/**
	 * Method called by spring to register the registrar with solstice scanning.
	 *
	 * @throws NullPointerException if there is no IRunnableDeviceService - this is intentional and an error
	 * @throws ClassCastException if IRunnableDeviceService is not a IScanService which is must be.
	 */
	public void register() {
		((IScanService)FileRegistrarServiceHolder.getRunnableDeviceService()).addScanParticipant(this);
		logger.info("Registered "+getClass().getSimpleName()+" as a particpant in scans");
	}

}