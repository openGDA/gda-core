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

package uk.ac.diamond.daq.mapping.ui.experiment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;
import uk.ac.diamond.daq.mapping.ui.Activator;

public abstract class AbstractMappingSection implements IMappingSection {

	private MappingExperimentView mappingView;

	private Label separator;

	protected DataBindingContext dataBindingContext;

	protected UISynchronize uiSync;

	private boolean createSeparator = true;

	@Override
	public void initialize(MappingExperimentView mappingView) {
		this.mappingView = mappingView;
		uiSync = getService(UISynchronize.class);
	}

	@Override
	public void createControls(Composite parent) {
		// Create the separator if required
		if (createSeparator) {
			separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
			GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(separator);
		}
	}

	protected Shell getShell() {
		return mappingView.getShell();
	}

	protected <S> S getService(Class<S> serviceClass) {
		return mappingView.getEclipseContext().get(serviceClass);
	}

	protected IRunnableDeviceService getRunnableDeviceService() {
		return mappingView.getRunnableDeviceService();
	}

	protected IMappingExperimentBean getMappingBean() {
		return mappingView.getBean();
	}

	protected MappingExperimentView getMappingView() {
		return mappingView;
	}

	protected Image getImage(String imagePath) {
		return Activator.getImage(imagePath);
	}

	protected void relayoutMappingView() {
		mappingView.relayout();
		mappingView.recalculateMinimumSize();
	}

	protected IEclipseContext getEclipseContext() {
		return mappingView.getEclipseContext();
	}

	@Override
	public boolean createSeparator() {
		return createSeparator;
	}

	@Override
	public void setFocus() {
		// do nothing, subclasses may override
	}

	@Override
	public void dispose() {
		// do nothing, subclasses may override
	}

	protected void updateStatusLabel() {
		mappingView.updateStatusLabel();
	}

	protected void setStatusMessage(String message) {
		mappingView.setStatusMessage(message);
	}

	/**
	 * Updates this section based on the mapping bean.
	 */
	@Override
	public void updateControls() {
		// Default implementation does nothing. Subclasses may override.
	}

	/**
	 * Default implementation does nothing since most data is saved through the mapping bean
	 */
	@Override
	public void saveState(@SuppressWarnings("unused") Map<String, String> persistedState) {

	}

	/**
	 * Default implementation does nothing since most data is loaded through the mapping bean
	 */
	@Override
	public void loadState(@SuppressWarnings("unused") Map<String, String> persistedState) {

	}

	/**
	 * Show or hide the separator (if there is one)
	 * <p>
	 * You may have to call {@link #relayoutMappingView()} after doing this
	 *
	 * @param visible
	 *            true if the separator is to be shown, false if it is to be hidden
	 */
	protected void setSeparatorVisibility(boolean visible) {
		if (separator != null) {
			separator.setVisible(visible);
			((GridData) separator.getLayoutData()).exclude = !visible;
		}
	}

	/**
	 * Remove all existing bindings in {@link #dataBindingContext}
	 */
	protected void removeOldBindings() {
		if (dataBindingContext == null) {
			return;
		}

		// copy the bindings to prevent concurrent modification exception
		final List<Binding> bindings = new ArrayList<>(dataBindingContext.getBindings());
		for (Binding binding : bindings) {
			dataBindingContext.removeBinding(binding);
			binding.dispose();
		}
	}

	protected ScanRequest getScanRequest(final IMappingExperimentBean mappingBean) {
		final ScanRequestConverter converter = getService(ScanRequestConverter.class);
		return converter.convertToScanRequest(mappingBean);
	}

	public void setCreateSeparator(boolean createSeparator) {
		this.createSeparator = createSeparator;
	}
}
