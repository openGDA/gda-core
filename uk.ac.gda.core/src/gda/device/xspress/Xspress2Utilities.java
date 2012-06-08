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

package gda.device.xspress;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.net.TelnetInputStream;
import sun.net.ftp.FtpClient;

/**
 * Contains some utility methods for handling the binary data files produced by Xspress2.
 */
public class Xspress2Utilities {
	private static final Logger logger = LoggerFactory.getLogger(Xspress2Utilities.class);
	/**
	 * Number of resolution grades
	 */
	public static final int mcaGrades = 1;

	/**
	 * Number of energy channels
	 */
	public static final int mcaChannels = 4096;

	/**
	 * Number of detector elements
	 */
	public static final int numberOfDetectors = 9;

	/**
	 * Number of scaler fields being used
	 */
	public static final int scalerFields = 3;

	// These are very big arrays used in the long[][] version of
	// interpretDataFile, tests showed that significant time was being used
	// allocating them (or possibly that they take longer to read into when they
	// are local) so they are now static. The down side of this is that the
	// method has to be synchronized.
	private static long[][] twoDData = new long[numberOfDetectors][mcaGrades * mcaChannels];

	private static byte[] frameOfBytes = new byte[numberOfDetectors * mcaGrades * mcaChannels * 4];

	private static byte[] scalerBytes = new byte[numberOfDetectors * scalerFields * 4];

	// private static long[][] scalerData = new long[numberOfDetectors][scalerFields];
	private static long[][] scalerData = new long[scalerFields][numberOfDetectors];
	private static double[] process_times = { 3.4e-07, 3.8e-07, 3.7e-07, 3.0e-07, 3.4e-07, 3.5e-07, 3.3e-07, 3.0e-07,
			3.3e-07 };
	/*private static double[] process_timesv2= {3.63660099639e-007,3.01630902731e-007,3.64947227166e-007,2.94744845823e-007,
		4.89301439111e-007,	3.50009854185e-007,3.02599425131e-007,2.92175579676e-007,3.38309206192e-007};
	private static double[] window_times= {5.65497797031e-007,4.72469706167e-007,5.3355563578e-007,	6.44235494719e-007,	
		1.02562282079e-006,	7.62360996233e-007,	4.33817183725e-007,4.66530133448e-007,4.45027328546e-007};*/
	private static double[] process_timesv2= {0.0000002898491582,0.0000002194513858,0.0000003018830147,0.0000000924687774,0.0000003715410227,0.0000002239076202,
		0.0000002563124205,0.0000001757591203,0.0000003007834187};
	private static double[] window_times= {0.0000004112427823,0.0000003601390687,0.0000004017908746,0.0000004005931890,0.0000005768045330
		,0.0000004109086398,0.0000003855478998,0.0000003620763296,0.0000003841205047};
	private static double windowTotal = 0.0;

	/**
	 * Sums the data from different resolution grades.
	 * 
	 * @param toBeSummed
	 *            raw data
	 * @param startGrade
	 *            first grade to be added to sum
	 * @param endGrade
	 *            last grade to be added to sum
	 * @return an array of summed values
	 */
	public static double[] sumGrades(long[] toBeSummed, int startGrade, int endGrade) {
		double[] gradeSummedData = null;

		// The incoming data array should be interpreted as mcaGrades blocks of
		// mcaChannels number.
		if (toBeSummed.length == mcaGrades * mcaChannels) {
			gradeSummedData = new double[mcaChannels];

			for (int j = startGrade; j <= endGrade; j++) {
				for (int i = 0; i < mcaChannels; i++) {
					gradeSummedData[i] += toBeSummed[i + (mcaChannels * j)];
				}
			}
		}

		return gradeSummedData;
	}

