/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package gda.device.detector.areadetector.v17;

import java.io.IOException;

public interface NDCodec extends GetPluginBaseAvailable {
	public enum Status {
		SUCCESS, WARNING, ERROR;
	}
	public enum BloscShuffle {
		NONE, BYTE, BIT;
	}
	public enum BloscMode {
		BLOSCLZ, LZ4, LZ4HC, SNAPPY, ZLIB, ZSTD;
	}
	public enum Mode {
		COMPRESSION, DECOMPRESSION;
	}
	public enum Compressor {
		NONE, JPEG, BLOSC, LZ4, BSLZ4;
	}
	Mode getMode() throws IOException;
	void setMode(Mode mode) throws IOException;

	Compressor getCompressor() throws IOException;
	void setCompressor(Compressor comp) throws IOException;

	double getCompressionFactor() throws IOException;

	int getJpegQuality() throws IOException;
	void setJpegQuality(int quality) throws IOException;

	BloscMode getBloscMode() throws IOException;
	void setBloscMode(BloscMode mode) throws IOException;

	int getBloscLevel() throws IOException;
	void setBloscLevel(int level) throws IOException;

	BloscShuffle getBloscShuttle() throws IOException;
	void setBloscShuttle(BloscShuffle mode) throws IOException;

	int getBloscThreads() throws IOException;
	void setBloscThreads(int threads) throws IOException;

	Status getStatus() throws IOException;
	String getError() throws IOException;
}
