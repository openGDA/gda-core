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

package uk.ac.gda.exafs.ui;

import java.net.URL;

import org.eclipse.richbeans.widgets.wrappers.BooleanWrapper;
import org.eclipse.richbeans.widgets.wrappers.TextWrapper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import uk.ac.gda.beans.exafs.i20.I20OutputParameters;
import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.richbeans.editors.DirtyContainer;

public class I20OutputParametersUIEditor extends OutputParametersUIEditor {
	private TextWrapper asciiFileName;

	private BooleanWrapper vortexSaveRawSpectrum;
	private BooleanWrapper xspressOnlyShowFF;
	private BooleanWrapper xspressShowDTRawValues;
	private BooleanWrapper xspressSaveRawSpectrum;

	private ExpandableComposite detectorsExpandableComposite;
	private I20OutputParameters bean;

	public I20OutputParametersUIEditor(String path, URL mappingURL, DirtyContainer dirtyContainer, Object editingBean) {
		super(path, mappingURL, dirtyContainer, editingBean);
		this.bean=(I20OutputParameters) editingBean;
	}

	@Override
	protected String getRichEditorTabText() {
		return "Output Parameters";
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		createDetectorOptions(rightColumn);
	}

	private void createDetectorOptions(Composite left) {
		detectorsExpandableComposite = new ExpandableComposite(left, SWT.NONE);
		detectorsExpandableComposite.setText("Fluorescence detectors output");
		detectorsExpandableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		final Composite detFoldersComp = new Composite(detectorsExpandableComposite, SWT.NONE);
		detFoldersComp.setLayout(new GridLayout(1, false));

		Group vortexPreferencesGroup = new Group(detFoldersComp, SWT.NONE);
		vortexPreferencesGroup.setText("Vortex (Si)");
		vortexPreferencesGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		vortexPreferencesGroup.setLayout(new GridLayout(1,false));

		Group xspressPreferencesGroup = new Group(detFoldersComp, SWT.NONE);
		xspressPreferencesGroup.setText("Xspress (Ge)");
		xspressPreferencesGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		xspressPreferencesGroup.setLayout(new GridLayout(1,false));

		detectorsExpandableComposite.setClient(detFoldersComp);

		ExpansionAdapter detFoldersListener = new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				GridUtils.layoutFull(detFoldersComp.getParent());
			}
		};
		detectorsExpandableComposite.addExpansionListener(detFoldersListener);

		this.vortexSaveRawSpectrum = new BooleanWrapper(vortexPreferencesGroup, SWT.NONE);
		vortexSaveRawSpectrum.setText("Save raw spectrum to file");
		vortexSaveRawSpectrum.setValue(false);
		new Label(vortexPreferencesGroup, SWT.NONE);

		this.xspressOnlyShowFF = new BooleanWrapper(xspressPreferencesGroup, SWT.NONE);
		xspressOnlyShowFF.setText("Hide individual elements");
		xspressOnlyShowFF.setToolTipText("In ascii output, only display the total in-window counts (FF) from the Xspress detector");
		xspressOnlyShowFF.setValue(Boolean.FALSE);

		this.xspressShowDTRawValues = new BooleanWrapper(xspressPreferencesGroup, SWT.NONE);
		xspressShowDTRawValues.setText("Show DT values");
		xspressShowDTRawValues.setToolTipText("Add the raw scaler values used in deadtime (DT) calculations to ascii output");
		xspressShowDTRawValues.setValue(Boolean.FALSE);

		this.xspressSaveRawSpectrum = new BooleanWrapper(xspressPreferencesGroup, SWT.NONE);
		xspressSaveRawSpectrum.setText("Save raw spectrum to file");
		xspressSaveRawSpectrum.setValue(false);
		new Label(xspressPreferencesGroup, SWT.NONE);

		detectorsExpandableComposite.setClient(detFoldersComp);

		ExpansionAdapter detExpansionListener = new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				if(vortexSaveRawSpectrum.getValue() || xspressOnlyShowFF.getValue() || xspressShowDTRawValues.getValue() || xspressSaveRawSpectrum.getValue())
					detectorsExpandableComposite.setExpanded(true);
				GridUtils.layoutFull(detFoldersComp.getParent());
			}
		};
		detectorsExpandableComposite.addExpansionListener(detExpansionListener);

		if(bean.isVortexSaveRawSpectrum() || bean.isXspressOnlyShowFF() || bean.isXspressSaveRawSpectrum() || bean.isXspressShowDTRawValues())
			detectorsExpandableComposite.setExpanded(true);
	}

	public TextWrapper getAsciiFileName() {
		return asciiFileName;
	}

	public BooleanWrapper getVortexSaveRawSpectrum() {
		return vortexSaveRawSpectrum;
	}

	public BooleanWrapper getXspressOnlyShowFF() {
		return xspressOnlyShowFF;
	}

	public BooleanWrapper getXspressShowDTRawValues() {
		return xspressShowDTRawValues;
	}

	public BooleanWrapper getXspressSaveRawSpectrum() {
		return xspressSaveRawSpectrum;
	}
}
