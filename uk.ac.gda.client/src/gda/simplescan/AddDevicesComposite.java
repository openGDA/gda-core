/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.simplescan;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.richbeans.components.selector.BeanSelectionEvent;
import uk.ac.gda.richbeans.components.selector.BeanSelectionListener;
import org.eclipse.wb.swt.ResourceManager;

public class AddDevicesComposite extends Composite {
	private Button removeScannable;
	private Button removeDetector;
	private ObjectListEditor scannableList;
	private ObjectListEditor detectorList;
	private ExpandableComposite configureDevicesExpandableComposite;
	private ScannableManagerComposite scannableManagerComposite;
	private DetectorManagerComposite detectorManagerComposite;
	private Button addScannable;
	private Button addDetector;
	Composite composite;
	
	public AddDevicesComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(1, false));

		configureDevicesExpandableComposite = new ExpandableComposite(this, SWT.NONE);
		configureDevicesExpandableComposite.setText("Scannable/Detector List");
		GridData gd_configureDevicesExpandableComposite = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_configureDevicesExpandableComposite.widthHint = 378;
		gd_configureDevicesExpandableComposite.heightHint = 170;
		configureDevicesExpandableComposite.setLayoutData(gd_configureDevicesExpandableComposite);
		Composite configDevicesComp = new Composite(configureDevicesExpandableComposite, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		configDevicesComp.setLayout(gridLayout);

		addDevices(configDevicesComp);
	}

	private void addDevices(final Composite composite) {
		createFindScannable(composite);
		createFindDetector(composite);

		//updateScannables();

		addScannable.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String foundScannableName = scannableManagerComposite.getScannableName().getText();
				ScannableManagerBean smb = new ScannableManagerBean();
				smb.setScannableName(foundScannableName);
				if (scannableManagerComposite.getScannableName().isFound()) {
					//bean.addScannable(smb);
					//updateScannables();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
		
		//updateDetectors();

		addDetector.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String foundDetectorName = detectorManagerComposite.getDetectorName().getText();
				DetectorManagerBean smb = new DetectorManagerBean();
				smb.setDetectorName(foundDetectorName);
				smb.setDetectorDescription("");

				if (detectorManagerComposite.getDetectorName().isFound()) {
					//bean.addDetector(smb);
					//updateDetectors();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
		
		configureDevicesExpandableComposite.setClient(composite);

		ExpansionAdapter addScannableExpansionListener = new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				GridUtils.layoutFull(composite.getParent());
			}
		};
		configureDevicesExpandableComposite.addExpansionListener(addScannableExpansionListener);
	}

	private void createFindScannable(final Composite composite) {
		final Group addScannableGroup = new Group(composite, SWT.NONE);
		addScannableGroup.setText("Scannables");
		addScannableGroup.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		GridLayout gl_addScannableGroup = new GridLayout();
		gl_addScannableGroup.marginWidth = 0;
		gl_addScannableGroup.verticalSpacing = 0;
		gl_addScannableGroup.marginHeight = 0;
		gl_addScannableGroup.horizontalSpacing = 0;
		addScannableGroup.setLayout(gl_addScannableGroup);

		GridLayout gl_findScannableComp = new GridLayout(1, false);
		gl_findScannableComp.verticalSpacing = 0;
		gl_findScannableComp.marginWidth = 0;
		gl_findScannableComp.marginHeight = 0;
		gl_findScannableComp.horizontalSpacing = 0;

		Composite buttons = new Composite(addScannableGroup, SWT.NONE);
		GridData gd_buttons = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_buttons.widthHint = 182;
		buttons.setLayoutData(gd_buttons);
		GridLayout gl_buttons = new GridLayout(3, false);
		gl_buttons.marginWidth = 0;
		gl_buttons.verticalSpacing = 0;
		gl_buttons.horizontalSpacing = 0;
		gl_buttons.marginHeight = 0;
		buttons.setLayout(gl_buttons);
		
		scannableManagerComposite = new ScannableManagerComposite(buttons, SWT.NONE);
		((GridData) scannableManagerComposite.getScannableName().getLayoutData()).widthHint = 125;
		GridData gd_scannableManagerComposite = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_scannableManagerComposite.widthHint = 124;
		scannableManagerComposite.setLayoutData(gd_scannableManagerComposite);

		
		addScannable = new Button(buttons, SWT.PUSH);
		addScannable.setImage(ResourceManager.getPluginImage("uk.ac.gda.client", "icons/add.png"));
		
		removeScannable = new Button(buttons, SWT.PUSH);
		removeScannable.setImage(ResourceManager.getPluginImage("uk.ac.gda.client", "icons/delete.png"));

		scannableList = new ObjectListEditor(addScannableGroup, SWT.NONE, "");
		Table table_1 = scannableList.viewer.getTable();
		GridData gd_table_1 = new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1);
		gd_table_1.heightHint = -1;
		table_1.setLayoutData(gd_table_1);

		GridLayout gridLayout_1 = (GridLayout) scannableList.getLayout();
		gridLayout_1.horizontalSpacing = 0;
		gridLayout_1.marginHeight = 0;
		gridLayout_1.verticalSpacing = 0;
		gridLayout_1.marginWidth = 0;

		Table table = scannableList.viewer.getTable();
		GridData gd_table = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_table.widthHint = 128;
		table.setLayoutData(gd_table);
		GridData gd_scannableList = new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1);
		gd_scannableList.heightHint = -1;
		scannableList.setLayoutData(gd_scannableList);

		GridData gridData2 = new GridData();
		gridData2.grabExcessHorizontalSpace = false;
		gridData2.heightHint = 80;
		gridData2.widthHint = 160;
		scannableList.viewer.getControl().setLayoutData(gridData2);

		scannableList.setEditorClass(ScannableManagerBean.class);
		scannableList.setEditorUI(scannableManagerComposite);
		scannableList.addBeanSelectionListener(new BeanSelectionListener() {
			@Override
			public void selectionChanged(BeanSelectionEvent evt) {
				scannableManagerComposite.selectionChanged((ScannableManagerBean) evt.getSelectedBean());
			}
		});

		removeScannable.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//bean.removeScannable(scannableList.getSelected());
				//updateScannables();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
	}

	private void createFindDetector(final Composite composite) {
		final Group addDetectorGroup = new Group(composite, SWT.NONE);
		addDetectorGroup.setText("Detectors");
		addDetectorGroup.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		GridLayout gl_addDetectorsGroup = new GridLayout();
		gl_addDetectorsGroup.marginWidth = 0;
		gl_addDetectorsGroup.verticalSpacing = 0;
		gl_addDetectorsGroup.marginHeight = 0;
		gl_addDetectorsGroup.horizontalSpacing = 0;
		addDetectorGroup.setLayout(gl_addDetectorsGroup);

		GridLayout gl_findDetectorComp = new GridLayout(1, false);
		gl_findDetectorComp.verticalSpacing = 0;
		gl_findDetectorComp.marginWidth = 0;
		gl_findDetectorComp.marginHeight = 0;
		gl_findDetectorComp.horizontalSpacing = 0;

		Composite buttons = new Composite(addDetectorGroup, SWT.NONE);
		GridData gd_buttons = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_buttons.widthHint = 150;
		buttons.setLayoutData(gd_buttons);
		GridLayout gl_buttons = new GridLayout(3, false);
		gl_buttons.marginWidth = 0;
		gl_buttons.verticalSpacing = 0;
		gl_buttons.horizontalSpacing = 0;
		gl_buttons.marginHeight = 0;
		buttons.setLayout(gl_buttons);
		
		detectorManagerComposite = new DetectorManagerComposite(buttons, SWT.NONE);
		detectorManagerComposite.getDetectorName().getControl().setBounds(0, 0, 94, 27);
		GridLayout gridLayout = (GridLayout) detectorManagerComposite.getLayout();
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.horizontalSpacing = 0;
		gridLayout.marginHeight = 0;
		GridData gridData = (GridData) detectorManagerComposite.getDetectorName().getLayoutData();
		gridData.horizontalAlignment = SWT.LEFT;
		gridData.grabExcessVerticalSpace = true;
		gridData.widthHint = 94;
		detectorManagerComposite.getDetectorName().setLayout(null);
		GridData gd_detectorManagerComposite = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_detectorManagerComposite.widthHint = 94;
		detectorManagerComposite.setLayoutData(gd_detectorManagerComposite);


		addDetector = new Button(buttons, SWT.PUSH);
		addDetector.setImage(ResourceManager.getPluginImage("uk.ac.gda.client", "icons/add.png"));

		removeDetector = new Button(buttons, SWT.PUSH);
		removeDetector.setImage(ResourceManager.getPluginImage("uk.ac.gda.client", "icons/delete.png"));

		detectorList = new ObjectListEditor(addDetectorGroup, SWT.NONE, "");
		Table table_1 = detectorList.viewer.getTable();
		GridData gd_table_1 = new GridData(SWT.LEFT, SWT.CENTER, false, true, 1, 1);
		gd_table_1.heightHint = -1;
		table_1.setLayoutData(gd_table_1);

		GridLayout gridLayout_1 = (GridLayout) detectorList.getLayout();
		gridLayout_1.horizontalSpacing = 0;
		gridLayout_1.marginHeight = 0;
		gridLayout_1.verticalSpacing = 0;
		gridLayout_1.marginWidth = 0;

		Table table = detectorList.viewer.getTable();
		GridData gd_table = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_table.widthHint = 128;
		table.setLayoutData(gd_table);
		GridData gd_detectorList = new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1);
		gd_detectorList.heightHint = -1;
		detectorList.setLayoutData(gd_detectorList);

		GridData gridData2 = new GridData();
		gridData2.grabExcessHorizontalSpace = false;
		gridData2.heightHint = 80;
		gridData2.widthHint = 128;
		detectorList.viewer.getControl().setLayoutData(gridData2);

		detectorList.setEditorClass(DetectorManagerBean.class);
		detectorList.setEditorUI(detectorManagerComposite);
		detectorList.addBeanSelectionListener(new BeanSelectionListener() {
			@Override
			public void selectionChanged(BeanSelectionEvent evt) {
				detectorManagerComposite.selectionChanged((DetectorManagerBean) evt.getSelectedBean());
			}
		});

		removeDetector.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//bean.removeDetector(detectorList.getSelected());
				//updateDetectors();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
	}
	
	public ObjectListEditor getScannables() {
		return scannableList;
	}

	public ObjectListEditor getDetectors() {
		return detectorList;
	}
}
