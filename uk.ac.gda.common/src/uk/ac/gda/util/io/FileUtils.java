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

package uk.ac.gda.util.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A class with a collection of file management static utility classes. Several method contents copied from code
 * snippets available on the web.
 *
 * @author Matthew Gerring
 */
public final class FileUtils {

	private final static String NEWFOLDER = "New Folder";

	/**
	 *
	 */
	public static final char BOM; // note this character is the same whether UTF-8, UTF-16BE or UTF-16LE

	/**
	 * Notes on Java behaviour (what I think happens) If a file is read in as UTF-8 or UTF-16BE and a BOM is present it
	 * is left in. If a file is read in as UTF-16 and a BOM is present it is stripped off If a file is written out as
	 * UTF-16 a BOM is added. If a file is written out as UTF-8 or UTF-16BE a BOM is not added
	 */
	static {
		byte[] b1 = new byte[3];
		b1[0] = (byte) 0xEF;
		b1[1] = (byte) 0xBB;
		b1[2] = (byte) 0xBF;
		String bomstr = " ";
		try {
			bomstr = new String(b1, 0, 3, "UTF-8");
		} catch (Exception any) {
			throw new RuntimeException("Could not initialize byte order marker"); // important to do this as odd things
			// could happen if it fails
		}
		BOM = bomstr.charAt(0);
	}

	/**
	 * @param parent
	 * @return boolean
	 */
	static public final boolean recursiveDelete(File parent) {

		if (parent.exists()) {
			if (parent.isDirectory()) {

				File[] files = parent.listFiles();
				for (int ifile = 0; ifile < files.length; ++ifile) {
					if (files[ifile].isDirectory()) {
						recursiveDelete(files[ifile]);
					}
					if (files[ifile].exists()) {
						files[ifile].delete();
					}
				}
			}
			return parent.delete();
		}
		return false;
	}

	/**
	 * @param parent
	 */
	static public final void deleteContents(File parent) {

		if (parent.isDirectory()) {

			File[] files = parent.listFiles();
			for (int ifile = 0; ifile < files.length; ++ifile) {
				if (files[ifile].isDirectory()) {
					recursiveDelete(files[ifile]);
				}
				files[ifile].delete();
			}
		}
	}

	/**
	 * Generates a unique file of the name template or template+an integer
	 *
	 * @param dir
	 * @param template
	 * @param ext
	 * @return a unique file.
	 */
	public static File getUnique(final File dir, final String template, final String ext) {
		final String extension = ext != null ? (ext.startsWith(".")) ? ext : "." + ext : null;
		final File file = new File(dir, template + extension);
		if (!file.exists()) {
			return file;
		}

		return getUnique(dir, template, ext, 1);
	}

	/**
	 * @param dir
	 * @param template
	 * @param ext
	 * @param i
	 * @return file
	 */
	public static File getUnique(final File dir, final String template, final String ext, int i) {
		final String extension = ext != null ? (ext.startsWith(".")) ? ext : "." + ext : null;
		final File file = ext != null ? new File(dir, template + i + extension) : new File(dir, template + i);
		if (!file.exists()) {
			return file;
		}

		return getUnique(dir, template, ext, ++i);
	}

	/**
	 * Recursively delete parent folder on exit of JVM
	 *
	 * @param parent
	 */
	static public final void recursiveDeleteOnExit(File parent) {
		parent.deleteOnExit();
		if (parent.isDirectory()) {
			File[] files = parent.listFiles();
			for (int ifile = 0; ifile < files.length; ++ifile) {
				if (files[ifile].isDirectory()) {
					recursiveDeleteOnExit(files[ifile]);
				}
				files[ifile].deleteOnExit();
			}
		}
	}

	/**
	 * Define buffer size here.
	 *
	 * @return the buffer size
	 */
	static private final int getBufferSize() {
		final String size = System.getProperty("org.diamond.util.io.fileutils.buffer.size");
		if (size == null) {
			return 4096;
		}
		return Integer.parseInt(size);
	}

	/**
	 * Recursively copy one folder to another Deleting the contents of the destination folder before copying. Use at
	 * your peril!
	 *
	 * @param source_dir
	 * @param destination_dir
	 * @throws IOException
	 */
	static public final void recursiveCopy(final File source_dir, final File destination_dir) throws IOException {

		FileUtils.recursiveCopy(source_dir, destination_dir, new byte[FileUtils.getBufferSize()]);
	}

