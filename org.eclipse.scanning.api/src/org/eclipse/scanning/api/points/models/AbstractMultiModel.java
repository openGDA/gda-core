/*-
 * Copyright © 2020 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package org.eclipse.scanning.api.points.models;

import static org.eclipse.scanning.api.constants.PathConstants.MODELS;

import java.util.ArrayList;
import java.util.List;
/**
 * A model that is constructed of other models, e.g. a Consecutive model, which runs scans one after another, or a
 * Concurrent model, which runs scans in different axes simultaneously.
 * May be bounded to a particular type of model (for reimplementing AxialMultiStep, or limiting bounds to ScanPointGenerator
 * models.
 *
 * @param <T>
 */
public abstract class AbstractMultiModel<T extends IScanPointGeneratorModel> extends AbstractPointsModel {

	private List<T> models = new ArrayList<>();

	public List<T> getModels(){
		return models;
	}

	protected T getFirstModel() {
		return models.get(0);
	}

	public void setModels(List<T> models) {
		pcs.firePropertyChange(MODELS, this.models, models);
		this.models = models == null ? new ArrayList<>() : new ArrayList<>(models);
	}

	public void addModel(T model) {
		List<T> newModels = new ArrayList<>(this.models);
		newModels.add(model);
		pcs.firePropertyChange(MODELS, models, newModels);
		models = newModels;
	}

	/**
	 * Must implement clear() method on beans being used with BeanUI.
	 */
	public void clear() {
		List<T> oldModels = new ArrayList<>(models);
		models.clear();
		pcs.firePropertyChange(MODELS, oldModels, models);
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}
		AbstractMultiModel<?> other = (AbstractMultiModel<?>) obj;
		if (models == null) return other.models == null;
		return models.equals(other.models);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((models == null) ? 0 : models.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [models=" + models + ", " + super.toString() + "]";
	}

}
