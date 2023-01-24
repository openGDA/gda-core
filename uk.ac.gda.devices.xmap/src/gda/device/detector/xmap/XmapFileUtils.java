package gda.device.detector.xmap;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import gda.device.DeviceException;

public final class XmapFileUtils {

	private XmapFileUtils() {
	}

	/**
	 * Test that a file is available to be read and has been released by the Area Detector process writing it.
	 *
	 * @param filename
	 * @throws DeviceException
	 * @throws InterruptedException
	 */
	public static void waitForFileToBeReadable(String filename) throws DeviceException,
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

	/**
	 * Convert path from windows XMap IOC to path to a dls data directory
	 * by replacing 'windowsPrefex' with path to data directory. i.e. {@code X:/} with {@code /dls/<beamline>/data}. e.g.
	 * <p><p>
	 * X:/2018/sp1234-5/temp/dir1/   --> /dls/b18/data/2018/sp1234-5/temp/dir1/
	 * <p><p>
	 * Any backslashes are also replaced with forward slashes.
	 *
	 * @param windowsPrefix
	 * @param windowsDir
	 * @param beamline
	 * @return path to dls data directory for beamline
	 */
	public static String convertPathFromWindows(String windowsPrefix, String windowsDir, String beamline) {
		String windowsPath = windowsDir.replaceAll("\\\\", "/"); // replace backslashes with forward ones
		String linuxPath = windowsPath.replace(windowsPrefix, "/dls/"+beamline+"/data/");
		return Paths.get(linuxPath).toString();
	}

	/**
	 * Convert dls data directory path to windows path
	 * by replacing '{@code /dls/<beamline>/data}' with windowsPrefix. e.g.
	 * <p><p>
	 * /dls/b18/data/2018/sp1234-5/temp/dir1/ --> X:/2018/sp1234-5/temp/dir1/ , (windowsPrefix = X:/)
	 * /dls/b18/data/2018/sp1234-5/temp/dir1/ --> X:/data/2018/sp1234-5/temp/dir1/ , (windowsPrefix = X:/data)

	 * <p><p>
	 *
	 * @param windowsPrefix
	 * @param dlsDir path to data directory (i.e. /dls/<beamline>/data/...)
	 * @param beamline
	 * @return path to dls data directory for beamline
	 */
	public static String convertPathToWindows(String windowsPrefix, String dlsDir, String beamline) {
		Path relativePath = Paths.get("/dls", beamline, "data").relativize(Paths.get(dlsDir));
		if (relativePath.toString().isEmpty()) {
			return windowsPrefix;
		}
		return Paths.get(windowsPrefix).resolve(relativePath).toString();
	}
}