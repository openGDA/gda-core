/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.device.detector.xmap.util;

import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeBuilder;
import gda.data.nexus.tree.NexusTreeNodeSelection;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmapNexusFileLoader implements XmapFileLoader {

	private static final Logger logger = LoggerFactory.getLogger(XmapNexusFileLoader.class);
	private String fileName;
	private ChannelData[][] fullDataArray; // [number of pixels(data points)][element]
	private int numberChannelsToRead;

	public XmapNexusFileLoader(String testfile1, int numberChannelsToRead) {
		this.fileName = testfile1;
		this.numberChannelsToRead = numberChannelsToRead;
	}

	@Override
	public int getNumberOfDataPoints() {
		if (fullDataArray != null)
			return fullDataArray.length;
		return 0;
	}

	@Override
	public void loadFile() throws Exception {
		INexusTree tree = NexusTreeBuilder.getNexusTree(fileName.trim(),
				NexusTreeNodeSelection.createTreeForAllNXEntries());
		INexusTree mainTree = tree.getChildNode("entry", "NXentry");
		mainTree = mainTree.getChildNode("instrument", "NXinstrument");
		mainTree = mainTree.getChildNode("detector", "NXdetector");
		NexusGroupData gdata = mainTree.getChildNode("data", "SDS").getData();

		int[] dataDim = gdata.dimensions;
		short[] arry = (short[]) gdata.getBuffer();
		short[][][] packedArray = packArray(arry, dataDim); // [buffer][XIA module][data]
		int numberPixels = getNumberPixels(packedArray);
		fullDataArray = new ChannelData[numberPixels][numberChannelsToRead];
		
		logger.debug("Reading " + fileName.trim() + " which has " + packedArray.length + " buffers containing "
				+ numberPixels + " pixels");
		
		for (int m = 0; m < packedArray.length; m++) {
			for (int n = 0; n < packedArray[m].length; n++) {
				BufferHeader bufHeader = readCommonHeader(packedArray[m][n]);
				int readIndex = bufHeader.bufferHeaderSize;
				for (int pixel = 0; pixel < bufHeader.numberOfPixelsInBuffer; pixel++) {
					MappingMode1BufferData dataArray = new MappingMode1BufferData();
					dataArray.read(packedArray[m][n], readIndex);

					readIndex += (dataArray.channel0Size + dataArray.channel1Size + dataArray.channel2Size
							+ dataArray.channel3Size + dataArray.pixelheaderSize);

					if (fullDataArray[dataArray.pixelNumber][bufHeader.detectorChannel0] != null)
						logger.debug("oops");

					if (bufHeader.detectorChannel0 < numberChannelsToRead)
						fullDataArray[dataArray.pixelNumber][bufHeader.detectorChannel0] = dataArray.channel0Data;
					if (bufHeader.detectorChannel1 < numberChannelsToRead)
						fullDataArray[dataArray.pixelNumber][bufHeader.detectorChannel1] = dataArray.channel1Data;
					if (bufHeader.detectorChannel2 < numberChannelsToRead)
						fullDataArray[dataArray.pixelNumber][bufHeader.detectorChannel2] = dataArray.channel2Data;
					if (bufHeader.detectorChannel3 < numberChannelsToRead)
						fullDataArray[dataArray.pixelNumber][bufHeader.detectorChannel3] = dataArray.channel3Data;
				}
			}
		}
	}

	private int getNumberPixels(short[][][] packedArray) {
		// in the last buffer, add the number of pixels to the pixel-offset to get the total pixels in the HDF5 file
		int totalPixels = packedArray[packedArray.length - 1][0][8] + packedArray[packedArray.length - 1][0][9];
		return totalPixels;
	}

	private short[][][] packArray(short[] arry, int[] dataDim) {
		short[][][] pArray = new short[dataDim[0]][dataDim[1]][dataDim[2]];
		int startIndex = 0;
		for (int i = 0; i < pArray.length; i++) {
			for (int j = 0; j < pArray[i].length; j++) {
				for (int k = 0; k < pArray[i][j].length; k++) {
					pArray[i][j][k] = arry[startIndex++];
				}
			}
		}
		return pArray;
	}

	private BufferHeader readCommonHeader(short[] arry) {
		BufferHeader header = new BufferHeader();
		header.read(arry);
		return header;

	}

	private class BufferHeader {
		int tagWord0;
		int tagWord1;
		int bufferHeaderSize;
		int mappingMode;
		int runNumber;
		int sequentialBufferNumber;
		int bufferID;
		int numberOfPixelsInBuffer;
		int startingPixelNumber;
		int moduleSerialNumber;
		int detectorChannel0;
		int detectorElementChannel0;
		int detectorChannel1;
		int detectorElementChannel1;
		int detectorChannel2;
		int detectorElementChannel2;
		int detectorChannel3;
		int detectorElementChannel3;
		int channel0Size;
		int channel1Size;
		int channel2Size;
		int channel3Size;
		int bufferErrors;

		void read(short[] arry) {
			tagWord0 = arry[0];
			tagWord1 = arry[1];
			bufferHeaderSize = arry[2];
			mappingMode = arry[3];
			runNumber = arry[4];
			int right = arry[5];
			int left = arry[6];

			sequentialBufferNumber = left << 16 | (right & 0xFFFF);
			bufferID = arry[7];
			numberOfPixelsInBuffer = arry[8];
			startingPixelNumber = (arry[10] << 16) | (arry[9] & 0xFFFF);
			moduleSerialNumber = arry[11];
			detectorChannel0 = arry[12];
			detectorElementChannel0 = arry[13];
			detectorChannel1 = arry[14];
			detectorElementChannel1 = arry[15];
			detectorChannel2 = arry[16];
			detectorElementChannel2 = arry[17];
			detectorChannel3 = arry[18];
			detectorElementChannel3 = arry[19];
			channel0Size = arry[20];
			channel1Size = arry[21];
			channel2Size = arry[22];
			channel3Size = arry[23];
			bufferErrors = arry[24];
		}

		@Override
		public String toString() {
			String output = "";
			output += ("tagWord0 " + tagWord0 + "\n");
			output += ("tagWord1 " + tagWord1 + "\n");
			output += ("bufferHeadderSize " + bufferHeaderSize + "\n");
			output += ("mappingMode " + mappingMode + "\n");
			output += ("runNumber " + runNumber + "\n");
			output += ("sequentialBufferNumber " + sequentialBufferNumber + "\n");
			output += ("bufferID " + bufferID + "\n");
			output += ("numberOfPixelsInBuffer " + numberOfPixelsInBuffer + "\n");
			output += ("startingPixelNumber " + startingPixelNumber + "\n");
			output += ("moduleSerialNumber " + moduleSerialNumber + "\n");
			output += ("detectorChannel0 " + detectorChannel0 + "\n");
			output += ("detectorElementChannel0 " + detectorElementChannel0 + "\n");
			output += ("detectorChannel1 " + detectorChannel1 + "\n");
			output += ("detectorElementChannel1 " + detectorElementChannel1 + "\n");
			output += ("detectorChannel2 " + detectorChannel2 + "\n");
			output += ("detectorElementChannel2 " + detectorElementChannel2 + "\n");
			output += ("detectorChannel3 " + detectorChannel3 + "\n");
			output += ("detectorElementChannel3 " + detectorElementChannel3 + "\n");
			output += ("channel0Size " + channel0Size + "\n");
			output += ("channel1Size " + channel1Size + "\n");
			output += ("channel2Size " + channel2Size + "\n");
			output += ("channel3Size " + channel3Size + "\n");
			output += ("bufferErrors " + bufferErrors + "\n");
			return output;
		}

	}

	private class ChannelData {
		ChannelStatistics channelStatistics;
		short[] spectrum;
	}

	private class MappingMode1BufferData {
		int tagWord0;
		int tagWord1;
		int pixelheaderSize;
		int mappingMode;
		int pixelNumber;
		int totalPixelBlockSize;
		int channel0Size, k;
		int channel1Size, l;
		int channel2Size, m;
		int channel3Size, n;
		ChannelData channel0Data;
		ChannelData channel1Data;
		ChannelData channel2Data;
		ChannelData channel3Data;

		short[] readSpectrum(short[] in, int startIndex, int sizeOfSpectrum) {
			short[] spectrum = new short[sizeOfSpectrum];
			int endIndex = startIndex + sizeOfSpectrum;
			for (int i = startIndex; i < endIndex; i++) {
				spectrum[i - startIndex] = in[i];
			}
			return spectrum;
		}

		@SuppressWarnings("unused")
		void read(short[] in, int startIndex) throws IOException {

			tagWord0 = in[startIndex++];
			tagWord1 = in[startIndex++];
			pixelheaderSize = in[startIndex++];
			mappingMode = in[startIndex++];
			int right = in[startIndex++];
			int left = in[startIndex++];
			pixelNumber = left << 16 | (right & 0xFFFF);
			right = in[startIndex++];
			left = in[startIndex++];
			totalPixelBlockSize = left << 16 | (right & 0xFFFF);
			channel0Size = in[startIndex++];
			k = channel0Size;
			channel1Size = in[startIndex++];
			l = channel1Size;
			channel2Size = in[startIndex++];
			m = channel2Size;
			channel3Size = in[startIndex++];
			n = channel3Size;// skip 20 words
			startIndex += 20;
			channel0Data = new ChannelData();
			channel0Data.channelStatistics = new ChannelStatistics();
			channel0Data.channelStatistics.read(in, startIndex);
			startIndex += 8;
			channel1Data = new ChannelData();
			channel1Data.channelStatistics = new ChannelStatistics();
			channel1Data.channelStatistics.read(in, startIndex);
			startIndex += 8;
			channel2Data = new ChannelData();
			channel2Data.channelStatistics = new ChannelStatistics();
			channel2Data.channelStatistics.read(in, startIndex);
			startIndex += 8;
			channel3Data = new ChannelData();
			channel3Data.channelStatistics = new ChannelStatistics();
			channel3Data.channelStatistics.read(in, startIndex);
			startIndex += 8;
			// skipping 192 words
			startIndex += 192;

			channel0Data.spectrum = readSpectrum(in, startIndex, k);
			startIndex += k;
			channel1Data.spectrum = readSpectrum(in, startIndex, l);
			startIndex += l;
			channel2Data.spectrum = readSpectrum(in, startIndex, m);
			startIndex += m;
			channel3Data.spectrum = readSpectrum(in, startIndex, n);
			startIndex += n;
		}

		/**
		 * Overloaded tostring method
		 * 
		 * @return output
		 */
		@Override
		public String toString() {
			String output = "";
			output += "Tag Word 0 = " + tagWord0 + "\n";
			output += "Tag Word 1 = " + tagWord1 + "\n";
			output += "Pixel Header Size = " + pixelheaderSize + "\n";
			output += "Mapping Mode = " + mappingMode + "\n";
			output += "Pixel Number = " + pixelNumber + "\n";
			output += "Total Pixel Block Size = " + totalPixelBlockSize + "\n";
			output += "Channel 0 Size = " + channel0Size + "\n";
			output += "Channel 1 Size = " + channel1Size + "\n";
			output += "Channel 2 Size = " + channel2Size + "\n";
			output += "Channel 3 Size = " + channel3Size + "\n";
			output += "(k) = " + k + "\n";
			output += "(l) = " + l + "\n";
			output += "(m) = " + m + "\n";
			output += "(n) = " + n + "\n";
			output += "Channel 0 Statistics = " + channel0Data.channelStatistics + "\n";
			output += "Channel 1 Statistics = " + channel1Data.channelStatistics + "\n";
			output += "Channel 2 Statistics = " + channel2Data.channelStatistics + "\n";
			output += "Channel 3 Statistics = " + channel3Data.channelStatistics + "\n";
			return output;
		}
	}

	private class ChannelStatistics {
		float realtime;
		float livetime;
		int triggers;
		int outputEvents;

		void read(short[] in, int startIndex) {
			float clock = 3125000;
			int right = in[startIndex++];
			int left = in[startIndex++];
			int lreal = left << 16;
			int rreal = (right & 0xFFFF);
			int ored = lreal | rreal;
			realtime = ored / clock;
			right = in[startIndex++];
			left = in[startIndex++];
			livetime = (left << 16 | (right & 0xFFFF)) / clock;
			right = in[startIndex++];
			left = in[startIndex++];
			triggers = left << 16 | (right & 0xFFFF);
			right = in[startIndex++];
			left = in[startIndex++];
			outputEvents = left << 16 | (right & 0xFFFF);
		}

		/**
		 * Overloaded tostring method
		 * 
		 * @return output
		 */
		@Override
		public String toString() {
			return "Realtime " + realtime + ": Livetime " + livetime + ": Triggers " + triggers + ": Output Events "
					+ outputEvents;
		}

	}

	@Override
	public double getTrigger(int dataPointNumber, int element) {
		return fullDataArray[dataPointNumber][element].channelStatistics.triggers;
	}

	@Override
	public double getRealTime(int dataPointNumber, int element) {
		return fullDataArray[dataPointNumber][element].channelStatistics.realtime;
	}

	@Override
	public double getLiveTime(int dataPointNumber, int element) {
		return fullDataArray[dataPointNumber][element].channelStatistics.livetime;
	}

	@Override
	public double getEvents(int dataPointNumber, int element) {
		return fullDataArray[dataPointNumber][element].channelStatistics.outputEvents;
	}

	@Override
	public short[][] getData(int dataPointNumber) {
		if (fullDataArray != null) {
			short[][] dataFromArrays = new short[numberChannelsToRead][];
			for (int chn = 0; chn < numberChannelsToRead; chn++) {
				dataFromArrays[chn] = fullDataArray[dataPointNumber][chn].spectrum;
			}
			return dataFromArrays;
		}
		return null;
	}

	@Override
	public short[][][] getData(int fromDataPointNumber, int toDataPointNumber) throws Exception {
		int totalToRead = (toDataPointNumber - fromDataPointNumber) + 1;
		if (fullDataArray == null || fromDataPointNumber > fullDataArray.length
				|| toDataPointNumber >= fullDataArray.length) {
			throw new Exception("Data not available for the requested range " + fromDataPointNumber + " - "
					+ toDataPointNumber);
		}
		if (fromDataPointNumber > toDataPointNumber) {
			int temp = toDataPointNumber;
			toDataPointNumber = fromDataPointNumber;
			fromDataPointNumber = temp;
		}
		short[][][] returnData = new short[totalToRead][][];

		if (fullDataArray != null) {
			for (int dataPointNumber = fromDataPointNumber; dataPointNumber <= toDataPointNumber; dataPointNumber++) {
				returnData[dataPointNumber] = getData(dataPointNumber);
			}
			return returnData;
		}
		return null;
	}
}
