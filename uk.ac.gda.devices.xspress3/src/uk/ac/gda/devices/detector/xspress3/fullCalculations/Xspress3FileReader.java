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
	private double[][][] theData = null;

	public Xspress3FileReader(String filename, int numberOfDetectorElements, int mcaSize) throws NexusException {
		this.url = filename;
		this.numberOfDetectorElements = numberOfDetectorElements;
		this.mcaSize = mcaSize;
	}

	public double[][][] readFrames(int firstFrame, int lastFrame) throws NexusException, NexusExtractorException {

		if (theData == null) {
			fillDataBuffer();
		}

		int numFrames = lastFrame - firstFrame + 1;
		double[][][] data = new double[numFrames][numberOfDetectorElements][mcaSize];
		int index = 0;
		for (int frame = firstFrame; frame <= lastFrame; frame++) {
			data[index] = theData[frame];
			index++;
		}

		return data;
	}

	private void fillDataBuffer() throws NexusException, NexusExtractorException {
		// data is frame x numberOfDetectorElements x mcaSize

		INexusTree tree = NexusTreeBuilder.getNexusTree(url, NexusTreeNodeSelection.createTreeForAllNXData());
		INexusTree node = tree.getNode(DATA_PATH);
		// INexusTree node =
		// tree.getChildNode(0).getChildNode(0).getChildNode(1).getChildNode(1);
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

}
