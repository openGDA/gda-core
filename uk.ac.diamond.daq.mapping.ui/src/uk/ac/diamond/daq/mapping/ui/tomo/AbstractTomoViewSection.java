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

package uk.ac.diamond.daq.mapping.ui.tomo;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.diamond.daq.mapping.api.TensorTomoScanBean;

// TOO somehow link with AbstractMappingSection?
abstract class AbstractTomoViewSection {

	protected final TensorTomoScanSetupView tomoView;
	protected final DataBindingContext dataBindingContext = new DataBindingContext();

	protected AbstractTomoViewSection(TensorTomoScanSetupView tomoView) {
		this.tomoView = tomoView;
	}

	public abstract void createControls(Composite parent);

	public abstract void configureScanBean(ScanBean scanBean);

	protected TensorTomoScanBean getTomoBean() {
		return tomoView.getTomoBean();
	}

	protected IEclipseContext getEclipseContext() {
		return tomoView.getEclipseContext();
	}

	protected <S> S getService(Class<S> serviceClass) {
		return getEclipseContext().get(serviceClass);
	}

	protected void createSeparator(Composite parent) {
		final Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(separator);
	}

	protected void asyncExec(Runnable runnable) {
		getService(UISynchronize.class).asyncExec(runnable);
	}

	protected Composite createComposite(Composite parent, int numColumns, boolean margins) {
		final Composite composite = new Composite(parent, SWT.NONE);
		if (margins) {
			GridLayoutFactory.swtDefaults().numColumns(numColumns).applyTo(composite);
		} else {
			GridLayoutFactory.fillDefaults().numColumns(numColumns).applyTo(composite);
		}

		GridDataFactory.fillDefaults().grab(true, false).applyTo(composite);
		return composite;
	}

}
