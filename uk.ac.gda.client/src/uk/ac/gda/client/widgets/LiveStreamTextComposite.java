/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.gda.client.widgets;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Objects;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.widgets.LabelFactory;
import org.eclipse.jface.widgets.TextFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.observable.IObserver;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.api.camera.CameraControllerEvent;

/**
 * Composite with a text widget to update a property of a camera.
 *
 */
public abstract class LiveStreamTextComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(LiveStreamTextComposite.class);

	protected static final int TEXT_WIDTH = 55;

	protected static final int NUM_COLUMNS = 2;

	protected final Text text;

	protected final CameraControl cameraControl;

	protected final DataBindingContext dataBindingContext;

	/**
	 * If camera is acquiring, the camera's property cannot be updated
	 */
	protected final boolean modifyWhileCameraAcquiring;

	protected LiveStreamTextComposite(Composite parent, CameraControl cameraControl,
			boolean modifyWhileCameraAcquiring, String label) {

		super(parent, SWT.NONE);
		Objects.requireNonNull(cameraControl, "Camera control must not be null");
		this.cameraControl = cameraControl;
		GridLayoutFactory.swtDefaults().numColumns(NUM_COLUMNS).applyTo(this);

		dataBindingContext = new DataBindingContext();
		this.modifyWhileCameraAcquiring = modifyWhileCameraAcquiring;

		LabelFactory.newLabel(SWT.NONE).text(label).create(this);
		text = TextFactory.newText(SWT.BORDER).create(this);
		GridDataFactory.swtDefaults().hint(TEXT_WIDTH, SWT.DEFAULT).applyTo(text);
	}

	abstract void bindControls(Composite composite);

	abstract IStatus validateInput(Object value);


	/**
	 * Class for data binding to use to mediate between the camera control and the widget text box
	 *
	 * As the acquisition time can also be set in the console or directly in Epics, this class needs to observe the
	 * hardware and respond to {@link CameraControllerEvent}s.
	 */
	public abstract class CameraControlBinding implements IObserver {

		protected final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

		public void addPropertyChangeListener(PropertyChangeListener listener) {
			changeSupport.addPropertyChangeListener(listener);
		}

		public void removePropertyChangeListener(PropertyChangeListener listener) {
			changeSupport.removePropertyChangeListener(listener);
		}

		protected void displayError(final String message) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Camera error", message);
		}

		protected void logAndDisplayError(final String message, Exception e) {
			logger.error(message, e);
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Camera error", message);
		}

		@Override
		public void update(Object source, Object arg) {
			// subclasses override
		}

	}

}
