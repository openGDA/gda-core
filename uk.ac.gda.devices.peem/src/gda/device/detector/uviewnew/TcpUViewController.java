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

package gda.device.detector.uviewnew;

import java.awt.Rectangle;
import java.io.IOException;

import gda.device.DeviceException;

/**
 *
 */
public class TcpUViewController implements UViewController {

	private UViewTcpSocketConnection socket;
		
	public TcpUViewController(String address, int port) {
		socket = new UViewTcpSocketConnection();
		socket.setCmdTerm("\0");
		socket.setReplyTerm("\0");
		socket.setAddress(address);
		socket.setPort(port);
	}
	
	public void closeConnection() throws IOException {
		socket.closeConnection();
	}

	@Override
	public String getVersion() throws DeviceException {
		return socket.send("ver");
	}
	
	/*
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
	*/
	@Override
	public GrayAdjustment doGrayAdjust() throws DeviceException {
		return grayAdjust(true);
	}

	@Override
	public GrayAdjustment getGrayAdjustment() throws DeviceException {
		return grayAdjust(false);
	}
	/**
	 * hard coded to only return UNCOMPRESSED data without Embedded ASCII Data
	 */
	@Override
	public ImageData getImageData() throws DeviceException {
		int height, width;
		socket.sendCmdNoReply("ida 0 0");
		byte[] header = socket.readBinary(19);
		String[] headerStrings = new String(header).split("[ ]+");
		int argOffset = 1;
		if ( headerStrings[0].length() == 0 ) {
			argOffset++; //first argument might be a space
		}
		width = Integer.parseInt(headerStrings[argOffset]);
		height = Integer.parseInt(headerStrings[argOffset+1]);
		byte[] outputBytes = socket.readBinary(width * height * 2);
		socket.readBinary(1); //there's a null-byte at the end of the binary block we need to skip over
		short[] data = new short[width * height];
		int j = 0;
		for( int i = 0; i < outputBytes.length; i += 2 ) {
			data[j] = (short) (outputBytes[i + 1] << 8 | outputBytes[i] & 0xFF);
			j++;
		}
		return new ImageData(height, width, data);
	}

	@Override
	public int getCameraExpTime() throws DeviceException {
		String timeString = socket.send("ext");
		//returns as a decimal, but decimal part is always 0
		return Integer.parseInt( timeString.split("\\.")[0] );
	}

	@Override
	public void setCameraExpTime(int newMsecTime) throws DeviceException {
		if( ! "0".equals(socket.send("ext " + newMsecTime)) ){
			throw new DeviceException("Unexpected return value from TCP socket when setting exposure time");
		}

	}

	@Override
	public boolean getSequential() throws DeviceException {
		return ("1".equals(socket.send("seq")));
	}

	@Override
	public void setSequential(boolean newSeq) throws DeviceException {
		String cmd = "seq " + (newSeq ? "1" : "0");
		String reply = socket.send(cmd);
		if ( ! "0".equals(reply) ) {
			throw new DeviceException("Unexpected return value from TCP socket: " + reply);
		}
	}

	@Override
	public int getFrameAverage() throws DeviceException {
		try {
			return Integer.parseInt(socket.send("avr"));
		} catch (NumberFormatException e) {
			throw new DeviceException(e);
		}
	}

	@Override
	public void setFrameAverage(int newAveraging) throws DeviceException {
		if ( newAveraging < 0 ) newAveraging = 1; //may be some things that expect -1 to carry the same meaning as 1
		String cmd = "avr " + newAveraging;
		String reply = socket.send(cmd);
		if (! "0".equals(reply)) {
			throw new DeviceException("Unexpected return value from TCP socket: " + reply);
		}
	}

	@Override
	public boolean getNewImageReady() throws DeviceException {
		return "1".equals(socket.send("nir"));
	}

	@Override
	public boolean getAcquisitionInProgress() throws DeviceException {
		return "1".equals( socket.send("aip") );
	}

	@Override
	public void setAcquisitionInProgess(boolean newAcquisitionStatus) throws DeviceException {
		//throw new UnsupportedOperationException();
		String cmd = "aip " + (newAcquisitionStatus ? "1" : "0"); 
		String reply = socket.send(cmd);
		if (! "0".equals(reply)) {
			throw new DeviceException("Unexpected return value from TCP socket: " + reply);
		}
	}

	@Override
	public void acquireSingleImage(int imageId) throws DeviceException {
		String cmd = "asi " + imageId;
		if ( ! "0".equals(socket.send(cmd)) ) {
			throw new DeviceException("Acquistion already in progress");
		}
	}

