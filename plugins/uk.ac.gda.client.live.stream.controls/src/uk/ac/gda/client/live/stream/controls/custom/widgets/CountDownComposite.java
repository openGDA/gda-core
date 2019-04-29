/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.client.live.stream.controls.custom.widgets;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.swtdesigner.SWTResourceManager;

/**
 * A class which provides a GUI composite to display a given text value along with a given label and unit.
 * <p>
 * The format of the displayed text will be specified in the {@link Observable} instance.
 */
public class CountDownComposite extends Composite implements Observer {

	private static final int DEFAULT_TEXT_WIDTH = 30;

	// GUI Elements
	private Label displayNameLabel;
	private Text positionText;
	private Label unitLabel;
	
	private String displayName;    // Allow a different prettier name be used if required
	private Observable observable; // must be set to obtain text to be displayed
	private String userUnit;       // Allow unit to be specified if required
	private int textWidth = DEFAULT_TEXT_WIDTH;
	
	/**
	 * Constructor
	 *
	 * @param parent
	 *            The parent composite
	 * @param style
	 *            SWT style parameter (Typically SWT.NONE)
	 */
	public CountDownComposite(Composite parent, int style) {
		super(parent, style);

		parent.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		parent.setBackgroundMode(SWT.INHERIT_FORCE);

		// Setup layout
		GridLayout gridLayout = new GridLayout(3, false);
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		gridLayout.marginHeight = 0;
		this.setLayout(gridLayout);

		// Name label
		displayNameLabel = new Label(this, SWT.NONE);
		displayNameLabel.setLayoutData(GridDataFactory.fillDefaults().hint(SWT.DEFAULT, SWT.DEFAULT).align(SWT.CENTER, SWT.CENTER).grab(true, false).create());

		// Position text box
		positionText = new Text(this, SWT.BORDER);
		positionText.setLayoutData(GridDataFactory.fillDefaults().hint(getTextWidth(), SWT.DEFAULT).align(SWT.CENTER, SWT.CENTER).grab(true, false).create());
		positionText.setText("0");
		positionText.setEditable(false);
		positionText.setBackground(SWTResourceManager.getColor(SWT.COLOR_DARK_GRAY));
		positionText.setForeground(SWTResourceManager.getColor(SWT.COLOR_GREEN));
		// Name label
		unitLabel = new Label(this, SWT.NONE);
		unitLabel.setLayoutData(GridDataFactory.fillDefaults().hint(SWT.DEFAULT, SWT.DEFAULT).align(SWT.CENTER, SWT.CENTER).grab(true, false).create());
		
		if (observable!=null) {
			// required when the view containing this widget is re-opened
			observable.addObserver(this);
		}
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
		this.textWidth = textWidth;
		((GridData) positionText.getLayoutData()).widthHint = textWidth;
	}

	public Observable getObservable() {
		return observable;
	}

	public void setObservable(Observable observable) {
		this.observable = observable;
		// required when this is 1st instantiated in LiveControl
		this.observable.addObserver(this);
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o == observable) {
			// Update the GUI in the UI thread
			if (!positionText.isDisposed()) {
				Display.getDefault().asyncExec(()->	positionText.setText(arg.toString()));
			}
		}
	}
	@Override
	public void dispose() {
		if (observable != null) {
			observable.deleteObserver(this);
		}
		super.dispose();
	}
}