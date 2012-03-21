/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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
import java.util.StringTokenizer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import uk.ac.gda.beans.exafs.i18.I18SampleParameters;
import uk.ac.gda.exafs.ui.composites.AttenuatorParametersComposite;
import uk.ac.gda.exafs.ui.composites.I18SampleParametersComposite;
import uk.ac.gda.exafs.ui.composites.SampleStageParametersComposite;
import uk.ac.gda.richbeans.components.FieldComposite;
import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.editors.RichBeanEditorPart;

/**
 *
 */
public final class I18SampleParametersUIEditor extends RichBeanEditorPart {

	public I18SampleParametersUIEditor(String path, URL mappingURL, DirtyContainer dirtyContainer, Object editingBean) {
		super(path, mappingURL, dirtyContainer, editingBean);
	}

	private I18SampleParametersComposite beanComposite;

	/**
	 * @return editor text
	 */
	@Override
	public String getRichEditorTabText() {
		return "I18SampleParameters";
	}

	/**
	 * 
	 */
	@Override
	public void createPartControl(Composite comp) {
		final ScrolledComposite scrolledComposite = new ScrolledComposite(comp, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		this.beanComposite = new I18SampleParametersComposite(scrolledComposite, SWT.NONE);
		((GridData) beanComposite.getSampleStageParameters().getZ().getLayoutData()).widthHint = 544;
		fillAttenuatorPositions();
		addListeners();
		//loadRealPositions();
		scrolledComposite.setContent(beanComposite);
		beanComposite.layout();
		scrolledComposite.setMinSize(beanComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
	}

	private void loadRealPositions() {
		final JythonServerFacade jythonServerFacade = JythonServerFacade.getInstance();
		String command = "getSampleXYZPositions()";        
		StringTokenizer s =new StringTokenizer(jythonServerFacade.evaluateCommand(command).trim(), ",");
		//positions will be sent from the script as space separated string  "x y z"
		System.out.println(beanComposite.getSampleStageParameters().getX().getValue());
		beanComposite.getSampleStageParameters().getX().setValue(Double.valueOf(s.nextToken()));
		beanComposite.getSampleStageParameters().getY().setValue(Double.valueOf(s.nextToken()));
		beanComposite.getSampleStageParameters().getZ().setValue(Double.valueOf(s.nextToken()));
		//fill the real attenuator position
		/*command = "getAttenuatorPositions(\""+(String)beanComposite.getAttenuatorParameter1().getName().getValue()
		+"\",\""+(String)beanComposite.getAttenuatorParameter2().getName().getValue()+"\")";
		s =new StringTokenizer(jythonServerFacade.evaluateCommand(command).trim(), ",");
		beanComposite.getAttenuatorParameter1().getPosition().setText(s.nextToken());
		beanComposite.getAttenuatorParameter2().getPosition().setText(s.nextToken());*/
		
	}
	private void loadAttnRealPositions() {
		final JythonServerFacade jythonServerFacade = JythonServerFacade.getInstance();
		String command = "getAttenuatorPositions(\""+(String)beanComposite.getAttenuatorParameter1().getName().getValue()
		+"\",\""+(String)beanComposite.getAttenuatorParameter2().getName().getValue()+"\")";
		StringTokenizer s =new StringTokenizer(jythonServerFacade.evaluateCommand(command).trim(), ",");
		beanComposite.getAttenuatorParameter1().getPosition().setText(s.nextToken());
		beanComposite.getAttenuatorParameter2().getPosition().setText(s.nextToken());
		beanComposite.getAttenuatorParameter1().getSelectedPosition().setValue(beanComposite.getAttenuatorParameter1().getPosition().getText());
		beanComposite.getAttenuatorParameter2().getSelectedPosition().setValue(beanComposite.getAttenuatorParameter2().getPosition().getText());
		
	}

	private void addListeners() {
		this.beanComposite.getAttenuatorParameter1().getPosition().addSelectionListener(new SelectionListener(){

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				beanComposite.getAttenuatorParameter1().getSelectedPosition().setValue(beanComposite.getAttenuatorParameter1().getPosition().getText());
				
			}
			
		});
		this.beanComposite.getAttenuatorParameter2().getPosition().addSelectionListener(new SelectionListener(){

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				beanComposite.getAttenuatorParameter2().getSelectedPosition().setValue(beanComposite.getAttenuatorParameter2().getPosition().getText());
				
			}
			
		});
		
		beanComposite.getCurrentPosition().addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				loadRealPositions();
			
			}
		});
		beanComposite.getAttnCurrentPosition().addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				loadAttnRealPositions();
			
			}
		});
	}

	private void fillAttenuatorPositions() {
		AttenuatorParametersComposite at1 = this.beanComposite.getAttenuatorParameter1();
		AttenuatorParametersComposite at2 = this.beanComposite.getAttenuatorParameter2();
		I18SampleParameters sampleBean = (I18SampleParameters) editingBean;
		ArrayList <String>positions1 = sampleBean.getAttenuatorParameter1().getPosition();
		for (String pos : positions1)
			{
				at1.getPosition().add(pos);
			}
			at1.getPosition().setText(sampleBean.getAttenuatorParameter1().getSelectedPosition());
		ArrayList <String>positions2 = sampleBean.getAttenuatorParameter2().getPosition();
		for (String pos : positions2)
			{
				at2.getPosition().add(pos);
			}
		at2.getPosition().setText(sampleBean.getAttenuatorParameter2().getSelectedPosition());
		
	}

	/**
	 * 
	 */
	@Override
	public void setFocus() {
		//TODO
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
	

	@Override
	public void linkUI(final boolean isPageChange) {
		super.linkUI(isPageChange);
		//loadRealPositions();
	}
	
}
