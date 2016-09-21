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

package uk.ac.diamond.daq.guigenerator.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.junit.Test;
import org.metawidget.inspector.annotation.UiHidden;

import uk.ac.diamond.daq.guigenerator.example.TableBean;
import uk.ac.diamond.daq.guigenerator.example.TableExample;
import uk.ac.diamond.daq.guigenerator.example.TableItemBean;

public class TableExampleTest extends GuiGeneratorTestBase {
	@Test
	public void testUpdatesItems() throws Exception {
		TableBean updatingBean = new TableExample().createTestObject();
		metawidget = (Composite) guiGenerator.generateGui(updatingBean, shell);

		Table table = (Table)getNamedControl("list");
		assertThat(table.getItem(0).getText(0), is("kevin"));
		updatingBean.getList().get(0).setName("not kevin");

		assertThat(table.getItem(0).getText(0), is("not kevin"));
	}

	@Test
	public void testRemovesItems() throws Exception {
		TableBean updatingBean = new TableExample().createTestObject();
		metawidget = (Composite) guiGenerator.generateGui(updatingBean, shell);

		Table table = (Table)getNamedControl("list");
		assertThat(table.getItemCount(), is(1));
		updatingBean.clearList();

		assertThat(table.getItemCount(), is(0));
	}

	@Test
	public void testAddsItems() throws Exception {
		TableBean updatingBean = new TableExample().createTestObject();
		metawidget = (Composite) guiGenerator.generateGui(updatingBean, shell);

		Table table = (Table)getNamedControl("list");
		assertThat(table.getItemCount(), is(1));
		updatingBean.addItem(new TableItemBean());

		assertThat(table.getItemCount(), is(2));
	}

	@Test
	public void testTracksAddedItems() throws Exception {
		TableBean updatingBean = new TableExample().createTestObject();
		metawidget = (Composite) guiGenerator.generateGui(updatingBean, shell);
		Table table = (Table)getNamedControl("list");
		TableItemBean newBean = new TableItemBean();
		newBean.setName("Bob");
		updatingBean.addItem(newBean);

		assertThat(table.getItem(1).getText(0), is("Bob"));

		newBean.setName("Not Bob");

		assertThat(table.getItem(1).getText(), is("Not Bob"));
	}

	@Test
	public void testHiddenListFieldIsStub() throws Exception {
		TableBean tableBean = new TableBean() {
			private List<Object> hiddenList = new ArrayList<>();

			@UiHidden
			public List<Object> getHiddenList() {
				return hiddenList;
			}
		};
		metawidget = (Composite) guiGenerator.generateGui(tableBean, shell);
		Control control = getNamedControl("hiddenList");
		assertThat(control.getClass().getName(), is(equalTo("org.metawidget.swt.Stub")));
	}
}
