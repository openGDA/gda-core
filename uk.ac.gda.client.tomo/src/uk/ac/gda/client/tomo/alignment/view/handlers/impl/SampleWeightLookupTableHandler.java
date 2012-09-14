/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo.alignment.view.handlers.impl;

import gda.function.Lookup;
import uk.ac.gda.client.tomo.alignment.view.handlers.ISampleWeightLookupTableHandler;
import uk.ac.gda.client.tomo.composites.MotionControlComposite.SAMPLE_WEIGHT;
import uk.ac.gda.client.tomo.i12.ThetaServoSet;

public class SampleWeightLookupTableHandler implements ISampleWeightLookupTableHandler {
	private static final String SMALL_STEP_ACCL = "SmallStepAccl";
	private static final String SMALL_STEP_SERVOSET = "SmallStepServoset";
	private static final String SMALL_STEP_VELO = "SmallStepVelo";
	private static final String BIG_STEP_ACCL = "BigStepAccl";
	private static final String BIG_STEP_SERVOSET = "BigStepServoset";
	private static final String BIG_STEP_VELO = "BigStepVelo";

	private Lookup sampleWeightLookup;

	public void setSampleWeightLookup(Lookup sampleWeightLookup) {
		this.sampleWeightLookup = sampleWeightLookup;
	}

	@Override
	public double getBigStepVelocity(SAMPLE_WEIGHT sampleWeight) throws Exception {
		int index = getSampleWeightIndex(sampleWeight);
		return sampleWeightLookup.lookupValue(index, BIG_STEP_VELO);
	}

	@Override
	public ThetaServoSet getBigStepServoSet(SAMPLE_WEIGHT sampleWeight) throws Exception {
		int index = getSampleWeightIndex(sampleWeight);
		double lookupValue = sampleWeightLookup.lookupValue(index, BIG_STEP_SERVOSET);
		return ThetaServoSet.values()[(int) lookupValue];
	}

	@Override
	public double getBigStepAccl(SAMPLE_WEIGHT sampleWeight) throws Exception {
		int index = getSampleWeightIndex(sampleWeight);
		return sampleWeightLookup.lookupValue(index, BIG_STEP_ACCL);
	}

	@Override
	public double getSmallStepVelocity(SAMPLE_WEIGHT sampleWeight) throws Exception {
		int index = getSampleWeightIndex(sampleWeight);
		return sampleWeightLookup.lookupValue(index, SMALL_STEP_VELO);
	}

	@Override
	public ThetaServoSet getSmallStepServoSet(SAMPLE_WEIGHT sampleWeight) throws Exception {
		int index = getSampleWeightIndex(sampleWeight);
		double lookupValue = sampleWeightLookup.lookupValue(index, SMALL_STEP_SERVOSET);
		return ThetaServoSet.values()[(int) lookupValue];
	}

	@Override
	public double getSmallStepAccl(SAMPLE_WEIGHT sampleWeight) throws Exception {
		int index = getSampleWeightIndex(sampleWeight);
		return sampleWeightLookup.lookupValue(index, SMALL_STEP_ACCL);
	}

	private int getSampleWeightIndex(SAMPLE_WEIGHT sampleWeight) {
		int index = 1;
		switch (sampleWeight) {
		case LESS_THAN_ONE:
			index = 1;
			break;
		case ONE_TO_TEN:
			index = 2;
			break;
		case TEN_TO_TWENTY:
			index = 3;
			break;
		case TWENTY_TO_FIFTY:
			index = 4;
			break;
		}
		return index;
	}

}
