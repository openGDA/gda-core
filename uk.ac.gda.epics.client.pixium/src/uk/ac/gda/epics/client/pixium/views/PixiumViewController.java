/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.epics.client.pixium.views;

import gov.aps.jca.TimeoutException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.epics.client.views.controllers.IAdBaseViewController;
import uk.ac.gda.epics.client.views.controllers.IFileSaverViewController;
import uk.ac.gda.epics.client.views.model.AdBaseModel;
import uk.ac.gda.epics.client.views.model.FileSaverModel;

/**
 * Controller for the pixium view. This controller connects the pixium View part to the EPICS model - showing relevant
 * fields on the GUI.
 */
/**
 *
 */
public class PixiumViewController implements InitializingBean {

	private final static Logger logger = LoggerFactory.getLogger(PixiumViewController.class);

	public static final double UnixEpochDifferenceFromEPICS = 631152000;

	private FileSaverModel fileSaverModel;
	private AdBaseModel adBaseModel;

	private PixiumView pixiumView;
	//TODO add pixium specific PVs
	
	private IFileSaverViewController fileSaverViewController = new IFileSaverViewController.Stub() {
		@Override
		public void updateFileSaveX(int fileSaverX) {
				pixiumView.setFileSaverX(String.valueOf(fileSaverX));
		}

		@Override
		public void updateFileSaveY(int fileSaverY) {
				pixiumView.setFileSaverY(String.valueOf(fileSaverY));
		}

		@Override
		public void updateFileSaveTimeStamp(double timeStamp) {
			pixiumView.setFileSaverTimeStamp(getSimpleDateFormat(timeStamp));
		}

		@Override
		public void updateFileSaverCaptureState(short fileSaverCaptureState) {
				pixiumView.setFileSaverCaptureState(fileSaverCaptureState);
		}
	};

	private IAdBaseViewController adbaseViewController = new IAdBaseViewController.Stub() {
		@Override
		public void updateArrayCounter(int arrayCounter) {
			pixiumView.setArrayCounter(String.valueOf(arrayCounter));
		}

		@Override
		public void updateArrayRate(double arrayRate) {
			pixiumView.setArrayRate(String.valueOf(arrayRate));
		}

		@Override
		public void updateTimeRemaining(double timeRemaining) {
			pixiumView.setTime(String.valueOf(timeRemaining));
		}

		@Override
		public void updateNumberOfExposuresCounter(int numExposuresCounter) {
			pixiumView.setExp(String.valueOf(numExposuresCounter));
		}

		@Override
		public void updateNumberOfImagesCounter(int numImagesCounter) {
			pixiumView.setImg(String.valueOf(numImagesCounter));
		}

		@Override
		public void updateAcquireState(short acquireState) {
			pixiumView.setAcquireState(acquireState);
		}

		@Override
		public void updateAcqExposure(double acqExposure) {
			pixiumView.setAcqExposure(String.valueOf(acqExposure));
		}

		@Override
		public void updateAcqPeriod(double acqPeriod) {
			pixiumView.setAcqPeriod(String.valueOf(acqPeriod));
		}
	};

	public void setAcqExposure(double time) throws Exception {
		adBaseModel.setAcqExposure(time);
	}
	public void setAcqPeriod(double time) throws Exception {
		adBaseModel.setAcqPeriod(time);
	}
	public AdBaseModel getAdBaseModel() {
		return adBaseModel;
	}

	public void setAdBaseModel(AdBaseModel statusModel) {
		this.adBaseModel = statusModel;
		if (adBaseModel != null) {
			adBaseModel.registerAdBaseViewController(adbaseViewController);
		}
	}

	public void setFileSaverModel(FileSaverModel fileSaverModel) {
		this.fileSaverModel = fileSaverModel;
		if (this.fileSaverModel != null) {
			this.fileSaverModel.registerFileSaverViewController(fileSaverViewController);
		}
	}

	public FileSaverModel getFileSaverModel() {
		return fileSaverModel;
	}

	public void updateView() {
		// Do nothing
	}

