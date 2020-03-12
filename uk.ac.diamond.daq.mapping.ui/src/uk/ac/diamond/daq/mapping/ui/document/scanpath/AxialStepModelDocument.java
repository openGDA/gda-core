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

import static gda.mscan.element.Mutator.ALTERNATING;
import static gda.mscan.element.Mutator.CONTINUOUS;

import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import gda.mscan.element.Mutator;
import uk.ac.diamond.daq.mapping.api.document.deserializer.MutatorDeserializer;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument;

/**
 * Describes a model where an acquisition is performed on a single {@code Scannable}, that is on a single axis, and
 * a {@link Mutator} map defines the movement type
 *
 * @author Maurizio Nagni
 */
@JsonDeserialize(builder = AxialStepModelDocument.Builder.class)
public class AxialStepModelDocument extends ScanpathDocument {

	private final ScannableTrackDocument scannable;
	private final Map<Mutator, List<Number>> mutators;

	public AxialStepModelDocument(ScannableTrackDocument scannable, Map<Mutator, List<Number>> mutators) {
		super();
		this.scannable = scannable;
		this.mutators = mutators;
	}

	public ScannableTrackDocument getScannable() {
		return scannable;
	}

	public Map<Mutator, List<Number>> getMutators() {
		return mutators;
	}

	@Override
	public IScanPointGeneratorModel getIScanPointGeneratorModel() {
		if (pathModel != null) {
			return pathModel;
		}
		// This is actually inconsistent as the underlying AxialStepModel requires steps in the negative direction
		// to be negative (see below) but for consistency with classic scanning and the Mapping UI negative steps
		// are disallowed as part of a valid mscan string
		if (getScannable().getStep() < 0.0) {
			return null;
		}

		// Multiplier to adjust the step value direction
		double sign = getScannable().getStop() < getScannable().getStart() ? -1 : 1;

		AxialStepModel model = new AxialStepModel(getScannable().getScannable(), getScannable().getStart(),
				getScannable().getStop(), getScannable().getStep() * sign);
		model.setAlternating(mutators.containsKey(ALTERNATING));
		model.setContinuous(mutators.containsKey(CONTINUOUS));
		pathModel = model;
		return pathModel;
	}

	@Override
	public IROI getROI() {
		if (roi != null) {
			return roi;
		}
		double[] spt = { getScannable().getStart(), 0.0 };
		double[] ept = { getScannable().getStop(), 0.0 };
		roi = new LinearROI(spt, ept);
		return roi;
	}

	@JsonPOJOBuilder
	public static class Builder {
		private ScannableTrackDocument scannable;
		@JsonDeserialize(keyUsing = MutatorDeserializer.class)
		private Map<Mutator, List<Number>> mutators;

		Builder withScannable(ScannableTrackDocument scannable) {
			this.scannable = scannable;
			return this;
		}

		Builder withMutators(Map<Mutator, List<Number>> mutators) {
			this.mutators = mutators;
			return this;
		}

		public AxialStepModelDocument build() {
			return new AxialStepModelDocument(scannable, mutators);
		}
	}
}
