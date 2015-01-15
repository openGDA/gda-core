/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.PathConstructor;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.scan.datawriter.DataWriterExtenderBase;
import gda.data.scan.datawriter.IDataWriterExtender;
import gda.device.DeviceException;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.jython.InterfaceProvider;
import gda.jython.batoncontrol.ClientDetails;
import gda.scan.IScanDataPoint;

/**
 * DataWriterExtender to write CSV file listing all scans completed during a visit
 * includes date/time, scan command, title, scan file and fedID of user
 */
public class ScanListDataWriterExtender extends DataWriterExtenderBase implements Configurable {
	private static final Logger logger = LoggerFactory.getLogger(ScanListDataWriterExtender.class);
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	private File file;
	private IScanDataPoint lastScanDataPoint;
	private String filename;

	public void setFilename(String filename) {
		this.filename = filename;
	}

	@Override
	public void configure() throws FactoryException {
		if (filename != null && !filename.isEmpty()) {
			String path = PathConstructor.createFromProperty("gda.data.visitdirectory") + filename;
			this.file = new File(path);
			logger.debug("Configured to use file {}", file.getAbsolutePath());
		} else {
			logger.error("Can't configure scan list - file not set");
		}
	}


	@Override
	public void addData(IDataWriterExtender parent, IScanDataPoint dataPoint) throws Exception {
		if (lastScanDataPoint == null) {
			lastScanDataPoint = dataPoint;
			configure();
		}
	}

	@Override
	public void completeCollection(IDataWriterExtender parent) {
		if (file == null) {
			logger.error("File not set - not writing to scan list");
			return;
		}
		String outputLine = makeOutputLine();
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			logger.debug("Writing to {}", file.getAbsolutePath());
			if (file.canWrite()) {
				Writer writer = null;
				try {
					writer = new BufferedWriter(
							new OutputStreamWriter(
							new FileOutputStream(file.getAbsolutePath(), true)));
					writer.write(outputLine);
					logger.debug("Written to scanlist: {}", outputLine);
				} catch (FileNotFoundException e) {
					logger.error("File ({})not found", file.getAbsolutePath(), e);
				} catch (UnsupportedEncodingException e) {
					logger.error("Unsupported encoding", e);
				} catch (Exception e) {
					logger.error("Could not write to scan list", e);
				}  finally {
					if (writer != null) writer.close();
				}
			} else {
				logger.error("Cannot write to scan list file. Check permissions");
			}
		} catch (IOException ioe) {
			logger.error("Error writing to file", ioe);
		} finally {
			lastScanDataPoint = null;
		}
	}

	private String makeOutputLine() {
		Date today = new Date();

		String date = dateFormat.format(today);
		String command = StringEscapeUtils.escapeCsv(lastScanDataPoint.getCommand());
		String scanFile = StringEscapeUtils.escapeCsv(lastScanDataPoint.getCurrentFilename());
		String title = "";
		String visit = "";
		try {
			title = GDAMetadataProvider.getInstance().getMetadataValue("title");
			title = StringEscapeUtils.escapeCsv(title);
			visit = GDAMetadataProvider.getInstance().getMetadataValue("visit");
		} catch (DeviceException e) {
			logger.error("Could not get title or visit from metadata", e);
		}
		ClientDetails cd = InterfaceProvider.getBatonStateProvider().getBatonHolder();
		String user = cd.getUserID();

		String outLine = String.format("%s,%s,%s,%s,%s,%s\n", date, command, title, scanFile, user, visit);
		return outLine;
	}
}
