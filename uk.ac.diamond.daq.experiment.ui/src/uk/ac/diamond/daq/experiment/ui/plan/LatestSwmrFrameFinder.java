package uk.ac.diamond.daq.experiment.ui.plan;

import static java.util.stream.Collectors.toMap;
import static org.eclipse.dawnsci.analysis.api.tree.TreeUtils.getPath;
import static org.eclipse.dawnsci.analysis.api.tree.TreeUtils.treeBreadthFirstSearch;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.ILiveLoadedFileListener;
import org.dawnsci.datavis.model.IRefreshable;
import org.dawnsci.datavis.model.LoadedFile;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.IndexIterator;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.dataset.SliceND;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.RateLimiter;

/**
 * Listens for SWMR files from ongoing scans and finds their latest detector frame.
 * <p>
 * When a file is registered, the available detectors are given to the consumer
 * passed in the constructor. The caller should then select one of these via {@link #selectDetector(String)}
 */
public class LatestSwmrFrameFinder implements ILiveLoadedFileListener {
	
	private static final Logger logger = LoggerFactory.getLogger(LatestSwmrFrameFinder.class);
	
	/** We notify this consumer of available detectors when a file is loaded */
	private final Consumer<Set<String>> detectorsSubscriber;
	
	/** We pass the latest frame to this processor */
	private final Consumer<IDataset> frameProcessor;
	
	private final RateLimiter rateLimiter;
	private Optional<IRefreshable> refreshableOptional;

	/** Detector name to dataset path */
	private Map<String, String> detectorDatasets;
	
	/** Detector name to associated unique keys path */
	private Map<String, String> uniqueKeysDatasets;
	
	/** Name of detector to get the latest frame from */
	private String requestedDetector;
	
	/**
	 * Constructs an {@link ILiveLoadedFileListener} which finds the latest detector frame
	 * in the SWMR file of the ongoing scan, rate-limited at the given frequency, and passes
	 * the frame to the given frameProcessor.
	 * 
	 * @param detectorsSubscriber This consumer is passed a list of detectors available
	 * 			when a new SWMR file is loaded. The caller can choose any of these ({@link #selectDetector(String)}
	 * 
	 * @param frameProcessor something which works on the latest frame IDataset
	 * 
	 * @param updateRateFrequency in Hz
	 */
	public LatestSwmrFrameFinder(Consumer<Set<String>> detectorsSubscriber, Consumer<IDataset> frameProcessor, double updateRateFrequency) {
		this.detectorsSubscriber = detectorsSubscriber;
		this.frameProcessor = frameProcessor;
		this.rateLimiter = RateLimiter.create(updateRateFrequency);
		refreshableOptional = Optional.empty();
	}

	/**
	 * Clients choose which detector we should find the latest frame of.
	 * The argument should be one in the list given to the detectors subscriber passed in the constructor.
	 */
	public void selectDetector(String requestedDetector) {
		logger.debug("Detector requested = '{}'", requestedDetector);
		this.requestedDetector = requestedDetector;
		refreshableOptional.ifPresent(this::refresh);
	}
	
	/**
	 * This class operates only on SWMR files, denoted by being instances of IRefreshable.
	 * When one of these files is given to this method*, it is cached and from its tree
	 * we also cache links to detector and unique keys datasets.
	 * <p>
	 * *This is most likely since the method is specified by {@link ILiveLoadedFileListener}
	 * which deals with SWMR files.
	 */
	@Override
	public void fileLoaded(LoadedFile loadedFile) {
		if (loadedFile instanceof IRefreshable) {
			IRefreshable swmr = (IRefreshable) loadedFile;
			refreshableOptional = Optional.of(swmr);
			NexusTreeDatasetFinder datasetFinder = new NexusTreeDatasetFinder(loadedFile.getTree());
			detectorDatasets = datasetFinder.getDetectorDatasets();
			uniqueKeysDatasets = datasetFinder.getUniqueKeysDatasets(detectorDatasets.keySet());
			detectorsSubscriber.accept(detectorDatasets.keySet());
		} else {
			logger.debug("Was passed a non-SWMR file; will ignore");
		}
	}
	
	@Override
	public void refreshRequest() {
		if (rateLimiter.tryAcquire()) {
			refreshableOptional.ifPresent(this::refresh);
		}
	}
	
	@Override
	public void localReload(String path, boolean force) {
		/* The last frame is written by this point,
		 * so force a refresh without rate limiting */
		refreshableOptional.ifPresent(this::refresh);
	}
	
	private void refresh(IRefreshable file) {
		if (requestedDetector == null) {
			// no one is interested in the data just yet,
			// come back later
			return;
		}
		
		file.refresh();
		
		try {
			findLatestFrame(file).ifPresent(frameProcessor);
		} catch (NoSuchElementException e) {
			logger.warn("Could not refresh data", e);
		}
	}
	
	private Optional<IDataset> findLatestFrame(IRefreshable file) {
		List<DataOptions> dataOptions = file.getUninitialisedDataOptions();
		
		DataOptions data = getDetectorData(dataOptions);
		DataOptions keys = getKeys(dataOptions);
		
		try {
			
			int[] positionOfLastFrame = getPositionOfLastFrame(keys);
			if (positionOfLastFrame == null) {
				logger.debug("No data written yet");
				return Optional.empty();
			}
			
			ILazyDataset detectorData = data.getLazyDataset();
			SliceND slice = getSlice(detectorData, positionOfLastFrame);			
			return Optional.of(detectorData.getSlice(slice).squeeze());

		} catch (DatasetException e) {
			logger.error("Error displaying newer detector frame", e);
			return Optional.empty();
		}
	}
	