	/**
	 * @param source_dir
	 * @param destination_dir
	 * @param buffer
	 * @throws IOException
	 */
	static private final void recursiveCopy(final File source_dir, final File destination_dir, final byte[] buffer)
			throws IOException {

		if (source_dir == null || destination_dir == null) {
			return;
		}
		if (!source_dir.exists()) {
			throw new java.io.FileNotFoundException(source_dir.getAbsolutePath());
		}
		if (!source_dir.isDirectory()) {
			throw new java.io.IOException("recursiveCopy should only be used for folders!");
		}
		if (source_dir.equals(destination_dir)) {
			throw new java.io.IOException("Cannot copy folder on to itself!");
		}

		if (destination_dir.exists()) {
			FileUtils.recursiveDelete(destination_dir);
		}
		destination_dir.mkdirs();

		File[] files = source_dir.listFiles();
		for (int ifile = 0; ifile < files.length; ++ifile) {
			final File from = files[ifile];
			final File to = new File(destination_dir, from.getName());
			if (from.isDirectory()) {
				FileUtils.recursiveCopy(from, to, buffer);
				continue;
			}
			FileUtils.copy(from, to, buffer);
		}

	}

	/**
	 * Recursively copy one folder to another Deleting the contents of the destination folder before copying. Use at
	 * your peril!
	 *
	 * @param source_dir
	 * @param destination_dir
	 * @throws IOException
	 */
	static public final void recursiveCopyNio(final File source_dir, final File destination_dir) throws IOException {

		if (source_dir == null || destination_dir == null) {
			return;
		}
		if (!source_dir.exists()) {
			throw new java.io.FileNotFoundException(source_dir.getAbsolutePath());
		}
		if (!source_dir.isDirectory()) {
			throw new java.io.IOException("recursiveCopy should only be used for folders!");
		}
		if (source_dir.equals(destination_dir)) {
			throw new java.io.IOException("Cannot copy folder on to itself!");
		}

		if (destination_dir.exists()) {
			FileUtils.recursiveDelete(destination_dir);
		}
		destination_dir.mkdirs();

		File[] files = source_dir.listFiles();
		for (int ifile = 0; ifile < files.length; ++ifile) {
			final File from = files[ifile];
			final File to = new File(destination_dir, from.getName());
			if (from.isDirectory()) {
				FileUtils.recursiveCopyNio(from, to);
				continue;
			}

			FileUtils.copyNio(from, to);

		}

	}

	/**
	 * Recursively copy one folder to another Not deleting the contents of the destination folder before copying. Any
	 * file that already exists will not be copied.
	 *
	 * @param source_dir
	 * @param destination_dir
	 * @throws IOException
	 */
	static public final void recursiveCopyNioNoDelete(final File source_dir, final File destination_dir)
			throws IOException {

		if (source_dir == null || destination_dir == null) {
			return;
		}
		if (!source_dir.exists()) {
			throw new java.io.FileNotFoundException(source_dir.getAbsolutePath());
		}
		if (!source_dir.isDirectory()) {
			throw new java.io.IOException("recursiveCopy should only be used for folders!");
		}
		if (source_dir.equals(destination_dir)) {
			throw new java.io.IOException("Cannot copy folder on to itself!");
		}

		File[] files = source_dir.listFiles();
		for (int ifile = 0; ifile < files.length; ++ifile) {
			final File from = files[ifile];
			final File to = new File(destination_dir, from.getName());
			if (from.isDirectory()) {
				FileUtils.recursiveCopyNioNoDelete(from, to);
				continue;
			}
			FileUtils.copyNioNoCopyOver(from, to);
		}

	}

	/**
	 * Recursively copy one folder to another, but not deleting destination data. Only copies files if timestamp
	 * indicates the data is newer. inf[0] - returns files copied, can be used to track progress inf[1] - returns files
	 * skipped, can be used to track progress inf[2] - if set to -1 will halt copy
	 *
	 * @param source_dir
	 * @param destination_dir
	 * @throws IOException
	 */
	static public final void recursiveIncrementalCopy(final File source_dir, final File destination_dir)
			throws IOException {

		recursiveIncrementalCopy(source_dir, destination_dir, new int[] { 0, 0, 0 });
	}

