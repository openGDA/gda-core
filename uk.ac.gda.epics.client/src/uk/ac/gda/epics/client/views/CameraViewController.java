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

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.epics.client.views.controllers.IMJpegViewController;
import uk.ac.gda.epics.client.views.controllers.INDProcViewController;
import uk.ac.gda.epics.client.views.controllers.INDROIModelViewController;
import uk.ac.gda.epics.client.views.model.FfMpegModel;
import uk.ac.gda.epics.client.views.model.NdArrayModel;
import uk.ac.gda.epics.client.views.model.NdProcModel;
import uk.ac.gda.epics.client.views.model.NdRoiModel;

/**
 * @author rsr31645
 */
public class CameraViewController implements InitializingBean {

	private List<String> arrayPorts;

	private CameraPreviewView cameraView;

	private FfMpegModel ffmjpegModel1;

	private NdRoiModel roiModel1;

	private NdProcModel procModel1;

	/**
	 * This is mainly used in the case of PCO where 2 sets of ROI, PROC and MJPEG are used
	 */
	private FfMpegModel ffmjpegModel2;

	private NdRoiModel roiModel2;

	private NdProcModel procModel2;

	private NdArrayModel ndArray;

	private final static Logger logger = LoggerFactory.getLogger(CameraViewController.class);

	public List<String> getArrayPorts() {
		return arrayPorts;
	}

	public void setArrayPorts(List<String> arrayPorts) {
		this.arrayPorts = arrayPorts;
	}

	public void setCameraView(CameraPreviewView cameraView) {
		this.cameraView = cameraView;
	}

	public CameraPreviewView getCameraView() {
		return cameraView;
	}

	private INDROIModelViewController ndRoiModelViewController = new INDROIModelViewController.Stub() {
		@Override
		public void updateROIStartX(int startX) {
			if (cameraView != null) {
				cameraView.setROIStartX(String.valueOf(startX));
			}
		}

		@Override
		public void updateROIStartY(int startY) {
			if (cameraView != null) {
				cameraView.setROIStartY(String.valueOf(startY));
			}
		}

		@Override
		public void updateROISizeX(int sizeX) {
			if (cameraView != null) {
				cameraView.setROISizeX(String.valueOf(sizeX));
			}
		}

		@Override
		public void updateROISizeY(int sizeY) {
			if (cameraView != null) {
				cameraView.setROISizeY(String.valueOf(sizeY));
			}
		}
	};
	private INDProcViewController ndProcViewController = new INDProcViewController.Stub() {
		@Override
		public void updateProcScale(double procScale) {
			if (cameraView != null) {
				cameraView.setProcScale(String.valueOf(procScale));
			}
		}

		@Override
		public void updateProcOffset(double procOffset) {
			if (cameraView != null) {
				cameraView.setProcOffset(String.valueOf(procOffset));
			}
		}
	};
	private IMJpegViewController mjpegViewController = new IMJpegViewController.Stub() {
		@Override
		public void updateMJpegNDArrayPort(String ndArrayPort) {
			if (cameraView != null) {
				cameraView.setFFMpegNDArrayPort(ndArrayPort);
			}
		}
	};

	public void setFfmjpegModel1(FfMpegModel ffmjpegModel) {
		this.ffmjpegModel1 = ffmjpegModel;
		this.ffmjpegModel1.registerMJpegViewController(mjpegViewController);
	}

	public FfMpegModel getFfmjpegModel1() {
		return ffmjpegModel1;
	}

	public NdArrayModel getNdArray() {
		return ndArray;
	}

	public void setNdArray(NdArrayModel ndArrayModel) {
		this.ndArray = ndArrayModel;
	}

	public String getStreamUrl() throws Exception {
		return ffmjpegModel1.getMjpegUrl();
	}

	public String getFFMPegNDArrayPort() throws Exception {
		return ffmjpegModel1.getNdArrayPort();
	}

	public String getROIStartX() throws Exception {
		return String.valueOf(getRoiModel1().getMinX());
	}

	public String getROIStartY() throws Exception {
		return String.valueOf(getRoiModel1().getMinY());
	}

	public String getROISizeX() throws Exception {
		return String.valueOf(getRoiModel1().getSizeX());
	}

	public String getROISizeY() throws Exception {
		return String.valueOf(getRoiModel1().getSizeY());
	}

	public String getProcScale() throws Exception {
		return String.valueOf(getProcModel1().getProcScale());
	}

