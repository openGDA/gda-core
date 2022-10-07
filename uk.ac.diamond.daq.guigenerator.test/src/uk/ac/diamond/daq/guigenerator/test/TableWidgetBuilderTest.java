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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.metawidget.inspector.InspectionResultConstants.NAME;
import static org.metawidget.inspector.InspectionResultConstants.PARAMETERIZED_TYPE;
import static org.metawidget.inspector.InspectionResultConstants.TYPE;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.metawidget.swt.SwtMetawidget;

import uk.ac.diamond.daq.guigenerator.TableWidgetBuilder;
import uk.ac.diamond.daq.guigenerator.example.TableBean;
import uk.ac.diamond.daq.guigenerator.example.TableItemBean;

public class TableWidgetBuilderTest {
	private Shell shell;

	@Test
	public void testCreatesTablesForEmptyLists(){
		SwtMetawidget metawidget = new SwtMetawidget(shell, SWT.NONE);
		metawidget.setToInspect(new TableBean());
		Control widget = new TableWidgetBuilder().buildWidget(null, getAttributesMap(), metawidget);

		assertThat(widget, instanceOf(Table.class));

		Table table = (Table)widget;
		assertThat(table.getColumn(0).getText(), is("Name"));
		assertThat(table.getColumn(1).getText(), is("Age"));
		assertThat(table.getItemCount(), is(0));
	}

	@Test
	public void testCreatesTablesForFullLists(){
		TableBean bean = new TableBean();
		TableItemBean tableItem = new TableItemBean();
		tableItem.setName("bob");
		tableItem.setAge(99);
		bean.addItem(tableItem);

		SwtMetawidget metawidget = new SwtMetawidget(shell, SWT.NONE);
		metawidget.setToInspect(bean);
		Control widget = new TableWidgetBuilder().buildWidget(null, getAttributesMap(), metawidget);

		assertThat(widget, instanceOf(Table.class));

		TableItem[] items = ((Table)widget).getItems();
		assertThat(items.length, is(1));
		assertThat(items[0].getText(0), is("bob"));
		assertThat(items[0].getText(1), is("99"));
	}


	@Test
	public void testDoesntCreateAnythingForOtherTypes(){
		Map<String,String> map = new HashMap<>();
		map.put(TYPE, "java.util.HashMap");

		SwtMetawidget metawidget = new SwtMetawidget(shell, SWT.NONE);
		Control widget = new TableWidgetBuilder().buildWidget(null, map, metawidget);
		assertThat(widget, is(nullValue()));
	}

	@Before
	public void createShell(){
		this.shell = new Shell(Display.getDefault());
	}

	@After
	public void dispose(){
		Display.getDefault().dispose();
	}

	public Map<String, String> getAttributesMap(){
		Map<String,String> attributes = new HashMap<>();
		attributes.put(TYPE, "java.util.List");
		attributes.put(PARAMETERIZED_TYPE, "uk.ac.diamond.daq.guigenerator.example.TableItemBean");
		attributes.put(NAME, "list");
		return attributes;
	}
}