	/**
	 * Recursively copy one folder to another, but not deleting destination data. Only copies files if timestamp
	 * indicates the data is newer. inf[0] - returns files copied, can be used to track progress inf[1] - returns files
	 * skipped, can be used to track progress inf[2] - if set to -1 will halt copy
	 *
	 * @param source_dir
	 * @param destination_dir
	 * @param inf
	 * @throws IOException
	 */
	static public final void recursiveIncrementalCopy(final File source_dir, final File destination_dir, int[] inf)
			throws IOException {
		FileUtils.recursiveIncrementalCopy(source_dir, destination_dir, inf, new byte[FileUtils.getBufferSize()]);
	}

	/**
	 * @param source_dir
	 * @param destination_dir
	 * @param inf
	 * @param buffer
	 * @throws IOException
	 */
	static private final void recursiveIncrementalCopy(final File source_dir, final File destination_dir, int[] inf,
			final byte[] buffer) throws IOException {
		if (inf[2] == -1) {
			return;
		}
		if (source_dir == null || destination_dir == null || inf.length < 3) {
			return;
		}
		if (!source_dir.exists()) {
			throw new java.io.FileNotFoundException(source_dir.getAbsolutePath());
		}
		if (!source_dir.isDirectory()) {
			throw new java.io.IOException("recursiveCopy should only be used for folders!");
		}
		if (source_dir.equals(destination_dir)) {
			throw new java.io.IOException("Cannot copy folder on to itself!");
		}

		if (destination_dir.exists()) {
			destination_dir.mkdirs();
		}
		File[] files = source_dir.listFiles();
		for (int ifile = 0; ifile < files.length; ++ifile) {
			final File from = files[ifile];
			final File to = new File(destination_dir, from.getName());
			if (from.isDirectory()) {
				recursiveIncrementalCopy(from, to, inf, buffer);
				continue;
			}
			if (from.lastModified() > to.lastModified()) {
				FileUtils.copy(from, to, buffer);
				inf[0]++;
			} else {
				inf[1]++;
			}
		}
	}

	/**
	 * @param source_file
	 * @param destination_dir
	 * @throws IOException
	 */
	public final static void copy(final File source_file, String destination_dir) throws IOException {
		File dest_file = null;
		if (source_file.isDirectory()) {
			dest_file = new File(destination_dir);
			FileUtils.recursiveCopy(source_file, dest_file);
		} else {
			String fname = source_file.getName();
			dest_file = new File(destination_dir, fname);
			FileUtils.copy(source_file, dest_file);
		}
	}

	/**
	 * Overwrites destination_file if it exists, creates new if not.
	 *
	 * @param source_file
	 * @param destination_file
	 * @throws IOException
	 */
	public final static void copy(final File source_file, final File destination_file) throws IOException {
		FileUtils.copy(source_file, destination_file, new byte[FileUtils.getBufferSize()]);
	}

	/**
	 * Overwrites destination_file if it exists, creates new if not.
	 *
	 * @param source_file
	 * @param destination_file
	 * @param buffer
	 * @throws IOException
	 */
	@SuppressWarnings("null")
	public final static void copy(final File source_file, final File destination_file, final byte[] buffer)
			throws IOException {

		if (!source_file.exists()) {
			return;
		}

		final File parTo = destination_file.getParentFile();
		if (!parTo.exists()) {
			parTo.mkdirs();
		}
		if (!destination_file.exists()) {
			destination_file.createNewFile();
		}

		InputStream source = null;
		OutputStream destination = null;
		try {

			source = new BufferedInputStream(new FileInputStream(source_file));
			destination = new BufferedOutputStream(new FileOutputStream(destination_file));
			int bytes_read;
			while (true) {
				bytes_read = source.read(buffer);
				if (bytes_read == -1) {
					break;
				}
				destination.write(buffer, 0, bytes_read);
			}

		} finally {
			source.close();
			destination.close();
		}
	}

