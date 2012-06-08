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

package uk.ac.gda.epics.client.views;

import gov.aps.jca.TimeoutException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.epics.client.views.controllers.IAdBaseViewController;
import uk.ac.gda.epics.client.views.controllers.IFileSaverViewController;
import uk.ac.gda.epics.client.views.controllers.IMJpegViewController;
import uk.ac.gda.epics.client.views.model.AdBaseModel;
import uk.ac.gda.epics.client.views.model.FfMpegModel;
import uk.ac.gda.epics.client.views.model.FileSaverModel;

/**
 * Controller for the status view. This controller connects the Status View part to the EPICS model - showing relevant
 * statuses on the GUI.
 */
/**
 *
 */
public class StatusViewController implements InitializingBean {

	private final static Logger logger = LoggerFactory.getLogger(StatusViewController.class);

	public static final double UnixEpochDifferenceFromEPICS = 631152000;

	private AdBaseModel adBaseModel;

	private List<StatusView> statusViews = new ArrayList<StatusView>();

	private FileSaverModel fileSaverModel;

	private FfMpegModel ffmjpegModel;

	public FfMpegModel getFfmjpegModel() {
		return ffmjpegModel;
	}

	private IMJpegViewController mjpegViewController = new IMJpegViewController.Stub() {
		@Override
		public void updateMJpegX(int mjpegX) {
			for (StatusView sv : statusViews) {
				sv.setMJpegX(String.valueOf(mjpegX));
			}
		}

		@Override
		public void updateMJpegY(int mjpegY) {
			for (StatusView sv : statusViews) {
				sv.setMJpegY(String.valueOf(mjpegY));
			}
		}

		@Override
		public void updateMJpegTimeStamp(double mjpegTimestamp) {
			for (StatusView sv : statusViews) {
				sv.setMJpegTimeStamp(getSimpleDateFormat(mjpegTimestamp));
			}
		}
	};
	private IFileSaverViewController fileSaverViewController = new IFileSaverViewController.Stub() {
		@Override
		public void updateFileSaveX(int fileSaverX) {
			for (StatusView sv : statusViews) {
				sv.setFileSaverX(String.valueOf(fileSaverX));
			}
		}

		@Override
		public void updateFileSaveY(int fileSaverY) {
			for (StatusView sv : statusViews) {
				sv.setFileSaverY(String.valueOf(fileSaverY));
			}
		}

		@Override
		public void updateFileSaveTimeStamp(double timeStamp) {
			for (StatusView sv : statusViews) {
				sv.setFileSaverTimeStamp(getSimpleDateFormat(timeStamp));
			}
		}

		@Override
		public void updateFileSaverCaptureState(short fileSaverCaptureState) {
			for (StatusView sv : statusViews) {
				sv.setFileSaverCaptureState(fileSaverCaptureState);
			}
		}
	};

	private IAdBaseViewController adbaseViewController = new IAdBaseViewController.Stub() {
		@Override
		public void updateArrayCounter(int arrayCounter) {
			for (StatusView sv : statusViews) {
				sv.setArrayCounter(String.valueOf(arrayCounter));
			}
		}

		@Override
		public void updateDetectorDataType(String datatype) {
			for (StatusView sv : statusViews) {
				sv.setDetectorDataType(String.valueOf(datatype));
			}
		}

		@Override
		public void updateArrayRate(double arrayRate) {
			for (StatusView sv : statusViews) {
				sv.setArrayRate(String.valueOf(arrayRate));
			}
		}

		@Override
		public void updateTimeRemaining(double timeRemaining) {
			for (StatusView sv : statusViews) {
				sv.setTime(String.valueOf(timeRemaining));
			}
		}

		@Override
		public void updateNumberOfExposuresCounter(int numExposuresCounter) {
			for (StatusView sv : statusViews) {
				sv.setExp(String.valueOf(numExposuresCounter));
			}
		}

		@Override
		public void updateNumberOfImagesCounter(int numImagesCounter) {
			for (StatusView sv : statusViews) {
				sv.setImg(String.valueOf(numImagesCounter));
			}
		}

		@Override
		public void updateAcquireState(short acquireState) {
			for (StatusView sv : statusViews) {
				sv.setAcquireState(acquireState);
			}
		}

		@Override
		public void updateAcqExposure(double acqExposure) {
			for (StatusView sv : statusViews) {
				sv.setAcqExposure(String.valueOf(acqExposure));
			}
		}

		@Override
		public void updateAcqPeriod(double acqPeriod) {
			for (StatusView sv : statusViews) {
				sv.setAcqPeriod(String.valueOf(acqPeriod));
			}
		}
	};

	public void setFfmjpegModel(FfMpegModel ffmjpegModel) {
		this.ffmjpegModel = ffmjpegModel;
		this.ffmjpegModel.registerMJpegViewController(mjpegViewController);
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
		if (ffmjpegModel == null) {
			throw new IllegalArgumentException("ffmjpegModel needs to be defined.");
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

	public String getInitialMJpegX() throws Exception {
		return String.valueOf(ffmjpegModel.getDim0Size());
	}

	public String getInitialMJpegY() throws Exception {
		return String.valueOf(ffmjpegModel.getDim1Size());
	}

	public String getInitialMJpegTimestamp() throws Exception {
		double epoch = ffmjpegModel.getTimeStamp();
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
				// ffmjpef model update
				mjpegViewController.updateMJpegTimeStamp(ffmjpegModel.getTimeStamp());
				mjpegViewController.updateMJpegX(ffmjpegModel.getDim0Size());
				mjpegViewController.updateMJpegY(ffmjpegModel.getDim1Size());
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

	public void addListener(StatusView statusView) {
		statusViews.add(statusView);
	}

	public void removeStatusView(StatusView statusView) {
		statusViews.remove(statusView);
	}

}
