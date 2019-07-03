package uk.ac.diamond.daq.experiment.ui.plan.preview;

import java.math.BigDecimal;

import org.eclipse.january.dataset.Dataset;

import uk.ac.diamond.daq.experiment.api.remote.TriggerRequest;

public class RepeatingPositionBasedTriggerLocator extends RepeatingTriggerLocator {

	public RepeatingPositionBasedTriggerLocator(TriggerRequest trigger, Dataset xDataset, Dataset yDataset) {
		super(trigger, xDataset, yDataset);
	}
	
	@Override
	public void search(double xStart, double xEnd) {
		
		x.clear();
		y.clear();
		
		int iStart = getIndex(xStart);
		int iEnd = getIndex(xEnd);
		
		double yStart = interpolator.getY(xStart);
		double yEnd = interpolator.getY(xEnd);
		
		int distance = iEnd - iStart;
		
		if (distance == 0) {
			// start and end points lie on the same line
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
			
			double lastKnownTrigger = searchLine(yStart, yDataset.getElementDoubleAbs(iStart+1), iStart);
			for (int index = iStart + 1; index < iEnd; index ++) {
				lastKnownTrigger = searchLine(lastKnownTrigger, index);
			}
			searchLine(lastKnownTrigger, yEnd, iEnd);
		}
		
	}
	
	/**
	 * Searches line formed by index, index+1
	 */
	private double searchLine(double lastKnownTrigger, int index) {
		return searchLine(lastKnownTrigger, yDataset.getElementDoubleAbs(index+1), index);	
	}
	
	/**
	 * Travels along a line adding coordinates of triggers
	 * 
	 * returns y of last trigger
	 */
	private double searchLine(double yStart, double yEnd, int index) {
		int direction = yStart < yEnd ? 1 : -1;
		
		BigDecimal current = BigDecimal.valueOf(yStart);
		BigDecimal last = BigDecimal.valueOf(yEnd);
		BigDecimal increment = BigDecimal.valueOf(interval * direction);
		
		while (last.subtract(current).abs().compareTo(increment.abs()) >= 0) {
			current = current.add(increment);
			y.add(current.doubleValue());
			x.add(interpolator.getX(current.doubleValue(), index));
		}
		
		return current.doubleValue();
	}
	
}
