/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.client.logpanel.view;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class LogpanelView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "uk.ac.gda.client.logpanel.view";

	private Logpanel logpanel;

	public Logpanel getLogpanel() {
		return logpanel;
	}

	@Override
	public void createPartControl(Composite parent) {
		logpanel = new Logpanel(parent, SWT.NONE);

		IStatusLineManager manager = getViewSite().getActionBars().getStatusLineManager();
		manager.setMessage(String.format("Receiving from log server %s:%d", logpanel.getLogServerHost(), logpanel.getLogServerOutPort())); 
	}

	@Override
	public void setFocus() {
		logpanel.setFocus();
	}

}
