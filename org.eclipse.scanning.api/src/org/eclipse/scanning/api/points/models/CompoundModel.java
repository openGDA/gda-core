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
public class CompoundModel extends AbstractMultiModel<IScanPointGeneratorModel> {

	private Collection<ScanRegion>  regions = new ArrayList<>();
	private List<IMutator>	           mutators = new ArrayList<>();
	private double                     duration = -1;

	public CompoundModel() {
		// Must have no-arg constructor
	}

	/**
	 * Returns a copy (clone) of the given {@link CompoundModel}.
	 * @param toCopy the model to copy
	 */
	public CompoundModel(CompoundModel toCopy) {
		setModels(toCopy.getModels());
		setRegions(toCopy.getRegions());
		setMutators(toCopy.getMutators());
		setDuration(toCopy.getDuration());
	}

	@SuppressWarnings("unchecked")
	public CompoundModel(IScanPointGeneratorModel... models) {
		if (models !=null && models.length == 1 && models[0] instanceof List<?>) {
			setModels((List<IScanPointGeneratorModel>) models[0]);
		} else {
		    setModels(Arrays.asList(models));
		}
	}
	public CompoundModel(List<? extends IScanPointGeneratorModel> ms) {
		setModels(new ArrayList<>(ms));
	}
	public CompoundModel(IScanPointGeneratorModel model, IROI region) {
		setData(model, region, model.getScannableNames());
	}

	public void setData(IScanPointGeneratorModel model, IROI region) {
		setData(model, region, model.getScannableNames());
	}

	public void setData(IScanPointGeneratorModel model, IROI region, List<String> names) {
		setModels(Arrays.asList(model));
		if (region != null) setRegions(Arrays.asList(new ScanRegion(region, names)));
	}

	/**
	 * Method to add a model and regions which are assumed to act on the
	 * model provided and are assigned to it using its scannable names.
	 *
	 * @param model
	 * @param rois
	 */
	public void addData(IScanPointGeneratorModel model, Collection<IROI> rois) {

		addModel(model);

		// They are not really ordered but for now we maintain order.
		Set<ScanRegion> newRegions = new LinkedHashSet<>(7);
		if (rois!=null) for (IROI roi : rois) {
			if (roi != null) newRegions.add(new ScanRegion(roi, model.getScannableNames()));
		}
		addRegions(newRegions);
	}

	public void setModelsVarArgs(IScanPointGeneratorModel... models) {
		setModels(Arrays.asList(models));
	}
	public Collection<ScanRegion> getRegions() {
		return regions;
	}
	public void setRegions(Collection<ScanRegion> regions) {
		pcs.firePropertyChange("regions", this.regions, regions);
		this.regions = regions == null ? new ArrayList<>() : new ArrayList<>(regions);
	}
	public void setRegionsVarArgs(ScanRegion... regions) {
		setRegions(Arrays.asList(regions));
	}

	public List<IMutator> getMutators() {
		return mutators;
	}

	public void setMutators(List<IMutator> mutators) {
		pcs.firePropertyChange("mutators", this.mutators, mutators);
		this.mutators = mutators == null ? new ArrayList<>() : new ArrayList<>(mutators);
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
		int result = super.hashCode();
		result = prime * result + ((regions == null) ? 0 : regions.hashCode());
		result = prime * result + ((mutators == null) ? 0 : mutators.hashCode());
		long durationBits = Double.doubleToLongBits(duration);
		result = prime * result + (int)(durationBits ^ (durationBits >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) return false;
		CompoundModel other = (CompoundModel) obj;
		if (duration != other.duration) return false;
		if (regions == null) {
			if (other.regions != null) return false;
		}
		else if (!regions.equals(other.regions)) return false;
		if (mutators == null) {
			return other.mutators == null;
		}
		return mutators.equals(other.mutators);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " [models=" + getModels() + ", regions=" + regions + ", mutators=" + mutators +
				", duration=" + duration + "]";
	}

	@Override
	public boolean isContinuous() {
		// Has only noModel generators
		if (getModels().isEmpty()) return true;
		IScanPointGeneratorModel innermostModel = getModels().get(getModels().size()-1);
		return innermostModel.isContinuous();
	}

	public void addMutators(List<IMutator> mutators) {
		if (mutators == null) return;
		List<IMutator> tmp = new ArrayList<>();
		if (this.mutators != null) tmp.addAll(this.mutators);
		tmp.addAll(mutators);
		setMutators(tmp);
	}

	public void addRegions(Collection<ScanRegion> regions) {
		if (regions == null) return;
		List<ScanRegion> tmp = new ArrayList<>();
		if (this.regions != null) tmp.addAll(this.regions);
		tmp.addAll(regions);
		setRegions(tmp);
	}

	@Override
	public List<String> getScannableNames(){
		List<String> scannables = new ArrayList<>();
		for (IScanPointGeneratorModel model : getModels()) {
			scannables.addAll(model.getScannableNames());
		}
		return scannables;
	}

}