	public String getProcOffset() throws Exception {
		return String.valueOf(getProcModel1().getProcOffset());
	}

	public void setProcModel1(NdProcModel procModel) {
		this.procModel1 = procModel;
		if (this.procModel1 != null) {
			this.procModel1.registerProcViewController(ndProcViewController);
		}
	}

	public NdProcModel getProcModel1() {
		return procModel1;
	}

	public void setRoiModel1(NdRoiModel roiModel) {
		this.roiModel1 = roiModel;
		if (this.roiModel1 != null) {
			this.roiModel1.registerNDRoiModelViewController(ndRoiModelViewController);
		}
	}

	public NdRoiModel getRoiModel1() {
		return roiModel1;
	}

	public void setNDArrayPort(String ndArrayPort) throws Exception {
		ffmjpegModel1.setNdArrayPort(ndArrayPort);
	}

	public void setProcOffset(String offset) throws Exception {
		procModel1.setOffset(Double.parseDouble(offset));
	}

	public void setProcScale(String scale) throws Exception {
		procModel1.setScale(Double.parseDouble(scale));
	}

	public void setROISizeX(String sizeX) throws Exception {
		// roiModel.setSizeX(Double.parseDouble(sizeX));
		roiModel1.setSizeX((int) Double.parseDouble(sizeX));
	}

	public void setROISizeY(String sizeY) throws Exception {
		roiModel1.setSizeY((int) Double.parseDouble(sizeY));
	}

	public void setROIStartX(String startX) throws Exception {
		roiModel1.setStartX((int) Double.parseDouble(startX));
	}

	public void setROIStartY(String startY) throws Exception {
		roiModel1.setStartY((int) Double.parseDouble(startY));
	}

	public Future<Boolean> updateCameraViewFields() {
		ExecutorService executorService = Executors.newFixedThreadPool(3);
		return executorService.submit(updateFields);
	}

	private Callable<Boolean> updateFields = new Callable<Boolean>() {
		@Override
		public Boolean call() throws Exception {
			try {
				if (cameraView != null) {
					cameraView.updateStreamerUrl(getStreamUrl());
					cameraView.setFFMpegNDArrayPort(getFFMPegNDArrayPort());
					cameraView.setProcOffset(getProcOffset());
					cameraView.setProcScale(getProcScale());
					cameraView.setROISizeX(getROISizeX());
					cameraView.setROISizeY(getROISizeY());
					cameraView.setROIStartX(getROIStartX());
					cameraView.setROIStartY(getROIStartY());
				}
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

	public int[] getArrayDataForROI() throws Exception {
		return ndArray.getArrayData();
	}

	public int[] getArrayDataForROI(int numberOfElements) throws Exception {
		return ndArray.getArrayData(numberOfElements);
	}

	/**
	 * @return Returns the ffmjpegModel2.
	 */
	public FfMpegModel getFfmjpegModel2() {
		return ffmjpegModel2;
	}

	/**
	 * @param ffmjpegModel2
	 *            The ffmjpegModel2 to set.
	 */
	public void setFfmjpegModel2(FfMpegModel ffmjpegModel2) {
		this.ffmjpegModel2 = ffmjpegModel2;
	}

	/**
	 * @return Returns the roiModel2.
	 */
	public NdRoiModel getRoiModel2() {
		return roiModel2;
	}

	/**
	 * @param roiModel2
	 *            The roiModel2 to set.
	 */
	public void setRoiModel2(NdRoiModel roiModel2) {
		this.roiModel2 = roiModel2;
	}

	/**
	 * @return Returns the procModel2.
	 */
	public NdProcModel getProcModel2() {
		return procModel2;
	}

	/**
	 * @param procModel2
	 *            The procModel2 to set.
	 */
	public void setProcModel2(NdProcModel procModel2) {
		this.procModel2 = procModel2;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (arrayPorts == null || arrayPorts.isEmpty()) {
			throw new IllegalArgumentException("There must be at least one plugin ");
		}
		if (ffmjpegModel1 == null) {
			throw new IllegalArgumentException("'ffmjpegModel' should be provided to the camera view controller");
		}
		if (getRoiModel1() == null) {
			throw new IllegalArgumentException("'roiModel' should be provided to the camera view controller");
		}
		if (getProcModel1() == null) {
			throw new IllegalArgumentException("'procModel' should be provided to the camera view controller");
		}
	}

}