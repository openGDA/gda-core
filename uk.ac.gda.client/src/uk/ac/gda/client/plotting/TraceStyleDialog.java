/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.client.plotting;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.PointStyle;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace.TraceType;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.plotting.model.LineTraceProviderNode;
import uk.ac.gda.client.plotting.model.LineTraceProviderNode.TraceStyleDetails;

public class TraceStyleDialog extends TitleAreaDialog {

	private final DataBindingContext dataBindingCtx = new DataBindingContext();

	private final TraceStyleDetails traceStyle = new TraceStyleDetails();

	public TraceStyleDetails getTraceStyle() {
		return traceStyle;
	}

	public TraceStyleDialog(Shell parentShell, LineTraceProviderNode.TraceStyleDetails traceStyle) {
		super(parentShell);
		if (traceStyle != null) {
			try {
				BeanUtils.copyProperties(this.traceStyle, traceStyle);
			} catch (IllegalAccessException | InvocationTargetException e) {
				fillDefault();
			}
		} else {
			fillDefault();
		}
	}

	private void fillDefault() {
		traceStyle.setColorHexValue("#000000");
		traceStyle.setLineWidth(1);
		traceStyle.setTraceType(TraceType.SOLID_LINE);
		traceStyle.setPointSize(3);
		traceStyle.setPointStyle(PointStyle.DIAMOND);
	}

	@Override
	public void create() {
		super.create();
		setTitle("Trace line style");
		setMessage("Select the line style to apply", IMessageProvider.INFORMATION);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout(2, false);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		container.setLayout(layout);

		createColorName(container);
		createStyleName(container);
		createWidth(container);
		createPointStyle(container);
		createPointSize(container);
		return area;
	}

	private void createColorName(Composite container) {
		Label lbtFirstName = new Label(container, SWT.NONE);
		lbtFirstName.setText("Color");

		GridData dataFirstName = new GridData();
		dataFirstName.grabExcessHorizontalSpace = true;
		dataFirstName.horizontalAlignment = GridData.FILL;

		final Label colorLabel = new Label(container, SWT.BORDER);
		dataBindingCtx.bindValue(
				WidgetProperties.background().observe(colorLabel),
				BeanProperties.value(TraceStyleDetails.COLOR_HAX_VALUE_PROP_NAME).observe(traceStyle),
				new UpdateValueStrategy() {
					@Override
					public Object convert(Object fromObject) {
						return UIHelper.convertRGBToHexadecimal(((Color) fromObject).getRGB());
					}
				},
				new UpdateValueStrategy() {
					@Override
					public Object convert(Object fromObject) {
						return UIHelper.convertHexadecimalToColor(traceStyle.getColorHexValue(), TraceStyleDialog.this.getShell().getDisplay());
					}
				});

		colorLabel.setLayoutData(dataFirstName);
		colorLabel.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent e) {
				ColorDialog colorDialog = new ColorDialog(TraceStyleDialog.this.getShell());
				colorDialog.setText("Select your favorite color");
				RGB selectedColor = colorDialog.open();
				if (selectedColor != null) {
					traceStyle.setColorHexValue(UIHelper.convertRGBToHexadecimal(selectedColor));
				}
			}

			@Override
			public void mouseDown(MouseEvent e) {}

			@Override
			public void mouseDoubleClick(MouseEvent e) {}
		});
	}

	private void createStyleName(Composite container) {
		Label lbtLastName = new Label(container, SWT.NONE);
		lbtLastName.setText("Type");

		GridData dataLastName = new GridData();
		dataLastName.grabExcessHorizontalSpace = true;
		dataLastName.horizontalAlignment = GridData.FILL;
		ComboViewer styleCombo = new ComboViewer(container, SWT.READ_ONLY);
		styleCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((TraceType) element).toString();
			}
		});
		styleCombo.setContentProvider(new ArrayContentProvider());
		styleCombo.setInput(TraceType.values());
		styleCombo.getControl().setLayoutData(dataLastName);
		dataBindingCtx.bindValue(
				ViewersObservables.observeSingleSelection(styleCombo),
				BeanProperties.value(TraceStyleDetails.TRACE_TYPE_PROP_NAME).observe(traceStyle));
	}

	private void createWidth(Composite container) {
		Label lbtLastName = new Label(container, SWT.NONE);
		lbtLastName.setText("Line Width");

		GridData dataLastName = new GridData();
		dataLastName.grabExcessHorizontalSpace = true;
		dataLastName.horizontalAlignment = GridData.FILL;
		ComboViewer widthCombo = new ComboViewer(container, SWT.READ_ONLY);
		widthCombo.setLabelProvider(new LabelProvider());
		widthCombo.setContentProvider(new ArrayContentProvider());
		widthCombo.setInput(new Integer[]{1, 2, 3, 4, 5});
		widthCombo.getCombo().setLayoutData(dataLastName);
		dataBindingCtx.bindValue(
				ViewersObservables.observeSingleSelection(widthCombo),
				BeanProperties.value(TraceStyleDetails.LINE_WIDTH_PROP_NAME).observe(traceStyle));
	}

	private void createPointStyle(Composite container) {
		Label lbtLastName = new Label(container, SWT.NONE);
		lbtLastName.setText("Point style");

		GridData dataLastName = new GridData();
		dataLastName.grabExcessHorizontalSpace = true;
		dataLastName.horizontalAlignment = GridData.FILL;

		ComboViewer pointStyleCombo = new ComboViewer(container, SWT.READ_ONLY);
		pointStyleCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((PointStyle) element).toString();
			}
		});
		pointStyleCombo.setContentProvider(new ArrayContentProvider());
		pointStyleCombo.setInput(PointStyle.values());
		pointStyleCombo.getControl().setLayoutData(dataLastName);
		dataBindingCtx.bindValue(
				ViewersObservables.observeSingleSelection(pointStyleCombo),
				BeanProperties.value(TraceStyleDetails.POINT_STYLE_PROP_NAME).observe(traceStyle));
	}

	private void createPointSize(Composite container) {
		Label lbtLastName = new Label(container, SWT.NONE);
		lbtLastName.setText("Point Width");

		GridData dataLastName = new GridData();
		dataLastName.grabExcessHorizontalSpace = true;
		dataLastName.horizontalAlignment = GridData.FILL;
		ComboViewer widthCombo = new ComboViewer(container, SWT.READ_ONLY);
		widthCombo.setLabelProvider(new LabelProvider());
		widthCombo.setContentProvider(new ArrayContentProvider());
		widthCombo.setInput(new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
		widthCombo.getCombo().setLayoutData(dataLastName);
		dataBindingCtx.bindValue(
				ViewersObservables.observeSingleSelection(widthCombo),
				BeanProperties.value(TraceStyleDetails.POINT_SIZE_PROP_NAME).observe(traceStyle));
	}


	@Override
	protected boolean isResizable() {
		return true;
	}
}