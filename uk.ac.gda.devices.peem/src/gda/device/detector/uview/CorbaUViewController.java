/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.device.detector.uview;
import org.omg.CORBA.ShortHolder;

import gda.device.peem.MicroscopeControl.Microscope;

/**
 *
 */
public class CorbaUViewController implements UViewController {
	
	public class CorbaGrayAdjustment implements UViewController.GrayAdjustment {

		public CorbaGrayAdjustment(int windowLow, int windowHigh) {
			super();
			this.windowLow = windowLow;
			this.windowHigh = windowHigh;
		}

		private int windowLow;
		private int windowHigh;

		@Override
		public int getWindowLow() {
			return windowLow;
		}

		@Override
		public int getWindowHigh() {
			return windowHigh;
		}
	}
	
	private Microscope msImpl;

	@Override
	public GrayAdjustment doGrayAdjust() {
		return grayAdjust(false);	
	}

	@Override
	public GrayAdjustment getGrayAdjustment() {
		return grayAdjust(false);
	}
	
	private GrayAdjustment grayAdjust(boolean performAdjustment) {
		org.omg.CORBA.ShortHolder low = new ShortHolder();
		org.omg.CORBA.ShortHolder high = new ShortHolder();
		msImpl.DoGrayAdjust( (short) (performAdjustment ? 0 : 1), low, high );
		return new CorbaGrayAdjustment( low.value, high.value );
		
	}

	@Override
	public ImageData getImageData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getCameraExpTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setCameraExpTime(int newMsecTime) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean getSequential() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setSequential(boolean newSeq) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getFrameAverage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setFrameAverage(int newAveraging) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean getNewImageReady() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean getAcquisitionInProgress() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setAcquisitionInProgess(boolean newAcqusitionStatus) {
		// TODO Auto-generated method stub

	}

	@Override
	public void acquireSingleImage(int imageId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exportImage(ImageFile fileDetails) {
		// TODO Auto-generated method stub

	}

	@Override
	public void roiData(RegionOfInterest roi) {
		// TODO Auto-generated method stub

	}

}
