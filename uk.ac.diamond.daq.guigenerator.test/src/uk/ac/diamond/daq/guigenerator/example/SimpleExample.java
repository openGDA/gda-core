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

package uk.ac.diamond.daq.guigenerator.example;

import uk.ac.diamond.daq.guigenerator.example.SimpleBean.Type;

/**
 * A simple example of the GUI generator, using a bean with a few fields of different types.
 */
public class SimpleExample extends GuiGeneratorRunnerBase<SimpleBean> {

	public static void main(String[] args) {
		new SimpleExample().run();
	}

	@Override
	public SimpleBean createTestObject() {
		SimpleBean testBean = new SimpleBean();
		testBean.setName("Read-only name");
		testBean.setDescription("This is an editable, \nmulti-line description\n");
		testBean.setType(Type.TWO);
		testBean.setCount(4);
		testBean.setX(-5.3); // X is annotated to have a minimum value of zero, so the GUI will mark this as invalid
		testBean.setY(11.8);
		return testBean;
	}
}
