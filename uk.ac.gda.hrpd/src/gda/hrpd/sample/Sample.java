/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.hrpd.sample;

import java.io.File;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.data.PathConstructor;
import gda.factory.FactoryException;
import gda.factory.FindableConfigurableBase;
import gda.hrpd.SampleInfo;
import gda.hrpd.data.ExcelWorkbook;
import gda.jython.JythonServerFacade;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;

/**
 * This class reads the sample information data from a table in an Excel spreadsheet (default to sheet 0) and/or writes
 * experiment summary or metadata for each sample to a second spreadsheet. The default file name of this Excel file is
 * {@code Sample.xls}, which may be configured using java property {@code gda.data.sample.information.file} or in its
 * XML configuration element {@code <sampleInfoFile>}. The default directory where this spreadsheet is expected to be
 * should be user's home directory.
 */
@SuppressWarnings("serial")
public class Sample extends FindableConfigurableBase implements SampleInfo {
	private static final Logger logger = LoggerFactory.getLogger(Sample.class);

	private String carouselNo;
	// user input information on samples, mapped onto the template
	private String sampleID;
	private String sampleName;
	private String description;
	private String title;
	private String comment;
	// gda generated experiment metadata
	private String runNumber;
	private String date;
	private String time;
	private String beamline;
	private String project;
	private String experiment;
	private String wavelength;
	private String temperature;

	private String filename;

	private HSSFSheet sampleInfo;
	private HSSFSheet experimentSummary;

	private String sampleInfoFile;

	private int rowOffset = 0;

	private static final String NAME = "Sample";

	private boolean saveExperimentSummary = false;

	private ExcelWorkbook excel;

	private ObservableComponent observableComponent = new ObservableComponent();

	private boolean configureAtStartup = false;

	private boolean opened = false;

	/**
	 */
	public Sample() {
		super.setName(NAME);
	}

	@Override
	public void configure() throws FactoryException {
		if (!isConfigured()) {
			if ((sampleInfoFile = getSampleInfoFile()) != null) {
				// XML object configuration
				if (sampleInfoFile.startsWith("/")) {
					this.filename = sampleInfoFile;
				} else {
					// assume the file is at user's home directory
					String processingDir = LocalProperties.getVarDir();
					this.filename = processingDir + sampleInfoFile;
				}
			} else {
				// if XML does not configure, using property
				sampleInfoFile = LocalProperties.get("gda.data.sample.information.file", "SampleInfo.xls");
				if (sampleInfoFile.startsWith("/")) {
					this.filename = sampleInfoFile;
				} else {
					// assume the file is at user's home directory
					String processingDir = LocalProperties.getVarDir();
					this.filename = processingDir + sampleInfoFile;
				}
			}

			File file = new File(this.filename);

			if (!file.exists()) {
				logger.debug("Cannot find Sample Information file {}.", this.filename);
				logger.info("You may maunally input information per sample using object 'si' from JythonTerminal." );
				// sample information object will not be configured.
				return;
			}
			if (!file.canRead()) {
				logger.warn("Can not read from file {}. Permission denied. Scan will not be able to read data from this file.", this.filename);
			}
			if (!file.canWrite()){
				logger.warn("Can not write to file {}. Permission denied. Scan will not be able to save data to this file. ", this.filename);
			}
			setConfigured(true);
		}
	}

