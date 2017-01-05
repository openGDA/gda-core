/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.epics.dxp.client.views;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.edxd.common.IEdxdAlignment;
import uk.ac.gda.epics.dxp.client.BeamlineHutch;
import uk.ac.gda.epics.dxp.client.BeamlineHutch.Collimator;

public class EDXDDetectorSetupView extends ViewPart implements ISelectionProvider {

	private static final Logger logger = LoggerFactory.getLogger(EDXDDetectorSetupView.class);

	private static final String DEFAULT_SAMPLE_COLLIMATOR_DIST_VALUE = "100";
	private static final String DEFAULT_SAMPLE_DETECTOR_DISTANCE_VALUE = "2200";
	private static final String DEFAULT_2_THETA_ANGLE_VALUE = "4.61";

	private static final String LBL_SAMPLE_COLLIMATOR_DISTANCE = "Sample-Collimator Distance (mm)";
	private static final String LBL_SAMPLE_DETECTOR_DISTANCE = "Sample-Detector Distance (mm)";
	private static final String LBL_2_THETA_ANGLE = "2 theta angle";
	private static final String LBL_COLLIMATOR_4 = "C4";
	private static final String LBL_COLLIMATOR_3 = "C3";
	private static final String LBL_HUTCH_2 = "EH2";
	private static final String LBL_COLLIMATOR_2 = "C2";

	private static final String LBL_COLLIMATOR_1 = "C1";
	private static final String LBL_HUTCH_1 = "EH1";
	private static final String LBL_FORM_HEADER = "EDXD Detector Setup";
	public static final String ID = "uk.ac.gda.epics.dxp.client.setupview";

	private Button btnEh1;
	private Button btnC1;
	private Button btnC2;
	private Button btnEh2;
	private Button btnC3;
	private Button btnC4;

	private BeamlineHutch currentHutch = BeamlineHutch.None;
	private Collimator currentCollimator = BeamlineHutch.Collimator.Nil;

	private IEdxdAlignment edxdAlignment;

	public void setEdxdAlignment(IEdxdAlignment edxdAlignment) {
		this.edxdAlignment = edxdAlignment;
	}

