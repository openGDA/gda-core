/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.client.gui.exitslit.configuration;

import static uk.ac.diamond.daq.client.gui.exitslit.configuration.ConfigureExitSlitsUtils.COLOUR_WHITE;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;

/**
 * Base class for a composite to control a step in the exit slit configuration process
 * <p>
 * This class provides basic communication between the composite and the enclosing dialog, but no UI elements.
 */
public abstract class ConfigureExitSlitsComposite extends Composite implements IObservable {

	protected static final int NUDGE_BUTTON_WIDTH = 28;

	private final String title;
	private final String description;

	private final ObservableComponent observable = new ObservableComponent();

	protected final IObserver updateObserver = this::onUpdate;

	public ConfigureExitSlitsComposite(Composite parent, int style, String title, String description) {
		super(parent, style);
		this.title = title;
		this.description = description;
		setBackground(COLOUR_WHITE);
	}

	public abstract boolean canGoToNextPage();

	public abstract boolean canGoToPreviousPage();

	protected boolean canCancel() {
		return true;
	}

	protected boolean canFinish() {
		return false;
	}

	protected abstract void updateButtons();

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	protected void onUpdate(@SuppressWarnings("unused") Object source, @SuppressWarnings("unused") Object arg) {
		Display.getDefault().asyncExec(this::updateButtons);
		notifyIObservers(this, arg);
	}

	@Override
	public void addIObserver(IObserver observer) {
		observable.addIObserver(observer);
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		observable.deleteIObserver(observer);
	}

	@Override
	public void deleteIObservers() {
		observable.deleteIObservers();
	}

	public void notifyIObservers(Object theObserved, Object changeCode) {
		observable.notifyIObservers(theObserved, changeCode);
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			// The state of the hardware may have changed since this page was last shown
			updateButtons();
		}
		super.setVisible(visible);
	}

	@Override
	public void dispose() {
		deleteIObservers();
		super.dispose();
	}
}
