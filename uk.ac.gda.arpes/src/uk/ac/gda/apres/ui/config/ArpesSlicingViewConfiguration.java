/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package uk.ac.gda.apres.ui.config;

import gda.factory.Findable;
import gda.observable.IObservable;

public class ArpesSlicingViewConfiguration implements Findable {

	private String name;
	private String analyserName;
	private String defaultScannableName;
	private IObservable liveDataDispatcher;

	private String[] degreeScannableNames;

	private int[] initialImageDims;
	private int[] order; //ordering of the dimensions in the view

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	public String[] getDegreeScannableNames() {
		return degreeScannableNames;
	}

	public void setDegreeScannableNames(String[] degScannables) {
		this.degreeScannableNames = degScannables;
	}

	public int[] getInitialImageDims() {
		return initialImageDims;
	}

	public void setInitialImageDims(int[] initialImageDims) {
		this.initialImageDims = initialImageDims;
	}

	public String getAnalyserName() {
		return analyserName;
	}

	public void setAnalyserName(String analyserName) {
		this.analyserName = analyserName;
	}

	public String getDefaultScannableName() {
		return defaultScannableName;
	}

	public void setDefaultScannableName(String defaultScannable) {
		this.defaultScannableName = defaultScannable;
	}

	public IObservable getLiveDataDispatcher() {
		return liveDataDispatcher;
	}

	public void setLiveDataDispatcher(IObservable liveDataDispatcher) {
		this.liveDataDispatcher = liveDataDispatcher;
	}

	public int[] getOrder() {
		return order;
	}

	public void setOrder(int[] order) {
		this.order = order;
	}
}
