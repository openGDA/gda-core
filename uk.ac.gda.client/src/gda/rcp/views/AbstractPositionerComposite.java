/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package gda.rcp.views;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.jython.JythonServerFacade;
import gda.observable.IObserver;
import gda.rcp.GDAClientActivator;
import uk.ac.diamond.daq.concurrent.Async;

/**
 * This GUI elements of this class are a label for a {@link Scannable} and a 'stop' button.
 * Between these, concrete subclasses will {@link #createPositionerControl()} suitable for a particular scannable.
 * <p>
 * This base class registers a listener which calls {@link #updatePositionerControl(Object, boolean)}
 * while the scannable is moving.
 * <p>
 * Should subclasses wish to manually trigger the update job, they can call {@link #scheduleUpdateReadbackJob()}.
 */
public abstract class AbstractPositionerComposite extends Composite {

	private static final Logger logger = LoggerFactory.getLogger(AbstractPositionerComposite.class);

	// GUI Elements
	private final CLabel displayNameLabel;
	private final Button stopButton;
	private final RowData stopButtonRowData;

	// About the scannable
	private Scannable scannable;
	private String scannableName;
	private String scannableOutputFormat;
	private String displayName; // Allow a different prettier name be used if required

	private String reasonForDisallowingMove = "";

	// Update job
	private Job updateReadbackJob;

	/**
	 * @param parent the parent composite on which to draw this one
	 * @param style SWT.HORIZONTAL or SWT.VERTICAL will define a horizontal or vertical (default) layout
	 */
	public AbstractPositionerComposite(Composite parent, int style) {
		super(parent, style & ~SWT.HORIZONTAL & ~SWT.VERTICAL);

		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		parent.setBackgroundMode(SWT.INHERIT_FORCE);

		// Setup layout
		final int rowLayoutType = ((style & SWT.HORIZONTAL) != 0) ? SWT.HORIZONTAL : SWT.VERTICAL;
		final RowLayout rowLayout = new RowLayout(rowLayoutType);
		rowLayout.fill = true;
		rowLayout.center = true;
		rowLayout.marginTop = 1;
		rowLayout.marginBottom = 1;
		rowLayout.spacing = 1;
		this.setLayout(rowLayout);

		// Name label
		displayNameLabel = new CLabel(this, SWT.CENTER);

		// PositionerControl
		createPositionerControl();

		// Stop button
		stopButton = new Button(this, SWT.NONE);
		stopButton.addSelectionListener(widgetSelectedAdapter(e -> {
			try {
				scannable.stop();
			} catch (DeviceException ex) {
				logger.error("Error while stopping " + scannableName, ex);
			}
		}));
		stopButtonRowData = new RowData();
		stopButton.setLayoutData(stopButtonRowData);
		stopButton.setText("Stop");
		final ImageDescriptor stopImage = GDAClientActivator.getImageDescriptor("icons/stop.png");
		Objects.requireNonNull(stopImage, "Missing image for stop button");
		stopButton.setImage(stopImage.createImage());

		// At this time the control is built but no scannable is set so disable it.
		disable();
	}

	/**
	 * Creates the control(s) (drawn between the label and the stop button)
	 * to display and/or control the scannable's position.
	 * <p>
	 * For consistent results, implementors are advised to create widgets
	 * directly on top of {@code this} Composite.
	 */
	protected abstract void createPositionerControl();

	/**
	 * Updates the control(s) created at {@link #createPositionerControl()}
	 * to reflect the given scannable position and whether it is currently moving.
	 * <p>
	 * This method is called as soon as the scannable is set, so implementors may use it
	 * to initialise their controls.
	 */
	protected abstract void updatePositionerControl(final Object newPosition, boolean moving);

	/**
	 * Subclasses can override to check whether the requested position lies within limits, for instance.
	 * <p>
	 * If a move is disallowed, it is advised that a reason be set via {@link #setReasonForDisallowingMove(String)}
	 * so that it can be presented to the user.
	 */
	protected boolean moveAllowed(@SuppressWarnings("unused") Object newPosition) { // NOSONAR: subclasses may need to know this
		return true;
	}

	/**
	 * Specify why the the last call to {@link #moveAllowed(Object)} returned {@code false}.
	 * This will be shown to the user in an error dialog, and then cleared.
	 */
	protected void setReasonForDisallowingMove(String reason) {
		Objects.requireNonNull(reason);
		this.reasonForDisallowingMove = reason;
	}

	/**
	 * Moves the scannable to a new position.<br>
	 * Checks whether the scannable is busy before moving
	 *
	 * @param position
	 *            The demanded position
	 */
	protected void move(Object position) {
		final boolean batonHeld = JythonServerFacade.getInstance().amIBatonHolder();
		if (!batonHeld) {
			final MessageDialog dialog = new MessageDialog(Display.getDefault().getActiveShell(), "Baton not held", null,
					"You do not hold the baton, please take the baton using the baton manager.", MessageDialog.ERROR, new String[] { "Ok" }, 0);
			dialog.open();
		} else if (moveAllowed(position)) {
			try {
				if (!getScannable().isBusy()) {
					runMoveInThread(position);
				}
			} catch (DeviceException e) {
				logger.error("Error while trying to move {}", getScannable().getName(), e);
			}
		} else {
			logger.error("Cannot move {} to {}", getScannable().getName(), position);

			String message = "Move disallowed";
			if (!reasonForDisallowingMove.isEmpty()) {
				message += ":\n" + reasonForDisallowingMove;
			}
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Error moving device", message);
			setReasonForDisallowingMove("");
		}
	}

