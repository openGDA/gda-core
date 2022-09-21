/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package gda.images.camera.mjpeg;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convert an MJPEG stream to JPEG bytes. Call {@link #getNextFrame()} to get
 * the next JPEG in the stream.
 * <p>
 * This class doesn't extend {@link InputStream} but is {@link AutoCloseable} so that
 * the underlying stream will be closed.
 * <p>
 * An MJPEG stream is a stream of JPEG images. Before each image there is a header containing
 * several lines defining the content type, and length.
 * <p>
 * This class functions by reading the input stream, attempting to locate the header line: {@code Content-Length}.
 * This defines the length of bytes to read into an array which is returned to the caller. As a fallback the JPEG
 * bytes can be identified using the JPEG standard's start and end markers.
 *
 */
public class MjpegInputStream implements AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(MjpegInputStream.class);

	/** The first two bytes of every JPEG frame are the Start Of Image (SOI) marker */
	private static final byte[] JPEG_START_MARKER = { (byte) 0xFF, (byte) 0xD8 };

	/** At the end of the JPEG frame: End Of Image (EOI) marker */
	private static final byte[] JPEG_END_MARKER = { (byte) 0xFF, (byte) 0xD9 };

	/** Name of content length header */
	private static final String CONTENT_LENGTH = "Content-Length".toLowerCase();

	/** Maximum header length in bytes */
	private static final int HEADER_MAX_LENGTH = 100;

	/** Max frame length (100kB) */
	private static final int FRAME_MAX_LENGTH = 100000 + HEADER_MAX_LENGTH;

	/**
	 * The data source. This stream is decorated with a {@link BufferedInputStream} (for performance)
	 * and a {@link DataInputStream} (to provide useful methods e.g. readFully, skipBytes)
	 */
	private final DataInputStream dataStream;

	public MjpegInputStream(InputStream in) {
		dataStream = new DataInputStream(new BufferedInputStream(in, FRAME_MAX_LENGTH));
	}

	/**
	 *  Get the byte index of the of the next byte following the specified sequence
	 * @return the index or -1 if the complete sequence is not found
	 * @throws IOException
	 */
	private int getEndOfSeqeunce(byte[] sequence) throws IOException {
		int matchingSequenceBytes = 0;
		byte nextByte;
		for (int i = 0; i < FRAME_MAX_LENGTH; i++) {
			nextByte = (byte) dataStream.readUnsignedByte();
			if (nextByte == sequence[matchingSequenceBytes]) {
				matchingSequenceBytes++;
				if (matchingSequenceBytes == sequence.length) {
					return i + 1;
				}
			} else {
				matchingSequenceBytes = 0;
			}
		}
		return -1;
	}

	private int getStartOfSequence(byte[] sequence) throws IOException {
		int end = getEndOfSeqeunce(sequence);
		return end < 0 ? -1 : end - sequence.length;
	}

	/**
	 * Extract length from header bytes e.g. if header contains:
	 * {@code Content-Length: 92708}
	 * return {@code 92708}
	 * @param headerBytes
	 * @return content length specified in the header or zero if not found
	 * @throws IOException
	 */
	private int parseContentLength(byte[] headerBytes) throws IOException {

		try (ByteArrayInputStream bais = new ByteArrayInputStream(headerBytes);
				InputStreamReader isr = new InputStreamReader(bais);
				BufferedReader br = new BufferedReader(isr)) {

			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.toLowerCase().startsWith(CONTENT_LENGTH)) {
					String[] parts = line.split(":");
					if (parts.length == 2) {
						return Integer.parseInt(parts[1].trim());
					}
				}
			}
		}
		throw new NumberFormatException("No Content-Length in header");
	}



	/**
	 * Attempt to get jpeg length from header otherwise fall back to searching ahead for ending
	 */
	private int getJpegDataLength(byte[] header) throws IOException {
		try {
			return parseContentLength(header);
		} catch (NumberFormatException e) {
			int len = getEndOfSeqeunce(JPEG_END_MARKER);
			if (len <= 0) {
				logger.error("Invalid MJPEG stream, JPEG end marker not found");
			}
			return len;
		}
	}

	/**
	 * Read single MJPEG frame (JPEG image) from stream.
	 *
	 * @return JPEG image as byte array or null
	 * @throws IOException
	 *             when there is a problem in reading from stream
	 */
	public byte[] getNextFrame() throws IOException {
		dataStream.mark(FRAME_MAX_LENGTH); // mark current stream position
		int jpegStartIndex = getStartOfSequence(JPEG_START_MARKER);
		dataStream.reset();
		byte[] header = new byte[jpegStartIndex];
		dataStream.readFully(header); // read until header is full
		// stream will now be at the start of the jpeg data
		int length = getJpegDataLength(header);
		dataStream.reset();
		byte[] frame = new byte[length];
		dataStream.skipBytes(jpegStartIndex);
		dataStream.readFully(frame);
		return frame;
	}

	@Override
	public void close() throws IOException {
		// Closing dataStream will close decorated streams
		dataStream.close();
	}
}