/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.composites.detectors.internal;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.richbeans.widgets.scalebox.NumberBox;
import org.eclipse.richbeans.widgets.scalebox.ScaleBox;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.swtdesigner.SWTResourceManager;

public class FluoDetectorAcquireComposite extends Composite {

	private Label detectorNameLabel;
	private NumberBox acquireTime;
	private Button loadButton;
	private Button saveButton;
	private Button acquireButton;
	private Button autoSaveCheckBox;
	private Button liveCheckBox;
	private Button applySettingsButton;
	private Button autoScaleOnAcquireCheckBox;

	public FluoDetectorAcquireComposite(Composite composite, int style) {
		super(composite, style);

		this.setLayout(new FillLayout());

		Group acquireGroup = new Group(this, SWT.NONE);
		acquireGroup.setText("Acquire Spectra");

		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(acquireGroup);

		detectorNameLabel = new Label(acquireGroup, SWT.NONE);
		GridDataFactory.swtDefaults().span(3, 1).applyTo(detectorNameLabel);
		detectorNameLabel.setVisible(false);

		loadButton = new Button(acquireGroup, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(loadButton);
		loadButton.setImage(SWTResourceManager.getImage(FluoDetectorAcquireComposite.class, "/icons/folder.png"));
		loadButton.setText("Load");

		saveButton = new Button(acquireGroup, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(saveButton);
		saveButton.setImage(SWTResourceManager.getImage(FluoDetectorAcquireComposite.class, "/icons/disk.png"));
		saveButton.setText("Save");

		autoSaveCheckBox = new Button(acquireGroup, SWT.CHECK);
		GridDataFactory.swtDefaults().applyTo(autoSaveCheckBox);
		autoSaveCheckBox.setText("Save on Acquire");

		acquireButton = new Button(acquireGroup, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(acquireButton);
		setAcquireImageToSnapshot();
		acquireButton.setText("Acquire");

		acquireTime = new ScaleBox(acquireGroup, SWT.NONE);
		acquireTime.setMinimum(1);
		acquireTime.setValue(1000);
		acquireTime.setMaximum(50000);
		acquireTime.setUnit("ms");

		liveCheckBox = new Button(acquireGroup, SWT.CHECK);
		GridDataFactory.swtDefaults().applyTo(liveCheckBox);
		liveCheckBox.setText("Live");

		applySettingsButton = new Button(acquireGroup, SWT.NONE);
		GridDataFactory.swtDefaults().span(2, 1).applyTo(applySettingsButton);
		applySettingsButton.setText("Apply settings");
		applySettingsButton.setToolTipText("Apply current settings to detector.");

		autoScaleOnAcquireCheckBox = new Button(acquireGroup, SWT.CHECK);
		GridDataFactory.swtDefaults().applyTo(autoScaleOnAcquireCheckBox);
		autoScaleOnAcquireCheckBox.setText("Rescale on Acquire");
		autoScaleOnAcquireCheckBox.setToolTipText("Rescale the plot after each spectrum has been acquired.");
	}

	private void setAcquireImageToSnapshot() {
		acquireButton.setImage(SWTResourceManager.getImage(FluoDetectorAcquireComposite.class, "/icons/camera_go.png"));
	}

	private void setAcquireImageToGo() {
		acquireButton.setImage(SWTResourceManager.getImage(FluoDetectorAcquireComposite.class, "/icons/control_play_blue.png"));
	}

	private void setAcquireImageToStop() {
		acquireButton.setImage(SWTResourceManager.getImage(FluoDetectorAcquireComposite.class, "/icons/control_stop_blue.png"));
	}

	public void setDetectorNameLabel(String label) {
		if (label != null && label.length() > 0) {
			detectorNameLabel.setText(label);
			detectorNameLabel.setVisible(true);
		} else {
			detectorNameLabel.setText("");
			detectorNameLabel.setVisible(false);
		}
		this.layout(new Control[] { detectorNameLabel });
	}

	public NumberBox getCollectionTime() {
		return acquireTime;
	}

	public void setContinuousAcquireMode() {
		autoSaveCheckBox.setEnabled(false);
		setAcquireImageToGo();
	}

	public void setSingleAcquireMode() {
		autoSaveCheckBox.setEnabled(true);
		setAcquireImageToSnapshot();
	}

	public void showAcquireStarted() {
		acquireTime.setEnabled(false);
		autoSaveCheckBox.setEnabled(false);
		liveCheckBox.setEnabled(false);
		setAcquireImageToStop();
	}

	public void showAcquireFinished() {
		acquireTime.setEnabled(true);
		liveCheckBox.setEnabled(true);
		setAcquireImageToGo();
	}

	public Button getLoadButton() {
		return loadButton;
	}

	public Button getSaveButton() {
		return saveButton;
	}

	public Button getAcquireButton() {
		return acquireButton;
	}

	public Button getAutoSaveCheckBox() {
		return autoSaveCheckBox;
	}

	public Button getLiveCheckBox() {
		return liveCheckBox;
	}

	public Button getApplySettingsButton() {
		return applySettingsButton;
	}

	public Button getAutoScaleOnAcquireCheckBox() {
		return autoScaleOnAcquireCheckBox;
	}
}