	/**
	 * @param source_file
	 * @param destination_file
	 * @throws IOException
	 */
	public final static void copyNio(final File source_file, final File destination_file) throws IOException {

		if (!source_file.exists()) {
			return;
		}

		final File parTo = destination_file.getParentFile();
		if (!parTo.exists()) {
			parTo.mkdirs();
		}
		if (!destination_file.exists()) {
			destination_file.createNewFile();
		}

		FileInputStream inStream = null;
		FileOutputStream outStream = null;
		try {
			// Create channel on the source
			inStream = new FileInputStream(source_file);
			FileChannel srcChannel = inStream.getChannel();

			// Create channel on the destination
			outStream = new FileOutputStream(destination_file);
			FileChannel dstChannel = outStream.getChannel();

			// Copy file contents from source to destination
			dstChannel.transferFrom(srcChannel, 0, srcChannel.size());

		} finally {
			// Close the channels
			if (inStream != null) {
				inStream.close();
			}
			if (outStream != null) {
				outStream.close();
			}
		}
	}

	/**
	 * @param source_file
	 * @param destination_file
	 * @throws IOException
	 */
	public final static void copyNioNoCopyOver(final File source_file, final File destination_file) throws IOException {

		if (destination_file.exists()) {
			return;
		}
		FileUtils.copyNio(source_file, destination_file);
	}

	/**
	 * @param source
	 * @param destination_file
	 * @throws IOException
	 */
	public final static void write(final BufferedInputStream source, final File destination_file) throws IOException {

		final File parTo = destination_file.getParentFile();
		if (!parTo.exists()) {
			parTo.mkdirs();
		}
		if (!destination_file.exists()) {
			destination_file.createNewFile();
		}

		OutputStream destination = null;
		try {

			destination = new BufferedOutputStream(new FileOutputStream(destination_file));
			byte[] buffer = new byte[FileUtils.getBufferSize()];
			int bytes_read;

			while (true) {
				bytes_read = source.read(buffer);
				if (bytes_read == -1) {
					break;
				}
				destination.write(buffer, 0, bytes_read);
			}

		} finally {
			source.close();
			if (destination != null) {
				destination.close();
			}
		}
	}

