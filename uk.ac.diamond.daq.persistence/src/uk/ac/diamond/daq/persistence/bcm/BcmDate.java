/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package uk.ac.diamond.daq.persistence.bcm;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Date class used for pretty printing, i.e. we mostly override toString here
 *
 */
public class BcmDate extends Date {

	/**
	 *
	 */
	private static final long serialVersionUID = 5290207432221195793L;

	/**
	 *
	 */
	public BcmDate() {
		super();
	}

	/**
	 * @param date
	 */
	public BcmDate(long date) {
		super(date);
	}

	/**
	 * @param date
	 */
	public BcmDate(Date date) {
		super(date.getTime());
	}

	@Override
	public String toString() {
		SimpleDateFormat formatter = new SimpleDateFormat(BeamlineConfigurationManager.dateFormat);
		return formatter.format(this);
	}
}
