package uk.ac.diamond.daq.experiment.plan;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.ServiceHolder;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SEVNexusFile {
	
	private TreeFile treeFile;
	private NexusFile nexusFile;
	private String scannableName;
	private Map<String, ILazyWriteableDataset> datasets;
	
	private static final String POSITION_DATABASE_NAME = "Position";
	private static final String TIMESTAMP_DATABASE_NAME = "Timestamp";
	
	private static final Logger logger = LoggerFactory.getLogger(SEVNexusFile.class);
	
	public SEVNexusFile(String scannableName) {
		this.datasets = new HashMap<>();		
		this.scannableName = scannableName;
	}
	
	public void createNexusFile(String filePath) {
		treeFile = NexusNodeFactory.createTreeFile(filePath);
		NXroot root = NexusNodeFactory.createNXroot();
		treeFile.setGroupNode(root);
		
		NXentry entry = NexusNodeFactory.createNXentry();
		root.setEntry(entry);
		
		NXdata dataGroup = NexusNodeFactory.createNXdata();
		entry.setData(scannableName, dataGroup);
		
		datasets.put(POSITION_DATABASE_NAME, dataGroup.initializeLazyDataset(POSITION_DATABASE_NAME, 1, Double.class));
		datasets.put(TIMESTAMP_DATABASE_NAME, dataGroup.initializeLazyDataset(TIMESTAMP_DATABASE_NAME, 1, String.class));
		
		saveNexusFile();
	}
	
	public void writeData(double position, String timestamp) {
		try {
			writeDemandData(POSITION_DATABASE_NAME, position);
			writeDemandData(TIMESTAMP_DATABASE_NAME, timestamp);
		} catch (DatasetException e) {
			e.printStackTrace();
		}
	}
	
	private void writeDemandData(String databaseName, Object data) throws DatasetException {
		ILazyWriteableDataset dataset = datasets.get(databaseName);
		int index = dataset.getSize();
		final int[] startPos = new int[] { index };
		final int[] stopPos = new int[] { index + 1 };
		dataset.setSlice(null, DatasetFactory.createFromObject(data), startPos, stopPos, stopPos);
	}
	
	public void closeNexusFile() {
		try {
			nexusFile.close();
		} catch(NexusException e) {
			logger.error("Can't close Nexus file", e);
		}
	}
	
	private void saveNexusFile() {
		INexusFileFactory nff = ServiceHolder.getNexusFileFactory();
		nexusFile = nff.newNexusFile(treeFile.getFilename(), true);
		try {
			nexusFile.createAndOpenToWrite();
			nexusFile.addNode("/", treeFile.getGroupNode());
			nexusFile.flush();
		} catch(NexusException e) {
			logger.error("Can't create Nexus file", e);
		}
		
	}
}