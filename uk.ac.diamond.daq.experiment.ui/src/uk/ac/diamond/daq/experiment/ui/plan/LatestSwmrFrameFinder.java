package uk.ac.diamond.daq.experiment.ui.plan;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;

import org.dawnsci.datavis.model.DataOptions;
import org.dawnsci.datavis.model.ILiveLoadedFileListener;
import org.dawnsci.datavis.model.IRefreshable;
import org.dawnsci.datavis.model.LoadedFile;
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
 * Listens for SWMR files from ongoing scans and finds their latest detector frame
 */
public class LatestSwmrFrameFinder implements ILiveLoadedFileListener {
	
	private static final Logger logger = LoggerFactory.getLogger(LatestSwmrFrameFinder.class);
	
	private static final String DETECTOR_DATASET_SUFFIX = "/data";
	private static final String UNIQUE_KEYS_KEY_WORD = "keys";
	
	private final Consumer<IDataset> frameProcessor;
	private final RateLimiter rateLimiter;
	private Optional<IRefreshable> refreshableOptional;
	
	/**
	 * Constructs an {@link ILiveLoadedFileListener} which finds the latest detector frame
	 * in the SWMR file of the ongoing scan, rate-limited at the given frequency, and passes
	 * the frame to the given frameProcessor.
	 * 
	 * @param frameProcessor something which works on the latest frame IDataset
	 * @param updateRateFrequency in Hz
	 */
	public LatestSwmrFrameFinder(Consumer<IDataset> frameProcessor, double updateRateFrequency) {
		this.frameProcessor = frameProcessor;
		this.rateLimiter = RateLimiter.create(updateRateFrequency);
		refreshableOptional = Optional.empty();
	}
	
	/**
	 * This class operates only on SWMR files, denoted by being instances of IRefreshable.
	 * When one of these files is given to this method*, it is initialised and cached.
	 * <p>
	 * *This is most likely since the method is specified by {@link ILiveLoadedFileListener}
	 * which deals with SWMR files.
	 */
	@Override
	public void fileLoaded(LoadedFile loadedFile) {
		if (loadedFile instanceof IRefreshable) {
			IRefreshable swmr = (IRefreshable) loadedFile;
			refreshableOptional = Optional.of(swmr);
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
		file.refresh();
		try {
			findLatestFrame(file).ifPresent(frameProcessor);
		} catch (NoSuchElementException e) {
			logger.warn("Could not refresh data", e);
		}
	}
	
	private Optional<IDataset> findLatestFrame(IRefreshable file) {
		
		List<DataOptions> dataOptions = file.getUninitialisedDataOptions();
		
		DataOptions uniqueKeysDataOptions = dataOptions.stream()
				.filter(options -> options.getName().toLowerCase().contains(UNIQUE_KEYS_KEY_WORD))
				.findFirst().orElseThrow(() -> new NoSuchElementException("Unique keys dataset not found"));
		
		DataOptions detectorDataOptions = dataOptions.stream()
				.filter(options -> options.getName().endsWith(DETECTOR_DATASET_SUFFIX))
				.findFirst().orElseThrow(() -> new NoSuchElementException("Detector dataset not found"));
		
		/* FIXME We are about to use uniqueKeysDataOptions to find a frame in detectorDataOptions
		 * This is really only valid for a scan which contains a a single detector.
		 * See DAQ-2549
		 */
		
		try {
			
			int[] positionOfLastFrame = getPositionOfLastFrame(uniqueKeysDataOptions);
			if (positionOfLastFrame == null) {
				logger.debug("No data written yet");
				return Optional.empty();
			}
			
			ILazyDataset detectorData = detectorDataOptions.getLazyDataset();
			SliceND slice = getSlice(detectorData, positionOfLastFrame);			
			return Optional.of(detectorData.getSlice(slice).squeeze());

		} catch (DatasetException e) {
			logger.error("Error displaying newer detector frame", e);
			return Optional.empty();
		}
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
}
