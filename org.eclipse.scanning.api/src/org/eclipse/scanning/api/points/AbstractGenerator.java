/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.api.points;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.models.AbstractPointsModel;
import org.eclipse.scanning.api.points.models.IBoundingBoxModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Matthew Gerring
 *
 * @param <T>
 */
public abstract class AbstractGenerator<T extends AbstractPointsModel> implements IPointGenerator<T> {

	private static Logger logger = LoggerFactory.getLogger(AbstractGenerator.class);

	protected T model;
	private PPointGenerator pointGenerator;

	protected List<IROI> regions = new ArrayList<>();
	private String label;
	private String description;
	private String iconPath;
	private boolean visible=true;
	private boolean enabled=true;
	private int[] shape = null;

	protected AbstractGenerator(T model) {
		this.model = model;
		validateModel();
	}

	protected AbstractGenerator() {
		// For validating AxialMultiStepModels only
	}

	@Override
	public T getModel() {
		return model;
	}

	/**
	 * To allow Models to be validated prior to the iterator ever being called (to allow {@code ModelValidationException} to be checked,
	 * Generators are now created once with their model. IPointGeneratorService.createGenerator(model) should be used instead.
	 * Regions can still be added (where the generator supports them).
	 */
	@Deprecated
	@Override
	public void setModel(T model) throws GeneratorException {
		throw new IllegalArgumentException("Generators should be instantiated with their models, to allow validation at this time.");
	}

	@Override
	public final Iterator<IPosition> iterator() {
		return iteratorFromValidModel();
	}

	public abstract PPointGenerator createPythonPointGenerator();

	/**
	 * Creates and returns an iterator for this model. If possible subclasses should aim to
	 * return an instance of {@link ScanPointIterator}.
	 * @return iterator for this model
	 */
	protected Iterator<IPosition> iteratorFromValidModel(){
		if (pointGenerator == null) {
			pointGenerator = createPythonPointGenerator();
		}
		return pointGenerator.getPointIterator();
	}


	/**
	 * If the given model is considered "invalid", this method throws a
	 * ModelValidationException explaining why it is considered invalid.
	 * Otherwise, just returns. A model should be considered invalid if its
	 * parameters would cause the generator implementation to hang or crash.
	 *
	 * @throw exception if model invalid
	 */
	protected final void validateModel() throws ModelValidationException {
		validate(this.model);
	}

	@Override
	public void validate(T model) throws ModelValidationException {
		if (model.getScannableNames() == null || model.getScannableNames().contains(null)) throw new ModelValidationException("The model must have all the names of the scannables it is acting upon!", model, "name");
		if (model.getUnits().size() != model.getScannableNames().size()) {
			throw new ModelValidationException("Model must have units for each scannable axis!", model, "name"); // Not actually name
		}
		if (!AbstractPointsModel.supportsContinuous(model.getClass()) && model.isContinuous())
			throw new ModelValidationException(model.getClass().getSimpleName() + " cannot be continuous!", model, "continuous");
		if (!AbstractPointsModel.supportsAlternating(model.getClass()) && model.isAlternating())
			throw new ModelValidationException(model.getClass().getSimpleName() + " cannot be alternating!", model, "alternating");
		if (model instanceof IBoundingBoxModel) {
			IBoundingBoxModel boxModel = (IBoundingBoxModel) model;
			if (boxModel.getBoundingBox() == null)
				throw new ModelValidationException("The model must have a Bounding Box!", boxModel, "boundingBox");
			// As implemented, model width and/or height can be negative,
			// and this flips the slow and/or fast point order.
			if (boxModel.getBoundingBox().getxAxisLength()==0 || boxModel.getBoundingBox().getyAxisLength()==0)
	        	throw new ModelValidationException("The length must not be 0!", boxModel, "boundingBox");
		}
	}

	/**
	 * Final use sizeOfValidModel() to calculate size!
	 */
	@Override
	public final int size() throws GeneratorException {
		return sizeOfValidModel();
	}

	@Override
	public int getRank() throws GeneratorException {
		return getShape().length;
	}

