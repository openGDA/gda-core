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

import gda.jython.JythonServerFacade;

import java.net.URL;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.exafs.i18.I18SampleParameters;
import uk.ac.gda.exafs.ui.composites.AttenuatorParametersComposite;
import uk.ac.gda.exafs.ui.composites.I18SampleParametersComposite;
import uk.ac.gda.exafs.ui.composites.SampleStageParametersComposite;
import uk.ac.gda.richbeans.components.FieldComposite;
import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;

public final class I18SampleParametersUIEditor extends RichBeanEditorPart {

	private static final Logger logger = LoggerFactory.getLogger(I18SampleParametersUIEditor.class);

	private I18SampleParametersComposite beanComposite;

	public I18SampleParametersUIEditor(String path, URL mappingURL, DirtyContainer dirtyContainer, Object editingBean) {
		super(path, mappingURL, dirtyContainer, editingBean);
	}

	@Override
	public String getRichEditorTabText() {
		return "I18SampleParameters";
	}

	@Override
	public void createPartControl(Composite comp) {
		final ScrolledComposite scrolledComposite = new ScrolledComposite(comp, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		this.beanComposite = new I18SampleParametersComposite(scrolledComposite, SWT.NONE,
				(I18SampleParameters) editingBean);
		try {
			if (((I18SampleParameters) editingBean).getSampleStageParameters().getDisable())
				this.beanComposite.disableSample();
			else
				this.beanComposite.enableSample();

		} catch (Exception e) {
			logger.error("Error enabling/disabling sample", e);
		}

		ExpansionAdapter stageExpansionListener = new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				beanComposite.setVfmxActive(e.getState());
				dirtyContainer.setDirty(true);
			}
		};
		beanComposite.getKbExpandableComposite().addExpansionListener(stageExpansionListener);

		fillAttenuatorPositions();
		addListeners();
		scrolledComposite.setContent(beanComposite);
		beanComposite.layout();
		scrolledComposite.setMinSize(beanComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	private void addListeners() {

		beanComposite.getAttnCurrentPosition().addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				String att1val = JythonServerFacade.getInstance().evaluateCommand("D7A()");
				String att2val = JythonServerFacade.getInstance().evaluateCommand("D7B()");
				beanComposite.getAttenuatorParameter1().getSelectedPosition().setValue(att1val);
				beanComposite.getAttenuatorParameter2().getSelectedPosition().setValue(att2val);
				beanComposite.getAttenuatorParameter1().setPosition(att1val);
				beanComposite.getAttenuatorParameter2().setPosition(att2val);
			}
		});
	}

	private void fillAttenuatorPositions() {
		AttenuatorParametersComposite at1 = this.beanComposite.getAttenuatorParameter1();
		AttenuatorParametersComposite at2 = this.beanComposite.getAttenuatorParameter2();
		I18SampleParameters sampleBean = (I18SampleParameters) editingBean;

		ArrayList<String> positions1 = sampleBean.getAttenuatorParameter1().getPosition();
		ArrayList<String> positions2 = sampleBean.getAttenuatorParameter2().getPosition();
		
		at1.getSelectedPosition().setItems(positions1.toArray(new String[]{}));
		at2.getSelectedPosition().setItems(positions2.toArray(new String[]{}));
	}

	@Override
	public void setFocus() {
	}

	public FieldComposite getName() {
		return beanComposite.getName();
	}

	public FieldComposite getDescription() {
		return beanComposite.getDescription();
	}

	public SampleStageParametersComposite getSampleStageParameters() {
		return beanComposite.getSampleStageParameters();
	}

	public AttenuatorParametersComposite getAttenuatorParameter1() {
		return beanComposite.getAttenuatorParameter1();
	}

	public AttenuatorParametersComposite getAttenuatorParameter2() {
		return beanComposite.getAttenuatorParameter2();
	}

	public FieldComposite getVfmx() {
		return beanComposite.getVfmx();
	}

	public boolean isVfmxActive() {
		return beanComposite.isVfmxActive();
	}

	@Override
	public void linkUI(final boolean isPageChange) {
		super.linkUI(isPageChange);
		try {
			if (((I18SampleParameters) editingBean).getSampleStageParameters().getDisable())
				this.beanComposite.disableSample();
			else
				this.beanComposite.enableSample();
		} catch (Exception e) {
			logger.error("Error while trying to enable/disable sample on the bean for sample parameters", e);
		}
	}
}
