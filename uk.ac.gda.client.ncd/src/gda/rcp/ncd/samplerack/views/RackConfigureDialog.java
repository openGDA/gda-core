/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package gda.rcp.ncd.samplerack.views;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import uk.ac.gda.server.ncd.samplerack.RackConfigurationInput;
import uk.ac.gda.server.ncd.samplerack.SampleRack;
import uk.ac.gda.server.ncd.samplerack.SampleRackService;

public class RackConfigureDialog extends TitleAreaDialog {

	private SampleRackService sampleRackService;
	private RackConfigurationInput rackConfigurationInput;
	private SampleRack sampleRack;

	private Text xCalPosText;
	private Text yCalPosText;
	private Text xOffsetText;
	private Text yOffsetText;
	private Text xSpaceText;
	private Text ySpaceText;
	private Text xAxisText;
	private Text yAxisText;

	public RackConfigureDialog(SampleRack sampleRack, SampleRackService sampleRackService) {
		super(null);
		this.sampleRack=sampleRack;
		this.sampleRackService=sampleRackService;
		this.rackConfigurationInput=sampleRackService.getRackConfigurationInput(sampleRack);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void okPressed() {
		saveInput();
		sampleRackService.configureSampleRack(sampleRack, rackConfigurationInput);
		super.okPressed();
	}

	@Override
	public void create() {
		super.create();
		setTitle("Rack Configuration");
		setMessage("Configure " + sampleRack.getName(), IMessageProvider.INFORMATION);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(2, true);

		container.setLayout(layout);
		createCalPosition(container);
		createOffset(container);
		createSpace(container);
		createAxis(container);
		area.pack();
		return area;
	}

	private void createCalPosition(Composite container) {

		GridLayout layout = new GridLayout(2, true);

		Group posInfo = new Group(container, SWT.NULL);
		posInfo.setText("Position Info");
		posInfo.setLayout(layout);
		GridData layoutDataGroup = new GridData(SWT.FILL, SWT.FILL, true, true);
		posInfo.setLayoutData(layoutDataGroup);

		CLabel xPosLabel = new CLabel(posInfo, SWT.RIGHT);
		xPosLabel.setText("X Position");
		xPosLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.RIGHT, true, true));

		xCalPosText = new Text(posInfo, SWT.BORDER);
		xCalPosText.setLayoutData(container.getLayoutData());
		xCalPosText.setText(String.valueOf(rackConfigurationInput.xCalPos()));
		xCalPosText.addModifyListener(textModifyValidationListener(xCalPosText));

