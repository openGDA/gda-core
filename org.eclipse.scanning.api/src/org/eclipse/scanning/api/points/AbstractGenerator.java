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
import java.util.List;

import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Matthew Gerring
 *
 * @param <T>
 */
public abstract class AbstractGenerator<T extends IScanPathModel> implements IPointGenerator<T> {

	private static Logger logger = LoggerFactory.getLogger(AbstractGenerator.class);

	protected T model;

	protected AbstractGenerator(T model) {
		this.model = model;
		validate(model);
	}

	protected AbstractGenerator() {
		// For validating models only
	}

	@Override
	public T getModel() {
		return model;
	}

	@Override
	public List<IPosition> createPoints() {
		logger.info("{} creating points from: {}", getClass().getSimpleName(), model);
		final List<IPosition> points = new ArrayList<>();
		iterator().forEachRemaining(points::add);
		return points;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((model == null) ? 0 : model.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		AbstractGenerator<?> other = (AbstractGenerator<?>) obj;
		if (model == null) {
			return other.model == null;
		}
		return model.equals(other.model);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [model=" + model + "]";
	}

}