	/**
	 * @param source_raw
	 * @param destination_raw
	 * @throws IOException
	 */
	public final static void write(final InputStream source_raw, final OutputStream destination_raw) throws IOException {

		BufferedOutputStream destination = null;
		BufferedInputStream source = null;
		try {

			source = new BufferedInputStream(source_raw);
			destination = new BufferedOutputStream(destination_raw);
			byte[] buffer = new byte[FileUtils.getBufferSize()];
			int bytes_read;

			while (true) {
				bytes_read = source.read(buffer);
				if (bytes_read == -1) {
					break;
				}
				destination.write(buffer, 0, bytes_read);
			}

		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
	}

	/**
	 * @param source
	 * @return byte[]
	 * @throws IOException
	 */
	public final static byte[] getByteArrayFromStream(final InputStream source) throws IOException {

		return getByteArrayFromStream(source, true);
	}

	/**
	 * @param source
	 * @param shouldClose
	 * @return byte[]
	 * @throws IOException
	 */
	public final static byte[] getByteArrayFromStream(final InputStream source, final boolean shouldClose)
			throws IOException {

		ByteArrayOutputStream destination = FileUtils.getByteStream(source, shouldClose);
		return destination.toByteArray();
	}

	/**
	 * @param source
	 * @return ByteArrayOutputStream
	 * @throws IOException
	 */
	public final static ByteArrayOutputStream getByteStream(final InputStream source) throws IOException {
		return getByteStream(source, true);
	}

	/**
	 * @param source
	 * @param shouldClose
	 * @return ByteArrayOutputStream
	 * @throws IOException
	 */
	public final static ByteArrayOutputStream getByteStream(final InputStream source, final boolean shouldClose)
			throws IOException {

		ByteArrayOutputStream destination = new ByteArrayOutputStream();
		try {

			byte[] buffer = new byte[FileUtils.getBufferSize()];
			int bytes_read;

			while (true) {
				bytes_read = source.read(buffer);
				if (bytes_read == -1) {
					break;
				}
				destination.write(buffer, 0, bytes_read);
			}

		} finally {
			if (source != null) {
				if (shouldClose) {
					source.close();
				}
			}
		}
		return destination;
	}

	/**
	 * @param file
	 * @return StringBuffer
	 * @throws Exception
	 */
	public static final StringBuffer readFile(final File file) throws Exception {
		return FileUtils.readFile(new FileInputStream(file));
	}

	/**
	 * @param in
	 * @return StringBuffer
	 * @throws Exception
	 */
	public static final StringBuffer readFile(final InputStream in) throws Exception {

		return readFile(in, null);
	}

	/**
	 * @param in
	 * @param charsetName
	 * @return StringBuffer
	 * @throws Exception
	 */
	public static final StringBuffer readFile(final InputStream in, final String charsetName) throws Exception {

		BufferedReader ir = null;
		try {
			if (charsetName != null) {
				ir = new BufferedReader(new InputStreamReader(in, charsetName));
			} else {
				ir = new BufferedReader(new InputStreamReader(in));
			}

			// deliberately do not remove BOM here
			int c;
			StringBuffer currentStrBuffer = new StringBuffer();
			final char[] buf = new char[4096];
			while ((c = ir.read(buf, 0, 4096)) > 0) {
				currentStrBuffer.append(buf, 0, c);
			}
			return currentStrBuffer;

		} finally {
			if (ir != null) {
				ir.close();
			}
		}
	}

	/**
	 * @param f
	 * @return List<String>
	 * @throws Exception
	 */
	public static final List<String> readFileAsList(File f) throws Exception {

		List<String> l = new ArrayList<String>();
		BufferedReader br = null;
		try {
			Reader reader = new FileReader(f);
			br = new BufferedReader(reader);

			String str;
			while ((str = br.readLine()) != null) {
				l.add(str);
			}
		} finally {
			if (br != null) {
				br.close();
			}
		}
		return l;
	}

	/**
	 * Reads a file using the encoding parameter passed and returns each line as an item in the result list Optionally:
	 * the BOM can be removed (first character of file if present) the line can be trimmed of whitespace
	 *
	 * @param file
	 *            The file to read
	 * @param encodingOfFile
	 *            The encoding format the file should be read as
	 * @param removeBom
	 *            Whether the BOM should be removed, if present.
	 * @param trimLines
	 *            If each line should have leading and trailing whitespace removed
	 * @return List<String> lines of the file optionally trimmed
	 * @throws IOException
	 *             If the file can not be read
	 */
	public static final List<String> readFileAsList(final File file, final String encodingOfFile,
			final boolean removeBom, final boolean trimLines) throws IOException {

		final InputStream is = new FileInputStream(file);
		final Reader br = new BufferedReader(new InputStreamReader(is, encodingOfFile));

		final List<String> fileContents = new ArrayList<String>(33);
		try {
			String line;
			boolean readFirstLine = false;
			while ((line = ((BufferedReader) br).readLine()) != null) {

				// Remove BOM from file if specified at first part of file
				if (removeBom && !readFirstLine) {
					readFirstLine = true;

					final char firstChar = line.charAt(0);
					if (firstChar == FileUtils.BOM) {
						line = line.substring(1);
					}
				}

				if (trimLines) {
					fileContents.add(line.trim());

				} else {
					fileContents.add(line);
				}
			}

		} finally {
			br.close();
			is.close();
		}
		return fileContents;
	}

	private final static int TESTNUM = 1;

	/**
	 * This method returns true if file is Unix and false if Windows line endings.
	 *
	 * @param stringToTest
	 *            The text of the file
	 *@return boolean true if the file is Unix.
	 */
	public static boolean isUnix(final String stringToTest) {

		boolean isUnix = false;
		StringBuffer sb = new StringBuffer(stringToTest);

		int unix = 0, win = 0;
		for (int j = 0; j < sb.length(); j++) {

			if (unix >= TESTNUM) {
				isUnix = true;
				break;
			} else if (win >= TESTNUM) {
				isUnix = false;
				break;
			}

			if (sb.charAt(j) == '\n' && (j == 0 || sb.charAt(j - 1) != '\r')) {
				unix += 1;
			} else if (sb.charAt(j) == '\n' && (j == 0 || sb.charAt(j - 1) == '\r')) {
				win += 1;
			}
		}

		return isUnix;
	}

	/**
	 * Returns true if the string is starting with a BOM
	 *
	 * @param stringToTest
	 * @return boolean
	 */
	public static boolean isBOM(String stringToTest) {
		if (stringToTest == null) {
			return false;
		}
		if ("".equals(stringToTest)) {
			return false;
		}
		return stringToTest.charAt(0) == BOM;
	}

	/**
	 * @param file
	 * @param text
	 * @param encoding
	 * @throws Exception
	 */
	public static void write(final File file, final String text, String encoding) throws Exception {
		BufferedWriter b = null;
		try {
			final OutputStream out = new FileOutputStream(file);
			final OutputStreamWriter writer = new OutputStreamWriter(out, encoding);
			b = new BufferedWriter(writer);
			b.write(text.toCharArray());
		} finally {
			if (b != null) {
				b.close();
			}
		}
	}

	/**
	 * This method attempts to write a string to file in US-ASCII. The code was moved directly from atos.SaveTextFile
	 * and no check has been made to how efficient it is.
	 *
	 * @param file
	 * @param text
	 * @throws Exception
	 */
	public static void write(final File file, final String text) throws Exception {
		write(file, text, "US-ASCII");
	}

	/**
	 * This method writes to a stream a potentially large String. The current limit for Java servlets in 90Mb with this
	 * method there is a workaround in Java 1.5 for this problem:
	 * http://forum.java.sun.com/thread.jspa?threadID=418441&messageID=2816084
	 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5026745
	 *
	 * @param out
	 * @param text
	 * @param charset
	 * @param addBomIfNeeded
	 * @throws Exception
	 */
	public static void write(final OutputStream out, String text, final String charset, final boolean addBomIfNeeded)
			throws Exception {
		BufferedWriter b = null;
		try {
			if (addBomIfNeeded && text.charAt(0) != BOM) {
				text = BOM + text;
			}
			b = new BufferedWriter(new OutputStreamWriter(out, charset));
			b.write(text.toCharArray());
		} finally {
			if (b != null) {
				b.close();
			}
		}
	}

	/**
	 * @param file
	 * @param list
	 * @throws Exception
	 */
	public static void write(final File file, final List<String> list) throws Exception {

		BufferedWriter bw = null;

		try {
			final OutputStream out = new FileOutputStream(file);
			bw = new BufferedWriter(new OutputStreamWriter(out));
			for (int i = 0; i < list.size(); i++) {
				final String line = list.get(i);
				bw.write(line, 0, line.length());
				bw.newLine();
			}
		} finally {
			if (bw != null) {
				bw.close();
			}
		}
	}

	/**
	 * @param file
	 * @return File
	 */
	public static File createNewUniqueDir(final File file) {
		return createNewUniqueDir(file, FileUtils.NEWFOLDER);
	}

	/**
	 * @param file
	 * @param templateName
	 * @return File
	 */
	public static File createNewUniqueDir(final File file, final String templateName) {
		File sug = new File(file, templateName);
		if (sug.exists()) {
			int i = 2;
			while (sug.exists()) {
				sug = new File(file, templateName + " (" + i + ")");
				++i;
			}
		}
		sug.mkdirs();
		return sug;
	}

	/**
	 * @param name
	 * @return String
	 */
	public final static String getParentDirName(final String name) {
		final int indx = name.lastIndexOf("/");

		String ret;
		if (indx > -1) {
			ret = name.substring(0, indx);
		} else {
			ret = "/";
		}
		if (!ret.endsWith("/")) {
			ret = ret + "/";
		}
		return ret;
	}

	/**
	 * @param file
	 * @param fromNoClose
	 * @throws Exception
	 */
	static public final void writeToFile(final File file, final InputStream fromNoClose) throws Exception {
		final BufferedInputStream buf = new BufferedInputStream(fromNoClose);
		final FileOutputStream fl = new FileOutputStream(file);
		final BufferedOutputStream out = new BufferedOutputStream(fl);
		try {
			byte[] buffer = new byte[FileUtils.getBufferSize()];
			int bytes_read;
			while (true) {
				bytes_read = buf.read(buffer);
				if (bytes_read == -1) {
					break;
				}
				out.write(buffer, 0, bytes_read);
			}

		} finally {
			out.flush();
			out.close();
		}
	}

	/**
	 * @param dir
	 * @return long
	 */
	public static long getDiskSpace(final File dir) {

		long inuse = 0;
		if (dir.isDirectory()) {
			final File[] files = dir.listFiles();
			for (File element : files) {
				inuse += getDiskSpace(element);
			}
		} else {
			inuse += dir.length();
		}
		return inuse;
	}

	/**
	 * @param tmp
	 * @param sizeInMB
	 * @return boolean
	 * @throws IOException
	 */
	public static boolean isDiskSpaceAvaliableMB(final File tmp, final long sizeInMB) throws IOException {
		return isDiskSpaceAvaliable(tmp, sizeInMB * 1000000);
	}

	/**
	 * @param tmp
	 * @param sizeInB
	 * @return boolean
	 * @throws IOException
	 */
	public static boolean isDiskSpaceAvaliable(final File tmp, final long sizeInB) throws IOException {
		if (!tmp.getParentFile().exists()) {
			tmp.getParentFile().mkdirs();
		}
		if (!tmp.exists()) {
			tmp.createNewFile();
		}

		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(tmp, "rw");
			raf.setLength(sizeInB);
			return true;

		} catch (IOException ioe) {
			return false;

		} finally {
			if (raf != null) {
				raf.close();
			}
			tmp.delete();
		}
	}

