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

package uk.ac.gda.exafs.ui.xes;

import java.util.List;

import org.eclipse.richbeans.api.widget.IFieldWidget;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;

public abstract class XesControlsBuilder implements IObservable {

	protected ObservableComponent observableComponent = new ObservableComponent();

	public abstract void createControls(Composite parent);

	@Override
	public void addIObserver(IObserver observer) {
		observableComponent.addIObserver(observer);
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		observableComponent.deleteIObserver(observer);
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}

	public void notifyObservers(Object source, Object event) {
		observableComponent.notifyIObservers(source, event);
	}

	/**
	 * Show/hide a widget, depending on value of 'show'.
	 * The parent widget is also hidden (this typically a Group or other 'containing' composite)
	 *
	 * @param widget
	 * @param show - set to true to show the widget, false to hide it.
	 */
	protected void showWidget(Composite widget, boolean show) {
		setVisible(widget, show);
		setVisible(widget.getParent(), show);
	}

	protected void setVisible(Composite comp, boolean visible) {
		GridData gridData = (GridData) comp.getLayoutData();
		gridData.exclude = !visible;
		comp.setVisible(visible);
	}

	protected void setupFieldWidgets(List<? extends IFieldWidget> fieldComposites) {
		fieldComposites.forEach(this::setupFieldWidget);
	}

	protected void setupFieldWidget(IFieldWidget fieldComposite) {
		fieldComposite.on();
		fieldComposite.addValueListener(l -> notifyObservers(this, fieldComposite));
	}

}