		CLabel yPosLabel = new CLabel(posInfo, SWT.RIGHT);
		yPosLabel.setText("Y Position");
		yPosLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.RIGHT, true, true));

		yCalPosText = new Text(posInfo, SWT.BORDER);
		yCalPosText.setLayoutData(container.getLayoutData());
		yCalPosText.setText(String.valueOf(rackConfigurationInput.yCalPos()));
		yCalPosText.addModifyListener(textModifyValidationListener(yCalPosText));
	}

	private ModifyListener textModifyValidationListener(Text textField) {
		return new ModifyListener() {

			// decorator for UI warning
			ControlDecoration decorator;
			/*
			 * In this anonymous constructor we will initialise what needs to be initialised only once, namely the decorator.
			 */
			{
				decorator = new ControlDecoration(textField, SWT.CENTER);
				decorator.setDescriptionText("Not a valid double number");
				Image image = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage();
				decorator.setImage(image);
				decorator.hide();
			}

			@Override
			public void modifyText(ModifyEvent e) {
				try {
					Double.parseDouble(((Text) e.getSource()).getText());
					decorator.hide();
				} catch (NumberFormatException ex) {
					decorator.show();
				}
			}
		};
	}

	private void createOffset(Composite container) {
		GridLayout layout = new GridLayout(2, true);

		Group offsetInfo = new Group(container, SWT.NULL);
		offsetInfo.setText("Offset Info");
		offsetInfo.setLayout(layout);
		GridData layoutDataGroup = new GridData(SWT.FILL, SWT.FILL, true, true);
		offsetInfo.setLayoutData(layoutDataGroup);

		CLabel xOffsetLabel = new CLabel(offsetInfo, SWT.RIGHT);
		xOffsetLabel.setText("X Offset");
		xOffsetLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.RIGHT, true, true));

		xOffsetText = new Text(offsetInfo, SWT.READ_ONLY | SWT.BORDER);
		xOffsetText.setText(String.valueOf(rackConfigurationInput.xOffset()));
		xOffsetText.setLayoutData(container.getLayoutData());
		xOffsetText.setEnabled(false);

		CLabel yOffsetLabel = new CLabel(offsetInfo, SWT.RIGHT);
		yOffsetLabel.setText("Y Offset");
		yOffsetLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.RIGHT, true, true));

		yOffsetText = new Text(offsetInfo, SWT.READ_ONLY | SWT.BORDER);
		yOffsetText.setText(String.valueOf(rackConfigurationInput.yOffset()));
		yOffsetText.setLayoutData(container.getLayoutData());
		yOffsetText.setEnabled(false);
	}

	private void createSpace(Composite container) {
		GridLayout layout = new GridLayout(2, true);

		Group spaceInfo = new Group(container, SWT.BEGINNING);
		spaceInfo.setText("Spacing Info");
		spaceInfo.setLayout(layout);
		GridData layoutDataGroup = new GridData(SWT.FILL, SWT.FILL, true, true);
		spaceInfo.setLayoutData(layoutDataGroup);

		CLabel xSpaceLabel = new CLabel(spaceInfo, SWT.NONE);
		xSpaceLabel.setText("X Spacing");
		xSpaceLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.RIGHT, true, true));

		xSpaceText = new Text(spaceInfo, SWT.READ_ONLY | SWT.BORDER);
		xSpaceText.setText(String.valueOf(rackConfigurationInput.xSpace()));
		xSpaceText.setLayoutData(container.getLayoutData());
		xSpaceText.setEnabled(false);

		CLabel ySpaceLabel = new CLabel(spaceInfo, SWT.NONE);
		ySpaceLabel.setText("Y Spacing");
		ySpaceLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.RIGHT, true, true));

		ySpaceText = new Text(spaceInfo,SWT.READ_ONLY |  SWT.BORDER);
		ySpaceText.setText(String.valueOf(rackConfigurationInput.ySpace()));
		ySpaceText.setLayoutData(container.getLayoutData());
		ySpaceText.setEnabled(false);
	}

	private void createAxis(Composite container) {
		GridLayout layout = new GridLayout(2, true);

		Group axisInfo = new Group(container, SWT.END);
		axisInfo.setText("Axis Info");
		axisInfo.setLayout(layout);
		GridData layoutDataGroup = new GridData(SWT.FILL, SWT.FILL, true, true);
		axisInfo.setLayoutData(layoutDataGroup);

		CLabel xAxisLabel = new CLabel(axisInfo, SWT.NONE);
		xAxisLabel.setText("X Axis");
		xAxisLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.RIGHT, true, true));

		xAxisText = new Text(axisInfo, SWT.READ_ONLY | SWT.BORDER);
		xAxisText.setText(String.valueOf(rackConfigurationInput.xAxis()));
		xAxisText.setLayoutData(container.getLayoutData());
		xAxisText.setEnabled(false);

		CLabel yAxisLabel = new CLabel(axisInfo, SWT.NONE);
		yAxisLabel.setText("Y Axis");
		yAxisLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.RIGHT, true, true));

		yAxisText = new Text(axisInfo, SWT.READ_ONLY | SWT.BORDER);
		yAxisText.setText(String.valueOf(rackConfigurationInput.yAxis()));
		yAxisText.setLayoutData(container.getLayoutData());
		yAxisText.setEnabled(false);
	}

	private void saveInput() {
		rackConfigurationInput = new RackConfigurationInput(Double.valueOf(xCalPosText.getText()), Double.valueOf(yCalPosText.getText()),
				Double.valueOf(xOffsetText.getText()), Double.valueOf(yOffsetText.getText()), Double.valueOf(xSpaceText.getText()),
				Double.valueOf(ySpaceText.getText()), xAxisText.getText(), yAxisText.getText());
	}
}