	/**
	 * Gets the data for a single detector element for a particular frame. This reads the file in detector element sized
	 * chunks (mcaGrades * mcaChannels * 4). It may be possible to get more speed out of it by using bigger chunks.
	 * 
	 * @param filename
	 * @param frameNumber
	 *            NB count from zero
	 * @param detectorNumber
	 *            NB count from zero
	 * @return the data for detector detectorNumber from frame frameNumber
	 */
	public static long[] interpretDataFile(String filename, int frameNumber, int detectorNumber) {
		long[] data = new long[mcaGrades * mcaChannels];
		DataInputStream dis = null;
		try {
			
			dis = new DataInputStream(new FileInputStream(filename));
			byte[] bytes = new byte[mcaGrades * mcaChannels * 4];
			int firstByte, secondByte, thirdByte, fourthByte;

			// Skip to the frame and detector we want
			dis.skipBytes((frameNumber * numberOfDetectors + detectorNumber) * (mcaGrades * mcaChannels * 4));

			// Read the required detector
			dis.read(bytes, 0, mcaGrades * mcaChannels * 4);

			// Array bytes should now contain the required data, we need to
			// construct longs from sets of four bytes.

			for (int l = 0; l < (mcaGrades * mcaChannels * 4); l += 4) {
				firstByte = (0x000000FF & bytes[l + 0]);
				secondByte = (0x000000FF & bytes[l + 1]);
				thirdByte = (0x000000FF & bytes[l + 2]);
				fourthByte = (0x000000FF & bytes[l + 3]);

				data[l / 4] = ((firstByte << 24 | secondByte << 16 | thirdByte << 8 | fourthByte)) & 0xFFFFFFFFL;
			}
			
		} catch (IOException ioe) {
			System.out.println("EOF");
			data = null;
		}
		finally{
			if(dis != null)
				try {
					dis.close();
				} catch (IOException e) {
					logger.error("Unable to close the data stream " );
					dis = null;
				}
		}
		return data;
	}

