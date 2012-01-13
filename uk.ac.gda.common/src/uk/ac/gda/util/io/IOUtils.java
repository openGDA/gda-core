/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.util.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class IOUtils {

	private final static Logger logger = LoggerFactory.getLogger(IOUtils.class);
	/**
	 * Unconditionally close a <code>ZipFile</code>.
	 * and optionally log errors 
	 */
	public static void close(FileChannel channel, String msg) {
		if (channel == null) {
			logger.error("FileChannel is null", msg);
			return;
		}

		try {
			channel.close();
		} catch (IOException ioe) {
			logger.error("Cannot close stream", ioe);
		}
	}

	/**
	 * Unconditionally close a <code>ZipFile</code>.
	 * and optionally log errors 
	 *
	 * @param input A (possibly null) Reader
	 */
	public static void close(ZipFile input, String msg) {
		if (input == null) {
			logger.error("ZipFile is null", msg, 1);
			return;
		}

		try {
			input.close();
		} catch (IOException ioe) {
			logger.error("Cannot close stream", ioe);
		}
	}

	/**
	 * Unconditionally close a <code>Reader</code>
	 * and optionally log errors 
	 *
	 * @param input A (possibly null) Reader
	 */
	public static void close(Reader input, String msg) {
		if (input == null) {
			logger.error("Reader is null", msg, 1);
			return;
		}

		try {
			input.close();
		} catch (IOException ioe) {
			logger.error("Cannot close stream", ioe);
		}
	}

	/**
	 * Unconditionally close an <code>Writer</code>
	 * and optionally log errors
	 *
	 * @param output A (possibly null) Writer
	 */
	public static void close(Writer output, String msg) {
		if (output == null) {
			logger.error("Writer is null", msg, 1);
			return;
		}

		try {
			output.close();
		} catch (IOException ioe) {
			logger.error("Cannot close stream", ioe);
		}
	}

	/**
	 * Unconditionally close an <code>OutputStream</code>
	 * and optionally log errors
	 * @param output A (possibly null) OutputStream
	 */
	public static void close(OutputStream output, String msg) {
		if (output == null) {
			logger.error("OutputStream is null", msg, 1);
			return;
		}

		try {
			output.close();
		} catch (IOException ioe) {
			logger.error("Cannot close stream", ioe);
		}
	}

	/**
	 * Unconditionally close an <code>InputStream</code>
	 * and optionally log errors
	 * @param input A (possibly null) InputStream
	 */
	public static void close(InputStream input, String msg) {
		if (input == null) {
			logger.error("InputStream is null", msg, 1);
			return;
		}

		try {
			input.close();
		} catch (IOException ioe) {
			logger.error("Cannot close stream", ioe);
		}
	}

	/**
	 * @return information about possible null file for use in diagnostic message
	 */
	public static String fileInfo(File f) {
		if (f==null) {
			return "File is null";
		}
		return f.getPath();
	}

	/**
	 * @return information about possible null file for use in diagnostic message
	 */
	public static String fileInfo(ZipFile f) {
		if (f==null) {
			return "File is null";
		}
		return f.getName();
	}

}
