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

public class DoublyNestedExample extends GuiGeneratorRunnerBase<DoubleWrapperBean> {

	public static void main(String[] args) {
		new DoublyNestedExample().run();
	}

	@Override
	public DoubleWrapperBean createTestObject() {

		WrapperBean wrapperBean = new NestedExample().createTestObject();
		wrapperBean.setDescription("This is a nested object which \ncontains more nested objects\n");

		DoubleWrapperBean outer = new DoubleWrapperBean();
		outer.setName("Multi-level nesting example");
		outer.setDescription("This example shows an auto-generated GUI for \n"
				+ "multi-level nested objects.\n\n"
				+ "The two-way data binding on the Updating Bean \n"
				+ "should still work correctly even when it is deeply \n"
				+ "nested. Try clicking the \"Update\" box to check.");
		outer.setWrapperBean(wrapperBean);

		return outer;
	}

}
