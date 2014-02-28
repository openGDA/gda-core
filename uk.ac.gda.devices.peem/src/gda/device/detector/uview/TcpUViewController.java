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

import gda.device.DeviceException;

/**
 *
 */
public class TcpUViewController implements UViewController {

	private UViewTcpSocketConnection socket;
	
	public class TcpGrayAdjustment implements UViewController.GrayAdjustment {

		@Override
		public int getWindowLow() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getWindowHigh() {
			// TODO Auto-generated method stub
			return 0;
		}
		
	}
	
	public class TcpRoi implements UViewController.RegionOfInterest {

		@Override
		public int getIndex() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Point getLowerBound() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Point getUpperBound() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void addPoint(Point point) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void resetData() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	@Override
	public GrayAdjustment doGrayAdjust() {
		return null;
		// TODO Auto-generated method stub

	}

	@Override
	public GrayAdjustment getGrayAdjustment() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ImageData getImageData() throws DeviceException {
		int height, width;
		socket.sendCmdNoReply("ida 0 0");
		byte[] header = socket.readBinary(19);
		String[] headerStrings = new String(header).split(" ");
		int argOffset = 1;
		if ( headerStrings[0].length() == 0) {
			argOffset++; //first argument might be a space
		}
		width = Integer.parseInt(headerStrings[argOffset]);
		height = Integer.parseInt(headerStrings[argOffset+1]);
		byte[] outputBytes = socket.readBinary(width * height * 2);
		short[] data = new short[width * height];
		int j = 0;
		for( int i = 0; i < outputBytes.length; i += 2 ) {
			data[j] = (short) (outputBytes[i] << 8 | outputBytes[i + 1] & 0xFF);
			j++;
		}
		return new ImageData(height, width, data);
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
	public boolean getAcquisitionInProgress() throws DeviceException {
		return "1".equals( socket.send("aip") );
	}

	@Override
	public void setAcquisitionInProgess(boolean newAcqusitionStatus) {
		// TODO Auto-generated method stub

	}

	@Override
	public void acquireSingleImage(int imageId) throws DeviceException {
		String cmd = "asi " + imageId;
		if ( ! "0".equals(socket.send(cmd)) ) {
			throw new DeviceException("Acquistion already in progress");
		}
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
