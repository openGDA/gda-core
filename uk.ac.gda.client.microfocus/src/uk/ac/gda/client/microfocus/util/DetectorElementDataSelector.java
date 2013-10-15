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

package uk.ac.gda.client.microfocus.util;

import gda.analysis.io.ScanFileHolderException;
import gda.data.nexus.GdaNexusFile;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.nexusformat.NXlink;
import org.nexusformat.NeXusFileInterface;
import org.nexusformat.NexusException;
import org.nexusformat.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.NexusLoader;

public class DetectorElementDataSelector {
	private INexusTree mainTree;
	protected INexusTree detectorNode;
	private String detectorName = "xspress2system";

	public String getDetectorName() {
		return detectorName;
	}

	public void setDetectorName(String detectorName) {
		this.detectorName = detectorName;
	}

	private double[] data;
	private static final Logger logger = LoggerFactory.getLogger(DetectorElementDataSelector.class);
	private int[] selectedElements = { 0, 1, 3, 4, 5, 6, 8 };

	/**
	 * Load a MicroFocus Nexus file and read in the x and y axis values
	 * 
	 * @param FileName
	 */
	public void loadData(String FileName) {
		NexusLoader nl = new NexusLoader(FileName, true);

		try {
			INexusTree tree = nl.loadTree(null);
			System.out.println("loaded the nexus file");

			mainTree = tree.getChildNode("entry1", "NXentry");

		} catch (ScanFileHolderException e) {
			logger.error("Error Reading the Nexus file", e);
		} catch (Exception ed) {
			logger.error("Error Reading the Nexus file", ed);
		}

	}

	/**
	 * Get the data for the detector Element from the loaded data
	 */
	public double[][][] getElementData(int detectorNo) {

		int[] intData = null;
		INexusTree tmptree = mainTree.getChildNode("instrument", "NXinstrument");
		detectorNode = tmptree.getChildNode(detectorName, "NXdetector");
		// mainTree.
		NexusGroupData ngdata = null;
		if (detectorNode.getChildNode("MCAs", "SDS") != null) {
			double[][][][] dataret = null;
			ngdata = detectorNode.getChildNode("MCAs", "SDS").getData();
			data = (double[]) ngdata.getBuffer();
			if (ngdata.dimensions.length == 4) {
				dataret = packto4D(data, ngdata.dimensions[0], ngdata.dimensions[1], ngdata.dimensions[2],
						ngdata.dimensions[3]);

			} else
				logger.error("incorrect " + detectorName + " data");
			if (dataret != null)
				return dataret[detectorNo];
			return null;
		}
		// must be a xmap file

		double[][][] dataret = null;
		ngdata = detectorNode.getChildNode("Element" + detectorNo + "_fullSpectrum", "SDS").getData();
		intData = (int[]) ngdata.releaseData();
		if (ngdata.dimensions.length == 3) {
			dataret = packto4D(intData, ngdata.dimensions[0], ngdata.dimensions[1], ngdata.dimensions[2]);
		} else
			logger.error("incorrect " + detectorName + " data");
		return dataret;

	}

	private double[][][] packto4D(int[] d1, int ny, int nx, int mcasize) {
		double[][][] ret = new double[ny][nx][mcasize];
		int index = 0;
		for (int i = 0; i < ny; i++) {
			for (int j = 0; j < nx; j++) {
				for (int k = 0; k < mcasize; k++) {
					ret[i][j][k] = d1[index];
					index++;
				}
			}
		}
		return ret;
	}

	private double[][][][] packto4D(double[] d1, int ny, int nx, int noOfDetElements, int mcasize) {
		double[][][][] ret = new double[noOfDetElements][ny][nx][mcasize];
		int index = 0;
		for (int i = 0; i < ny; i++) {
			for (int j = 0; j < nx; j++) {
				for (int l = 0; l < noOfDetElements; l++) {
					for (int k = 0; k < mcasize; k++) {
						ret[l][i][j][k] = d1[index];
						index++;
					}
				}
			}
		}
		return ret;
	}

	public void editNexusFile(String fileName, String outputFileDir, int... selElements) {
		boolean dataOpen = false;
		loadData(fileName);
		this.selectedElements = selElements;
		double sumData[][][] = null;
		for (int i = 0; i < selectedElements.length; i++) {
			double elementData[][][] = getElementData(selectedElements[i]);
			if (sumData == null) {
				sumData = elementData;
			} else {
				for (int j = 0; j < sumData.length; j++) {
					for (int k = 0; k < sumData[j].length; k++) {
						for (int l = 0; l < sumData[j][k].length; l++) {
							sumData[j][k][l] += elementData[j][k][l];
						}

					}
				}
			}
		}
		// write the sumData back in to the nexus file
		NeXusFileInterface file = null;
		NXlink link = null;
		try {

			if (sumData != null) {
				// fileName = fileName.replace(".nxs", "_1.nxs");
				String newFileName = fileCopy(fileName, outputFileDir);
				file = new GdaNexusFile(newFileName, NexusFile.NXACC_RDWR);
				file.opengroup("entry1", "NXentry");
				file.opengroup("instrument", "NXinstrument");
				file.opengroup(detectorName, "NXdetector");
				file.makedata("selectedElementSum", NexusFile.NX_FLOAT64, 3, new int[] { sumData.length,
						sumData[0].length, sumData[0][0].length });
				file.opendata("selectedElementSum");
				file.putslab(sumData, new int[] { 0, 0, 0 }, new int[] { sumData.length, sumData[0].length,
						sumData[0][0].length });
				link = file.getdataID();
				dataOpen = true;
			}

		} catch (NexusException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (file != null) {
				try {
					if (dataOpen)
						file.closedata();
					file.closegroup();
					file.closegroup();
					// make the link in default level
					if (link != null) {
						file.opengroup(detectorName, "NXdata");
						file.makelink(link);
						file.closegroup();
					}
					file.closegroup();
					file.close();
				} catch (NexusException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

	}

	private String fileCopy(String input, String outputDir) {
		
		String outFileName = input.replace(".nxs", "_1.nxs");
		String outFileFullPath = outputDir + File.separator + outFileName;
		
		try (FileInputStream srcStream = new FileInputStream(input);
				FileOutputStream dstStream = new FileOutputStream(outFileFullPath)) {
			dstStream.getChannel().transferFrom(srcStream.getChannel(), 0, srcStream.getChannel().size());
			return outFileFullPath;
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}

	public static void main(String[] args) {
		DetectorElementDataSelector selector = new DetectorElementDataSelector();
		selector.editNexusFile("/dls/i18/data/2011/cm2065-2/Experiment_1/nexus/FeKedge_1_29.nxs",
				"/scratch/users/data/2010/sp1-1/processing/", new int[] { 2, 3, 5, 6, 8 });
	}

}