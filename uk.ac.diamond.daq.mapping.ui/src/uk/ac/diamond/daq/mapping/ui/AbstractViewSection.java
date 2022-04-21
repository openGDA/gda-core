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

package uk.ac.diamond.daq.mapping.ui;

import java.net.URI;
import java.util.Map;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;

/**
 * Abstract superclass of classes that can be a section in an {@link ISectionView}.
 *
 * @param <B> bean class
 * @param <V> view class
 */
public abstract class AbstractViewSection<B, V extends ISectionView<B>> implements IViewSection<B, V> {

	private static final Logger logger = LoggerFactory.getLogger(AbstractViewSection.class);

	private DataBindingContext dataBindingContext;

	private V view;

	private Label separator;

	protected boolean shouldCreateSeparator = true;

	@Override
	public void initialize(V view) {
		this.view = view;
	}

	@Override
	public V getView() {
		return view;
	}

	@Override
	public B getBean() {
		return view.getBean();
	}

	public void setCreateSeparator(boolean createSeparator) {
		this.shouldCreateSeparator = createSeparator;
	}

	protected void createSeparator(Composite parent) {
		separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(separator);
	}


	@Override
	public void createControls(Composite parent) {
		// Create the separator if required
		if (shouldCreateSeparator) {
			createSeparator(parent);
		}
	}

	/**
	 * Creates a {@link Composite} with a {@link GridLayout} with the given number of columns, optionally having
	 * default margins. The composite will grab horizontally, but not vertically.
	 * @param parent parent composite
	 * @param numColumns number of columns
	 * @param margins <code>true</code> for default margins ({@link GridLayout#marginWidth}
	 * 		and {@link GridLayout#marginHeight} default value of <code>5</code>), <code>false</code> for no margins.
	 * @return the composite
	 */
	protected Composite createComposite(Composite parent, int numColumns, boolean margins) {
		return createComposite(parent, numColumns, margins, false);
	}

	/**
	 * Creates a {@link Composite} with a {@link GridLayout} with the given number of columns, optionally
	 * having default margins and/or grabbing vertical space. The composite will grab horizontal space, and
	 * will also grab vertical space if <code>grabVertical</code> is <code>true</code>.
	 * @param parent parent composite
	 * @param numColumns number of columns
	 * @param margins <code>true</code> for default margins ({@link GridLayout#marginWidth}
	 * 		and {@link GridLayout#marginHeight} default value of <code>5</code>), <code>false</code> for no margins.
	 * @param grabVertical
	 * @return the composite
	 */
	protected Composite createComposite(Composite parent, int numColumns, boolean margins, boolean grabVertical) {
		final Composite composite = new Composite(parent, SWT.NONE);
		if (margins) { // configure the layout for the composite
			GridLayoutFactory.swtDefaults().numColumns(numColumns).applyTo(composite);
		} else {
			GridLayoutFactory.fillDefaults().numColumns(numColumns).applyTo(composite);
		}

		// set the grid data for the composite
		GridDataFactory.fillDefaults().grab(true, grabVertical).applyTo(composite);
		return composite;
	}

	@Override
	public void updateControls() {
		// do nothing, subclasses may override
	}

	@Override
	public void saveState(Map<String, String> state) {
		// do nothing, subclasses may override
	}

	@Override
	public void loadState(Map<String, String> state) {
		// do nothing, subclasses may override
	}

	@Override
	public void setFocus() {
		// do nothing, subclasses may override
	}

	@Override
	public void dispose() {
		// do nothing, subclasses may override
	}

	protected IEclipseContext getEclipseContext() {
		return getView().getEclipseContext();
	}

	@Override
	public <S> S getService(Class<S> serviceClass) {
		return getView().getEclipseContext().get(serviceClass);
	}

	protected <T> T getRemoteService(Class<T> klass) {
		final IEventService eventService = getEclipseContext().get(IEventService.class);
		try {
			URI jmsURI = new URI(LocalProperties.getActiveMQBrokerURI());
			return eventService.createRemoteService(jmsURI, klass);
		} catch (Exception e) {
			logger.error("Error getting remote service {}", klass, e);
			return null;
		}
	}

	protected Shell getShell() {
		return getView().getShell();
	}


	protected DataBindingContext getDataBindingContext() {
		if (dataBindingContext == null) {
			dataBindingContext = new DataBindingContext();
		}

		return dataBindingContext;
	}

	/**
	 * Remove all existing bindings in the {@link DataBindingContext}.
	 */
	protected void removeOldBindings() {
		if (dataBindingContext == null) return;
		for (Binding binding : dataBindingContext.getBindings()) {
			binding.dispose();
		}
	}

	/**
	 * Show or hide the separator (if there is one)
	 * <p>
	 * You may have to call {@link #relayoutView()} after doing this
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

	protected void relayoutView() {
		getView().relayout();
	}

	protected String getVisitConfigDir() {
		return getService(IFilePathService.class).getVisitConfigDir();
	}

	protected Image getImage(String imagePath) {
		return Activator.getImage(imagePath);
	}

	protected void updateStatusLabel() {
		getView().updateStatusLabel();
	}

	protected void setStatusMessage(String message) {
		getView().setStatusMessage(message);
	}

	protected void asyncExec(Runnable runnable) {
		getService(UISynchronize.class).asyncExec(runnable);
	}

	protected void syncExec(Runnable runnable) {
		getService(UISynchronize.class).syncExec(runnable);
	}

}