	@Override
	public int[] getShape() throws GeneratorException {
		if (shape == null) {
			shape = calculateShape();
		}

		return shape;
	}

	/**
	 * Calculates the shape of the scan. This method is called when
	 * {@link #iteratorFromValidModel()} does not return a {@link ScanPointIterator}.
	 * Subclasses should override if a more efficient way of calculating the
	 * scan shape can be provided.
	 *
	 * @return scan shape
	 */
	protected int[] calculateShape() {
		Iterator<IPosition> iterator = iteratorFromValidModel();

		// if the iterator is an ScanPointIterator we can ask it for the shape
		if (iterator instanceof ScanPointIterator) {
			return ((ScanPointIterator) iterator).getShape();
		}

		if (!iterator.hasNext()) {
			// empty iterator
			return new int[0];
		}

		IPosition first = iterator.next();
		final int scanRank = first.getScanRank();

		if (scanRank == 0) {
			// scan of rank 0, e.g. static generator for a single empty point - i.e. acquire scans
			return new int[0];
		}

		// we fall back on iterating through all the points in the
		// scan to get the dimensions of the last one
		int pointNum = 1;
		long lastTime = System.currentTimeMillis();
		int[] shape = new int[scanRank];
		// Indices = all 0s so shape already set.
		IPosition last;
		while (iterator.hasNext()) {
			last = iterator.next(); // Could be large...
			pointNum++;
			for (int i = 0; i < scanRank; i++) {
				shape[i] = Math.max(shape[i], last.getIndex(i));
			}

			if (pointNum % 10000 == 0) {
				long newTime = System.currentTimeMillis();
				logger.debug("Point number {}, took {} ms", pointNum, (newTime - lastTime));
			}
		}
		return shape;
	}

	/**
	 * Please override this method, the default creates all points and
	 * returns their size
	 * @throws GeneratorException
	 */
	protected int sizeOfValidModel() throws GeneratorException {
		// For those generators which implement an iterator,
		// doing this loop is *much* faster for large arrays
		// because memory does not have to be allocated.
		Iterator<IPosition> it = iterator();

		// Always ask the iterator for size because it is
		// much faster than actual iteration.
		if (it instanceof ScanPointIterator) {
			return ((ScanPointIterator)it).getSize();
		}
		int index = 0;
		while (it.hasNext()) {
			it.next();
			index++;
		}
		return index;
	}

	@Override
	public List<IPosition> createPoints() throws GeneratorException {
		final List<IPosition> points = new ArrayList<>();
		iterator().forEachRemaining(points::add);
		return points;
	}

	@Override
	public List<IROI> getRegions() {
		return regions;
	}

	@Override
	public void setRegions(List<IROI> regions) throws GeneratorException {
		pointGenerator = null;
		shape = null;
		this.regions = regions == null ? new ArrayList<>() : regions;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	@Override
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public String getIconPath() {
		return iconPath;
	}

	@Override
	public void setIconPath(String iconPath) {
		this.iconPath = iconPath;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + (enabled ? 1231 : 1237);
		result = prime * result + ((iconPath == null) ? 0 : iconPath.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((model == null) ? 0 : model.hashCode());
		result = prime * result + (visible ? 1231 : 1237);
		result = prime * result + ((regions == null) ? 0 : regions.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractGenerator<?> other = (AbstractGenerator<?>) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (enabled != other.enabled)
			return false;
		if (iconPath == null) {
			if (other.iconPath != null)
				return false;
		} else if (!iconPath.equals(other.iconPath))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (model == null) {
			if (other.model != null)
				return false;
		} else if (!model.equals(other.model))
			return false;
		if (visible != other.visible)
			return false;
		if (regions == null) {
			if (other.regions != null)
				return false;
		} else if (!regions.equals(other.regions))
			return false;
		return true;
	}

	protected String description() {
	return "model=" + model + ", regions=" + regions + ", label=" + label
			+ ", visible=" + visible + ", enabled=" + enabled + ", shape=" + Arrays.toString(shape);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [" + description() + "]";
	}

}