	@Override
	public void exportImage(ImageFile fileDetails) throws DeviceException {
		StringBuffer sb = new StringBuffer("exp ");
		switch (fileDetails.getFormat()) {
		case DAT:
			sb.append("0,");
			break;
		case PNG:
			sb.append("1,");
			break;
		case TIFF:
			sb.append("2,");
			break;
		case BMP:
			sb.append("3,");
			break;
		case JPG:
			sb.append("4,");
			break;
		case TIFF_UNCOMPRESSED:
			sb.append("5,");
			break;
		}
		
		switch (fileDetails.getContentType()) {
		case RGB_XYZ:
			sb.append("0,");
			break;
		case RGB_XYZ_RAW:
			sb.append("1,");
			break;
		case GRAYLEVEL16:
			sb.append("2,");
			break;
		}

		sb.append( fileDetails.getFileName() );

		String status = socket.send(sb.toString());
		if ( !status.equals("0") ) {
			throw new DeviceException(String.format( "Could not export Image: return code %s - command: %s", status, sb.toString() ));
		}

	}

	@Override
	public void defineRoi(final RegionOfInterest roi) throws DeviceException {
		Rectangle region = roi.getRegion();
		String cmd = String.format("itd %d 0 %d %d %d %d", roi.id, region.x, region.y, region.width, region.height, '0');
		String reply = socket.send(cmd);
		if ( !reply.equals("0")) {
			throw new DeviceException("Invalid result from TCP socket: " + reply);
		}
	}
	@Override
	public void activateROI(final RegionOfInterest roi) throws DeviceException {
		Rectangle region = roi.getRegion();
		String cmd = String.format("itd %d 0 %d %d %d %d %c", roi.id, region.x, region.y, region.width, region.height, 'a');
		String reply = socket.send(cmd);
		if ( !reply.equals("0")) {
			throw new DeviceException("Invalid result from TCP socket: " + reply);
		}
	}
	
	@Override
	public void deactivateROI(final RegionOfInterest roi) throws DeviceException {
		Rectangle region = roi.getRegion();
		String cmd = String.format("itd %d 0 %d %d %d %d %c", roi.id, region.x, region.y, region.width, region.height, 'd');
		String reply = socket.send(cmd);
		if ( !reply.equals("0")) {
			throw new DeviceException("Invalid result from TCP socket: " + reply);
		}
	}
	@Override
	public boolean isROIActive(final RegionOfInterest roi) throws DeviceException {
		Rectangle region = roi.getRegion();
		String cmd = String.format("itd %d 0 %d %d %d %d %c", roi.id, region.x, region.y, region.width, region.height, '6');
		String reply = socket.send(cmd);
		return reply.equals("1");
	}
	@Override
	public double getRoiData(final int roiId) throws DeviceException {
		String cmd = "roi " + roiId;
		String reply = socket.send(cmd);
		double result = 0;
		try {
			result = Double.parseDouble(reply);
		} catch (NumberFormatException e) {
			throw new DeviceException("Unexpected response from TCP socket: '" + reply + "'");
		}
		if ( result < 0 ) {
			throw new DeviceException(String.format("Error code from TCP socket: %d", result));
		}
		return result;
	}

	private GrayAdjustment grayAdjust(boolean perform) throws DeviceException {
		String cmd = "doa " + (perform ? "1" : "0");
		String reply = socket.send(cmd);
		String[] replyArray = reply.split(" ");
		if ( reply.length() < 2 || replyArray.length != 2 || replyArray[0].length() == 0 || replyArray[1].length() == 0 ) {
			throw new DeviceException("Invalid result from TCP socket: " + reply);
		}
		int[] values = new int[2];
		values[0] = Integer.parseInt(replyArray[0]);
		values[1] = Integer.parseInt(replyArray[1]);
		return new GrayAdjustment(values[1], values[0]);
		
	}

	@Override
	public void setPixelClock(int MHz) throws DeviceException {
		if (MHz != 40 && MHz != 10) {
			throw new DeviceException("Invalid clock settins; valid settings are 10 and 40 MHz");
		}
		String cmd = "spx " + MHz;
		String reply = socket.send(cmd);
		if ( !reply.equals("0") ) {
			throw new DeviceException("Unexpected response from TCP socket: '" + reply + "'");
		}
	}
	
	@Override
	public int getPixelClock() throws DeviceException {
		String cmd = "spx";
		return Integer.parseInt(socket.send(cmd));
	}
	
	@Override
	public void setTriggerMode(int mode) throws DeviceException {
		if (mode < 0 || mode > 3) {
			throw new DeviceException("Invalid trigger mode settings; valid settings are 0, 1, 2 and 3");
		}
		String cmd = "str " + mode;
		String reply = socket.send(cmd);
		if ( !reply.equals("0") ) {
			throw new DeviceException("Unexpected response from TCP socket: '" + reply + "'");
		}
	}
	@Override
	public int getTriggerMode() throws DeviceException {
		String cmd = "str";
		return Integer.parseInt(socket.send(cmd));
	}

	@Override
	public void setCameraADC(int adc) throws DeviceException {
		if (adc < 1 || adc > 2) {
			throw new DeviceException("Invalid trigger mode settings; valid settings are 1 and 2");
		}
		String cmd = "adc " + adc;
		String reply = socket.send(cmd);
		if ( !reply.equals("0") ) {
			throw new DeviceException("Unexpected response from TCP socket: '" + reply + "'");
		}
	}

	@Override
	public int getCameraADC() throws DeviceException {
		String cmd = "adc";
		return Integer.parseInt(socket.send(cmd));
	}

}
