package uk.ac.diamond.daq.mapping.path;

import java.util.Iterator;

import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.PointsValidationException;

public class SpiralGenerator extends AbstractGenerator<SpiralModel> {

	@Override
	public Iterator<IPosition> iteratorFromValidModel() {
		return new SpiralIterator(this);
	}

	@Override
	protected void validateModel() {
		if (model.getScale() == 0.0) throw new PointsValidationException("Scale must be non-zero!");
	}
}
