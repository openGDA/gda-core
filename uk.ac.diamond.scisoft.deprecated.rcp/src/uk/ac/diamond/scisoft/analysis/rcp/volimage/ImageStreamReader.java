/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.rcp.volimage;

import java.io.BufferedInputStream;
import java.net.Socket;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.widgets.Canvas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@Deprecated
public class ImageStreamReader implements Runnable {

	private Logger logger = LoggerFactory.getLogger(ImageStreamReader.class);	
	private Image streamImage = null;
	private ImageData imgData;
	private BufferedInputStream bIn = null;
	private Socket socket;
	private byte[] dataBlock = null;
	private boolean terminate = false;
//	private boolean hasFinished = false;
	private boolean isConnected = false;
	private int imageWidth = 0;
	private int imageHeight = 0;
	private GC offImageGC = null;
	private Canvas canvas;
	private final static int TOTALNUMTRIES = 15;

	/**
	 * @param hostname
	 * @param portNumber
	 * @param offImageGC
	 * @param drawCanvas
	 */
	public ImageStreamReader(String hostname, 
							 int portNumber, 
							 GC offImageGC, 
							 Canvas drawCanvas) 
	{
		this.offImageGC = offImageGC;
		this.canvas = drawCanvas;
		int numTries = TOTALNUMTRIES;
		while (numTries > 0) {
			try {
				socket = new Socket(hostname,portNumber);
				if (socket.isConnected())
				{
					bIn = new BufferedInputStream(socket.getInputStream());
					isConnected = true;
					numTries = 0;
				}
			} catch (Exception ex) {
				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				numTries--;
			}
		}
		if (numTries == 0 && isConnected == false)
			logger.error("Could not connect to imagestream # of tries exceeded");
		
		imgData = new ImageData(640,480,24,new PaletteData(0xff0000, 0x00ff00, 0xf0000ff));
	}
	
	/**
	 * Determine if the StreamReader is currently connected to the image stream or not
	 * @return true if it is otherwise false
	 */
	public boolean isConnected()
	{
		return isConnected;
	}
	
	/**
	 * 
	 */
	public void stop() {
		terminate = true;
		// wait till stream has finished reading
//		while (!hasFinished) {
//			try {
//				Thread.sleep(50);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
		try {
			bIn.close();
			socket.close();
		} catch (Exception ex) {ex.printStackTrace(); }
	}
	
	@Override
	public void run() {
		if (bIn != null)
		{
			byte[] header = new byte[4];
			while (!terminate) 
			{
				try {
					bIn.read(header,0,4);
					int width = ((header[0]&0xff) << 8)+(header[1]&0xff);
					int height = ((header[2]&0xff) << 8)+(header[3]&0xff);
					
					// check if height and width read off from the stream
					// correspond to what we expect
					// otherwise read on until we find the right information
					
					if (width != imgData.width ||
						height != imgData.height) {
						while (width != imgData.width ||
							   height != imgData.height) {
							bIn.read(header,0,4);
							width = ((header[0]&0xff) << 8)+(header[1]&0xff);
							height = ((header[2]&0xff) << 8)+(header[3]&0xff);
						}
					}
					
					if (imageWidth == 0)
						imageWidth = width;
					if (imageHeight == 0)
						imageHeight = height;
					
					int numOfBytes = imageWidth*imageHeight * 4;
					if (numOfBytes != 1228800)
						System.err.println("# bytes "+numOfBytes+" width "+width+" height "+height);
					int totalBytesRead = 0;
					int leftToRead = numOfBytes;
					if (dataBlock == null)
						dataBlock = new byte[numOfBytes];
					while (totalBytesRead < numOfBytes)
					{
						int read = bIn.read(dataBlock,totalBytesRead,leftToRead);
						totalBytesRead += read;
						leftToRead -= read;
					}
				} catch (Exception ex) { isConnected = false; ex.printStackTrace(); }
				for (int y = 0; y < imageHeight; y++)
					for (int x = 0; x < imageWidth; x++)
					{
						byte blue = dataBlock[(x+y*imageWidth)*4];
						byte green = dataBlock[(x+y*imageWidth)*4+1];
						byte red = dataBlock[(x+y*imageWidth)*4+2];
						int RGB = ((red&0xff) << 16)+((green&0xff) << 8)+(blue&0xff);
						imgData.setPixel(x,imageHeight-1-y, RGB);
					}
				if (!canvas.isDisposed() && !offImageGC.isDisposed() &&
					!canvas.getDisplay().isDisposed())
				{
					canvas.getDisplay().asyncExec(new Runnable() {
						@Override
						public void run() {
							if (streamImage != null && !streamImage.isDisposed())
								streamImage.dispose();
							
							streamImage = new Image(canvas.getDisplay(),imgData);
							offImageGC.drawImage(streamImage, 0, 0);
							canvas.redraw();
						}
					});
				}
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
//		hasFinished = true;
	}
}
