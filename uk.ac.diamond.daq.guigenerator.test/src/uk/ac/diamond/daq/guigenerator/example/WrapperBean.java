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

import org.metawidget.inspector.annotation.UiComesAfter;
import org.metawidget.inspector.annotation.UiLarge;
import org.metawidget.inspector.annotation.UiReadOnly;

public class WrapperBean {
	private String name;
	private String description;
	private SimpleBean simpleBean;
	private UpdatingBean updatingBean;

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
	public SimpleBean getSimpleBean() {
		return simpleBean;
	}
	public void setSimpleBean(SimpleBean simpleBean) {
		this.simpleBean = simpleBean;
	}
	public UpdatingBean getUpdatingBean() {
		return updatingBean;
	}
	public void setUpdatingBean(UpdatingBean updatingBean) {
		this.updatingBean = updatingBean;
	}
}
