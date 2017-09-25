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

package gda.data.scan.datawriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.Findable;
import gda.factory.Finder;
import gda.scan.IScanDataPoint;
import gda.scan.ScanDataPointFormatter;

/**
 * Generic and configurable writer of ascii files. The format of the file is defined using an
 * AsciiDataWriterConfiguration object.
 */
public class AsciiDataWriter extends IncrementalFile implements ConfigurableAsciiFormat{

	private static Logger logger = LoggerFactory.getLogger(AsciiDataWriter.class);

	protected AsciiDataWriterConfiguration configuration;

	private ScanDataPointFormatter scanDataPointFormatter = null;
	protected String columnHeader = "";

	public AsciiDataWriter() throws InstantiationException {
		List<Findable> configs = Finder.getInstance().listAllObjects(FindableAsciiDataWriterConfiguration.class.getSimpleName());
		if (configs.size() == 0)
			throw new InstantiationException("No configuration object for " + this.getClass().getName() + " found");
		configuration = ((AsciiDataWriterConfiguration) configs.get(0));
	}

	public AsciiDataWriter(AsciiDataWriterConfiguration configuration) throws InstantiationException{
		this.configuration = configuration;
	}

	@Override
	public void addData(IScanDataPoint dataPoint) throws Exception {
		try {
			if (firstData) {
				this.setupFile();
				columnHeader = dataPoint.getHeaderString(scanDataPointFormatter);
				writeColumnHeadings();
				firstData = false;
			}
			try {
				file.write(dataPoint.toFormattedString(getScanDataPointFormatter()) + "\n");
				file.flush();
			} catch (IOException e) {
				logger.error("IOException while writing data point to ascii file: " + e.getMessage());
			}

		} finally {
			try {
				super.addData(this, dataPoint);
			} catch (Exception e) {
				logger.error("exception received from DataWriterBase.addData(...)", e);
			}
		}
	}

	@Override
	public void writeColumnHeadings() {
		try {
			file.write("# "+columnHeader + "\n");
			file.flush();
		} catch (IOException e) {
			logger.error("IOException while writing column headings to ascii file: " + e.getMessage());
		}
	}

	@Override
	public void writeFooter() {
		if (configuration == null) return;
		for (AsciiMetadataConfig line : this.configuration.getFooter()) {
			try {
				String string = this.configuration.getCommentMarker() + " " + line.toString() + "\n";
				file.write(string);
				file.flush();
			} catch (IOException e) {
				// ignore and keep trying!
			}
		}
	}

	@Override
	public void writeHeader() {
		if (configuration == null) return;
		ArrayList<AsciiMetadataConfig> header = configuration.getHeader();
		if (header != null) {
			for (AsciiMetadataConfig line : header) {
				try {
					String string = this.configuration.getCommentMarker() + " " + line.toString() + "\n";
					file.write(string);
					file.flush();
				} catch (IOException e) {
					// ignore and keep trying!
				}
			}
		}
	}

	@Override
	public void setHeader(String header) {
	}

	@Override
	public AsciiDataWriterConfiguration getConfiguration() {
		return configuration;
	}

	@Override
	public void setConfiguration(AsciiDataWriterConfiguration configuration) {
		this.configuration = configuration;
	}

	public void setScanDataPointFormatter(ScanDataPointFormatter scanDataPointFormatter){
		this.scanDataPointFormatter = scanDataPointFormatter;
	}

	public ScanDataPointFormatter getScanDataPointFormatter() {
		return scanDataPointFormatter;
	}

	/*
	 * This allows inheriting classes to extend the functionality at this point
	 *
	 * @throws Exception
	 */
	protected void setupFile() throws Exception {
		super.prepareForCollection();
	}
}
