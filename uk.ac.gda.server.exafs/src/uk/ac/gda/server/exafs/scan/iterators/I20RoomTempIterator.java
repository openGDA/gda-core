/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.server.exafs.scan.iterators;

import gda.device.DeviceException;
import gda.device.Scannable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import uk.ac.gda.beans.exafs.ISampleParameters;
import uk.ac.gda.beans.exafs.i20.I20SampleParameters;
import uk.ac.gda.beans.exafs.i20.SampleStageParameters;

public class I20RoomTempIterator implements SampleEnvironmentIterator {

	private Scannable sample_pitch;
	private Scannable sample_roll;
	private Scannable sample_rot;
	private Scannable sample_z;
	private Scannable sample_y;
	private Scannable sample_x;
	private List<SampleStageParameters> parameters;
	private Iterator<SampleStageParameters> iterator;
	private SampleStageParameters nextParameters = null;
	private int iterationNumberOnThisSample = -1;
	private int iterationsToDoNumberOnThisSample = -1;

	public I20RoomTempIterator(Scannable sample_x, Scannable sample_y, Scannable sample_z, Scannable sample_rot,
			Scannable sample_roll, Scannable sample_pitch) {
		this.sample_x = sample_x;
		this.sample_y = sample_y;
		this.sample_z = sample_z;
		this.sample_rot = sample_rot;
		this.sample_roll = sample_roll;
		this.sample_pitch = sample_pitch;

	}

	@Override
	public void setSampleBean(ISampleParameters sampleBean) {
		I20SampleParameters i20Bean = (I20SampleParameters) sampleBean;
		this.parameters = i20Bean.getRoomTemperatureParameters();
		iterator = parameters.iterator();
	}

	@Override
	public int getNumberOfRepeats() {
		int numRepeats = 0;
		for (SampleStageParameters params : parameters) {
			numRepeats += params.getNumberOfRepetitions();
		}
		return numRepeats;
	}

	@Override
	public void next() throws DeviceException, InterruptedException {
		if (nextParameters == null || iterationNumberOnThisSample == iterationsToDoNumberOnThisSample) {
			nextParameters = iterator.next();
			iterationNumberOnThisSample = 1;
			iterationsToDoNumberOnThisSample = nextParameters.getNumberOfRepetitions();
		} else {
			iterationNumberOnThisSample++;
		}

		boolean samXEnabled = nextParameters.isSamXEnabled();
		boolean samYEnabled = nextParameters.isSamYEnabled();
		boolean samZEnabled = nextParameters.isSamZEnabled();
		boolean rotEnabled = nextParameters.isRotEnabled();
		boolean rollEnabled = nextParameters.isRollEnabled();
		boolean pitchEnabled = nextParameters.isPitchEnabled();

		// print "samXEnabled ", samXEnabled
		// print "samYEnabled ", samYEnabled
		// print "samZEnabled ", samZEnabled
		// print "rotEnabled ", rotEnabled
		// print "rollEnabled ", rollEnabled
		// print "pitchEnabled ", pitchEnabled
		// print "fineRotEnabled ", fineRotEnabled

		Double x = nextParameters.getSample_x();
		Double y = nextParameters.getSample_y();
		Double z = nextParameters.getSample_z();
		Double rotation = nextParameters.getSample_rotation();
		Double roll = nextParameters.getSample_roll();
		Double pitch = nextParameters.getSample_pitch();
	
		// sample_repeats = nextParameters.getNumberOfRepetitions();
		// self.log("Running sample:",samplename) ;

		// if self.sample_x == None or self.sample_y ==None or self.sample_z == None or self.sample_rot == None or
		// self.sample_roll == None or self.sample_pitch == None:
		// raise DeviceException("I20 scan script - could not find all sample stage motors!")
		// self.log( "Moving sample stage to",x,y,z,rotation,roll,pitch,"...")

		if (samXEnabled)
			sample_x.asynchronousMoveTo(x);

		if (samYEnabled)
			sample_y.asynchronousMoveTo(y);

		if (samZEnabled)
			sample_z.asynchronousMoveTo(z);

		if (rotEnabled)
			sample_rot.asynchronousMoveTo(rotation);

		if (rollEnabled)
			sample_roll.asynchronousMoveTo(roll);

		if (pitchEnabled)
			sample_pitch.asynchronousMoveTo(pitch);

		sample_x.waitWhileBusy();
		sample_y.waitWhileBusy();
		sample_z.waitWhileBusy();
		sample_rot.waitWhileBusy();
		sample_roll.waitWhileBusy();
		sample_pitch.waitWhileBusy();
		// self.log( "Sample stage move complete.");

	}

	@Override
	public void resetIterator() {
		iterator = parameters.iterator();
	}

	@Override
	public String getNextSampleName() {
		return nextParameters.getSample_name();
	}

	@Override
	public List<String> getNextSampleDescriptions() {
		List<String> descriptions = new ArrayList<String>();
		descriptions.add(nextParameters.getSample_description());
		return descriptions;
	}
}