	private DataOptions getDetectorData(List<DataOptions> dataOptions) {
		return dataOptions.stream()
				.filter(options -> options.getName().equals(detectorDatasets.get(requestedDetector)))
				.findFirst().orElseThrow(() -> new NoSuchElementException("Could not find requested dataset '" + requestedDetector + "'"));
	}
	
	private DataOptions getKeys(List<DataOptions> dataOptions) {
		return dataOptions.stream()
				.filter(options -> options.getName().equals(uniqueKeysDatasets.get(requestedDetector)))
				.findFirst().orElseThrow(() -> new NoSuchElementException("Cannot find unique keys to match dataset '" + requestedDetector + "'"));
	}

	/**
	 * Returns the slice corresponding to the last detector frame
	 */
	private SliceND getSlice(ILazyDataset detectorData, int[] positionOfLastFrame) {
		int[] shape = detectorData.getShape();
		final SliceND slice = new SliceND(shape);
		
		int dataRank = detectorData.getRank();
		int positionRank = positionOfLastFrame.length;
		int finalDimension = dataRank == positionRank
				? positionRank - 2 	// Malcolm scan
				: positionRank; 	// GDA scan
		
		for (int dimension = 0; dimension < finalDimension; dimension++) {
			slice.setSlice(dimension,
					new Slice(positionOfLastFrame[dimension],
					positionOfLastFrame[dimension]+1));
		}
		
 		return slice;
	}

	/**
	 * Finds position of latest frame by searching for the latest non-zero unique key.
	 * Returns {@code null} if no non-zero unique keys are found
	 */
	private int[] getPositionOfLastFrame(DataOptions uniqueKeysDataOptions) throws DatasetException {
		Dataset uniqueKeys = DatasetUtils.convertToDataset(uniqueKeysDataOptions.getLazyDataset().getSlice());
		
		IndexIterator iterator = uniqueKeys.getIterator(true);
		
		int[] positionOfLastFrame = null;
		boolean zeroFound = false;
		
		
		/* 
		 * The position of the latest frame is given
		 * by the position of the latest non-zero unique key.
		 * 
		 * We cannot tell whether this is an alternating acquisition,
		 * so we have to iterate through the entire dataset, even if we find zeros!
		 * 
		 * The exception is if we find a non-zero key after finding a zero,
		 * in which case this is definitely a alternating acquisition,
		 * and that first non-zero key following the zero indicates the position of the latest frame.
		 */
		while (iterator.hasNext()) {
			
			long key = uniqueKeys.getElementLongAbs(iterator.index);
			if (key == 0) {
				zeroFound = true;
			} else {
				positionOfLastFrame = iterator.getPos().clone();
				if (zeroFound) break;
			}
		}
		
		return positionOfLastFrame;
	}
	
	
	/**
	 * Traverses a NeXus tree to find detector datasets and their associated unique keys
	 */
	private class NexusTreeDatasetFinder {
		
		private static final String KEYS_GROUP_NAME = "keys";
		private Tree tree;
		
		public NexusTreeDatasetFinder(Tree tree) {
			this.tree = tree;
		}
		
		/**
		 * Returns a map of detector name to dataset path
		 */
		public Map<String, String> getDetectorDatasets() {
			return treeBreadthFirstSearch(tree.getGroupNode(), this::nodeIsDetector, false, null).values().stream()
					.collect(toMap(NodeLink::getName, node -> getPath(tree, 
							((GroupNode) node.getDestination()).getNode(NexusConstants.DATA_DATA))));
		}
		
		/**
		 * Returns a map of detector name to unique keys dataset path
		 */
		public Map<String, String> getUniqueKeysDatasets(Set<String> detectorNames) {
			Map<String, NodeLink> result = treeBreadthFirstSearch(tree.getGroupNode(), this::nodeIsUniqueKeysGroup, true, null);
			GroupNode keysGroup = (GroupNode) result.values().iterator().next().getDestination();
			
			Map<String, DataNode> keyNodes = keysGroup.getDataNodeMap();
			
			if (keysGroup.getNumberOfDataNodes() == 1) {
				// GDA scan with any number of detectors, or Malcolm scan with single detector
				return detectorNames.stream().collect(toMap(det -> det, det -> getPath(tree, keyNodes.values().iterator().next())));
			} else {
				// Malcolm scan: one unique keys dataset per detector
				return detectorNames.stream().collect(toMap(det -> det,
					det -> keyNodes.entrySet().stream()
						.filter(entry -> entry.getKey().contains(det))
						.map(Entry<String, DataNode>::getValue)
						.map(node -> getPath(tree, node))
						.findFirst().orElseThrow(() -> new NoSuchElementException("Cannot find unique keys for detector '" + det + "'"))
					));
			}
		}
		
		private boolean nodeIsDetector(NodeLink node) {
			return node.isDestinationGroup() // node is a group
					&& ((GroupNode) node.getDestination()).getAttribute(NexusConstants.NXCLASS)
						.getFirstElement().equals(NexusConstants.DETECTOR); // and an NXdetector
		}
		
		private boolean nodeIsUniqueKeysGroup(NodeLink node) {
			return node.isDestinationGroup() // node is a group
					&& ((GroupNode) node.getDestination()).getAttribute(NexusConstants.NXCLASS)
						.getFirstElement().equals(NexusConstants.COLLECTION) // and an NXcollection
					&& node.getName().equals(KEYS_GROUP_NAME); // called "keys"
		}
	}
}