	public String getInitialTxtReadBackCounterVal() throws Exception {
		return String.valueOf(adBaseModel.getArrayCounter_RBV());
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (adBaseModel == null) {
			throw new IllegalArgumentException("statusModel needs to be defined.");
		}
		if (fileSaverModel == null) {
			throw new IllegalArgumentException("fileSaverModel needs to be defined.");
		}
	}

	public String getInitialArrayRateVal() throws Exception {
		return String.valueOf(adBaseModel.getArrayRate_RBV());
	}

	public String getInitialTimeRemainingVal() throws Exception {
		return String.valueOf(adBaseModel.getTimeRemaining_RBV());
	}

	public String getInitialRemainingExposure() throws Exception {
		return String.valueOf(adBaseModel.getNumExposuresCounter_RBV());
	}

	public String getInitialRemainingImages() throws Exception {
		return String.valueOf(adBaseModel.getNumImagesCounter_RBV());
	}

	public String getInitialFileSaverX() throws Exception {
		return String.valueOf(fileSaverModel.getDim0Size());
	}

	public String getInitialFileSaverY() throws Exception {
		return String.valueOf(fileSaverModel.getDim1Size());
	}

	public String getInitialFileSaverTimestamp() throws Exception {
		double epoch = fileSaverModel.getTimeStamp();
		return getSimpleDateFormat(epoch);
	}

	private String getSimpleDateFormat(double epoch) {
		Date date = new Date((long) ((epoch + UnixEpochDifferenceFromEPICS) * 1000)); // move EPICS 1990 to Unix 1970
																						// epoch time reference
		SimpleDateFormat simpleDatef = new SimpleDateFormat("dd/MM/yy hh:mm:ss.SSS");
		return simpleDatef.format(date);
	}

	public String getInitialAcqExposure() throws Exception {
		return String.valueOf(adBaseModel.getAcqExposureRBV());
	}

	public String getInitialAcqPeriod() throws Exception {
		return String.valueOf(adBaseModel.getAcqPeriodRBV());
	}

	public short getInitialAcqStatus() throws Exception {
		return adBaseModel.getAcquireState();
	}

	public short getInitialFileSaverCaptureStatus() throws Exception {
		return fileSaverModel.getCaptureState();
	}

	/**
	 */
	public Future<Boolean> updateAllFields() {
		ExecutorService executorService = Executors.newFixedThreadPool(3);
		return executorService.submit(updateFields);
	}

	private Callable<Boolean> updateFields = new Callable<Boolean>() {
		@Override
		public Boolean call() throws Exception {
			try {
				// adbase model values update
				adbaseViewController.updateAcqExposure(adBaseModel.getAcqExposureRBV());
				adbaseViewController.updateAcqPeriod(adBaseModel.getAcqPeriodRBV());
				adbaseViewController.updateAcquireState(adBaseModel.getAcquireState());
				adbaseViewController.updateArrayCounter(adBaseModel.getArrayCounter_RBV());
				adbaseViewController.updateArrayRate(adBaseModel.getArrayRate_RBV());
				adbaseViewController.updateNumberOfExposuresCounter(adBaseModel.getNumExposuresCounter_RBV());
				adbaseViewController.updateNumberOfImagesCounter(adBaseModel.getNumImagesCounter_RBV());
				adbaseViewController.updateTimeRemaining(adBaseModel.getTimeRemaining_RBV());
				adbaseViewController.updateDetectorDataType(adBaseModel.getDatatype());
				// file saver model update
				fileSaverViewController.updateFileSaverCaptureState(fileSaverModel.getCaptureState());
				fileSaverViewController.updateFileSaveTimeStamp(fileSaverModel.getTimeStamp());
				fileSaverViewController.updateFileSaveX(fileSaverModel.getDim0Size());
				fileSaverViewController.updateFileSaveY(fileSaverModel.getDim1Size());
			} catch (TimeoutException tme) {
				logger.error("IOC doesn't seem to be running", tme);
				throw tme;
			} catch (Exception ex) {
				logger.error("Problem with loading the channel", ex);
				throw ex;
			}
			return Boolean.TRUE;

		}

	};

	public void setPixiumView(PixiumView view) {
		this.pixiumView=view;
	}

	public PixiumView getPixiumView() {
		return pixiumView;
	}

}