	/**
	 * @param file
	 * @param subFolders
	 * @return long
	 */
	public static long getFileSizeRecursive(File file, boolean subFolders) {
		long size = 0;
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files != null) {
				for (File element : files) {
					if (!subFolders && element.isDirectory()) {
						continue;
					}
					long tmpSize = getFileSizeRecursive(element, subFolders);
					if (tmpSize != -1) {
						size += tmpSize;
					}
				}
				return size;
			}
			return -1;
		}
		return file.length();
	}

	/**
	 * Get File extension (result will NOT include ".")
	 *
	 * @param fileName
	 * @return String file extension value, or "" is no extension
	 */
	public static String getFileExtension(String fileName) {
		int posExt = fileName.lastIndexOf(".");
		// No File Extension
		return posExt == -1 ? "" : fileName.substring(posExt + 1);
	}

	/**
	 * Get File extension (result will NOT include ".")
	 *
	 * @param file
	 * @return String file extension value, or "" is no extension
	 */
	public static String getFileExtension(File file) {
		return getFileExtension(file.getName());
	}

	/**
	 * Get Filename minus it's extension if present
	 *
	 * @param file
	 *            File to get filename from
	 * @return String filename minus its extension
	 */
	public static String getFileNameNoExtension(File file) {
		final String fileName = file.getName();
		int posExt = fileName.lastIndexOf(".");
		// No File Extension
		return posExt == -1 ? fileName : fileName.substring(0, posExt);
	}

	/**
	 * Formats a file size
	 *
	 * @param longSize
	 * @param decimalPos
	 * @return formatted string for size.
	 */
	public static String formatSize(long longSize, int decimalPos) {
		NumberFormat fmt = NumberFormat.getNumberInstance();
		if (decimalPos >= 0) {
			fmt.setMaximumFractionDigits(decimalPos);
		}
		final double size = longSize;
		double val = size / (1024 * 1024 * 1024);
		if (val > 1) {
			return fmt.format(val).concat(" GB");
		}
		val = size / (1024 * 1024);
		if (val > 1) {
			return fmt.format(val).concat(" MB");
		}
		val = size / 1024;
		if (val > 10) {
			return fmt.format(val).concat(" KB");
		}
		return fmt.format(size).concat(" bytes");
	}

	private static DateFormat dateFormat;
	/**
	 * Gets the last modified and size formatted as a string
	 *
	 * @param file
	 * @return date and size
	 */
	public static String getSystemInfo(final File file) {
		if (dateFormat==null) dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm");
		final StringBuilder buf = new StringBuilder();
		buf.append("Last Modified ");
		buf.append(dateFormat.format(new Date(file.lastModified())));
		buf.append("\nFile size ");
		buf.append(formatSize(file.length(), 0));

		return buf.toString();
	}
}