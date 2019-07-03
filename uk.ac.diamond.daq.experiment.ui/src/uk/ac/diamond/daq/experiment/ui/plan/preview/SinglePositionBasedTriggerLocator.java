package uk.ac.diamond.daq.experiment.ui.plan.preview;

import org.eclipse.january.dataset.Dataset;

import uk.ac.diamond.daq.experiment.api.remote.TriggerRequest;

public class SinglePositionBasedTriggerLocator extends SingleTriggerLocator {
	
	public SinglePositionBasedTriggerLocator(TriggerRequest trigger, Dataset xDataset, Dataset yDataset) {
		super(trigger, xDataset, yDataset);
	}

	@Override
	public void search(double xStart, double xEnd) {

		int iStart = getIndex(xStart);
		int iEnd = getIndex(xEnd);
		
		double yStart = interpolator.getY(xStart);
		double yEnd = interpolator.getY(xEnd);
		
		int distance = iEnd - iStart;
		
		if (distance == 0) {
			// search start to end on line istart : istart + 1
			searchLine(yStart, yEnd, iStart);
		} else {
			/* 
			 * we must search across multiple lines
			 *
			 * search:
			 *		yStart to y(iStart + 1),
			 *		y(iStart + 1) to y(iEnd),
			 *		y(iEnd) to yEnd
			 */
			if (searchLine(yStart, yDataset.getElementDoubleAbs(iStart+1), iStart)) return;
			for (int index = iStart + 1; index < iEnd; index ++) {
				if (searchLine(index)) return;
			}
			searchLine(yDataset.getElementDoubleAbs(iEnd), yEnd, iEnd);
		}
	}
	
	/**
	 * Search the entire line given by index, index + 1
	 * returns {@code true} if found, else {@code false}
	 */
	private boolean searchLine(int index) {
		return searchLine(yDataset.getElementDoubleAbs(index), yDataset.getElementDoubleAbs(index + 1), index);
	}
	
	/**
	 * returns {@code true} if found, else {@code false}
	 */
	boolean searchLine(double yStart, double yEnd, int index) {
		
		double reference;
		boolean isInThisSection;
		
		if (yStart < yEnd) {
			if (target > yStart) {
				reference = target - tolerance;
			} else {
				reference = target + tolerance;
			}
			isInThisSection = yStart <= reference && reference <= yEnd;
		} else {
			if (target < yStart ) {
				reference = target + tolerance;
			} else {
				reference = target - tolerance;
			}
			isInThisSection = yStart >= reference && reference >= yEnd;
		}
		
		if (isInThisSection) {
			x = interpolator.getX(reference, index);
			y = reference;
			return true;
		}
		
		return false;
	}
	
}
