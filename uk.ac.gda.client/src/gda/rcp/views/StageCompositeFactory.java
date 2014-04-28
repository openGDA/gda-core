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

package gda.rcp.views;

import gda.device.DeviceException;
import gda.rcp.GDAClientActivator;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.composites.MotorPositionEditorControl;
import uk.ac.gda.client.observablemodels.ScannableWrapper;

public class StageCompositeFactory implements CompositeFactory {
	private static final Logger logger = LoggerFactory.getLogger(StageCompositeFactory.class);
	StageCompositeDefinition[] stageCompositeDefinitions;

	public StageCompositeDefinition[] getStageCompositeDefinitions() {
		return stageCompositeDefinitions;
	}

	public void setStageCompositeDefinitions(StageCompositeDefinition[] stageCompositeDefinitions) {
		this.stageCompositeDefinitions = stageCompositeDefinitions;
	}

	String label;
	private Integer labelWidth = null;
	private Image image;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public int getLabelWidth() {
		return labelWidth;
	}

	public void setLabelWidth(int labelWidth) {
		this.labelWidth = labelWidth;
	}

	public Control getTabControl(Composite parent) {
		Composite cmp;
		if (label != null) {
			Group translationGroup = new Group(parent, SWT.SHADOW_NONE);
			translationGroup.setText(label);
			cmp = translationGroup;
		} else {
			cmp = new Composite(parent, SWT.NONE);
		}
		GridDataFactory.fillDefaults().applyTo(cmp);
		GridLayoutFactory.fillDefaults().margins(1, 1).spacing(1, 1).applyTo(cmp);

		Composite c1 = new Composite(cmp, SWT.NONE);
		GridLayoutFactory.swtDefaults().margins(1, 1).spacing(2, 2).numColumns(2).applyTo(c1);
		Label label2 = new Label(c1, SWT.NONE);
		label2.setText("Stop all motors on this stage");
		GridDataFactory.swtDefaults().applyTo(label2);
		Button button = new Button(c1, SWT.PUSH);
		ImageDescriptor descr = GDAClientActivator.getImageDescriptor("icons/stop.png");
		if (descr != null) {
			image = descr.createImage();
			button.setImage(image);

		}
		button.setToolTipText("Stop all the motors on this stage");
		GridDataFactory.swtDefaults().applyTo(button);
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				for (StageCompositeDefinition s : stageCompositeDefinitions) {
					try {
						s.scannable.stop();
					} catch (DeviceException e1) {
						logger.error("Error stopping " + s.scannable.getName(), e1);
					}
				}
			}

		});

		Label sep = new Label(cmp, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sep);
		for (StageCompositeDefinition s : stageCompositeDefinitions) {
			
			try {
				Composite motorComp = new Composite(cmp, SWT.NONE);
				GridDataFactory.fillDefaults().grab(true, false).applyTo(motorComp);
				motorComp.setLayout(new GridLayout(2, false));

				Label label = new Label(motorComp, SWT.NONE);
				label.setText( s.label != null ? s.label :s.scannable.getName());
				GridDataFactory.swtDefaults().hint(labelWidth != null ? labelWidth : 120, SWT.DEFAULT).applyTo(label);
				
				MotorPositionEditorControl motorPosControl;
				motorPosControl = new MotorPositionEditorControl(motorComp, SWT.NONE, new ScannableWrapper(s.scannable), true, false);
				motorPosControl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
				double d = s.stepSize*Math.pow(10, s.getDecimalPlaces());
				motorPosControl.setIncrement((int)d);
			} catch (Exception e1) {
				logger.error("Error creating control for '", s.scannable.getName() + "'");
			}
			
			
			Label sep1 = new Label(cmp, SWT.SEPARATOR | SWT.HORIZONTAL);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(sep1);
		}

		cmp.addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (image != null) {
					image.dispose();
					image = null;
				}

			}
		});
		return cmp;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		return (Composite) getTabControl(parent);
	}

}
