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

package gda.device.detector.odccd;

/**
 * <p>
 * <b>Title: </b>Container class for Oxford Diffraction IS binary header.
 * </p>
 * <p>
 * <b>Description: </b>Use this class to hold the header information of the binary IS data format.
 * </p>
 */

public class ISBinaryHeader {

	// should theses really be public? They have accessor functions!

	/**
	 * 
	 */
	public int mNameOffset = 0;

	/**
	 * 
	 */
	public int mNameLength = 0;

	/**
	 * 
	 */
	public int mDataOffset = 0;

	/**
	 * 
	 */
	public int mDataLength = 0;

	/**
	 * 
	 */
	public int[] mDataSize = new int[2];

	/**
	 * 
	 */
	public int[] mDataDstOffset = new int[2];

	/**
	 * 
	 */
	public int mUnitOffset = 0;

	/**
	 * 
	 */
	public int mUnitLength = 0;

	/**
	 * 
	 */
	public int mFrameLength = 0;

	/**
	 * 
	 */
	public int mFlags = 0;

	/**
	 * Get the data dst offset
	 * 
	 * @return offset
	 */
	public int[] getDataDstOffset() {
		return mDataDstOffset;
	}

	/**
	 * Set the data dst offset
	 * 
	 * @param dataDstOffset
	 */
	public void setDataDstOffset(int[] dataDstOffset) {
		System.arraycopy(dataDstOffset, 0, mDataDstOffset, 0, 2);
	}

	/**
	 * Get the size of the data buffer
	 * 
	 * @return size of data buffer (bytes)
	 */
	public int getDataLength() {
		return mDataLength;
	}

	/**
	 * Set the size of the data buffer
	 * 
	 * @param dataLength
	 *            (bytes)
	 */
	public void setDataLength(int dataLength) {
		mDataLength = dataLength;
	}

	/**
	 * Get the offset of the data from the start of the buffer.
	 * 
	 * @return offset (bytes)
	 */
	public int getDataOffset() {
		return mDataOffset;
	}

	/**
	 * Set the offset of the data from the start of the buffer.
	 * 
	 * @param dataOffset
	 */
	public void setDataOffset(int dataOffset) {
		mDataOffset = dataOffset;
	}

	/**
	 * Get the 2-D size of the data buffer
	 * 
	 * @return 2-d size
	 */
	public int[] getDataSize() {
		return mDataSize;
	}

	/**
	 * Set the 2-D size of the data buffer.
	 * 
	 * @param dataSize
	 */
	public void setDataSize(int[] dataSize) {
		System.arraycopy(dataSize, 0, mDataSize, 0, 2);
	}

	/**
	 * Get the buffer flags
	 * 
	 * @return flags
	 */
	public int getFlags() {
		return mFlags;
	}

	/**
	 * Set the buffer flags
	 * 
	 * @param flags
	 */
	public void setFlags(int flags) {
		mFlags = flags;
	}

	/**
	 * Get the total frame/buffer length
	 * 
	 * @return length (bytes)
	 */
	public int getFrameLength() {
		return mFrameLength;
	}

	/**
	 * Set the total frame/buffer length
	 * 
	 * @param frameLength
	 */
	public void setFrameLength(int frameLength) {
		mFrameLength = frameLength;
	}

	/**
	 * Get the size of the name buffer
	 * 
	 * @return size (bytes)
	 */
	public int getNameLength() {
		return mNameLength;
	}

	/**
	 * Set the size of the name buffer
	 * 
	 * @param nameLength
	 */
	public void setNameLength(int nameLength) {
		mNameLength = nameLength;
	}

	/**
	 * Get the offset of the name buffer from the start of the buffer.
	 * 
	 * @return offset (bytes)
	 */
	public int getNameOffset() {
		return mNameOffset;
	}

	/**
	 * Set the offset of the name buffer from the start of the buffer.
	 * 
	 * @param nameOffset
	 */
	public void setNameOffset(int nameOffset) {
		mNameOffset = nameOffset;
	}

	/**
	 * Get the size of the unit buffer
	 * 
	 * @return size (bytes)
	 */
	public int getUnitLength() {
		return mUnitLength;
	}

	/**
	 * Set the size of the unit buffer
	 * 
	 * @param unitLength
	 *            (bytes)
	 */
	public void setUnitLength(int unitLength) {
		mUnitLength = unitLength;
	}

	/**
	 * Get the unit buffer offset from the start of the buffer.
	 * 
	 * @return offset (bytes)
	 */
	public int getUnitOffset() {
		return mUnitOffset;
	}

	/**
	 * Set the unit buffer offset from the start of the buffer.
	 * 
	 * @param unitOffset
	 *            (bytes)
	 */
	public void setUnitOffset(int unitOffset) {
		mUnitOffset = unitOffset;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%x %x %x %x %x,%x %x,%x %x %x %x %x", mNameOffset, mNameLength, mDataOffset, mDataLength,
				mDataSize[0], mDataSize[1], mDataDstOffset[0], mDataDstOffset[1], mUnitOffset, mUnitLength,
				mFrameLength, mFlags);
	}
}
