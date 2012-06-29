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

import gda.factory.Findable;
import gda.observable.IObservable;

import java.io.Serializable;

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
	public abstract void open();

	/**
	 * Closes the workbook.
	 */
	public abstract void close();

	/**
	 * check if this object is configured or not.
	 * 
	 * @return true or false
	 */
	public boolean isConfigured();

	/**
	 * prints the current sample information to terminal.
	 */
	public void values();

	/**
	 * gets sample information for specified sample from spreadsheet 0
	 * 
	 * @param sampleNo
	 */
	public abstract void loadSampleInfo(int sampleNo);

	/**
	 * save modified sample information data the spreadsheet 0.
	 * 
	 * @param sampleNo
	 */
	public abstract void saveSampleInfo(int sampleNo);

	/**
	 * save Experiment result metadata of the specified sample to another spreadsheet named as {@code ExperimentSummary}.
	 * 
	 * @param sampleNo
	 */
	public abstract void saveExperimentInfo(int sampleNo);

	/**
	 * gets this sample position in the carousel.
	 * 
	 * @return carouselNo
	 */
	public abstract String getCarouselNo();

	/**
	 * sets the sample position on the carousel.
	 * 
	 * @param caroselNo
	 */
	public abstract void setCarouselNo(String caroselNo);

	/**
	 * sets the sample position on the carousel.
	 * 
	 * @param caroselNo
	 */
	public void setCarouselNo(int caroselNo);

	/**
	 * gets the sample ID
	 * 
	 * @return sample id
	 */
	public abstract String getSampleID();

	/**
	 * sets the sample ID
	 * 
	 * @param sampelID
	 */
	public abstract void setSampleID(String sampelID);

	/**
	 * gets the sample name
	 * 
	 * @return sampleName
	 */
	public abstract String getSampleName();

	/**
	 * sets the sample name
	 * 
	 * @param sampelName
	 */
	public abstract void setSampleName(String sampelName);

	/**
	 * gets the sample description
	 * 
	 * @return description
	 */
	public abstract String getDescription();

	/**
	 * sets the sample description.
	 * 
	 * @param description
	 */
	public abstract void setDescription(String description);

	/**
	 * gets the title of the sample
	 * 
	 * @return title
	 */
	public abstract String getTitle();

	/**
	 * sets the title of the sample
	 * 
	 * @param title
	 */
	public abstract void setTitle(String title);

	/**
	 * gets comment for the sample
	 * 
	 * @return comment
	 */
	public abstract String getComment();

	/**
	 * sets comment for the sample
	 * 
	 * @param comment
	 */
	public abstract void setComment(String comment);

	/**
	 * sets Run number for the sample
	 * 
	 * @param runNumber
	 */
	public abstract void setRunNumber(String runNumber);

	/**
	 * sets the date for experiment run of this sample
	 * 
	 * @param date
	 */
	public abstract void setDate(String date);

	/**
	 * sets the time for the experiment run of this sample
	 * 
	 * @param time
	 */
	public abstract void setTime(String time);

	/**
	 * sets the beamline name on which this sample is X-rayed
	 * 
	 * @param beamline
	 */
	public abstract void setBeamline(String beamline);

	/**
	 * sets the project name or proposal number for this sample
	 * 
	 * @param project
	 */
	public void setProject(String project);

	/**
	 * sets the experiment title or investigation name for this sample
	 * 
	 * @param experiment
	 */
	public abstract void setExperiment(String experiment);

	/**
	 * sets the photon beam wavelength
	 * 
	 * @param wavelength
	 */
	public void setWavelength(String wavelength);

	/**
	 * sets the temperature of the same
	 * 
	 * @param temperature
	 */
	public void setTemperature(String temperature);

	/**
	 * gets the row offset number for the table - where sample list starts
	 * 
	 * @return rowOffset
	 */
	public abstract int getRowOffset();

	/**
	 * sets the row offset of the sample information table
	 * 
	 * @param rowOffset
	 */
	public abstract void setRowOffset(int rowOffset);

	/**
	 * @return sample info file
	 */
	public abstract String getSampleInfoFile();

	/**
	 * @param sampleInfoFile
	 */
	public abstract void setSampleInfoFile(String sampleInfoFile);

	/**
	 * check if saveExperimentSummary is true or not
	 * 
	 * @return true or false
	 */
	public boolean isSaveExperimentSummary();

	/**
	 * sets save experiment summary state
	 * 
	 * @param saveExperimentSummary
	 */
	public void setSaveExperimentSummary(boolean saveExperimentSummary);
}
