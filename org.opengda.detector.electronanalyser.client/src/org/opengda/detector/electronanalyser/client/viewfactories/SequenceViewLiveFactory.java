/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package org.opengda.detector.electronanalyser.client.viewfactories;

import org.eclipse.core.runtime.CoreException;
import org.opengda.detector.electronanalyser.client.views.SequenceViewLive;

import gda.rcp.views.FindableExecutableExtension;
import uk.ac.gda.devices.vgscienta.IVGScientaAnalyserRMI;

/**
 * Factory method that invokes the View object
 */
public class SequenceViewLiveFactory extends SequenceViewCreatorFactory implements FindableExecutableExtension {
	private IVGScientaAnalyserRMI analyser;
	private String analyserStatePV;
	private String analyserTotalTimeRemainingPV;
	private boolean disableSequenceEditingDuringAnalyserScan = true;

	@Override
	public Object create() throws CoreException {
		SequenceViewLive sequenceViewLive = (SequenceViewLive) super.create();
		if (analyser != null) sequenceViewLive.setAnalyser(analyser);
		if (getAnalyserStatePV() != null) sequenceViewLive.setDetectorStatePV(analyserStatePV);
		if (getAnalyserTotalTimeRemainingPV()!=null) sequenceViewLive.setAnalyserTotalTimeRemianingPV(analyserTotalTimeRemainingPV);
		return sequenceViewLive;
	}

	@Override
	protected SequenceViewLive createView() {
		return new SequenceViewLive();
	}

	public IVGScientaAnalyserRMI getAnalyser() {
		return analyser;
	}

	public void setAnalyser(IVGScientaAnalyserRMI analyser) {
		this.analyser = analyser;
	}

	public void setAnalyserTotalTimeRemainingPV(String analyserTotalTimeRemainingPV) {
		this.analyserTotalTimeRemainingPV = analyserTotalTimeRemainingPV;
	}

	public String getAnalyserTotalTimeRemainingPV() {
		return analyserTotalTimeRemainingPV;
	}

	public String getAnalyserStatePV() {
		return analyserStatePV;
	}

	public void setAnalyserStatePV(String analyserStatePV) {
		this.analyserStatePV = analyserStatePV;
	}

	public void setDisableSequenceEditingDuringAnalyserScan(boolean value) {
		disableSequenceEditingDuringAnalyserScan = value;
	}

	public boolean getDisableSequenceEditingDuringAnalyserScan() {
		return disableSequenceEditingDuringAnalyserScan;
	}
}