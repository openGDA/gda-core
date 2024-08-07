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

package uk.ac.gda.client.live.stream.controls.custom.widgets;

import static uk.ac.gda.client.live.stream.controls.widgets.css.WidgetCSSStyling.SWT_INTERNAL_GTK_CSS;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

import com.swtdesigner.SWTResourceManager;

import gda.observable.IObservable;
import gda.observable.IObserver;
import uk.ac.gda.client.live.stream.controls.widgets.css.WidgetCSSStyling;

/**
 * A class which provides a GUI composite to display progress of a count down timer.
 *
 * @author fy65
 *
 */
public class CountdownProgressComposite extends Composite implements IObserver {

	// GUI Elements
	private Label displayNameLabel;
	private ProgressBar progressBar;

	private String displayName;    // Allow a different prettier name be used if required
	private IObservable observable; // must be set to obtain text to be displayed
	private Integer barWidth;
	private GridData gdProgressBar;
	private Color color;

	public CountdownProgressComposite(Composite parent, int style) {
		super(parent, style);

		color = SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT);
		parent.setBackground(color);
		parent.setBackgroundMode(SWT.INHERIT_FORCE);
		
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		gridLayout.marginHeight = 0;
		this.setLayout(gridLayout);

		displayNameLabel = new Label(this, SWT.NONE);
		displayNameLabel.setLayoutData(GridDataFactory.fillDefaults().hint(SWT.DEFAULT, SWT.DEFAULT).align(SWT.CENTER, SWT.CENTER).grab(true, false).create());

		progressBar = new ProgressBar(this, SWT.HORIZONTAL);
		gdProgressBar = new GridData(GridData.FILL_HORIZONTAL);
		gdProgressBar.grabExcessHorizontalSpace = true;
		gdProgressBar.minimumWidth=SWT.DEFAULT;
		progressBar.setLayoutData(gdProgressBar);
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);
		// override GTK default ProgressBAr widget style
		WidgetCSSStyling.applyStyling(progressBar, SWT_INTERNAL_GTK_CSS, "platform:/plugin/uk.ac.gda.client.live.stream.controls/css/progressBar.css");
		
		if (getObservable()!=null) {
			// required when view containing this being re-opened.
			getObservable().addIObserver(this);
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
	public IObservable getObservable() {
		return observable;
	}

	public void setObservable(IObservable observable) {
		this.observable = observable;
		//required when 1st time this being created in LiveControl
		this.observable.addIObserver(this);
	}	

	public Integer getBarWidth() {
		return barWidth;
	}

	public void setBarWidth(Integer barWidth) {
		this.barWidth = barWidth;
		gdProgressBar.widthHint=barWidth;
		this.redraw();
	}
	@Override
	public void dispose() {
		if (observable!=null) {
			observable.deleteIObserver(this);
		}
		if (color!=null) {
			color.dispose();
		}
		if (progressBar!=null) {
			progressBar.dispose();
		}
		super.dispose();
	}

	@Override
	public void update(Object source, Object arg) {
		if (source == observable && !progressBar.isDisposed()) {
			Display.getDefault().asyncExec(()-> progressBar.setSelection(Integer.parseInt(arg.toString())));
		}
	}

}
