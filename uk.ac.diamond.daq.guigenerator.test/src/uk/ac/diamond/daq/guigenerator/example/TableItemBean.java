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

import org.eclipse.richbeans.api.generator.RichbeansAnnotations.MaximumValue;
import org.eclipse.richbeans.api.generator.RichbeansAnnotations.MinimumValue;
import org.metawidget.inspector.annotation.UiComesAfter;

public class TableItemBean {
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(listener);
	}
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(listener);
	}

	private String name;
	private int age;

	public String getName() {
		return name;
	}
	public void setName(String newValue) {
		Object oldValue = this.name;
		this.name = newValue;
		pcs.firePropertyChange("name", oldValue, newValue);
	}
	@UiComesAfter("name")
	@MinimumValue("10")
	@MaximumValue("100")
	public int getAge() {
		return age;
	}
	public void setAge(int newValue) {
		Object oldValue = this.age;
		this.age = newValue;
		pcs.firePropertyChange("age", oldValue, newValue);
	}
}
