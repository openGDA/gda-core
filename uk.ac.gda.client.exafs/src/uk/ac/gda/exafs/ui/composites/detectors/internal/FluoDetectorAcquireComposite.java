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

import gda.configuration.properties.LocalProperties;

import org.dawnsci.common.richbeans.components.FieldComposite;
import org.dawnsci.common.richbeans.components.scalebox.ScaleBox;
import org.dawnsci.common.richbeans.components.wrappers.LabelWrapper;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
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

	public FluoDetectorAcquireComposite(Composite composite, final FluorescenceDetectorCompositeController controller) {
		super(composite, SWT.NONE);
		
		this.setLayout(new FillLayout());

		Group grpAcquire = new Group(this, SWT.NONE);
		grpAcquire.setText("Acquire Spectra");
		GridLayoutFactory.fillDefaults().applyTo(grpAcquire);

		loadButton = new Button(grpAcquire, SWT.NONE);
		loadButton.setImage(SWTResourceManager.getImage(DetectorEditor.class, "/icons/folder.png"));
		loadButton.setText("Load");
		loadButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO 
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		Composite acquire = new Composite(grpAcquire, SWT.NONE);
		final GridLayout gridLayoutAcq = new GridLayout();
		gridLayoutAcq.numColumns = 9;
		gridLayoutAcq.marginWidth = 0;
		acquire.setLayout(gridLayoutAcq);

		acquireButton = new Button(acquire, SWT.NONE);
		setAcquireImageToSnapshot();
		acquireButton.setText("Acquire");

		acquireTime = new ScaleBox(acquire, SWT.NONE);
		acquireTime.setMinimum(1);
		acquireTime.setValue(1000);
		acquireTime.setMaximum(50000);
		acquireTime.setUnit("ms");
		acquireTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		autoSaveCheckBox = new Button(acquire, SWT.CHECK);
		autoSaveCheckBox.setText("Save on Acquire");
		autoSaveCheckBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

		liveCheckBox = new Button(acquire, SWT.CHECK);
		liveCheckBox.setText("Live");
		liveCheckBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		liveCheckBox.addSelectionListener(new SelectionListener() {

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

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		acquireButton.addSelectionListener(new SelectionListener() {
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

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		FileDialog openDialog = new FileDialog(controller.getSite().getShell(), SWT.OPEN);
		openDialog.setFilterPath(LocalProperties.get(LocalProperties.GDA_DATAWRITER_DIR));

		Composite composite_1 = new Composite(grpAcquire, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(composite_1);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite_1);

		Label lblDeadTime = new Label(composite_1, SWT.NONE);
		lblDeadTime.setText("Dead Time");
		lblDeadTime.setVisible(false);

		LabelWrapper deadTimeLabel = new LabelWrapper(composite_1, SWT.NONE);
		deadTimeLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		deadTimeLabel.setText("12");
		deadTimeLabel.setUnit("%");
		deadTimeLabel.setDecimalPlaces(3);
		deadTimeLabel.setVisible(false);
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

	public FieldComposite getCollectionTime() {
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
