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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.richbeans.api.generator.RichbeansAnnotations.RowDeleteAction;

public class TableBean {
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	private List<TableItemBean> list = new ArrayList<>();

	public void addItem(TableItemBean newBean) {
		List<TableItemBean> newList = new ArrayList<>(getList());
		newList.add(newBean);
		setList(newList);
	}
	public void clearList() {
		setList(new ArrayList<>());
	}
	public void delete(TableItemBean toDelete){
		List<TableItemBean> newList = new ArrayList<>(getList());
		newList.remove(toDelete);
		setList(newList);
	}

	@RowDeleteAction("delete")
	public List<TableItemBean> getList() {
		return Collections.unmodifiableList(list);
	}
	public void setList(List<TableItemBean> list) {
		List<TableItemBean> originalList = this.list;
		this.list = list;
		pcs.firePropertyChange("list", originalList, list);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(listener);
	}
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(listener);
	}
}