	/**
	 * open the workbook
	 */
	@Override
	public void open() {

		try {
			excel = new ExcelWorkbook(this.filename);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if ((sampleInfo = excel.getSheet("Sample")) == null) {
			sampleInfo = excel.getSheetAt(0);
		}
		if ((experimentSummary = excel.getSheet("ExperimentSummary")) == null) {
			if (isSaveExperimentSummary()) {
				try {
					experimentSummary = excel.createSheet("ExperimentSummary");
				} catch (IOException e) {
					logger.warn("{}, No Experiement Summary will be saved.",e.getMessage());
				}
			}
		}
		opened = true;
	}

	/**
	 * Closes the workbook.
	 */
	@Override
	public void close() {
		excel.close();
	}

	/**
	 * prints the current sample information to terminal.
	 */
	@Override
	public void values() {
		JythonServerFacade jsf = JythonServerFacade.getInstance();
		jsf.print("CarouselNo=" + getCarouselNo());
		jsf.print("SampleID=" + getSampleID());
		jsf.print("SampleName=" + getSampleName());
		jsf.print("Description=" + getDescription());
		jsf.print("Title=" + getTitle());
		jsf.print("Comment=" + getComment());
	}

	/**
	 * read existing data only {@inheritDoc}
	 *
	 * @see gda.hrpd.SampleInfo#loadSampleInfo(int)
	 */
	@Override
	public void loadSampleInfo(int sampleNo) {
		if (!opened) {
			logger.error("Sample Information File is not opened yet.");
			throw new IllegalStateException("Sample Information file " + this.filename + " must be opened first.");
		}
		logger.info("Load sample information from {} for sample number {}", getSampleInfoFile(), sampleNo);
		HSSFRow row = sampleInfo.getRow(sampleNo + rowOffset);
		if (row == null) {
			logger.error("specified sample Number {} does not exist.", sampleNo);
			JythonServerFacade.getInstance().print("No sample information is provided for sample number " + sampleNo);
		} else {
			carouselNo = excel.getCellValue(row.getCell(0));
			sampleID = excel.getCellValue(row.getCell(1));
			sampleName = excel.getCellValue(row.getCell(2));
			description = excel.getCellValue(row.getCell(3));
			title = excel.getCellValue(row.getCell(4));
			comment = excel.getCellValue(row.getCell(5));
			logger.info("Sample information for sample number {} is loaded.", sampleNo);
		}
	}

	/**
	 * to support modification option {@inheritDoc}
	 *
	 * @see gda.hrpd.SampleInfo#saveSampleInfo(int)
	 */
	@Override
	public void saveSampleInfo(int sampleNo) {
		HSSFRow row = sampleInfo.getRow(sampleNo + rowOffset);
		if (row == null) {
			// add more row to the spreadsheet
			row = sampleInfo.createRow(sampleNo + rowOffset);
		}
		try {
			excel.setCellValue(row, 0, carouselNo);
			excel.setCellValue(row, 1, sampleID);
			excel.setCellValue(row, 2, sampleName);
			excel.setCellValue(row, 3, description);
			excel.setCellValue(row, 4, title);
			excel.setCellValue(row, 5, comment);

			excel.write(this.filename);
		} catch (IOException e) {
			logger.warn("{}, Cannot save sample information to {}.",e.getMessage(), this.filename);
		}
	}

	@Override
	public void saveExperimentInfo(int sampleNo) {
		if (saveExperimentSummary) {
			HSSFRow row = experimentSummary.getRow(sampleNo + rowOffset);
			if (row == null) {
				// add more row to the spreadsheet
				row = experimentSummary.createRow(sampleNo + rowOffset);
			}
			try {
				excel.setCellValue(row, 0, carouselNo);
				excel.setCellValue(row, 1, runNumber);
				excel.setCellValue(row, 2, date);
				excel.setCellValue(row, 3, time);
				excel.setCellValue(row, 4, beamline);
				excel.setCellValue(row, 5, project);
				excel.setCellValue(row, 6, experiment);
				excel.setCellValue(row, 7, wavelength);
				excel.setCellValue(row, 8, temperature);

				excel.write(this.filename);
			} catch (IOException e) {
				logger.warn("{}, Cannot save experiement summary to {}.",e.getMessage(), this.filename);
			}
		} else {
			logger.warn("Cannot save experiment summary to Excel file. {}'s 'saveExperimentSummary' state is {}",
					getName(), saveExperimentSummary);
		}
	}

	@Override
	public String getCarouselNo() {
		return carouselNo;
	}

	@Override
	public void setCarouselNo(String caroselNo) {
		this.carouselNo = caroselNo;
	}

	@Override
	public void setCarouselNo(int caroselNo) {
		setCarouselNo(String.valueOf(caroselNo));
	}

	@Override
	public String getSampleID() {
		return sampleID;
	}

	@Override
	public void setSampleID(String sampelID) {
		this.sampleID = sampelID;
	}

	@Override
	public String getSampleName() {
		return sampleName;
	}

	@Override
	public void setSampleName(String sampelName) {
		this.sampleName = sampelName;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getComment() {
		return comment;
	}

	@Override
	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public void setRunNumber(String runNumber) {
		this.runNumber = runNumber;
	}

	@Override
	public void setDate(String date) {
		this.date = date;
	}

	@Override
	public void setTime(String time) {
		this.time = time;
	}

	@Override
	public void setBeamline(String beamline) {
		this.beamline = beamline;
	}

	@Override
	public void setExperiment(String experiment) {
		this.experiment = experiment;
	}

	@Override
	public void setProject(String project) {
		this.project = project;
	}

	@Override
	public void setWavelength(String wavelength) {
		this.wavelength = wavelength;
	}

	@Override
	public void setTemperature(String temperature) {
		this.temperature = temperature;
	}

	@Override
	public int getRowOffset() {
		return rowOffset;
	}

	@Override
	public void setRowOffset(int rowOffset) {
		this.rowOffset = rowOffset;
	}

	@Override
	public String getSampleInfoFile() {
		return sampleInfoFile;
	}

	@Override
	public void setSampleInfoFile(String sampleInfoFile) {
		String oldFile = this.sampleInfoFile;
		this.sampleInfoFile = sampleInfoFile;
		if (isConfigured()) {
			try {
				reconfigure(sampleInfoFile);
			} catch (FactoryException e) {
				this.sampleInfoFile = oldFile;
				logger.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public boolean isSaveExperimentSummary() {
		return saveExperimentSummary;
	}

	@Override
	public void setSaveExperimentSummary(boolean saveExperimentSummary) {
		this.saveExperimentSummary = saveExperimentSummary;
	}

	/**
	 * @param filename
	 * @throws FactoryException
	 */
	private void reconfigure(String filename) throws FactoryException {
		if (filename.startsWith("/")) {
			// full path is provided for the constructor.
			this.filename = filename;
		} else if (!filename.startsWith("/")) {
			// assume the file is at user's home directory
			String processingDir = PathConstructor.createFromDefaultProperty() + File.separator + "processing";
			this.filename = processingDir + File.separator + filename;
		}

		File file = new File(this.filename);
		if (!file.exists()) {
			logger.debug("Cannot find file {}", this.filename);
			throw new FactoryException("Cannot find file " + this.filename);
		}
		setConfigured(false);
		configure();
	}

	/**
	 * Check whether the configure method should be called when the server is instantiated.
	 *
	 * @return true if configuration is required at startup.
	 */
	@Override
	public boolean isConfigureAtStartup() {
		return configureAtStartup;
	}

	/**
	 * Set a flag to inform the server whether the configure method should be called at startup.
	 *
	 * @param configureAtStartup
	 *            true to configure at startup.
	 */
	public void setConfigureAtStartup(boolean configureAtStartup) {
		this.configureAtStartup = configureAtStartup;
	}

	@Override
	public void addIObserver(IObserver anIObserver) {
		observableComponent.addIObserver(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		observableComponent.deleteIObserver(anIObserver);
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}
}
