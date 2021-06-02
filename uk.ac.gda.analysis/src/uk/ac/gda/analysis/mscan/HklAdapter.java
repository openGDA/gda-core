/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.analysis.mscan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.scanning.api.points.IPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The hkl scannable (diffcalc.gdasupport.scannable.hkl.Hkl) is a ScannableMotionWithScannableFieldsBase. It has a
 * simulateMoveTo method which could be used, alternatively its _diffcalc object (module 'diffcalc.dc.dcyou') which has
 * hkl_to_angles. The diffhw is a scannabke group _fourc.
 *
 * From angles_to_hkl we get ((h, k, l), paramDict)
 *
 */
public abstract class HklAdapter {

	private static final Logger logger = LoggerFactory.getLogger(HklAdapter.class);

	protected abstract List<String> getFourCNames();

	/**
	 * Call angles_to_hkl on diffcalc to get the calculated hkl values corresponding to the position
	 *
	 * @param angles
	 *            array of angles in the same order as _fourc
	 */
	protected abstract List<Number> getHkl(Object angles);

	/**
	 * @return current position of the four circle scannable group
	 */
	protected abstract Map<String, Double> getCurrentAnglePositions();

	public void populateHkl(ILazyWriteableDataset hData, ILazyWriteableDataset kData, ILazyWriteableDataset lData,
			List<IPosition> positions, Set<String> axisNames) {
		populateDatasets(hData, kData, lData, getHklValues(getAnglesForEachPoint(positions, axisNames)));
	}

	private List<Map<String, Double>> getAnglesForEachPoint(List<IPosition> positions, Set<String> axisNames) {
		Map<String, Double> current = getCurrentAnglePositions();
		List<Map<String, Double>> allAnglePositions = new ArrayList<>();
		for (IPosition pos : positions) {
			Map<String, Double> thisPoint = new HashMap<>(current);
			axisNames.stream().filter(current::containsKey).forEach(n -> thisPoint.put(n, pos.getValue(n)));
			allAnglePositions.add(thisPoint);
		}
		return allAnglePositions;
	}

	private List<List<Number>> getHklValues(List<Map<String, Double>> anglePositions) {
		return anglePositions.stream().map(this::arrayFromPos).map(this::getHkl).collect(Collectors.toList());
	}

	private Double[] arrayFromPos(Map<String, Double> pos) {
		return getFourCNames().stream().map(pos::get).toArray(Double[]::new);
	}

	private void populateDatasets(ILazyWriteableDataset hData, ILazyWriteableDataset kData, ILazyWriteableDataset lData,
			List<List<Number>> hklvalues) {
		List<Double> hVals = new ArrayList<>();
		List<Double> kVals = new ArrayList<>();
		List<Double> lVals = new ArrayList<>();
		for (List<Number> hkl : hklvalues) {
			hVals.add(hkl.get(0).doubleValue());
			kVals.add(hkl.get(1).doubleValue());
			lVals.add(hkl.get(2).doubleValue());
		}
		Dataset hd = DatasetFactory.createFromList(hVals);
		Dataset kd = DatasetFactory.createFromList(kVals);
		Dataset ld = DatasetFactory.createFromList(lVals);

		try {
			hData.setSlice(null, hd, new SliceND(hd.getShape()));
			kData.setSlice(null, kd, new SliceND(hd.getShape()));
			lData.setSlice(null, ld, new SliceND(hd.getShape()));
		} catch (DatasetException e) {
			logger.error("Error writing to hkl dataset", e);
		}
	}

}
