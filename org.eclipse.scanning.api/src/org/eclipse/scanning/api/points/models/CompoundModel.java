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
package org.eclipse.scanning.api.points.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.api.points.IMutator;
/**
 * This class is designed to encapsulate the information
 * to required to provide all the locations, with regions
 * of an n-Dimensional scan.
 *
 * <pre>
 * CompoundModel {
 *     models:  [ list of models]
 *     regions: [ list of regions]
 *     mutators: [ list of mutators ]
 * }
 * Region {
 *     roi: geometric roi
 *     scannables: [ list of scannable names]
 * }
 * </pre>
 * <b>Example:</b><p>
 * <pre>
 * CompoundModel {
 *     models : [
 *         {type: SpiralModel
 *          xAxisName: x
 *          yAxisName: y
 *          ...
 *         }
 *     ]
 *     regions : [
 *        {
 *          roi : {type: CircularROI
 *           centre: [0,1]
 *           radius: 2
 *          }
 *          scannables: ["x", "y"]
 *        }
 *     ]
 *     mutators : [
 *        {
 *           RandomOffsetMutator: {
 *            seed: 10
 *            axes: ["x"]
 *            max_offset: {
 *              "x":0.1
 *            }
 *           }
 *        }
 *     ]
 * }
 *
 *
 * </pre>
 *
 * @author Matthew Gerring
 *
 */
public class CompoundModel extends AbstractPointsModel implements Cloneable {

	private List<Object>               models;
	private Collection<ScanRegion>  regions;
	private List<IMutator>	           mutators;
	private double                     duration = -1;

	public CompoundModel() {
		// Must have no-arg constructor
	}

	/**
	 * Returns a copy (clone) of the given {@link CompoundModel}.
	 * @param toCopy the model to copy
	 */
	public static  CompoundModel copy(CompoundModel toCopy) {
		final CompoundModel copy = new CompoundModel();
		copy.models = toCopy.models;
		copy.regions = toCopy.regions;
		copy.mutators = toCopy.mutators;
		copy.duration = toCopy.duration;
		return copy;
	}

	/**
	 * Copies the mutators and regions of another CompoundModel, but sets new models
	 */
	public static CompoundModel copyAndSetModels(CompoundModel toCopy, List<Object> models) {
		CompoundModel copy = copy(toCopy);
		copy.models = models;
		return copy;
	}


	@SuppressWarnings("unchecked")
	public CompoundModel(Object... models) {
		if (models !=null && models.length == 1 && models[0] instanceof List<?>) {
			this.models = (List<Object>) models[0];
		} else {
		    this.models = Arrays.asList(models);
		}
	}
	public CompoundModel(List<Object> ms) {
		models = ms;
	}
	public CompoundModel(IScanPathModel model, IROI region) {
		setData(model, region, model.getScannableNames());
	}

	public void setData(IScanPathModel model, IROI region) {
		setData(model, region, model.getScannableNames());
	}

	public void setData(IScanPathModel model, IROI region, List<String> names) {
		// We do it this way to make setData(...) fast. This means addData(...) has to deal with unmodifiable lists.
		this.models  = Arrays.asList(model);
	    this.regions = Arrays.asList(new ScanRegion(region, names));
	}

	/**
	 * Method to add a model and regions which are assumed to act on the
	 * model provided and are assigned to it using its scannable names.
	 *
	 * @param model
	 * @param rois
	 */
	public void addData(Object model, Collection<IROI> rois) {

		addModel(model);

		// They are not really ordered but for now we maintain order.
		Set<ScanRegion> newRegions = new LinkedHashSet<>(7);
		if (rois!=null) for (IROI roi : rois) {
			newRegions.add(new ScanRegion(roi, AbstractPointsModel.getScannableNames(model)));
		}
		addRegions(newRegions);
	}

