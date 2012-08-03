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

package gda.device.detector.addetectorprovisional.filewriter;

import gda.data.PathConstructor;
import gda.device.detector.addetector.filewriter.FileWriterBase;
import gda.device.detector.areadetector.v17.NDFile;

import org.apache.commons.lang.StringUtils;

/**
 * A provisional version of FileWriterBase with simpler and more versatile configuration settings.
 */
public abstract class ProvisionalFileWriterBase extends FileWriterBase {

	private String filePathTemplate;
	
	private String fileNameTemplate;
	
	private long fileNumberAtScanStart;

	/**
	 * @param ndFile
	 * @param fileTemplate
	 *            File template to pass directly to AreaDetector. AreaDetector's NDFilePlugin
	 *            (http://cars.uchicago.edu/software/epics/NDPluginFile.html) will always apply arguments to the
	 *            template in the order filepath, filename, filenumber
	 * @param filePathTemplate
	 *            The file path to pass to AreadDetector, with $scan$ resolving to the currently running scan number,
	 *            and $datadir$ to the current scan file's directory.
	 * @param fileNameTemplate
	 *            The file name to pass to AreadDetector, with $scan$ resolving to the currently running scan number,
	 *            and $datadir$ to the current scan file's directory.
	 * @param fileNumberAtScanStart If non-negative, the file number to preset in AreaDetector before staring the exposure
	 */
	public ProvisionalFileWriterBase(NDFile ndFile, String fileTemplate, String filePathTemplate,
			String fileNameTemplate, long fileNumberAtScanStart) {

		super(ndFile, null, fileTemplate, false, true);
		this.filePathTemplate = filePathTemplate;
		this.fileNameTemplate = fileNameTemplate;
		this.fileNumberAtScanStart = fileNumberAtScanStart;

	}
	
	@Override
	protected boolean isSetFileNumberToZero() {
		throw new AssertionError("this mechanism not used in this provisional version");
	}
	
	@Override
	protected void setSetFileNumberToZero(boolean setFileNumberToZero) {
		throw new AssertionError("this mechanism not used in this provisional version");
	}
	
	
	/**
	 * @return the file template to configure in AreaDetector
	 */
	@Override
	protected String getFileTemplate() {
		return fileTemplate;
	}

	
	public void setFileTemplate(String fileTemplate) {
		this.fileTemplate = fileTemplate;
	}
	
	public void setFilePathTemplate(String filePathTemplate) {
		this.filePathTemplate = filePathTemplate;
	}

	public String getFilePathTemplate() {
		return filePathTemplate;
	}
	
	public void setFileNameTemplate(String fileNameTemplate) {
		this.fileNameTemplate = fileNameTemplate;
	}

	public String getFileNameTemplate() {
		return fileNameTemplate;
	}

	public void setFileNumberAtScanStart(long fileNumberAtScanStart) {
		this.fileNumberAtScanStart = fileNumberAtScanStart;
	}

	public long getFileNumberAtScanStart() {
		return fileNumberAtScanStart;
	}

	/**
	 * @return the file path to configure in AreaDetector
	 */
	protected String getFilePath() {
		return substituteDatadirAndScan(getFilePathTemplate());
	}

	private String substituteDatadirAndScan(String template) {
		
		if (StringUtils.contains(template, "$datadir$")) {
			template = StringUtils.replace(template, "$datadir$", PathConstructor.createFromDefaultProperty());
		}
		
		if (StringUtils.contains(template, "$scan$")) {
			long scanNumber;
			try {
				scanNumber = getScanNumber();
			} catch (Exception e) {
				throw new IllegalStateException("Could not determine current scan number", e);
			}
			template = StringUtils.replace(template, "$scan$", String.valueOf(scanNumber));
		}
		return template;
	}

	/**
	 * @return the file name to configure in AreaDetector
	 */
	@Override
	protected String getFileName() {
		return substituteDatadirAndScan(getFileNameTemplate());
	}
	
}
