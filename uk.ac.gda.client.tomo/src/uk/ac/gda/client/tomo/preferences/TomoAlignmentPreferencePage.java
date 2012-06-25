/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.TomoClientActivator;

/**
 * Preference page for tomography alignment.
 */
public class TomoAlignmentPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private static final String INVALID_VALUE_ERRMSG = "Invalid value - %1$s";
	private static final String DARK = "Dark";
	private static final String FLAT = "Flat";
	private static final String PREFERENCES_FOR_TOMOGRAPHY_ALIGNMENT = "Preferences for tomography alignment";
	private static final String SAMPLE_MOVE_DISTANCE = "Sample move Distance";
	private static final String DISTANCE_TO_MOVE_SAMPLE = "Distance to move sample";
	private static final String NUMBER_OF_IMAGES = "Number of Images";
	private static final String ERR_MSG_ID = "ERR_MSG_ID";
	private static final Logger logger = LoggerFactory.getLogger(TomoAlignmentPreferencePage.class);
	private IPreferenceStore preferenceStore;
	private Text txtNumFlatImages;
	private Text txtSampleMoveDist;
	private Text txtNumDarkImages;
	public static final String TOMO_CLIENT_FLAT_NUM_IMG_PREF = "TOMO_CLIENT_FLAT_NUM_IMG_PREF";
	public static final String TOMO_CLIENT_DARK_NUM_IMG_PREF = "TOMO_CLIENT_DARK_NUM_IMG_PREF";
	public static final String TOMO_CLIENT_FLAT_DIST_MOVE_PREF = "TOMO_CLIENT_FLAT_DIST_MOVE_PREF";
	public static final String TOMO_CLIENT_SAMPLE_EXPOSURE_TIME = "TOMO_CLIENT_SAMPLE_EXPOSURE_TIME";
	public static final String TOMO_CLIENT_FLAT_EXPOSURE_TIME = "TOMO_CLIENT_FLAT_EXPOSURE_TIME";

	/**
	 * 
	 */
	public TomoAlignmentPreferencePage() {
	}

	/**
	 * @param title
	 */
	public TomoAlignmentPreferencePage(String title) {
		super(title);
	}

	/**
	 * @param title
	 * @param image
	 */
	public TomoAlignmentPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	@Override
	public void init(IWorkbench workbench) {
		logger.info("Pref page initialized");
		setDescription(PREFERENCES_FOR_TOMOGRAPHY_ALIGNMENT);
		preferenceStore = TomoClientActivator.getDefault().getPreferenceStore();
	}

	@Override
	protected Control createContents(Composite parent) {
		logger.info("Pref page controls created");

		Composite root = new Composite(parent, SWT.None);
		root.setLayout(new GridLayout());
		// Flat group
		Group flatGroup = new Group(root, SWT.None);
		flatGroup.setText(FLAT);
		GridLayout gl = new GridLayout(2, true);

		flatGroup.setLayout(gl);
		flatGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label lblFlatImages = new Label(flatGroup, SWT.None);
		lblFlatImages.setText(NUMBER_OF_IMAGES);
		lblFlatImages.setLayoutData(new GridData());

		int intNumImgs = preferenceStore.getInt(TOMO_CLIENT_FLAT_NUM_IMG_PREF);

		txtNumFlatImages = new Text(flatGroup, SWT.BORDER);
		txtNumFlatImages.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtNumFlatImages.setText(Integer.toString(intNumImgs));
		txtNumFlatImages.addModifyListener(intVerifyListener);
		txtNumFlatImages.setData(ERR_MSG_ID, NUMBER_OF_IMAGES);

		Label lblDistanceToMove = new Label(flatGroup, SWT.None);
		lblDistanceToMove.setText(DISTANCE_TO_MOVE_SAMPLE);
		lblDistanceToMove.setLayoutData(new GridData());

		txtSampleMoveDist = new Text(flatGroup, SWT.BORDER);
		txtSampleMoveDist.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtSampleMoveDist.addVerifyListener(doubleVerifyListener);
		txtSampleMoveDist.setText(Double.toString(preferenceStore.getDouble(TOMO_CLIENT_FLAT_DIST_MOVE_PREF)));
		txtSampleMoveDist.setData(ERR_MSG_ID, SAMPLE_MOVE_DISTANCE);

		// Dark group
		Group darkGroup = new Group(root, SWT.None);
		darkGroup.setText(DARK);
		gl = new GridLayout(2, true);

		darkGroup.setLayout(gl);
		darkGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label lblNumDarkImages = new Label(darkGroup, SWT.None);
		lblNumDarkImages.setText(NUMBER_OF_IMAGES);
		lblNumDarkImages.setLayoutData(new GridData());

		int intNumDarkImgs = preferenceStore.getInt(TOMO_CLIENT_DARK_NUM_IMG_PREF);

		txtNumDarkImages = new Text(darkGroup, SWT.BORDER);
		txtNumDarkImages.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtNumDarkImages.setText(Integer.toString(intNumDarkImgs));
		txtNumDarkImages.addModifyListener(intVerifyListener);
		txtNumDarkImages.setData(ERR_MSG_ID, NUMBER_OF_IMAGES);

		return root;
	}

	private ModifyListener intVerifyListener = new ModifyListener() {

		@Override
		public void modifyText(ModifyEvent e) {
			Object source = e.getSource();
			if (source.equals(txtNumFlatImages)) {
				String txt = txtNumFlatImages.getText();
				if (txt == null || txt.length() < 1) {
					setErrorMessage(String.format(INVALID_VALUE_ERRMSG, e.widget.getData(ERR_MSG_ID)));
				} else if (!txt.matches("(\\d)*")) {
					setErrorMessage(String.format(INVALID_VALUE_ERRMSG, e.widget.getData(ERR_MSG_ID)));
				} else {
					setErrorMessage(null);
				}
			}
			if (source.equals(txtNumDarkImages)) {
				String txt = txtNumDarkImages.getText();
				if (txt == null || txt.length() < 1) {
					setErrorMessage(String.format(INVALID_VALUE_ERRMSG, e.widget.getData(ERR_MSG_ID)));
				} else if (!txt.matches("(\\d)*")) {
					setErrorMessage(String.format(INVALID_VALUE_ERRMSG, e.widget.getData(ERR_MSG_ID)));
				} else {
					setErrorMessage(null);
				}
			}
		}
	};
	private VerifyListener doubleVerifyListener = new VerifyListener() {

		@Override
		public void verifyText(VerifyEvent e) {
			Object source = e.getSource();
			if (source.equals(txtSampleMoveDist)) {
				String txt = txtNumFlatImages.getText();
				if (txt == null || txt.length() < 1) {
					setErrorMessage(String.format(INVALID_VALUE_ERRMSG, e.widget.getData(ERR_MSG_ID)));
				} else if (!txt.matches("(\\d)*.?(\\d)*")) {
					setErrorMessage(String.format(INVALID_VALUE_ERRMSG, e.widget.getData(ERR_MSG_ID)));
				} else {
					setErrorMessage(null);
				}
			}

		}
	};

	@Override
	public boolean performOk() {
		preferenceStore.setValue(TOMO_CLIENT_FLAT_DIST_MOVE_PREF, Double.parseDouble(txtSampleMoveDist.getText()));
		preferenceStore.setValue(TOMO_CLIENT_FLAT_NUM_IMG_PREF, Integer.parseInt(txtNumFlatImages.getText()));
		preferenceStore.setValue(TOMO_CLIENT_DARK_NUM_IMG_PREF, Integer.parseInt(txtNumDarkImages.getText()));
		return true;
	}

	@Override
	protected void performDefaults() {
		txtSampleMoveDist.setText(Double.toString(preferenceStore.getDefaultDouble(TOMO_CLIENT_FLAT_DIST_MOVE_PREF)));
		txtNumFlatImages.setText(Integer.toString(preferenceStore.getDefaultInt(TOMO_CLIENT_FLAT_NUM_IMG_PREF)));
		txtNumDarkImages.setText(Integer.toString(preferenceStore.getDefaultInt(TOMO_CLIENT_DARK_NUM_IMG_PREF)));
	}
}
