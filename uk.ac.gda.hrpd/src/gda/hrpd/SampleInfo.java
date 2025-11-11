/*-
 * Copyright © 2010 Diamond Light Source Ltd., Science and Technology
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

package gda.hrpd;

import java.io.Serializable;

import gda.factory.Findable;
import gda.observable.IObservable;

/**
 * This interface specified methods required to access sample information data from Excel file provided by users. The
 * Excel spreadsheet table format must be in the following order in columns starting with column 0:
 * <hr>
 * <code>
 * carouselNo	sampleID	sampleName	description	title	comment
 * </code>
 * <hr>
 * Row offset of the table should also be specified starting from 0.
 */
public interface SampleInfo extends Findable, IObservable, Serializable {

	/**
	 * open the workbook
	 */
	void open();

	/**
	 * Closes the workbook.
	 */
	void close();

	/**
	 * check if this object is configured or not.
	 *
	 * @return true or false
	 */
	boolean isConfigured();

	/**
	 * prints the current sample information to terminal.
	 */
	void values();

	/**
	 * gets sample information for specified sample from spreadsheet 0
	 *
	 * @param sampleNo
	 */
	void loadSampleInfo(int sampleNo);

	/**
	 * save modified sample information data the spreadsheet 0.
	 *
	 * @param sampleNo
	 */
	void saveSampleInfo(int sampleNo);

	/**
	 * save Experiment result metadata of the specified sample to another spreadsheet named as {@code ExperimentSummary}.
	 *
	 * @param sampleNo
	 */
	void saveExperimentInfo(int sampleNo);

	/**
	 * gets this sample position in the carousel.
	 *
	 * @return carouselNo
	 */
	String getCarouselNo();

	/**
	 * sets the sample position on the carousel.
	 *
	 * @param caroselNo
	 */
	void setCarouselNo(String caroselNo);

	/**
	 * sets the sample position on the carousel.
	 *
	 * @param caroselNo
	 */
	void setCarouselNo(int caroselNo);

	/**
	 * gets the sample ID
	 *
	 * @return sample id
	 */
	String getSampleID();

	/**
	 * sets the sample ID
	 *
	 * @param sampelID
	 */
	void setSampleID(String sampelID);

	/**
	 * gets the sample name
	 *
	 * @return sampleName
	 */
	String getSampleName();

	/**
	 * sets the sample name
	 *
	 * @param sampelName
	 */
	void setSampleName(String sampelName);

	/**
	 * gets the sample description
	 *
	 * @return description
	 */
	String getDescription();

	/**
	 * sets the sample description.
	 *
	 * @param description
	 */
	void setDescription(String description);

	/**
	 * gets the title of the sample
	 *
	 * @return title
	 */
	String getTitle();

	/**
	 * sets the title of the sample
	 *
	 * @param title
	 */
	void setTitle(String title);

	/**
	 * gets comment for the sample
	 *
	 * @return comment
	 */
	String getComment();

	/**
	 * sets comment for the sample
	 *
	 * @param comment
	 */
	void setComment(String comment);

	/**
	 * sets Run number for the sample
	 *
	 * @param runNumber
	 */
	void setRunNumber(String runNumber);

	/**
	 * sets the date for experiment run of this sample
	 *
	 * @param date
	 */
	void setDate(String date);

	/**
	 * sets the time for the experiment run of this sample
	 *
	 * @param time
	 */
	void setTime(String time);

	/**
	 * sets the beamline name on which this sample is X-rayed
	 *
	 * @param beamline
	 */
	void setBeamline(String beamline);

	/**
	 * sets the project name or proposal number for this sample
	 *
	 * @param project
	 */
	void setProject(String project);

	/**
	 * sets the experiment title or investigation name for this sample
	 *
	 * @param experiment
	 */
	void setExperiment(String experiment);

	/**
	 * sets the photon beam wavelength
	 *
	 * @param wavelength
	 */
	void setWavelength(String wavelength);

	/**
	 * sets the temperature of the same
	 *
	 * @param temperature
	 */
	void setTemperature(String temperature);

	/**
	 * gets the row offset number for the table - where sample list starts
	 *
	 * @return rowOffset
	 */
	int getRowOffset();

	/**
	 * sets the row offset of the sample information table
	 *
	 * @param rowOffset
	 */
	void setRowOffset(int rowOffset);

	/**
	 * @return sample info file
	 */
	String getSampleInfoFile();

	/**
	 * @param sampleInfoFile
	 */
	void setSampleInfoFile(String sampleInfoFile);

	/**
	 * check if saveExperimentSummary is true or not
	 *
	 * @return true or false
	 */
	boolean isSaveExperimentSummary();

	/**
	 * sets save experiment summary state
	 *
	 * @param saveExperimentSummary
	 */
	void setSaveExperimentSummary(boolean saveExperimentSummary);
}