	/**
	 * Gets the data for all detector elements for a particular frame. Synchronized because the long and byte arrays
	 * used are static (because they are very big and allocating them was taking measurable time).
	 * 
	 * @param filename
	 * @param frameNumber
	 *            NB count from zero
	 * @return the data for frame frameNumber
	 */
	public static synchronized long[][] interpretDataFile(String filename, int frameNumber) {

		int firstByte, secondByte, thirdByte, fourthByte;
		DataInputStream dis = null;
		//logger.info("file name is " + filename);
		try {
			if (twoDData == null)
				 twoDData = new long[numberOfDetectors][mcaGrades * mcaChannels];
			
			if (filename.startsWith("ftp")) {
				logger.info("the file name begins with ftp");
				/*
				 * URL url = new URL(filename); logger.info("the url is "+ url); URLConnection urlc =
				 * url.openConnection(); logger.info("the connection is "+ urlc); dis = new
				 * DataInputStream(urlc.getInputStream()); logger.info("the dis is " + dis);
				 */
				// ftpcclient comm
				int index = filename.indexOf("//");
				filename = filename.substring(index + 2, filename.length());
				index = filename.indexOf(":");
				String user = filename.substring(0, index);
				filename = filename.substring(index + 1, filename.length());
				index = filename.indexOf("@");
				String pass = filename.substring(0, index);
				filename = filename.substring(index + 1, filename.length());
				index = filename.indexOf("//");
				String host = filename.substring(0, index);
				filename = filename.substring(index + 1, filename.length());
				index = filename.lastIndexOf("/");
				String dir = filename.substring(0, index);
				filename = filename.substring(index + 1, filename.length());
				// System.out.println("the dir is " + dir);
				// System.out.println("the filename is " + filename);
				FtpClient ftp = new FtpClient(host);
				ftp.login(user, pass);
				ftp.binary();
				ftp.cd(dir);
				int loopCount = 0;
				while (true) {
					loopCount++;
					// System.out.println("the ftp loop no for file " + filename + " is " + loopCount );
					TelnetInputStream sr = ftp.list();
					InputStreamReader br = new InputStreamReader(sr);

					BufferedReader reader = new BufferedReader(br);

					String line = reader.readLine();
					int match = -1;
					while (line != null) {
						// System.out.println("the lines are " + line);
						line = reader.readLine();
						if (line != null && line.endsWith(filename)) {
							// System.out.println("found file " + filename );
							match = 1;
							break;
						}

					}
					reader.close();
					if (match == 1 || loopCount == 10)
						break;
				}
				dis = new DataInputStream(ftp.get(filename));
				// ftpcclient comm
			} else {
				FileInputStream fis = new FileInputStream(filename);
				dis = new DataInputStream(fis);
			}

			// Skip to the frame
			dis.skipBytes(frameNumber * numberOfDetectors * mcaGrades * mcaChannels * 4);

			// Read the required frame
			dis.read(frameOfBytes, 0, numberOfDetectors * mcaGrades * mcaChannels * 4);

			// Array bytes should now contain the required data, we need to
			// construct longs from sets of four bytes.

			int offset = 0;
			for (int j = 0; j < numberOfDetectors; j++) {
				for (int l = 0; l < (mcaGrades * mcaChannels * 4); l += 4) {
					firstByte = (0x000000FF & frameOfBytes[offset + l + 0]);
					secondByte = (0x000000FF & frameOfBytes[offset + l + 1]);
					thirdByte = (0x000000FF & frameOfBytes[offset + l + 2]);
					fourthByte = (0x000000FF & frameOfBytes[offset + l + 3]);

					twoDData[j][l / 4] = ((firstByte << 24 | secondByte << 16 | thirdByte << 8 | fourthByte)) & 0xFFFFFFFFL;
				}
				offset += mcaGrades * mcaChannels * 4;
			}

			// In the new firmware thw data format is changed to
			// resgrades * detector element, 4096,frame
			/*
			 * int resOffset = 0; for(int k =1 ; k <= mcaGrades ; k++) { for (int j = 0; j < numberOfDetectors; j++) {
			 * for (int l = 0; l < (mcaChannels * 4); l += 4) { firstByte = (0x000000FF & frameOfBytes[offset + l + 0]);
			 * secondByte = (0x000000FF & frameOfBytes[offset + l + 1]); thirdByte = (0x000000FF & frameOfBytes[offset +
			 * l + 2]); fourthByte = (0x000000FF & frameOfBytes[offset + l + 3]); twoDData[j][(l / 4 +resOffset) ] =
			 * ((firstByte << 24 | secondByte << 16 | thirdByte << 8 | fourthByte)) & 0xFFFFFFFFL; } offset +=
			 * mcaChannels * 4; } resOffset = (k* mcaChannels * 4); }
			 */
			
		} catch (IOException ioe) {
			// System.out.println("EOF");
			ioe.printStackTrace();
			twoDData = null;

		}
		finally
		{
			try {
				if(dis != null)
					dis.close();
			} catch (IOException e) {
				logger.error("Unable to close the data stream " , e);
				dis = null;
			}
			
		}
		//logger.debug("the data length is " + twoDData);
		return twoDData;
	}

	
	/**
	 * Gets the data for all detector elements for a particular frame. Synchronized because the long and byte arrays
	 * used are static (because they are very big and allocating them was taking measurable time).
	 * 
	 * @param filename
	 * @return the data for all frames
	 * 
	 */
	public  static synchronized long[][][] interpretAllFrames(String filename, int numberOfFrames) {

		int firstByte, secondByte, thirdByte, fourthByte;
		DataInputStream dis = null;
		long[][][] threeDData = null;
		//logger.info("file name is " + filename);
		try {
				threeDData = new long[numberOfFrames][numberOfDetectors][mcaGrades * mcaChannels];
			
				FileInputStream fis = new FileInputStream(filename);
				dis = new DataInputStream(fis);			

			// Array bytes should now contain the required data, we need to
			// construct longs from sets of four bytes.
			
			
			for(int k =0 ; k < numberOfFrames ; k++){
				// Read the required frame
				
				int bytesRead = dis.read(frameOfBytes, 0 , numberOfDetectors * mcaGrades * mcaChannels * 4);
				int offset = 0;
				logger.info("bytes read is " + bytesRead + " " + k);
			for (int j = 0; j < numberOfDetectors; j++) {
				for (int l = 0; l < (mcaGrades * mcaChannels * 4); l += 4) {
					firstByte = (0x000000FF & frameOfBytes[offset + l + 0]);
					secondByte = (0x000000FF & frameOfBytes[offset + l + 1]);
					thirdByte = (0x000000FF & frameOfBytes[offset + l + 2]);
					fourthByte = (0x000000FF & frameOfBytes[offset + l + 3]);

					threeDData[k][j][l / 4] = ((firstByte << 24 | secondByte << 16 | thirdByte << 8 | fourthByte)) & 0xFFFFFFFFL;
				}
				offset += mcaGrades * mcaChannels * 4;
			}
			}

			
			
		} catch (IOException ioe) {
			// System.out.println("EOF");
			ioe.printStackTrace();
			threeDData = null;

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try {
				if(dis != null)
					dis.close();
			} catch (IOException e) {
				logger.error("Unable to close the data stream " , e);
				dis = null;
			}
			
		}
		//logger.debug("the data length is " + twoDData);
		return threeDData;
	}
	/**
	 * @param filename
	 * @param frameNumber
	 * @return array containing the scaler data from file filename.
	 */
	public static synchronized long[][] interpretScalerFile(String filename, int frameNumber) {

		int firstByte, secondByte, thirdByte, fourthByte;
		DataInputStream dis = null;
		try {
			if(scalerData == null)
				scalerData = new long[scalerFields][numberOfDetectors];
			dis = new DataInputStream(new FileInputStream(filename));

			// Skip to the frame
			dis.skipBytes(frameNumber * numberOfDetectors * scalerFields * 4);

			// Read the required frame
			dis.read(scalerBytes, 0, numberOfDetectors * scalerFields * 4);

			// Array bytes should now contain the required data, we need to
			// construct longs from sets of four bytes.

			/*
			 * int offset = 0; for (int j = 0; j < numberOfDetectors; j++) { for (int l = 0; l < (scalerFields * 4); l +=
			 * 4) { firstByte = (0x000000FF & scalerBytes[offset + l + 0]); secondByte = (0x000000FF &
			 * scalerBytes[offset + l + 1]); thirdByte = (0x000000FF & scalerBytes[offset + l + 2]); fourthByte =
			 * (0x000000FF & scalerBytes[offset + l + 3]); scalerData[j][l / 4] = ((firstByte << 24 | secondByte << 16 |
			 * thirdByte << 8 | fourthByte)) & 0xFFFFFFFFL; } offset += scalerFields * 4; }
			 */
			int offset = 0;
			for (int j = 0; j < numberOfDetectors; j++) {
				for (int l = 0; l < (scalerFields * 4); l += 4) {
					firstByte = (0x000000FF & scalerBytes[offset + l + 0]);
					secondByte = (0x000000FF & scalerBytes[offset + l + 1]);
					thirdByte = (0x000000FF & scalerBytes[offset + l + 2]);
					fourthByte = (0x000000FF & scalerBytes[offset + l + 3]);

					/*
					 * scalerData[l/4][j] = ((firstByte << 24 | secondByte << 16 | thirdByte << 8 | fourthByte)) &
					 * 0xFFFFFFFFL;
					 */
					scalerData[l / 4][j] = ((firstByte | secondByte << 8 | thirdByte << 16 | fourthByte << 24));
					// System.out.println("scaler data " + l/4 + " " + j +" " + scalerData[l/4][j] + " " );
				}
				offset += scalerFields * 4;
			}
			
		} catch (IOException ioe) {
			ioe.printStackTrace();
			scalerData = null;
		} catch (Exception o) {
			o.printStackTrace();
		}
		finally
		{
			try {
				if(dis != null)
					dis.close();
			} catch (IOException e) {
				logger.error("Unable to close the data stream " , e);
				dis = null;
			}
			
		}

		return scalerData;
	}

