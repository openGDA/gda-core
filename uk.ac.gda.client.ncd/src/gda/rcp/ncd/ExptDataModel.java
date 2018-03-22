/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.rcp.ncd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;

/**
 * class that contains data pertinent to a users experiment. The class includes methods to save and restore this data
 * from file. This should most probably be merged with the NcdController to give a single point of contact for the GUI
 */

public class ExptDataModel implements IObservable {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(ExptDataModel.class);

	private static ExptDataModel instance = new ExptDataModel();

	private ObservableComponent oc;

	private String saxsXLabel = "Channel";

	private String waxsXLabel = "Channel";

	private int totalFrames;

	private String directory;

	private String fileName = " ";

	private String lastHeader = " ";

	private ExptDataModel() {
		oc = new ObservableComponent();
	}

	/**
	 * @return the singleton instance of this class
	 */
	public static ExptDataModel getInstance() {
		return instance;
	}

	/**
	 * @return the total number of frames configured
	 */
	public int getTotalFrames() {
		return totalFrames;
	}

	/**
	 * @param totalFrames
	 */
	public void setTotalFrames(int totalFrames) {
		this.totalFrames = totalFrames;
		notifyIObservers(this, null);
	}

	/**
	 * parameter File changed
	 *
	 * @param fileName
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
		notifyIObservers(this, null);
	}

	/**
	 * @return the filename
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param lastHeader
	 */
	public void setLastHeader(String lastHeader) {
		this.lastHeader = lastHeader;
		notifyIObservers(this, null);
	}

	/**
	 * @return the laster header file name
	 */
	public String getLastHeader() {
		return lastHeader;
	}

	/**
	 * @param directory
	 */
	public void setDirectory(String directory) {
		String olddir = this.directory;
		this.directory = directory;
		if (this.directory != null && !this.directory.equals(olddir))
			notifyIObservers(this, null);
	}

	/**
	 * @return the directory on the remote host where data is to be stored
	 */
	public String getDirectory() {
		return directory;
	}

	@Override
	public void addIObserver(IObserver io) {
		oc.addIObserver(io);
	}

	@Override
	public void deleteIObserver(IObserver io) {
		oc.deleteIObserver(io);
	}

	@Override
	public void deleteIObservers() {
		oc.deleteIObservers();
	}

	/**
	 * @param theObserver
	 * @param theArgument
	 */
	private void notifyIObservers(Object theObserver, Object theArgument) {
		oc.notifyIObservers(theObserver, theArgument);
	}

	/**
	 * @return Returns the saxsXLabel.
	 */
	public String getSaxsXLabel() {
		return saxsXLabel;
	}

	/**
	 * @param saxsXLabel
	 *            The saxsXLabel to set.
	 */
	public void setSaxsXLabel(String saxsXLabel) {
		this.saxsXLabel = saxsXLabel;
	}

	/**
	 * @return Returns the waxsXLabel.
	 */
	public String getWaxsXLabel() {
		return waxsXLabel;
	}

	/**
	 * @param waxsXLabel
	 *            The waxsXLabel to set.
	 */
	public void setWaxsXLabel(String waxsXLabel) {
		this.waxsXLabel = waxsXLabel;
	}
}