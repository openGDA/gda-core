/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.util;

import gda.configuration.properties.LocalProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;

import org.springframework.util.StringUtils;

/**
 * A class containing utility methods for files.
 */
public class FileUtil {
	/**
	 * Calculate a checksum for a file, based on the Adler32 algorithm invented by Mark Adler. This is almost as
	 * reliable as a 32-bit cyclic redundancy check for protecting against accidental modification of data.
	 * 
	 * @param filename
	 *            The name of the file to be checksummed.
	 * @return The Adler32 checksum.
	 * @throws IOException
	 *             the IO exception
	 */
	public static long checksum(String filename) throws IOException {
		CheckedInputStream cis = new CheckedInputStream(new FileInputStream(filename), new Adler32());
		byte[] buffer = new byte[128];
		while (cis.read(buffer) >= 0)
			continue;
		return cis.getChecksum().getValue();
	}

	/**
	 * copy one file from one place to another
	 * @param fromFileName
	 * @param toFileName
	 * @throws IOException
	 */
	public static void copy(String fromFileName, String toFileName) throws IOException {
		if( !StringUtils.hasText(fromFileName))
			throw new IOException("fromFileName has no length");
		if( !StringUtils.hasText(toFileName))
			throw new IOException("toFileName has no length");

		File fromFile = new File(fromFileName);
		if (!fromFile.exists())
			throw new IOException("FileCopy: " + "no such source file: " + fromFileName);
		if (!fromFile.isFile())
			throw new IOException("FileCopy: " + "can't copy directory: " + fromFileName);

		/**
		 * if the from and to are the same and from exists then just return
		 */
		if( fromFileName.equals(toFileName))
			return;

		
		File toFile = new File(toFileName);

		if (!fromFile.canRead())
			throw new IOException("FileCopy: " + "source file is unreadable: " + fromFileName);

		if (toFile.isDirectory())
			toFile = new File(toFile, fromFile.getName());

		if (toFile.exists()) {
			if (!toFile.canWrite())
				throw new IOException("FileCopy: " + "destination file is unwriteable: " + toFileName);
//			System.out.print("Overwrite existing file " + toFile.getName() + "? (Y/N): ");
//			System.out.flush();
//			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
//			String response = in.readLine();
//			if (!response.equals("Y") && !response.equals("y"))
//				throw new IOException("FileCopy: " + "existing file was not overwritten.");
//		} else {
			String parent = toFile.getParent();
			if (parent == null)
				parent = LocalProperties.getVarDir();
			File dir = new File(parent);
			if (!dir.exists())
				throw new IOException("FileCopy: " + "destination directory doesn't exist: " + parent);
			if (dir.isFile())
				throw new IOException("FileCopy: " + "destination is not a directory: " + parent);
			if (!dir.canWrite())
				throw new IOException("FileCopy: " + "destination directory is unwriteable: " + parent);
		}

		FileInputStream from = null;
		FileOutputStream to = null;
		try {
			from = new FileInputStream(fromFile);
			to = new FileOutputStream(toFile);
			byte[] buffer = new byte[4096];
			int bytesRead;

			while ((bytesRead = from.read(buffer)) != -1)
				to.write(buffer, 0, bytesRead); // write
		} finally {
			if (from != null)
				try {
					from.close();
				} catch (IOException e) {
				}
			if (to != null)
				try {
					to.close();
				} catch (IOException e) {
				}
		}
	}

	/**
	 * copy one file from one place to another
	 * @param fromURL 
	 * @param toFile 
	 * @throws IOException
	 */
	public static void copy(URL fromURL, File toFile) throws IOException {
		InputStream from = null;
		FileOutputStream to = null;
		try {
			from = fromURL.openStream();
			to = new FileOutputStream(toFile);
			byte[] buffer = new byte[4096];
			int bytesRead;

			while ((bytesRead = from.read(buffer)) != -1)
				to.write(buffer, 0, bytesRead); // write
		} finally {
			if (from != null)
				try {
					from.close();
				} catch (IOException e) {
				}
			if (to != null)
				try {
					to.close();
				} catch (IOException e) {
				}
		}
	}

}
