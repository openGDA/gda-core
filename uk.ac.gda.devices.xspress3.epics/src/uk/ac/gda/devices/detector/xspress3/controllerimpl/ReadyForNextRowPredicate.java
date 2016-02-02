/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.detector.xspress3.controllerimpl;

import gda.observable.Predicate;
import uk.ac.gda.devices.detector.xspress3.ReadyForNextRow;

public class ReadyForNextRowPredicate implements Predicate<ReadyForNextRow> {

	private ReadyForNextRow readyForNextRow;

	public ReadyForNextRowPredicate(ReadyForNextRow readyForNextRow) {
		this.readyForNextRow = readyForNextRow;
	}

	@Override
	public boolean apply(ReadyForNextRow object) {
		return (object == readyForNextRow);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((readyForNextRow == null) ? 0 : readyForNextRow.hashCode());
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
		ReadyForNextRowPredicate other = (ReadyForNextRowPredicate) obj;
		if (readyForNextRow != other.readyForNextRow)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ReadyForNextRowPredicate [readyForNextRow=" + readyForNextRow + "]";
	}

}
