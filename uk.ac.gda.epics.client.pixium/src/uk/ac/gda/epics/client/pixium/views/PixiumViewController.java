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
public class PixiumViewController implements InitializingBean, IPixiumViewController {

	private final static Logger logger = LoggerFactory.getLogger(PixiumViewController.class);

	public static final double UnixEpochDifferenceFromEPICS = 631152000;

	private FileSaverModel fileSaverModel;
	private AdBaseModel adBaseModel;
	private PixiumModel pixiumModel;

	private PixiumView pixiumView;
	
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
		private int numExposuresPerImage=1;
		private int numExposuresCounter;
		private int numImages=1;
		private int numImagesCounter;

		@Override
		public void updateArrayCounter(int arrayCounter) {
			if (pixiumView!=null) {
				pixiumView.setArrayCounter(String.valueOf(arrayCounter));
			}
		}

		@Override
		public void updateArrayRate(double arrayRate) {
			if (pixiumView!=null) {
				pixiumView.setArrayRate(String.valueOf(arrayRate));
			}
		}

		@Override
		public void updateTimeRemaining(double timeRemaining) {
			if (pixiumView!=null) {
				pixiumView.setTime(String.valueOf(timeRemaining));
			}
		}

		@Override
		public void updateNumberOfExposuresCounter(int numExposuresCounter) {
			if (pixiumView!=null) {
				pixiumView.setExp(String.valueOf(numExposuresCounter));
				if (numExposuresCounter!=0) {
					int value=((numExposuresCounter+numExposuresPerImage*numImagesCounter)*100)/(numExposuresPerImage*numImages);
					pixiumView.setProgressBarState(value);
				}
			}
		}

		@Override
		public void updateNumberOfImagesCounter(int numImagesCounter) {
			this.numImagesCounter=numImagesCounter;
			int value=((numExposuresCounter+numExposuresPerImage*numImagesCounter)*100)/(numExposuresPerImage*numImages);
			if (pixiumView!=null) {
				pixiumView.setImg(String.valueOf(numImagesCounter));
				pixiumView.setProgressBarState(value);
			}
		}

		@Override
		public void updateAcquireState(short acquireState) {
			if (pixiumView!=null) {
				pixiumView.setAcquireState(acquireState);
			}
		}

		@Override
		public void updateAcqExposure(double acqExposure) {
			if (pixiumView!=null) {
				pixiumView.setAcqExposure(String.valueOf(acqExposure));
			}
		}

		@Override
		public void updateAcqPeriod(double acqPeriod) {
			if (pixiumView!=null) {
				pixiumView.setAcqPeriod(String.valueOf(acqPeriod));
			}
		}
		
		@Override
		public void updateNumExposures(int i) {
			this.numExposuresPerImage=i;
		}
		
		@Override
		public void updateNumImages(int i) {
			this.numImages=i;
		}
	};

	public void setAcqExposure(double time) throws Exception {
		adBaseModel.setAcqExposure(time);
	}
	public void setAcqPeriod(double time) throws Exception {
		adBaseModel.setAcqPeriod(time);
	}
	public int getNumExposuresPerImage() throws Exception {
		return adBaseModel.getNumberOfExposuresPerImage_RBV();
	}
	public int getNumImages() throws Exception {
		return adBaseModel.getNumberOfImages_RBV();
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
	
	public void setPixiumModel(PixiumModel pixiumModel) {
		this.pixiumModel = pixiumModel;
		if (this.pixiumModel != null) {
			this.pixiumModel.registerPixiumViewController(this);
		}
	}
	public FileSaverModel getFileSaverModel() {
		return fileSaverModel;
	}

	public void updateView() {
		// Do nothing
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

	public String getInitialArrayCounterVal() throws Exception {
		return String.valueOf(adBaseModel.getArrayCounter_RBV());
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
				adbaseViewController.updateNumExposures(adBaseModel.getNumberOfExposuresPerImage_RBV());
				adbaseViewController.updateNumImages(adBaseModel.getNumberOfImages_RBV());
				adbaseViewController.updateArrayCounter(adBaseModel.getArrayCounter_RBV());
				adbaseViewController.updateArrayRate(adBaseModel.getArrayRate_RBV());
				adbaseViewController.updateNumberOfExposuresCounter(adBaseModel.getNumExposuresCounter_RBV());
				adbaseViewController.updateNumberOfImagesCounter(adBaseModel.getNumImagesCounter_RBV());
				adbaseViewController.updateTimeRemaining(adBaseModel.getTimeRemaining_RBV());
				adbaseViewController.updateDetectorDataType(adBaseModel.getDatatype());
				//Pixium specific controls
				updateCalibrationRequiredState(pixiumModel.getCalibrationRequiredState());
				updateCalibrationRunningState(pixiumModel.getCalibrateState());
				updatePUMode(pixiumModel.getPUMode());
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

	public PixiumModel getPixiumModel() {
		return pixiumModel;
	}

	@Override
	public void updateCalibrationRequiredState(short requiredState) {
		if (pixiumView!=null) {
			pixiumView.setCalibrationRequiredState(requiredState);		
		}
	}
	@Override
	public void updatePUMode(int mode) {
		if (pixiumView!=null) {
			pixiumView.setPUMode(mode);
		}
	}
	@Override
	public void updateCalibrationRunningState(short runningState) {
		if (pixiumView!=null) {
			pixiumView.setCalibrationRunningState(runningState);
		}
	}
	public void startCalibration() throws Exception {
		pixiumModel.calibrate();
	}
	public void stopCalibration() throws Exception {
		pixiumModel.stop();
	}
	public void setPUMode(int puModeID) throws Exception {
		pixiumModel.setPUMode(puModeID);		
	}
	public int getPUMode() throws Exception {
		return pixiumModel.getPUMode();		
	}

}
