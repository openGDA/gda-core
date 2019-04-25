/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.rcp.views;

import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.springframework.beans.factory.InitializingBean;

public class OpenViewListBoxCompositeFactory implements CompositeFactory, InitializingBean {
	private String tooltipText;
	private List<OpenViewOption> options;
	private String label;

	@Override
	public void afterPropertiesSet() throws Exception {
		if (tooltipText == null) {
			throw new IllegalArgumentException("tooltipText is null");
		}
		if (label == null) {
			throw new IllegalArgumentException("label is null");
		}
		if (options == null) {
			throw new IllegalArgumentException("options is null");
		}
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		return new OpenViewListBoxComposite(parent, style, label, tooltipText, options);
	}

	public List<OpenViewOption> getOptions() {
		return options;
	}

	public void setOptions(List<OpenViewOption> options) {
		this.options = options;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getTooltipText() {
		return tooltipText;
	}

	public void setTooltipText(String tooltipText) {
		this.tooltipText = tooltipText;
	}
}
