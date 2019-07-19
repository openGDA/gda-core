package uk.ac.diamond.daq.experiment.ui.plan.preview;

import java.util.Objects;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;

public class LinearInterpolator {

	private double x0;
	private double x1;
	private double y0;
	private double y1;
	
	private double m;
	private double c;
	
	private final Dataset xDataset;
	private final Dataset yDataset;
	
	public LinearInterpolator(Dataset xDataset, Dataset yDataset) {
		Objects.requireNonNull(xDataset);
		Objects.requireNonNull(yDataset);
		
		if (xDataset.getShape().length != 1 ||
			yDataset.getShape().length != 1) {
			throw new IllegalArgumentException("I can only handle one-dimensional datasets!");
		}
		
		if (xDataset.getSize() != yDataset.getSize()) {
			throw new IllegalArgumentException("I need both datasets to be the same size");
		}
		
		this.xDataset = xDataset;
		this.yDataset = yDataset;
	}
	
	private double calculateGradient() {
		return (y1-y0) / (x1-x0);
	}
	
	private double calculateIntercept() {
		return y0 - m * x0;
	}
	
	private void initiateLine(int index) {
		
		int i = index < xDataset.getSize() - 1 ? index : index - 1;
		x0 = xDataset.getElementDoubleAbs(i);
		x1 = xDataset.getElementDoubleAbs(i+1);
		y0 = yDataset.getElementDoubleAbs(i);
		y1 = yDataset.getElementDoubleAbs(i+1);
		m = calculateGradient();
		c = calculateIntercept();
	}
	
	/**
	 * Interpolated Y given X
	 */
	double getY(double x) {		
		int startIndex = DatasetUtils.findIndexGreaterThan(xDataset, x) - 1;
		initiateLine(startIndex);
		
		return m * x + c;
	}
	
	/**
	 * Interpolated X by searching for the first instance of Y
	 */
	double getX(double y) {
		return getX(y, DatasetUtils.findIndexGreaterThan(yDataset, y) - 1);
	}
	
	/**
	 * Interpolated X given Y in line formed by index, index + 1
	 */
	double getX(double y, int index) {
		initiateLine(index);
		
		return (y - c) / m;
	}
	
}
