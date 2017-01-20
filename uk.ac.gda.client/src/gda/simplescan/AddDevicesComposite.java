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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.richbeans.widgets.selector.BeanSelectionEvent;
import org.eclipse.richbeans.widgets.selector.BeanSelectionListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import com.swtdesigner.SWTResourceManager;

import uk.ac.gda.common.rcp.util.GridUtils;

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
	private SimpleScan bean;

	public AddDevicesComposite(Composite parent, int style, Object editingBean) {
		super(parent, style);
		setLayout(new GridLayout(1, false));

		bean = (SimpleScan) editingBean;

		configureDevicesExpandableComposite = new ExpandableComposite(this, SWT.NONE);
		configureDevicesExpandableComposite.setText("Set Available Scannables/Detectors");
		GridData gd_configureDevicesExpandableComposite = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_configureDevicesExpandableComposite.widthHint = 378;
		gd_configureDevicesExpandableComposite.heightHint = 170;
		configureDevicesExpandableComposite.setLayoutData(gd_configureDevicesExpandableComposite);
		Composite configDevicesComp = new Composite(configureDevicesExpandableComposite, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		configDevicesComp.setLayout(gridLayout);

		addDevices(configDevicesComp);

		updateScannables();
		updateDetectors();
	}

	private void addDevices(final Composite composite) {
		createFindScannable(composite);
		createFindDetector(composite);

		configureDevicesExpandableComposite.setClient(composite);

		ExpansionAdapter addScannableExpansionListener = new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				GridUtils.layoutFull(composite.getParent());
			}
		};
		configureDevicesExpandableComposite.addExpansionListener(addScannableExpansionListener);
	}

	public void updateScannables() {
		List<String> names = new ArrayList<String>(bean.getScannables().size());
		for (int i = 1; i < bean.getScannables().size() + 1; i++)
			names.add(bean.getScannables().get(i - 1).getScannableName());
		scannableList.addItem(names);
	}

	public void updateDetectors() {
		List<String> names = new ArrayList<String>(bean.getDetectors().size());
		for (int i = 1; i < bean.getDetectors().size() + 1; i++)
			names.add(bean.getDetectors().get(i - 1).getDetectorName());
		detectorList.addItem(names);
	}

	private void createFindScannable(final Composite composite) {
		final Group addScannableGroup = new Group(composite, SWT.NONE);
		addScannableGroup.setText("Scannables");
		GridData gd_addScannableGroup = new GridData(SWT.LEFT, SWT.TOP, false, false);
		gd_addScannableGroup.widthHint = 160;
		addScannableGroup.setLayoutData(gd_addScannableGroup);
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
		gd_buttons.widthHint = 160;
		buttons.setLayoutData(gd_buttons);
		GridLayout gl_buttons = new GridLayout(3, false);
		gl_buttons.marginWidth = 0;
		gl_buttons.verticalSpacing = 0;
		gl_buttons.horizontalSpacing = 0;
		gl_buttons.marginHeight = 0;
		buttons.setLayout(gl_buttons);

		scannableManagerComposite = new ScannableManagerComposite(buttons, SWT.NONE);
		((GridData) scannableManagerComposite.getScannableName().getLayoutData()).widthHint = 104;
		GridData gd_scannableManagerComposite = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_scannableManagerComposite.widthHint = 104;
		scannableManagerComposite.setLayoutData(gd_scannableManagerComposite);

		addScannable = new Button(buttons, SWT.PUSH);
		addScannable.setImage(SWTResourceManager.getImage(getClass(), "/icons/add.png"));

		removeScannable = new Button(buttons, SWT.PUSH);
		removeScannable.setImage(SWTResourceManager.getImage(getClass(), "/icons/delete.png"));

		scannableList = new ObjectListEditor(addScannableGroup, SWT.NONE, "");
		Table table_2 = scannableList.viewer.getTable();
		GridData gd_table_2 = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_table_2.widthHint = 161;
		table_2.setLayoutData(gd_table_2);
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
		gd_scannableList.widthHint = 160;
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
	}

	private void createFindDetector(final Composite composite) {
		final Group addDetectorGroup = new Group(composite, SWT.NONE);
		addDetectorGroup.setText("Detectors");
		GridData gd_addDetectorGroup = new GridData(SWT.LEFT, SWT.TOP, false, false);
		gd_addDetectorGroup.widthHint = 160;
		addDetectorGroup.setLayoutData(gd_addDetectorGroup);
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
		gd_buttons.widthHint = 160;
		buttons.setLayoutData(gd_buttons);
		GridLayout gl_buttons = new GridLayout(3, false);
		gl_buttons.marginWidth = 0;
		gl_buttons.verticalSpacing = 0;
		gl_buttons.horizontalSpacing = 0;
		gl_buttons.marginHeight = 0;
		buttons.setLayout(gl_buttons);

		detectorManagerComposite = new DetectorManagerComposite(buttons, SWT.NONE);
		((GridData) detectorManagerComposite.getDetectorName().getLayoutData()).widthHint = 104;
		GridData gd_detectorManagerComposite = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_detectorManagerComposite.widthHint = 104;
		detectorManagerComposite.setLayoutData(gd_detectorManagerComposite);

		addDetector = new Button(buttons, SWT.PUSH);
		addDetector.setImage(SWTResourceManager.getImage(getClass(), "/icons/add.png"));

		removeDetector = new Button(buttons, SWT.PUSH);
		removeDetector.setImage(SWTResourceManager.getImage(getClass(), "/icons/delete.png"));

		detectorList = new ObjectListEditor(addDetectorGroup, SWT.NONE, "");
		Table table_2 = detectorList.viewer.getTable();
		GridData gd_table_2 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_table_2.widthHint = 160;
		table_2.setLayoutData(gd_table_2);
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
		GridData gd_detectorList = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_detectorList.widthHint = 160;
		gd_detectorList.heightHint = -1;
		detectorList.setLayoutData(gd_detectorList);

		GridData gridData2 = new GridData();
		gridData2.grabExcessHorizontalSpace = false;
		gridData2.heightHint = 80;
		gridData2.widthHint = 160;
		detectorList.viewer.getControl().setLayoutData(gridData2);

		detectorList.setEditorClass(DetectorManagerBean.class);
		detectorList.setEditorUI(detectorManagerComposite);
		detectorList.addBeanSelectionListener(new BeanSelectionListener() {
			@Override
			public void selectionChanged(BeanSelectionEvent evt) {
				detectorManagerComposite.selectionChanged((DetectorManagerBean) evt.getSelectedBean());
			}
		});
	}

	public ObjectListEditor getScannables() {
		return scannableList;
	}

	public ObjectListEditor getDetectors() {
		return detectorList;
	}

	public Button getRemoveScannable() {
		return removeScannable;
	}

	public Button getRemoveDetector() {
		return removeDetector;
	}

	public Button getAddDetector() {
		return addDetector;
	}

	public Button getAddScannable() {
		return addScannable;
	}

	public void setRemoveScannable(Button removeScannable) {
		this.removeScannable = removeScannable;
	}

	public void setBean(SimpleScan bean) {
		this.bean = bean;
	}

	public ScannableManagerComposite getScannableManagerComposite() {
		return scannableManagerComposite;
	}

	public DetectorManagerComposite getDetectorManagerComposite() {
		return detectorManagerComposite;
	}
}