	public List<Object> getModels() {
		return models;
	}
	public void setModels(List<Object> models) {
		this.models = models;
	}
	public void setModelsVarArgs(Object... models) {
		this.models = Arrays.asList(models);
	}
	public Collection<ScanRegion> getRegions() {
		return regions;
	}
	public void setRegions(Collection<ScanRegion> regions) {
		this.regions = regions;
	}
	public void setRegionsVarArgs(ScanRegion... regions) {
		this.regions = Arrays.asList(regions);
	}

	public List<IMutator> getMutators() {
		return mutators;
	}

	public void setMutators(List<IMutator> mutators) {
		this.mutators = mutators;
	}

	public double getDuration() {
		return duration;
	}

	public void setDuration(double duration) {
		this.duration = duration;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((models == null) ? 0 : models.hashCode());
		result = prime * result + ((regions == null) ? 0 : regions.hashCode());
		result = prime * result + ((mutators == null) ? 0 : mutators.hashCode());
		long durationBits = Double.doubleToLongBits(duration);
		result = prime * result + (int)(durationBits ^ (durationBits >>> 32));
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
		CompoundModel other = (CompoundModel) obj;
		if (duration != other.duration) return false;
		if (models == null) {
			if (other.models != null)
				return false;
		} else if (!equals(models, other.models))
			return false;
		if (regions == null) {
			if (other.regions != null)
				return false;
		} else if (!equals(regions, other.regions))
			return false;
		if (mutators == null) {
			if (other.mutators != null)
				return false;
		} else if (!equals(mutators, other.mutators))
			return false;
		return true;
	}

	/**
	 * This equals does an equals on two collections
	 * as if they were two lists because order matters with the names.
	 * @param o
	 * @param t
	 * @return
	 */
    private boolean equals(Collection<?> o, Collection<?> t) {

	if (o == t)
            return true;
	if (o == null && t == null)
            return true;
	if (o == null || t == null)
            return false;

        Iterator<?> e1 = o.iterator();
        Iterator<?> e2 = t.iterator();
        while (e1.hasNext() && e2.hasNext()) {
            Object o1 = e1.next();
            Object o2 = e2.next();

            // Collections go down to the same equals.
            if (o1 instanceof Collection && o2 instanceof Collection) {
		boolean collectionsEqual = equals((Collection<?>)o1,(Collection<?>)o2);
		if (!collectionsEqual) {
			return false;
		} else {
			continue;
		}
            }

            // Otherwise we use object equals.
            if (!(o1==null ? o2==null : o1.equals(o2)))
                return false;
        }
        return !(e1.hasNext() || e2.hasNext());
    }

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [models=" + models + ", regions=" + regions + ", mutators=" + mutators +
				", duration=" + duration + "]";
	}

	// CompoundModel not a valid model for Zip/Concat generator currently
	// May be possible when ScanPointGenerator changes behaviour, see
	// https://github.com/dls-controls/scanpointgenerator/issues/76
	@Override
	public int size() {
		int size = 1;
		for (Object model : models) {
			size *= ((IScanPathModel) model).size();
		}
		return size;
	}

	@Override
	public boolean isContinuous() {
		IScanPathModel innermostModel = (IScanPathModel) getModels().get(getModels().size()-1);
		return innermostModel.isContinuous();
	}

	/*
	 * Dealing with unmodifiable lists from setData
	 */
	public void addModel(Object model) {
		List<Object> tmp = new ArrayList<>();
		if (models != null) tmp.addAll(models);
		tmp.add(model);
		models = tmp;
	}

	public void addMutators(List<IMutator> mutators) {
		if (mutators == null) return;
		List<IMutator> tmp = new ArrayList<>();
		if (this.mutators != null) tmp.addAll(this.mutators);
		tmp.addAll(mutators);
		this.mutators = tmp;
	}

	public void addRegions(Collection<ScanRegion> regions) {
		if (regions == null) return;
		List<ScanRegion> tmp = new ArrayList<>();
		if (this.regions != null) tmp.addAll(this.regions);
		tmp.addAll(regions);
		this.regions = tmp;
	}

}