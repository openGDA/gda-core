package uk.ac.gda.devices.detector.xspress3.fullCalculations;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusExtractorException;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusSourceProvider;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeBuilder;
import gda.data.nexus.tree.NexusTreeNodeSelection;

import org.nexusformat.NexusException;

public class Xspress3FileReader {

	private static final String DATA_PATH = "entry/instrument/detector/data";
	private String url;
	private int numberOfDetectorElements;
	private int mcaSize;
	private double[][][] theData = null; // [frame][element][mcaChannel]

	public Xspress3FileReader(String filename, int numberOfDetectorElements, int mcaSize) throws NexusException {
		this.url = filename;
		this.numberOfDetectorElements = numberOfDetectorElements;
		this.mcaSize = mcaSize;
	}

	public void readFile() throws NexusException, NexusExtractorException {
		if (theData == null) {
			fillDataBuffer();
		}
	}

	/*
	 * Reads the whole row (whole file) into memory
	 * <p>
	 * 
	 * @throws NexusException
	 * @throws NexusExtractorException
	 */
	private void fillDataBuffer() throws NexusException, NexusExtractorException {
		// data is frame x numberOfDetectorElements x mcaSize

		INexusTree tree = NexusTreeBuilder.getNexusTree(url, NexusTreeNodeSelection.createTreeForAllNXData());
		INexusTree node = tree.getNode(DATA_PATH);
		NexusGroupData nexusGroupData = NexusExtractor.getNexusGroupData(((INexusSourceProvider) tree).getSource(),
				node.getNodePathWithClasses(), null, null, true);

		double[] buffer = (double[]) nexusGroupData.getBuffer();

		int numFrames = nexusGroupData.dimensions[0];
		theData = new double[numFrames][numberOfDetectorElements][mcaSize];

		int index = 0;
		for (int frame = 0; frame < numFrames; frame++) {
			for (int element = 0; element < numberOfDetectorElements; element++) {
				for (int mcaChannel = 0; mcaChannel < mcaSize; mcaChannel++) {
					theData[frame][element][mcaChannel] = buffer[index];
					index++;
				}
			}
		}
	}

	/**
	 * Assumes {@link #readFile()} has been called and returned normally.
	 * 
	 * @param frameNumber
	 * @return
	 */
	public double[][] getFrame(int frameNumber) {
		return theData[frameNumber];
	}

}
