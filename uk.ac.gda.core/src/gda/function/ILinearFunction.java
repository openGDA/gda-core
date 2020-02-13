/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

import java.util.function.Function;

import javax.measure.Quantity;

import gda.factory.Findable;

public interface ILinearFunction<T extends Quantity<T>, R extends Quantity<R>> extends Findable, Function<Quantity<T>, Quantity<R>> {

	Quantity<R> getInterception();

	void setInterception(Quantity<R> interception);

	Quantity<R> getSlopeDividend();

	void setSlopeDividend(Quantity<R> slopeDividend);

	Quantity<T> getSlopeDivisor();

	void setSlopeDivisor(Quantity<T> slopeDivisor);

	/**
	 * Allow client to get the string value through an RMI proxy
	 * <p>
	 * Calls to toStrng() are captured by the proxy
	 *
	 * @return the function as a String
	 */
	String getAsString();
}
