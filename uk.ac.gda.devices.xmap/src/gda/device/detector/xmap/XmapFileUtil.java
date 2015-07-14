package gda.device.detector.xmap;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import gda.device.DeviceException;

/**
 * Tests that a file is available to be read and has been released by the Arae Detector process writing it.
 *
 * @author rjw82
 *
 */
public class XmapFileUtil {

	private String filename;

	public XmapFileUtil(String filename) {
		this.filename = filename;
	}

	public void waitForFileToBeReadable() throws DeviceException,
			InterruptedException {
		try {
			int timeout = 5000; // ms
			int timeWaited = 0; // ms
			int waitTime = 500; // ms

			// first wait until the file can be seen on the filesystem
			File existsTest = new File(filename);
			while (!existsTest.exists() && timeWaited < timeout) {
				Thread.sleep(waitTime);
				timeWaited += waitTime;
			}

			// then keep trying to read from it, as it may still being read to
			while (true && timeWaited < timeout) {
				try {
					// if get here then it at leasts exists
					BufferedInputStream fileTester = new BufferedInputStream(
							new FileInputStream(filename));
					fileTester.read(new byte[64]);
					// if no exception then its OK to properly open the file
					fileTester.close();
					return;
				} catch (FileNotFoundException e) {
					// logger.warn("FileNotFoundException while waiting for Xmap HDF5, will try again...");
					Thread.sleep(waitTime);
					timeWaited += waitTime;
				} catch (IOException e) {
					// logger.warn("IOException while waiting for Xmap HDF5, will try again...");
					Thread.sleep(waitTime);
					timeWaited += waitTime;
				}
			}

			throw new DeviceException(
					"Too many attempts / timeout while waiting for XMAP HDF5 file to be readable - "
							+ filename);
		} catch (InterruptedException e) {
			throw new InterruptedException(
					"InterruptedException while waiting for XMAP HDF5 file to be readable - "
							+ filename);
		}
	}
}
