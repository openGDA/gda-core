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

import java.util.IllegalFormatConversionException;

import org.apache.commons.lang3.math.NumberUtils;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannablePositionChangeEvent;
import gda.observable.IObserver;

/**
 * A class which provides a GUI composite to allow easy control of a scannable.
 * <p>
 * It provides the current position which can be edited and set to new value.
 * <p>
 * The format of the displayed number will be specified by the scannable output format.
 */
public class ScannableDisplayComposite extends Composite {

	private static final Logger logger = LoggerFactory.getLogger(ScannableDisplayComposite.class);
	private static final int DEFAULT_TEXT_WIDTH = 120;

	// GUI Elements
	private Label displayNameLabel;
	private Text positionText;

	private Scannable scannable;
	private String scannableName;
	private String userUnit;
	private String displayName; // Allow a different prettier name be used if required
	private int textWidth = DEFAULT_TEXT_WIDTH;
	private Label unitLabel;
	private boolean textInput;
	private String currentPosition;
	private double valueThreshold = Double.POSITIVE_INFINITY;
	private int aboveThresholdColour = SWT.COLOR_RED;
	private int valueColour;
	private boolean rescalingFont = false;

	private Font resizedFont;
	private Font boldFont;
	private String[] outputFormat;

	/**
	 * Constructor
	 *
	 * @param parent
	 *            The parent composite
	 * @param style
	 *            SWT style parameter (Typically SWT.NONE)
	 */
	public ScannableDisplayComposite(Composite parent, int style) {

		super(parent, style);

		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		parent.setBackgroundMode(SWT.INHERIT_FORCE);

		// Setup layout
		GridLayout gridLayout = new GridLayout(3, false);
		gridLayout.horizontalSpacing = 10;
		gridLayout.verticalSpacing = 0;
		gridLayout.marginHeight = 0;
		this.setLayout(gridLayout);

		// Name label
		displayNameLabel = new Label(this, SWT.NONE);
		displayNameLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

		// Position text box
		positionText = new Text(this, SWT.READ_ONLY);
		positionText.setEditable(false);
		positionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));


		// Name label
		unitLabel = new Label(this, SWT.NONE);
		unitLabel.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));

		// At this time the control is built but no scannable is set so disable it.
		disable();
	}

	private void resizeProcess(Composite parent) {
		Point partSize = parent.getParent().getSize();
		setValueSize(partSize.x/10);
		parent.getParent().layout();
	}

	public String getDisplayName() {
		return displayName;
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

	@Override
	public void setEnabled(boolean enabled) {
		if (enabled) {
			// When the control is enabled reconfigure to ensure values are current
			configure();
		} else { // disabled
			// Clear the position and increments
			positionText.setText("");
		}
		// Disable the controls
		displayNameLabel.setEnabled(enabled);
		positionText.setEnabled(enabled);
		unitLabel.setEnabled(enabled);

		this.redraw();
	}

	/**
	 * Sets the control enabled
	 * <p>
	 * Equivalent to setEnabled(true)
	 *
	 * @see #setEnabled(boolean)
	 */
	public void enable() {
		setEnabled(true);
	}

	/**
	 * Sets the control disabled (grayed)
	 * <p>
	 * Equivalent to setEnabled(false)
	 *
	 * @see #setEnabled(boolean)
	 */
	public void disable() {
		setEnabled(false);
	}

	public Scannable getScannable() {
		return scannable;
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
		// If no display name is set when the scannable is set, set it to the scannable name
		if (displayName == null) {
			setDisplayName(scannableName);
		}
		this.outputFormat = scannable.getOutputFormat();
		enable();
	}

	private void configure() {
		if (scannable == null) {
			throw new IllegalStateException("Scannable is not set");
		}

		class ResizeListener implements ControlListener, Runnable, Listener {

		    private long lastEvent = 0;

		    private boolean mouse = true;

		    @Override
		    public void controlMoved(ControlEvent e) {
		    	controlResized(e);
		    }

		    @Override
		    public void controlResized(ControlEvent e) {
		        lastEvent = System.currentTimeMillis();
		        Display.getDefault().timerExec(200, this);
		    }

		    @Override
			public void run() {
		        if ((lastEvent + 200) < System.currentTimeMillis() && mouse) {
		        	resizeProcess(getParent());
		        	Display.getDefault().timerExec(100, this);
		        	resizeProcess(getParent()); // workaround - otherwise text doesn't rescale to a new font vertically

		        } else {
		            Display.getDefault().timerExec(200, this);
		        }
		    }

		    @Override
			public void handleEvent(Event event) {
		        mouse = event.type == SWT.MouseUp;
		    }
		}

		logger.debug("Here rescaling get is {}", isRescalingFont());
		if (isRescalingFont()) {
			ResizeListener listener = new ResizeListener();
			getParent().getParent().addControlListener(listener);
		}

		// Add an observer to the scannable when an event occurs
		final IObserver iObserver = (source, arg) -> {
			Object[] argArray;
			Object newValue = null;
			if (arg instanceof ScannablePositionChangeEvent event) {
				newValue = event.newPosition;
			} else if (arg.getClass().isArray()) {
				// EPICS monitor by default always sending array
				argArray = (Object[]) arg;
				newValue = argArray[0];
			} else {
				newValue = arg;
			}
			prepareGuiUpdate(newValue);
		};

		scannable.addIObserver(iObserver);
		this.addDisposeListener(e -> scannable.deleteIObserver(iObserver));
		currentPosition = getCurrentPosition(); // current poistion can be null if failed to get from scannable
		if (currentPosition != null) {
			updateGui(currentPosition);
		}
	}

	private void prepareGuiUpdate(Object arg) {
		if (isTextInput()) {
			updateGui(arg.toString());
		} else {
			String valueOf;
			Float farg = null;
			try {
				if (arg instanceof Integer iarg) {
					farg = (float) iarg;
				}
				if (farg!=null) {
					valueOf = (outputFormat.length>0)? String.format(outputFormat[0], farg).trim(): String.format("%.2e", farg).trim();
				} else {
					valueOf = (outputFormat.length>0)? String.format(outputFormat[0], arg).trim(): String.format("%.2e", arg).trim();
				}

			} catch (IllegalFormatConversionException e){
				logger.debug("String format failed - applying no format",e);
				valueOf = arg.toString();
			}
			updateGui(valueOf);
		}
	}

	/**
	 * Calls {@link Scannable} getPosition() method and parses it into a String using getOutputFormat().
	 * If the scannable returns an array the first element is used.
	 *
	 * @return The current position of the scannable
	 */
	private String getCurrentPosition() {
		String currentPosition = null;
		Object cPosition;
		try {
			Object getPosition = scannable.getPosition();

			if (getPosition.getClass().isArray())
				// The scannable returns an array assume the relevant value is the first
				cPosition = ((Object[]) getPosition)[0];
			else
				cPosition = getPosition;
			if (cPosition instanceof Number) {
				try {
					currentPosition=String.format(scannable.getOutputFormat()[0], cPosition).trim();
				} catch (IllegalFormatConversionException e){
					logger.error("Initial string format failed",e);
					currentPosition = cPosition.toString();
				}
			} else {
				currentPosition=cPosition.toString();
			}
		} catch (DeviceException e) {
			logger.error("Error while getting currrent position of {}", scannableName, e);
			return "Unavailable";
		}

		return currentPosition;
	}
	/**
	 * This is used to update the GUI. Only this method should be used to update the GUI to ensure display is consistent.
	 *
	 * @param currentPosition
	 *            The newest position to display typically from {@link #getCurrentPosition()}
	 */
	private void updateGui(final String currentPosition) {
		// Save the new position
		this.currentPosition = currentPosition;
		// Update the GUI in the UI thread
		Display.getDefault().asyncExec(()->	{
			checkThreshold(currentPosition);
			positionText.setText(currentPosition);
			});
	}

	private void checkThreshold(final String currentPosition) {
		if (NumberUtils.isNumber(currentPosition)) {
			double currentValue = Double.parseDouble(currentPosition);
			if (currentValue > valueThreshold) {
				setValueColour(aboveThresholdColour);
			} else {
				setValueColour(valueColour);
			}
		}
	}

	/**
	 * Changes label to a bold font and sets its size
	 * @param size A point size integer
	 */
	public void setLabelSize(int size) {
		FontDescriptor boldDescriptor = FontDescriptor.createFrom(displayNameLabel.getFont()).setStyle(SWT.BOLD).setHeight(size);
		Font boldFont = boldDescriptor.createFont(displayNameLabel.getDisplay());
		displayNameLabel.setFont(boldFont);
	}

	/**
	 * Set the size of value text
	 * @param size A point size integer
	 */
	public void setValueSize(int size) {
		FontDescriptor fontDescriptor = FontDescriptor.createFrom(positionText.getFont()).setHeight(size);
		if (resizedFont!=null) resizedFont.dispose();
		resizedFont = fontDescriptor.createFont(positionText.getDisplay());
		positionText.setFont(resizedFont);
	}

	/**
	 * Set the colour of the value text
	 * @param colour An SWT colour constant eg. SWT.COLOR_DARK_BLUE
	 */
	public void setValueColour(int colour) {
		positionText.setForeground(getDisplay().getSystemColor(colour));
	}

	public void setValueColourDefault(int colour) {
		this.valueColour = colour;
	}

	public void setValueBold(boolean boldValue) {
		int style = SWT.NORMAL;

		if (boldValue) {
			style = SWT.BOLD;
		}

		FontDescriptor boldDescriptor = FontDescriptor.createFrom(positionText.getFont()).setStyle(style);
		if (boldFont!=null) boldFont.dispose();
		boldFont = boldDescriptor.createFont(positionText.getDisplay());
		positionText.setFont(boldFont);
	}

	/**
	 * Set the colour of the label text
	 * @param colour An SWT colour constant eg. SWT.COLOR_DARK_BLUE
	 */
	public void setLabelColour(int colour) {
		displayNameLabel.setForeground(getDisplay().getSystemColor(colour));
	}

	public boolean isTextInput() {
		return textInput;
	}

	public void setTextInput(boolean textInput) {
		this.textInput = textInput;
	}

	public String getUserUnit() {
		return userUnit;
	}

	public void setUserUnit(String userUnit) {
		this.userUnit = userUnit;
		unitLabel.setText(userUnit);
		this.redraw();
	}

	public int getTextWidth() {
		return textWidth;
	}

	public void setTextWidth(int textWidth) {
		if (isRescalingFont()) return;
		this.textWidth = textWidth;
		((GridData) positionText.getLayoutData()).widthHint = textWidth;
	}

	public void setValueThreshold(double valueThreshold) {
		this.valueThreshold = valueThreshold;
	}

	public void setAboveThresholdColour(int aboveThresholdColour) {
		this.aboveThresholdColour = aboveThresholdColour;

	}

	@Override
	public void dispose () {
		if (boldFont!=null) boldFont.dispose();
		if (resizedFont!=null) resizedFont.dispose();
		super.dispose();
	}

	public boolean isRescalingFont() {
		return rescalingFont;
	}

	public void setRescalingFont(boolean rescalingFont) {
		this.rescalingFont = rescalingFont;
		logger.debug("Just set rescaling to {}", isRescalingFont());
	}

}