	/**
	 * Gets the scaler data for a single detector element for a particular frame. This data consists of the total
	 * counts, no. of resets and windowed counts.
	 * 
	 * @param filename
	 * @param frameNumber -
	 *            counts from zero.
	 * @param detectorNumber -
	 *            counts from zero.
	 * @return the scaler data for detector detectorNumber from frame frameNumber
	 */
	public static long[] interpretScalerFile(String filename, int frameNumber, int detectorNumber) {
		long[] data = new long[scalerFields];
		DataInputStream dis = null;
		try {
			
			dis = new DataInputStream(new FileInputStream(filename));
			byte[] bytes = new byte[scalerFields * 4];
			int firstByte, secondByte, thirdByte, fourthByte;

			// Skip to the frame we want
			dis.skipBytes(frameNumber * numberOfDetectors * scalerFields * 4);
			// Skip to the detector we want
			dis.skipBytes(detectorNumber * scalerFields * 4);
			// read data
			dis.read(bytes, 0, scalerFields * 4);

			for (int l = 0; l < (scalerFields * 4); l += 4) {
				firstByte = (0x000000FF & bytes[l + 0]);
				secondByte = (0x000000FF & bytes[l + 1]);
				thirdByte = (0x000000FF & bytes[l + 2]);
				fourthByte = (0x000000FF & bytes[l + 3]);

				data[l / 4] = ((firstByte << 24 | secondByte << 16 | thirdByte << 8 | fourthByte)) & 0xFFFFFFFFL;
			}
			
		} catch (IOException e) {
			data = null;
		}
		finally
		{
			try {
				if(dis != null)
					dis.close();
			} catch (IOException e) {
				logger.error("Unable to close the data stream " , e);
				dis = null;
			}
			
		}

		return data;
	}

