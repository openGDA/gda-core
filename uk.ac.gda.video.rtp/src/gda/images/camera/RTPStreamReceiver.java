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

package gda.images.camera;

import gda.device.DeviceException;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.media.Buffer;
import javax.media.format.VideoFormat;
import javax.media.util.BufferToImage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connects to an RTP stream, and runs a capture task at a regular fixed interval that captures frames from the stream
 * at (approximately) the desired frame rate, and passes them to a list of {@link ImageListener}s. By default, the
 * desired frame rate is 25fps.
 * 
 * <p>Configuring the receiver establishes the RTP connection and begins image capture. Image capture can be turned off
 * and on using the {@link #stop()} and {@link #start()} methods.
 * 
 * <p>The entire connection can be terminated using {@link #closeConnection()}, and then started again using
 * {@link #createConnection()} (which will also resume image capture once the connection has been established).
 */
public class RTPStreamReceiver extends RTPStreamReceiverBase<Image>
{
	
	private static final Logger logger = LoggerFactory.getLogger(RTPStreamReceiver.class);
	
	@Override
	public BufferedImage getImage() throws DeviceException {
		if (frameGrabber == null) {
			throw new DeviceException("Couldn't capture image - receiver not ready");
		}
		
		Buffer buf = frameGrabber.grabFrame();
		return getBufferedImage(buf);
	}
	
	static BufferedImage getBufferedImage(Buffer buf) throws DeviceException{
		if (buf == null || buf.getData() == null) {
			logger.debug("Couldn't capture image - frame contains no data");
			return null;
		}
		
		if (System.getProperty("os.name").startsWith("Windows") || buf.getData() instanceof int[]) {
			VideoFormat vf = (VideoFormat) buf.getFormat();
			BufferToImage btoi = new BufferToImage(vf);
			return (BufferedImage) btoi.createImage(buf);
		}
		
		try {
			return ImageIO.read(new ByteArrayInputStream((byte[]) buf.getData()));
		} catch (IOException e) {
			throw new DeviceException("Received a frame, but could not convert it to an image", e);
		}		
	}
}
