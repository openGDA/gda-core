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

import org.eclipse.richbeans.api.generator.RichbeansAnnotations.MinimumValue;
import org.metawidget.inspector.annotation.UiComesAfter;
import org.metawidget.inspector.annotation.UiLarge;
import org.metawidget.inspector.annotation.UiReadOnly;
import org.metawidget.inspector.annotation.UiRequired;

public class SimpleBean {

	public enum Type {
		ONE, TWO;
	}

	private String name;
	private String description;
	private Type type;
	private int count;
	private double x;
	private double y;

	@UiReadOnly
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@UiLarge
	@UiComesAfter("name")
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	@UiRequired
	@UiComesAfter("description")
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	@MinimumValue("0")
	@UiComesAfter("type")
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	@MinimumValue("0")
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	@MinimumValue("0")
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
}