	/**
	 * @param scalarFile
	 * @param liveTime
	 * @return deadtime corrected double[]
	 */
	public static double[] deadTimeCorrectWindows(String scalarFile, double liveTime) {
		// now read scalar data
		//
		long[][] scalarData = interpretScalerFile(scalarFile, 0);
		return deadTimeCorrectWindows(scalarData, liveTime);
		/*
		 * double []corrCounts=getCorrectedCounts(scalarData[0],scalarData[1],liveTime); long[]
		 * measuredCounts=scalarData[0]; //windowed values are stored in the scaler long [] windowedData= scalarData[2];
		 * double [] relinCounts=reLinearizeCounts(corrCounts); double factor[] = new double[relinCounts.length]; double
		 * val =0.0; for (int j =0; j < relinCounts.length;j++) { if(relinCounts[j]==0 || measuredCounts[j]==0) {
		 * factor[j]= 1.0; } else{ val = (relinCounts[j]/measuredCounts[j]); factor[j] = (val*liveTime); } } double
		 * []correctWindows=new double[windowedData.length]; double corrWindTot=0.0; for (int k =0; k<
		 * numberOfDetectors; k++) { double correctW=windowedData[k]*factor[k]; corrWindTot=corrWindTot+correctW;
		 * correctWindows[k] = (correctW); } windowTotal = corrWindTot; return correctWindows;
		 */
	}

	/**
	 * Deadtime correction for Xspress2 new version with resettime and parametric correction
	 * @param scalerValues 2d array of scaler counts ,resets and window values for all the detector elements
	 * @param livetime should be in seconds / 12.5e-9
	 * @return corrected windows double[]
	 */
	public static double[] deadTimeCorrectWindows2(long scalerValues[][] , double livetime)
	{
		double correctedCounts = 0.0;
		double factor = 0.0;
		long[] measuredCounts = scalerValues[0];
		// windowed values are stored in the scaler
		long resets[] = scalerValues[1];
		long[] windowedData = scalerValues[2];
		double[] windowCorrectRate1 = new double[measuredCounts.length];
		double[] windowCorrectRate2 = new double[measuredCounts.length];
		for(int i =0 ; i< measuredCounts.length; i++)
		{
			correctedCounts = getResetTimeCorrectedCountRate(measuredCounts[i],resets[i],livetime);
			correctedCounts=parametricCorrection(correctedCounts,process_timesv2[i]);
		    factor=correctedCounts*livetime*12.5e-9/(measuredCounts[i]);
			double windowRate=getResetTimeCorrectedCountRate(windowedData[i],resets[i],livetime);
			double windowcr=windowRate/Math.exp(-correctedCounts * 2.0 * window_times[i]);		
			windowCorrectRate1[i] = (windowcr*livetime*12.5e-9);
			windowCorrectRate2[i] = (factor*windowedData[i]);
		}
		double corrWindowTotal = 0.0;
		for (int k =0; k< windowedData.length; k++)
		{
			corrWindowTotal = corrWindowTotal + windowCorrectRate1[k];
		}
		windowTotal = corrWindowTotal;
		return windowCorrectRate1;
	}
	private static double parametricCorrection(double counts, double processTime) {
		double maxOut = 1.0/processTime * Math.exp(-1.0);
		double input=0.0;
		if (counts < 0){
			input = 0.0;}
		else if (counts > maxOut){
			input= 1.0/processTime;
		}
		else{
			//Gives 50 bit absolute precision, but note low count rate
			double div =0.0;
			double tryIn =0.0;
			for (int i = 0; i<50 ;i++)
			{
				div = 1 << i;
				tryIn = input + 1.0 / (div *processTime);
				if (counts >= tryIn * Math.exp(-tryIn * processTime))
					input = tryIn;
			}
		}
		return input;
	
	}

	private static double getResetTimeCorrectedCountRate(long counts, long noOfResets,
			double livetime) {
		double realCounts= counts/((livetime-noOfResets)*12.5e-9);
		return realCounts;

	}

