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

import java.util.Map;

import org.eclipse.swt.widgets.Composite;

/**
 * A section within an {@link ISectionView}. A section encapsulates
 * the UI components for a particular aspect of a view, i.e. to setup a particular
 * part of a scan
 *
 * @param <B> the bean class
 * @param <V> the view class
 */
public interface IViewSection<B, V extends ISectionView<B>> {

	/**
	 * Initialize the section.
	 *
	 * @param view the parent view
	 */
	public void initialize(V view);

	/**
	 * Create the controls for this section.
	 *
	 * @param parent the parent {@link Composite} for this section
	 */
	public void createControls(Composite parent);

	/**
	 * Returns the view, an instance of {@link ISectionView}
	 * @return the view
	 */
	public V getView();

	/**
	 * Returns the bean.
	 * @return the bean
	 */
	public B getBean();

	/**
	 * Performs any necessary cleanup for this section.
	 */
	public void dispose();

	/**
	 * Save the state of this view, if necessary to the given map
	 *
	 * @param state state map to save to
	 */
	public void saveState(Map<String, String> state);

	/**
	 * Load the state of the view from the given state.
	 *
	 * @param state state map to load from
	 */
	public void loadState(Map<String, String> state);

	/**
	 * Updates this section, based on the bean. This method is usually called when the
	 * bean has changed outside of this view.
	 */
	public void updateControls();

	/**
	 * Do whatever is required when this section gets focus
	 */
	public void setFocus();

	/**
	 * Returns the service implementation of the given class.
	 * @param <S> service class
	 * @param serviceClass service class
	 * @return service implementation
	 */
	public <S> S getService(Class<S> serviceClass);



}