	@Override
	public void createPartControl(Composite parent) {
		FormToolkit formToolkit = new FormToolkit(getViewSite().getShell().getDisplay());

		formToolkit.setBackground(ColorConstants.white);

		ScrolledForm scrolledForm = formToolkit.createScrolledForm(parent);
		scrolledForm.setText(LBL_FORM_HEADER);
		formToolkit.decorateFormHeading(scrolledForm.getForm());

		scrolledForm.getBody().setLayout(new FillLayout());
		Composite formContents = formToolkit.createComposite(scrolledForm.getBody());
		// Composite formContents = scrolledForm.getBody();

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		formContents.setLayout(layout);

		Composite buttonsComposite = formToolkit.createComposite(formContents);
		buttonsComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		buttonsComposite.setLayout(new GridLayout(3, true));

		btnEh1 = formToolkit.createButton(buttonsComposite, LBL_HUTCH_1, SWT.TOGGLE);
		btnEh1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnEh1.addSelectionListener(buttonSelectionAdapter);

		btnC1 = formToolkit.createButton(buttonsComposite, LBL_COLLIMATOR_1, SWT.TOGGLE);
		btnC1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnC1.addSelectionListener(buttonSelectionAdapter);

		btnC2 = formToolkit.createButton(buttonsComposite, LBL_COLLIMATOR_2, SWT.TOGGLE);
		btnC2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnC2.addSelectionListener(buttonSelectionAdapter);

		btnEh2 = formToolkit.createButton(buttonsComposite, LBL_HUTCH_2, SWT.TOGGLE);
		btnEh2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnEh2.addSelectionListener(buttonSelectionAdapter);

		btnC3 = formToolkit.createButton(buttonsComposite, LBL_COLLIMATOR_3, SWT.TOGGLE);
		btnC3.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnC3.addSelectionListener(buttonSelectionAdapter);

		btnC4 = formToolkit.createButton(buttonsComposite, LBL_COLLIMATOR_4, SWT.TOGGLE);
		btnC4.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnC4.addSelectionListener(buttonSelectionAdapter);

		buttonsComposite.setSize(buttonsComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		Composite remainingElementsComposite = formToolkit.createComposite(formContents);
		remainingElementsComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		remainingElementsComposite.setLayout(new GridLayout(2, false));

		Label lbl2ThetaAngle = formToolkit.createLabel(remainingElementsComposite, LBL_2_THETA_ANGLE, SWT.RIGHT);
		lbl2ThetaAngle.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label lbl2ThetaVal = formToolkit.createLabel(remainingElementsComposite, DEFAULT_2_THETA_ANGLE_VALUE,
				SWT.BORDER | SWT.CENTER);
		lbl2ThetaVal.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label lblSampleDetDistance = formToolkit.createLabel(remainingElementsComposite, LBL_SAMPLE_DETECTOR_DISTANCE,
				SWT.RIGHT);
		lblSampleDetDistance.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label lblSampleDetDistVal = formToolkit.createLabel(remainingElementsComposite,
				DEFAULT_SAMPLE_DETECTOR_DISTANCE_VALUE, SWT.BORDER | SWT.CENTER);
		lblSampleDetDistVal.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label lblSampleColDist = formToolkit.createLabel(remainingElementsComposite, LBL_SAMPLE_COLLIMATOR_DISTANCE,
				SWT.RIGHT);
		lblSampleColDist.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label lblSampleColDistVal = formToolkit.createLabel(remainingElementsComposite,
				DEFAULT_SAMPLE_COLLIMATOR_DIST_VALUE, SWT.BORDER | SWT.CENTER);
		lblSampleColDistVal.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		scrolledForm.setMinSize(formContents.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		scrolledForm.reflow(true);
	}

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		site.setSelectionProvider(this);
	}

	private SelectionAdapter buttonSelectionAdapter = new SelectionAdapter() {
		@Override
		public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
			if (btnEh1.equals(e.getSource())) {
				if (btnEh1.getSelection()) {
					btnEh2.setSelection(false);
					btnC3.setSelection(false);
					btnC4.setSelection(false);
					currentHutch = BeamlineHutch.EH1;
				} else {
					btnC1.setSelection(false);
					btnC2.setSelection(false);
					currentHutch = BeamlineHutch.None;
				}
				notifyListeners();

			} else if (btnEh2.equals(e.getSource())) {
				if (btnEh2.getSelection()) {
					btnEh1.setSelection(false);
					btnC1.setSelection(false);
					btnC2.setSelection(false);
					currentHutch = BeamlineHutch.EH2;
				} else {
					btnC3.setSelection(false);
					btnC4.setSelection(false);
					currentHutch = BeamlineHutch.None;
				}
				notifyListeners();
			} else if (btnC1.equals(e.getSource()) && btnC1.getSelection()) {
				btnEh2.setSelection(false);
				btnC3.setSelection(false);
				btnC4.setSelection(false);
				btnC2.setSelection(false);
				btnEh1.setSelection(true);
				currentCollimator = BeamlineHutch.Collimator.C1;
			} else if (btnC2.equals(e.getSource()) && btnC2.getSelection()) {
				btnEh2.setSelection(false);
				btnC3.setSelection(false);
				btnC4.setSelection(false);
				btnC1.setSelection(false);
				btnEh1.setSelection(true);
				currentCollimator = BeamlineHutch.Collimator.C2;
			} else if (btnC3.equals(e.getSource()) && btnC3.getSelection()) {
				btnEh1.setSelection(false);
				btnC1.setSelection(false);
				btnC2.setSelection(false);
				btnC4.setSelection(false);
				btnEh2.setSelection(true);
				currentCollimator = BeamlineHutch.Collimator.C3;
			} else if (btnC4.equals(e.getSource()) && btnC4.getSelection()) {
				btnEh1.setSelection(false);
				btnC1.setSelection(false);
				btnC2.setSelection(false);
				btnC3.setSelection(false);
				btnEh2.setSelection(true);
				currentCollimator = BeamlineHutch.Collimator.C4;
			}
		}
	};

	@Override
	public void setFocus() {

	}

	protected void notifyListeners() {
		for (Object lis : listeners.getListeners()) {
			((ISelectionChangedListener) lis).selectionChanged(new SelectionChangedEvent(this,
					new DetectorViewSelection(currentHutch, currentCollimator)));

		}
	}

	@Override
	public String getPartName() {
		return "Edxd Detector Setup";
	}

	private ListenerList listeners = new ListenerList();

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);
	}

	@Override
	public ISelection getSelection() {
		return new DetectorViewSelection(currentHutch, currentCollimator);
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void setSelection(ISelection selection) {
		logger.debug("Selection is :{}", selection);
	}

}
