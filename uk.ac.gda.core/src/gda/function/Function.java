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

package gda.function;

import gda.factory.Configurable;
import gda.factory.Findable;

import org.jscience.physics.quantities.Quantity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class of a functions of a single (Quantity) variable. Implements Findable and so that Functions can be specified
 * in XML
 */

public abstract class Function implements Findable, Configurable {
	protected static final Logger logger = LoggerFactory.getLogger(Function.class);

	private String name;

	/**
	 * Evaluates the function at the given xValue.
	 * 
	 * @param xValue
	 *            the xValue (a Quantity)
	 * @return the value of the function at xValue
	 */
	public abstract Quantity evaluate(Quantity xValue);

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}
}
