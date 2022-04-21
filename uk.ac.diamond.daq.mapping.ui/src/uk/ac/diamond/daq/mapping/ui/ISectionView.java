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

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * A view made up of {@link IViewSection}s  that manipulate a bean of some kind.
 *
 * @param <B> bean class
 */
public interface ISectionView<B> {

	/**
	 * Returns the bean for this view.
	 *
	 * @return the bean
	 */
	public B getBean();

	/**
	 * Creates the view given the parent composite and {@link MPart}
	 * @param parent parent composite
	 * @param part part
	 * @implNote implementation must have @PostConstruct annotation
	 */
	public void createView(Composite parent, MPart part);

	/**
	 * Saves the state of the view to given part.
	 * @param part part
	 * @implNote implementation must have @SaveState annotation
	 */
	public void saveState(MPart part);

	/**
	 * Disposes of the view.
	 * @implNote implementation must have @PreDestroy annotation
	 */
	public void dispose();

	/**
	 * @return the eclipse context
	 */
	public IEclipseContext getEclipseContext();

	/**
	 * Returns the service implementation of the given class.
	 * @param <S> service class
	 * @param serviceClass service class
	 * @return service implementation
	 */
	public <S> S getService(Class<S> serviceClass);

	/**
	 * @return the shell
	 */
	public Shell getShell();

	/**
	 * Update the status label.
	 */
	public void updateStatusLabel();

	/**
	 * Sets the status message to that given
	 * @param message status message
	 */
	public void setStatusMessage(String message);

	/**
	 * Gets the section with the given class, or <code>null</code> if  none.
	 *
	 * @param <V> view class
	 * @param <S> section class
	 * @param sectionClass section class
	 * @return section for class, or <code>null</code> if none
	 */
	public <V extends ISectionView<B>, S extends IViewSection<B, V>> IViewSection<B, V> getSection(Class<S> sectionClass);

	/**
	 * Causes this view to be relayed out.
	 */
	public void relayout();

}
