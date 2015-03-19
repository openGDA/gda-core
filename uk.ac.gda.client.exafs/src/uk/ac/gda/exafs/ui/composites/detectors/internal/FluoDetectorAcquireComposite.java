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

import org.dawnsci.common.richbeans.components.scalebox.ScaleBox;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.exafs.ui.detector.DetectorEditor;

import com.swtdesigner.SWTResourceManager;

public class FluoDetectorAcquireComposite extends Composite {

	private static final Logger logger = LoggerFactory.getLogger(FluoDetectorAcquireComposite.class);

	private ScaleBox acquireTime;
	private Button loadButton;
	private Button acquireButton;
	private Button autoSaveCheckBox;
	private Button liveCheckBox;

	public FluoDetectorAcquireComposite(Composite composite, final FluoDetectorCompositeController controller) {
		super(composite, SWT.NONE);

		this.setLayout(new FillLayout());

		Group acquireGroup = new Group(this, SWT.NONE);
		acquireGroup.setText("Acquire Spectra");

		final int numberOfColumns = 4;
		GridLayoutFactory.swtDefaults().numColumns(numberOfColumns).applyTo(acquireGroup);

		loadButton = new Button(acquireGroup, SWT.NONE);
		GridDataFactory.swtDefaults().span(numberOfColumns, 1).applyTo(loadButton);
		loadButton.setImage(SWTResourceManager.getImage(DetectorEditor.class, "/icons/folder.png"));
		loadButton.setText("Load");
		loadButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				controller.loadAcquireDataFromFile();
			}
		});

		acquireButton = new Button(acquireGroup, SWT.NONE);
		setAcquireImageToSnapshot();
		acquireButton.setText("Acquire");

		acquireTime = new ScaleBox(acquireGroup, SWT.NONE);
		acquireTime.setMinimum(1);
		acquireTime.setValue(1000);
		acquireTime.setMaximum(50000);
		acquireTime.setUnit("ms");

		autoSaveCheckBox = new Button(acquireGroup, SWT.CHECK);
		autoSaveCheckBox.setText("Save on Acquire");

		liveCheckBox = new Button(acquireGroup, SWT.CHECK);
		liveCheckBox.setText("Live");
		liveCheckBox.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (liveCheckBox.getSelection()) {
					autoSaveCheckBox.setEnabled(false);
					setAcquireImageToGo();
				} else {
					autoSaveCheckBox.setEnabled(true);
					setAcquireImageToSnapshot();
				}
			}
		});

		acquireButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					if (liveCheckBox.getSelection()) {
						controller.continuousAcquire(acquireTime.getNumericValue());
					} else {
						controller.singleAcquire(acquireTime.getNumericValue(), autoSaveCheckBox.getSelection());
					}
				} catch (Exception e1) {
					logger.error("Cannot acquire xmap data", e1);
				}
			}
		});
	}

	private void setAcquireImageToSnapshot() {
		acquireButton.setImage(SWTResourceManager.getImage(DetectorEditor.class, "/icons/camera_go.png"));
	}

	private void setAcquireImageToGo() {
		acquireButton.setImage(SWTResourceManager.getImage(DetectorEditor.class, "/icons/control_play_blue.png"));
	}

	private void setAcquireImageToStop() {
		acquireButton.setImage(SWTResourceManager.getImage(DetectorEditor.class, "/icons/control_stop_blue.png"));
	}

	public ScaleBox getCollectionTime() {
		return acquireTime;
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
}