	/**
	 * @param scalarFileValues
	 * @param liveTime
	 * @return deadtime corrected double[]
	 */
	public static double[] deadTimeCorrectWindows(long scalarFileValues[][], double liveTime) {
		// now read scalar data
		//
		double[] corrCounts = getCorrectedCounts(scalarFileValues[0], scalarFileValues[1], liveTime);
		long[] measuredCounts = scalarFileValues[0];
		// windowed values are stored in the scaler
		long[] windowedData = scalarFileValues[2];
		double[] relinCounts = reLinearizeCounts(corrCounts);
		double factor[] = new double[relinCounts.length];
		double val = 0.0;
		for (int j = 0; j < relinCounts.length; j++) {
			if (relinCounts[j] == 0 || measuredCounts[j] == 0) {
				factor[j] = 1.0;
			} else {
				val = (relinCounts[j] / measuredCounts[j]);
				factor[j] = (val * liveTime);
			}
		}
		double[] correctWindows = new double[windowedData.length];
		double corrWindTot = 0.0;
		for (int k = 0; k < numberOfDetectors; k++) {
			double correctW = windowedData[k] * factor[k];
			corrWindTot = corrWindTot + correctW;
			correctWindows[k] = (correctW);
		}
		windowTotal = corrWindTot;
		return correctWindows;
	}

	/**
	 * @param countData
	 * @param resetData
	 * @param liveTime
	 * @return correctedcounts double[]
	 */
	public static double[] getCorrectedCounts(long[] countData, long[] resetData, double liveTime) {
		double[] realCounts = new double[countData.length];
		for (int i = 0; i < countData.length; i++) {
			realCounts[i] = countData[i] * (1.0 / (liveTime - ((12.5e-9) * resetData[i])));
		}
		return realCounts;
	}

	// ==================================================
	// Get corrected counts
	// ==================================================
	/**
	 * @param corrCounts
	 * @return double[]
	 */
	public static double[] reLinearizeCounts(double[] corrCounts) {
		// if(process_times == null || process_times.length !=numberOfDetectors)
		// process_times[]= {3.4e-07,3.8e-07,3.7e-07,3.0e-07,3.4e-07,3.5e-07,3.3e-07,3.0e-07,3.3e-07};
		double[] realCounts = new double[numberOfDetectors];
		double a, aa, eqn1, eqn2 = 0.0;
		for (int i = 0; i < numberOfDetectors; i++) {
			a = process_times[i] * corrCounts[i];
			aa = a * a;
			eqn1 = Math.pow((-10.0 + 27.0 * a + 5.196152423 * Math.sqrt(4 - 20.0 * a + 27.0 * aa)), (1.0 / 3.0));
			eqn2 = (1.0 / (3.0 * process_times[i]));
			realCounts[i] = eqn2 * eqn1 - 2 * eqn2 / eqn1 + 2 * eqn2;

		}
		return realCounts;
	}

	/**
	 * @param args
	 */
	public static void main(String args[]) {
		// long frame[][] =
		// Xspress2Utilities.interpretDataFile("ftp://"+"i18user:m1cr0f0cus!@i18-ftp//exports/data/2008/sp0/mca/21314/21314_scan_1_index_0.dat",
		// 0);
		// long value[][] =
		// Xspress2Utilities.interpretScalerFile("/dls/i18/data/2008/sp0/mca/27444/27444_scan_1_index_0_scalar.dat", 0);
		//long[][] scalarData = interpretScalerFile("/dls/i18/data/2010/sp1833-1/mca/41702/41702_scan_1_index_1_scalar.dat", 0);
		//double[] s = Xspress2Utilities.deadTimeCorrectWindows2(scalarData, 2002.0/(1000.0*12.5e-9));
		//for (int i = 0; i < s.length; i++)
		//	System.out.print(s[i] + "  ");
		//System.out.println("the total is " + Xspress2Utilities.getWindowTotal());
		
		/*long result[][] = Xspress2Utilities.interpretScalerFile("/dls/i18/data/2009/sp912-1/mca/34143/34143_scan_1_index_0_scalar.dat", 0);
		for(int j = 0; j < result.length; j++)
		{
		for (int i = 0; i < result[j].length; i++)
			System.out.print(result[j][i] + "  ");
		System.out.println();
		}*/
		long data[][][] =  Xspress2Utilities.interpretAllFrames("/dls/i18/data/2010/cm1946-2/tmp/mca/52949/row0/52949_yindex_0.xsp",10);
		System.out.println("the first point is " + data[0][0][0]);
	}

	/**
	 * @param process_times
	 */
	public static void setProcess_times(double[] process_times) {
		Xspress2Utilities.process_times = process_times;
	}

	/**
	 * @return windowtotal
	 */
	public static double getWindowTotal() {
		return windowTotal;
	}
}
