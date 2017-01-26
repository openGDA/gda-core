/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.epics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NDFileHDF5Impl extends gda.device.detector.areadetector.v17.impl.NDFileHDF5Impl {
	static final Logger logger = LoggerFactory.getLogger(NDFileHDF5Impl.class);

	public final String ColumnsPerChunk = "NumColChunks";
	public final String FramessPerChunk = "NumFramesChunks";
	public final String ChunkAlignment = "BoundaryAlign";
	public final String ColumnsPerChunk_RBV = "NumColChunks_RBV";
	public final String FramessPerChunk_RBV = "NumFramesChunks_RBV";
	public final String ChunkAlignment_RBV = "BoundaryAlign_RBV";
	public final String QueueUse = "QueueUse";
	public final String WriteStatus = "WriteStatus";
	public final String WriteMessage = "WriteMessage";

	private Integer initialColumnsPerChunk;
	private Integer initialFramesPerChunk;
	private Integer initialChunkAlignment;
	
	@Override
	public void reset() throws Exception {
		super.reset();
		
		if (initialColumnsPerChunk != null) {
			setColumnsPerChunk(initialColumnsPerChunk);
		}
		if (initialFramesPerChunk != null) {
			setFramesPerChunk(initialFramesPerChunk);
		}
		if (initialChunkAlignment != null) {
			setChunkAlignment(initialChunkAlignment);
		}
	}

	public int getColumnsPerChunk() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(ColumnsPerChunk_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getColumnsPerChunk", ex);
			throw ex;
		}
	}

	public void setColumnsPerChunk(int value) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(ColumnsPerChunk), value);
		} catch (Exception ex) {
			logger.warn("Cannot setColumnsPerChunk", ex);
			throw ex;
		}
	}
	public int getFramesPerChunk() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(FramessPerChunk_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getFramessPerChunk", ex);
			throw ex;
		}
	}

	public void setFramesPerChunk(int value) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(FramessPerChunk), value);
		} catch (Exception ex) {
			logger.warn("Cannot setFramessPerChunk", ex);
			throw ex;
		}
	}
	
	public int getChunkAlignment() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(ChunkAlignment_RBV));
		} catch (Exception ex) {
			logger.warn("Cannot getChunkAlignment", ex);
			throw ex;
		}
	}

	public void setChunkAlignment(int value) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(ChunkAlignment), value);
		} catch (Exception ex) {
			logger.warn("Cannot setChunkAlignment", ex);
			throw ex;
		}
	}

	public Integer getInitialColumnsPerChunk() {
		return initialColumnsPerChunk;
	}

	public void setInitialColumnsPerChunk(Integer initialColumnsPerChunk) {
		this.initialColumnsPerChunk = initialColumnsPerChunk;
	}

	public Integer getInitialFramesPerChunk() {
		return initialFramesPerChunk;
	}

	public void setInitialFramesPerChunk(Integer initialFramesPerChunk) {
		this.initialFramesPerChunk = initialFramesPerChunk;
	}

	public Integer getInitialChunkAlignment() {
		return initialChunkAlignment;
	}

	public void setInitialChunkAlignment(Integer initialChunkAlignment) {
		this.initialChunkAlignment = initialChunkAlignment;
	}
	
	public int getQueueUse() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(QueueUse));
		} catch (Exception ex) {
			logger.warn("Cannot getNumFramesFlush", ex);
			throw ex;
		}
	}
	
	public int getWriteStatus() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(WriteStatus));
		} catch (Exception ex) {
			logger.warn("Cannot getNumFramesFlush", ex);
			throw ex;
		}
	}
	
	public String getWriteMessage() throws Exception {
		try {
			return new String(EPICS_CONTROLLER.cagetByteArray(getChannel(WriteMessage))).trim();
		} catch (Exception ex) {
			logger.warn("Cannot getNumFramesFlush", ex);
			throw ex;
		}
	}
}