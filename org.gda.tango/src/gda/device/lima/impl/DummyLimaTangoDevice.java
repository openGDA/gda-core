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

package gda.device.lima.impl;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoApi.DeviceData;
import gda.device.TangoDevice;
import gda.device.TangoDeviceProxy;
import gda.device.impl.DummyTangoDeviceImpl;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class DummyLimaTangoDevice extends DummyTangoDeviceImpl{
	private static final Logger logger = LoggerFactory.getLogger(DummyLimaTangoDevice.class);
	private LimaCCDImpl limaCCDImpl;
	String[] testFilePaths;
	int imageTaken=0;
	
	public DummyLimaTangoDevice(String deviceName) {
		super(deviceName);
		limaCCDImpl = new LimaCCDImpl();
		limaCCDImpl.setTangoDeviceProxy(new TangoDeviceProxy(this));
	}



	public String[] getTestFilePaths() {
		return testFilePaths;
	}



	public void setTestFilePaths(String[] testFilePaths) {
		this.testFilePaths = testFilePaths;
	}



	@Override
	public DeviceData command_inout(String cmd, DeviceData argin) throws DevFailed {
		return super.command_inout(cmd, argin);
	}

	@Override
	public DeviceData command_inout(String cmd) throws DevFailed {
		if( cmd.equals(LimaCCDImpl.COMMAND_START_ACQ)){
			//create the files in another thread
			Thread thread = new Thread(new Runnable(){

				double expTime;
				private int acqNbFrames;
				private String template;
/*				private long imageWidth;
				private long imageHeight;
*/				TangoDevice dev=DummyLimaTangoDevice.this;
				{ 
					expTime = limaCCDImpl.getAcqExpoTime();
					acqNbFrames = limaCCDImpl.getAcqNbFrames();
/*					imageWidth = limaCCDImpl.getImageWidth();
					imageHeight = limaCCDImpl.getImageHeight();
*/					template = limaCCDImpl.getSavingDirectory() + limaCCDImpl.getSavingPrefix() + "%04d" + limaCCDImpl.getSavingSuffix();
				}
				@Override
				public void run() {
					try {
						for( int i=0; i< acqNbFrames; i++){
							Thread.sleep((long) (expTime*1000));
							int savingNextNumber = limaCCDImpl.getSavingNextNumber();
							String filename = String.format(template, savingNextNumber);
//							byte[] generateImage = DummyTangoDeviceImpl.generateImage((int)imageWidth, (int)imageHeight);
							String file = testFilePaths[imageTaken%testFilePaths.length];
							FileUtils.copyFile( new File(file), new File(filename));
							limaCCDImpl.setSavingNextNumber(savingNextNumber+1);
							imageTaken++;
						}
						dev.write_attribute(new DeviceAttribute(LimaCCDImpl.ATTRIBUTE_ACQ_STATUS, LimaCCDImpl.ACQ_STATUS_VAL_READY));
						
					} catch (Exception e) {
						try {
							dev.write_attribute(new DeviceAttribute(LimaCCDImpl.ATTRIBUTE_ACQ_STATUS, LimaCCDImpl.ACQ_STATUS_VAL_FAULT));
						} catch (DevFailed e1) {
							
							logger.error(e1.getMessage(), e1);
						}
						logger.error(e.getMessage(), e);
					}
				}
				
				
			});
			super.write_attribute(new DeviceAttribute(LimaCCDImpl.ATTRIBUTE_ACQ_STATUS, LimaCCDImpl.ACQ_STATUS_VAL_RUNNING));
			thread.start();
			return null;
		}
		return super.command_inout(cmd);
	}

	@Override
	public void write_attribute(DeviceAttribute attr) throws DevFailed {
		super.write_attribute(attr);
	}

	@Override
	public DeviceAttribute read_attribute(String attributeName) {
		return super.read_attribute(attributeName);
	}


}
