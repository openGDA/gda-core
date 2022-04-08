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

package uk.ac.diamond.daq.gasrig.api;

import java.util.List;
import java.util.Map;

import gda.factory.Findable;
import gda.observable.IObservable;

public interface IGasRig extends Findable, IObservable {

	public List<? extends IGas> getNonCabinetGases();

	public List<? extends ICabinet> getCabinets();

	public IGasMix getGasMix(int lineNumber) throws GasRigException;

	public Map<Integer, ? extends IGasMix> getGasMixes();

	public void runDummySequence();
}
