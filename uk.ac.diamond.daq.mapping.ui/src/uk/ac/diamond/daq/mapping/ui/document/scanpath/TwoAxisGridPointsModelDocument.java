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

package uk.ac.diamond.daq.mapping.ui.document.scanpath;

import static gda.mscan.element.Mutator.RANDOM_OFFSET;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsRandomOffsetModel;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import gda.mscan.element.Mutator;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument;

/**
 * Describes a model where an acquisition is performed on two {@code Scannable} and a {@link Mutator} map defines the
 * movement type
 *
 * @author Maurizio Nagni
 */
@JsonDeserialize(builder = TwoAxisGridPointsModelDocument.Builder.class)
public class TwoAxisGridPointsModelDocument extends ScanpathDocument {

	private final ScannableTrackDocument scannableOne;
	private final ScannableTrackDocument scannableTwo;
	private final Map<Mutator, List<Number>> mutators;

	public TwoAxisGridPointsModelDocument(ScannableTrackDocument scannableOne, ScannableTrackDocument scannableTwo,
			Map<Mutator, List<Number>> mutators) {
		super();
		this.scannableOne = scannableOne;
		this.scannableTwo = scannableTwo;
		this.mutators = mutators;
	}

	protected final ScannableTrackDocument getScannableOne() {
		return scannableOne;
	}

	protected final ScannableTrackDocument getScannableTwo() {
		return scannableTwo;
	}

	public Map<Mutator, List<Number>> getMutators() {
		return mutators;
	}

	@Override
	public IScanPointGeneratorModel getIScanPointGeneratorModel() {
		if (pathModel != null) {
			return pathModel;
		}

		if (scannableOne.hasNegativeValues() || scannableTwo.hasNegativeValues()) {
			return null;
		}

		TwoAxisGridPointsModel model;
		if (getMutators().containsKey(RANDOM_OFFSET)) {
			model = doRandomModel();
		} else {
			model = doGridPointModel();
		}
		model.setBoundingBox(getBoundingBox());
		model.setxAxisName(getScannableOne().getScannable());
		model.setyAxisName(getScannableTwo().getScannable());

		model.setxAxisPoints(scannableOne.getAxisPoints());
		model.setyAxisPoints(scannableTwo.getAxisPoints());
		model.setAlternating(getMutators().containsKey(Mutator.ALTERNATING));
		model.setContinuous(getMutators().containsKey(Mutator.CONTINUOUS));
		setPathModel(model);
		return model;
	}

	private TwoAxisGridPointsModel doGridPointModel() {
		return new TwoAxisGridPointsModel();
	}

	private TwoAxisGridPointsModel doRandomModel() {
		TwoAxisGridPointsRandomOffsetModel model = new TwoAxisGridPointsRandomOffsetModel();

		List<Number> params = getMutators().get(RANDOM_OFFSET);
		model.setOffset(params.get(OFFSET).doubleValue());
		if (params.size() > 1) {
			model.setSeed(getMutators().get(RANDOM_OFFSET).get(SEED).intValue());
		}
		return model;
	}

	@Override
	public IROI getROI() {
		if (roi != null) {
			return roi;
		}
		roi = new RectangularROI(getScannableOne().getStart(), getScannableTwo().getStart(), getScannableOne().length(),
				getScannableTwo().length(), 0.0);
		return roi;
	}

	private BoundingBox getBoundingBox() {
		return new BoundingBox(getScannableOne().getStart(), getScannableTwo().getStart(),
				getScannableOne().getStop() - getScannableOne().getStart(),
				getScannableTwo().getStop() - getScannableTwo().getStart());
	}

	@JsonPOJOBuilder
	public static class Builder {
		private ScannableTrackDocument scannableOne;
		private ScannableTrackDocument scannableTwo;
		private Map<Mutator, List<Number>> mutators = new EnumMap<>(Mutator.class);

		Builder withScannableOne(ScannableTrackDocument scannableOne) {
			this.scannableOne = scannableOne;
			return this;
		}

		Builder withScannableTwo(ScannableTrackDocument scannableTwo) {
			this.scannableTwo = scannableTwo;
			return this;
		}

		Builder withMutators(Map<Mutator, List<Number>> mutators) {
			if (mutators != null) {
				this.mutators = mutators;
			}
			return this;
		}

		public TwoAxisGridPointsModelDocument build() {
			return new TwoAxisGridPointsModelDocument(scannableOne, scannableTwo, mutators);
		}
	}
}
