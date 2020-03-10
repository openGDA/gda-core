/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.util.converters;

import javax.measure.Quantity;

/**
 * Base class for 'simple' converters that implement the {@link IConverter} interface, converting from one
 * {@link Quantity} to another {@link Quantity}, using an object that implements the
 * {@link IReloadableQuantitiesConverter} interface.
 */
public abstract class SimpleConverterBase<S extends Quantity<S>, T extends Quantity<T>>
		implements IConverter<Quantity<S>, Quantity<T>> {

	protected IReloadableQuantitiesConverter<S, T> converter;

	@Override
	public Quantity<T> toTarget(Quantity<S> source) throws Exception {
		@SuppressWarnings("unchecked")
		final Quantity<S>[] sources = new Quantity[] { source };
		final Object[] moveables = { null }; // this means we get a result set with 1 entry
		return converter.calculateMoveables(sources, moveables)[0];
	}

	@Override
	public Quantity<S> toSource(Quantity<T> target) throws Exception {
		@SuppressWarnings("unchecked")
		final Quantity<T>[] targets = new Quantity[] { target };
		final Object[] moveables = { null }; // this means we get a result set with 1 entry
		return converter.toSource(targets, moveables)[0];
	}

	@Override
	public boolean sourceMinIsTargetMax() {
		return converter.sourceMinIsTargetMax();
	}

	public IReloadableQuantitiesConverter<S, T> getConverter() {
		return converter;
	}

}