	/**
	 * Move the scannable, ensuring that the GUI is not blocked during the move.
	 * <p>
	 * In theory, we could call {@link gda.device.scannable.ScannableMotionBase#asynchronousMoveTo(Object)}, but
	 * creating our own thread here ensures that the GUI is not blocked if asynchronousmoveTo() is written in such a way
	 * that it can block.
	 *
	 * @param position
	 *            The demanded position
	 */
	private void runMoveInThread(Object position) {
		Async.execute(() -> {
			try {
				getScannable().moveTo(position);
			} catch (DeviceException e) {
				logger.error("Error while trying to move {}", getScannable().getName(), e);
			}
		});
	}

	/**
	 * Sets the control enabled
	 * <p>
	 * Equivalent to setEnabled(true)
	 *
	 * @see #setEnabled(boolean)
	 */
	private void enable() {
		setEnabled(true);
	}

	/**
	 * Sets the control disabled (grayed)
	 * <p>
	 * Equivalent to setEnabled(false)
	 *
	 * @see #setEnabled(boolean)
	 */
	private void disable() {
		setEnabled(false);
	}

	@Override
	public void setEnabled(boolean enabled) {
		if (enabled) {
			// When the control is enabled reconfigure to ensure values are current
			configure();
		}

		// Disable the controls
		displayNameLabel.setEnabled(enabled);
		stopButton.setEnabled(enabled);

		this.redraw();
	}

	private void configure() {
		if (getScannable() == null) {
			throw new IllegalStateException("Scannable is not set");
		}

		// TODO This should setup the control after the scannable is set
		// This is the job which handles updating of the composite. It need to be scheduled when a move is
		// started after which it will continue to run until the move finishes.
		updateReadbackJob = new Job("Update " + getScannable().getName() + " positioner readback value") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				boolean moving = true;
				while (moving) { // Loop which runs while scannable is moving
					try {
						moving = getScannable().isBusy();
					} catch (DeviceException e) {
						logger.error("Error while determining whether {} is busy", getScannable().getName(), e);
						return Status.CANCEL_STATUS;
					}

					// Check if the user has cancelled the job
					if (monitor.isCanceled()) {
						return Status.CANCEL_STATUS;
					}

					try {
						Thread.sleep(100); // Pause to stop loop running to fast. ~ 10 Hz
					} catch (InterruptedException e) {
						logger.error("Thread interrupted during update job for {}", getScannable().getName(), e);
						return Status.CANCEL_STATUS; // Thread interrupted so cancel update job
					}

					// Update the GUI
					updateBaseGui(moving);
					updatePositionerControl(getCurrentPosition(), moving);
				}
				return Status.OK_STATUS;
			}

		};

		// Add an observer to the scannable to start the updateReadbackJob when an event occurs such as starting to move.
		// If the job is already running a maximum of one extra will be scheduled.
		final IObserver iObserver = (source, arg) -> updateReadbackJob.schedule();
		getScannable().addIObserver(iObserver);

		this.addDisposeListener(e -> {
			getScannable().deleteIObserver(iObserver);
			updateReadbackJob.cancel();
		});

		updateReadbackJob.schedule(); // Get initial values
	}

	private void updateBaseGui(boolean moving) {
		if (stopButton != null) {
			Display.getDefault().asyncExec(() -> stopButton.setEnabled(moving));
		}
	}

	protected Object getCurrentPosition() {
		Object currentPosition = null;
		try {
			currentPosition = getScannable().getPosition();
		} catch (DeviceException e) {
			logger.error("Error while getting current position of {}", getScannable().getName(), e);
		}
		return currentPosition;
	}

	/**
	 * This sets the scannable which will be controlled and will automatically configure the control.
	 * <p>
	 * This can also be called to change the scannable controlled at any time which will reconfigure the control.
	 *
	 * @param scannable
	 *            The scannable to control
	 */
	public void setScannable(Scannable scannable) {
		if (scannable == null) {
			throw new IllegalArgumentException("Scannable cannot be set null");
		}

		this.scannable = scannable;
		this.scannableName = scannable.getName();
		this.scannableOutputFormat = scannable.getOutputFormat()[0];

		// If no display name is set when the scannable is set, set it to the scannable name
		if (displayName == null) {
			setDisplayName(scannableName);
		}
		enable();
	}

	protected Scannable getScannable() {
		return scannable;
	}

	/**
	 * Sets a different name to be used in the GUI instead of the default which is the scannable name
	 * <p>
	 * After calling this method the control will be automatically redrawn.
	 *
	 * @param displayName
	 *            The name to be used in the GUI
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
		displayNameLabel.setText(displayName);
		this.redraw();
	}

	protected String getDisplayName() {
		return displayName;
	}

	protected String getScannableOutputFormat() {
		return scannableOutputFormat;
	}

	/**
	 * Manually schedule the update readback job.
	 */
	protected void scheduleUpdateReadbackJob() {
		updateReadbackJob.schedule();
	}

	/**
	 * Hides the stop button.
	 * <p>
	 * This is useful for scannables which can't be stopped (move instantly) i.e. setting a voltage.
	 *
	 * @see #showStopButton()
	 */
	public void hideStopButton() {
		stopButton.setVisible(false);
		// Exclude the stop button so the layout will compress when it's hidden
		stopButtonRowData.exclude = true;
		this.redraw();
	}

	/**
	 * Shows the stop button.
	 *
	 * @see #hideStopButton()
	 */
	public void showStopButton() {
		stopButton.setVisible(true);
		// Don't exclude the stop button so the layout will expand when it's shown
		stopButtonRowData.exclude = false;
		this.redraw();
	}

	/**
	 * Check if the stop button is visible
	 *
	 * @return true if stop button is visible
	 * @see #showStopButton()
	 * @see #hideStopButton()
	 */
	public boolean isStopButtonVisible() {
		return stopButton.isVisible();
	}

}
