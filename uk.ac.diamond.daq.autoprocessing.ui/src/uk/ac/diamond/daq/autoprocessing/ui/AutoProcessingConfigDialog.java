/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.autoprocessing.ui;

import java.net.URI;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import gda.autoprocessing.AutoProcessingBean;

public class AutoProcessingConfigDialog extends Dialog {

	private URI uri;
	private IMarshallerService service;
	private AutoProcessingConfigComposite configComposite;
	private AutoProcessingBean configToShow;

	protected AutoProcessingConfigDialog(Shell parentShell, URI uri, IMarshallerService service) {
		super(parentShell);
		this.uri = uri;
		this.service = service;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		configComposite = new AutoProcessingConfigComposite(parent, service, uri);
		configComposite.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).create());
		if (configToShow != null) {
			configComposite.setupFromConfig(configToShow);
		}
		return configComposite;
	}

	public void setConfigToShow(AutoProcessingBean config) {
		configToShow = config;
	}

	public AutoProcessingBean getConfig() {
		AutoProcessingBean wrapper = configComposite.getConfiguration(false);

		wrapper.setActive(true);
		return wrapper;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected Point getInitialSize() {
		Rectangle bounds = getShell().getBounds();
		return new Point((int)(bounds.width*0.6),(int)(bounds.height*0.6));
	}
}
