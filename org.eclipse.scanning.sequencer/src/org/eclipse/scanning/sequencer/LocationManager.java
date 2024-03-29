package org.eclipse.scanning.sequencer;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.eclipse.scanning.api.annotation.scan.PointEnd;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.points.models.InterpolatedMultiScanModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.models.ScanModel;

/**
 *
 * This class manages the location of
 * various parts of the scan as the scan runs.
 *
 * It maintains a count of levels and creates a
 *
 * @author Matthew Gerring
 *
 */
public final class LocationManager {


	/**
	 * Variables used to monitor progress of inner scans
	 */
	private final int outerSize;
	private final int innerSize;
	private final int totalSize;

	private int outerCount = 0;
	private int stepNumber = -1;

	// External data
	private final ScanModel scanModel;
	private final boolean isInterpolatedMultiScan;
	private final IPointGenerator<?> outerPointGenerator;

	public LocationManager(SubscanModerator moderator) throws ScanningException {
		this.scanModel = moderator.getScanModel();
		stepNumber = 0;

		outerSize  = moderator.getOuterScanSize();
		innerSize  = moderator.getInnerScanSize();
		totalSize  = moderator.getTotalScanSize();

		outerPointGenerator = moderator.getOuterPointGenerator();
		isInterpolatedMultiScan = getMultiScanModel().isPresent();
	}

	public int getOuterSize() {
		return outerSize;
	}

	public int getOuterCount() {
		return outerCount;
	}

	public int getInnerSize() {
		return innerSize;
	}

	public int getTotalSize() {
		return totalSize;
	}

	public int getStepNumber() {
		return stepNumber;
	}

	public void setStepNumber(int stepNumber) {
		this.stepNumber = stepNumber;
	}

	public boolean isInterpolatedMultiScan() {
		return isInterpolatedMultiScan;
	}

	/**
	 * Called during Software scans to increment counts.
	 * outerCount only relevant in Malcolm scans, changed with setter/getters
	 * stepNumber for Malcolm scans changed with setter/getters
	 */
	@PointEnd
	public void increment() {
		if (innerSize == 0) stepNumber++;
	}

	/**
	 * Creates an {@link Iterator} over the {@link IPosition}s in the scan.
	 * Also initialized the scan size which are incremented during the scan.
	 *
	 * @return position iterator
	 * @throws ScanningException
	 */
	public Iterator<IPosition> createPositionIterator() throws ScanningException {
		// interpolated multi-scans are a special case, as the overall point gen model generated doesn't include
		// the interpolation points. This is necessary because malcolm doesn't move these positions, GDA does.
		if (isInterpolatedMultiScan) {
			return getMultiScanModel().orElseThrow().getInterpolatedPositions().iterator();
		}

		return outerPointGenerator.iterator();
	}

	private Optional<InterpolatedMultiScanModel> getMultiScanModel() throws ScanningException {
		final IScanPathModel scanPathModel = scanModel.getScanPathModel();
		// deal with cases where the model is not a CompoundModel first
		if (scanPathModel instanceof InterpolatedMultiScanModel interpolatedModel) {
			return Optional.of(interpolatedModel);
		} else if (!(scanPathModel instanceof CompoundModel)) {
			return Optional.empty();
		}

		// check if the CompoundModel contains an InterpolatedMultiScanModel
		final List<IScanPointGeneratorModel> models = ((CompoundModel) scanModel.getScanPathModel()).getModels();
		final IScanPointGeneratorModel lastModel = models.get(models.size() - 1);

		if (lastModel instanceof InterpolatedMultiScanModel interpolatedModel) {
			if (models.size() > 1) {
				throw new ScanningException("Multi-scans are not supported with outer-scans");
			}
			return Optional.of(interpolatedModel);
		}

		return Optional.empty();
	}

	/**
	 * Seek within the iterator for the given location.
	 * @param location
	 * @param iterator
	 * @return the position at the given location.
	 * @throws ScanningException
	 */
	public IPosition seek(int location, Iterator<IPosition> iterator) throws ScanningException {
		// FIXME See DAQ-4188: probable logic bug here

		stepNumber=0;
		// IMPORTANT We do not keep the positions in memory because there can be millions.
		// Running over them is fast however.
		while(iterator.hasNext()) {
			IPosition pos = iterator.next();
			pos.setStepIndex(stepNumber);
			if (stepNumber == location) return pos;
			stepNumber+=Math.max(innerSize, 1);
		}

		throw new ScanningException("Could not find requested position.");
	}

	public boolean isInnerScan() {
		return outerSize > 0 && innerSize > 0;
	}

	public int getOverallCount() {
		return getStepNumber();
	}

	/**
	 * Returns the total percent complete for the scan(s)
	 * @return
	 */
	public double getOuterPercent() {
		return ((double) (getOverallCount()) / getTotalSize()) * 100;
	}

}