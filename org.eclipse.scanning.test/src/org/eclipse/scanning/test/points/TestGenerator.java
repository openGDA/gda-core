/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.test.points;

import java.util.Iterator;
import java.util.List;

import org.eclipse.scanning.api.points.AbstractGenerator;
import org.eclipse.scanning.api.points.IPosition;

public class TestGenerator extends AbstractGenerator<TestGeneratorModel> {

	TestGenerator(TestGeneratorModel model){
		super(model);
	}

	@Override
	public void validate(TestGeneratorModel model) { }

	@Override
	public int[] getShape() {
		throw new UnsupportedOperationException("Not designed to be run, just to test extension point for when people want to load by extension!");
	}

	@Override
	public int size() {
		throw new UnsupportedOperationException("Not designed to be run, just to test extension point for when people want to load by extension!");
	}

	@Override
	public int getRank() {
		throw new UnsupportedOperationException("Not designed to be run, just to test extension point for when people want to load by extension!");
	}

	@Override
	public Iterator<IPosition> iterator() {
		throw new UnsupportedOperationException("Not designed to be run, just to test extension point for when people want to load by extension!");
	}

	@Override
	public List<String> getNames() {
		throw new UnsupportedOperationException("Not designed to be run, just to test extension point for when people want to load by extension!");
	}

}
