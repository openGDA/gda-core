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

package uk.ac.gda.client.microfocus.ui.editors;

import java.text.DecimalFormat;

import org.eclipse.richbeans.api.event.ValueAdapter;
import org.eclipse.richbeans.api.event.ValueEvent;
import org.eclipse.richbeans.widgets.FieldComposite;
import org.eclipse.richbeans.widgets.scalebox.ScaleBox;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

public final class MicroFocusScanParametersComposite extends Composite {

	private ScaleBox collectionTime;
	private ScaleBox xStart;
	private ScaleBox yStart;
	private ScaleBox xEnd;
	private ScaleBox yEnd;
	private ScaleBox xStepSize;
	private ScaleBox yStepSize;
	private ScaleBox energy;
	private ScaleBox zValue;
	private ScaleBox rowTime;
	private Label rowDistanceLabel;
	private Label pointsPerRowLabel;
	private Label numberOfRowsLabel;
	private Label timePerPointLabel;
	private Label scanTypeLabel;
	private Composite infoComposite;

	@SuppressWarnings("unused")
	public MicroFocusScanParametersComposite(Composite parent, int style) {
		super(parent, style);
		Group tableComposite = new Group(this, SWT.BORDER);
		{
			GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gridData.widthHint = 428;
			tableComposite.setLayoutData(gridData);
		}
		tableComposite.setText("Map parameters");
		tableComposite.setLayout(new GridLayout(2, false));
		setLayout(new GridLayout(2, false));

		Label label = new Label(tableComposite, SWT.NONE);
		{
			GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
			gridData.widthHint = 115;
			label.setLayoutData(gridData);
		}

		label.setText("Collection Time");
		this.collectionTime = new ScaleBox(tableComposite, SWT.NONE);
		collectionTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		collectionTime.setMinimum(0.0);
		collectionTime.setMaximum(100.0);
		collectionTime.setUnit("s");
		new Label(collectionTime, SWT.NONE);
		collectionTime.addValueListener(new ValueAdapter("collectionTimeListener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				updateScanInfo();
			}
		});
		label = new Label(tableComposite, SWT.NONE);
		label.setText("xStart");
		this.xStart = new ScaleBox(tableComposite, SWT.NONE);
		xStart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		xStart.setMinimum(-12.5);
		xStart.setMaximum(12.50);
		xStart.setUnit("mm");
		xStart.setDecimalPlaces(4);
		new Label(xStart, SWT.NONE);
		xStart.addValueListener(new ValueAdapter("xStartListener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				updateScanInfo();
			}
		});
		label = new Label(tableComposite, SWT.NONE);
		label.setText("xEnd");
		this.xEnd = new ScaleBox(tableComposite, SWT.NONE);
		xEnd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		xEnd.setMinimum(-12.5);
		xEnd.setMaximum(12.50);
		xEnd.setUnit("mm");
		xEnd.setDecimalPlaces(4);
		new Label(xEnd, SWT.NONE);
		xEnd.addValueListener(new ValueAdapter("xEndListener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				updateScanInfo();
			}
		});

		label = new Label(tableComposite, SWT.NONE);
		label.setText("xStepSize");
		this.xStepSize = new ScaleBox(tableComposite, SWT.NONE);
		xStepSize.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		xStepSize.setDecimalPlaces(4);
		xStepSize.setMinimum(-12.5);
		xStepSize.setMaximum(12.50);
		xStepSize.setUnit("mm");
		new Label(xStepSize, SWT.NONE);
		xStepSize.addValueListener(new ValueAdapter("xStepSizeListener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				updateScanInfo();
			}
		});

		label = new Label(tableComposite, SWT.NONE);
		label.setText("yStart");
		this.yStart = new ScaleBox(tableComposite, SWT.NONE);
		yStart.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		yStart.setMinimum(-15.0);
		yStart.setMaximum(15.0);
		yStart.setUnit("mm");
		yStart.setDecimalPlaces(4);
		new Label(yStart, SWT.NONE);
		yStart.addValueListener(new ValueAdapter("yStartListener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				updateScanInfo();
			}
		});

		label = new Label(tableComposite, SWT.NONE);
		label.setText("yEnd");
		this.yEnd = new ScaleBox(tableComposite, SWT.NONE);
		yEnd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		yEnd.setMinimum(-15.0);
		yEnd.setMaximum(15.0);
		yEnd.setUnit("mm");
		yEnd.setDecimalPlaces(4);
		new Label(yEnd, SWT.NONE);
		yEnd.addValueListener(new ValueAdapter("yEndListener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				updateScanInfo();
			}
		});

		label = new Label(tableComposite, SWT.NONE);
		label.setText("yStepSize");
		this.yStepSize = new ScaleBox(tableComposite, SWT.NONE);
		yStepSize.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		yStepSize.setMinimum(-15.0);
		yStepSize.setMaximum(15.0);
		yStepSize.setUnit("mm");
		yStepSize.setDecimalPlaces(4);
		new Label(yStepSize, SWT.NONE);
		yStepSize.addValueListener(new ValueAdapter("yStepSizeListener") {
			@Override
			public void valueChangePerformed(ValueEvent e) {
				updateScanInfo();
			}
		});


		label = new Label(tableComposite, SWT.NONE);
		label.setText("Energy");
		energy = new ScaleBox(tableComposite, SWT.NONE);
		energy.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		energy.setMinimum(0.0);
		energy.setMaximum(35000.0);
		energy.setUnit("eV");
		energy.setDecimalPlaces(4);
		new Label(energy, SWT.NONE);

		label = new Label(tableComposite, SWT.NONE);
		label.setText("zValue");
		this.zValue = new ScaleBox(tableComposite, SWT.NONE);
		zValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		this.zValue.setMinimum(-25.0);
		this.zValue.setMaximum(75.0);
		this.zValue.setUnit("mm");
		this.zValue.setDecimalPlaces(4);
		new Label(zValue, SWT.NONE);

		infoComposite = new Composite(this, SWT.NONE);
		infoComposite.setLayout(new GridLayout());
		GridData gridData2 = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gridData2.widthHint=250;
		infoComposite.setLayoutData(gridData2);

		scanTypeLabel = new Label(infoComposite, SWT.NONE);
		scanTypeLabel.setText("                                                                    ");
		rowDistanceLabel = new Label(infoComposite, SWT.NONE);
		rowDistanceLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		rowDistanceLabel.setText("                                                                 ");
		pointsPerRowLabel = new Label(infoComposite, SWT.NONE);
		pointsPerRowLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		pointsPerRowLabel.setText("                                                                ");
		numberOfRowsLabel = new Label(infoComposite, SWT.NONE);
		GridData gd_numberOfRowsLabel = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_numberOfRowsLabel.widthHint = 165;
		numberOfRowsLabel.setLayoutData(gd_numberOfRowsLabel);
		numberOfRowsLabel.setText("                                                                 ");
		timePerPointLabel = new Label(infoComposite, SWT.NONE);
		timePerPointLabel.setText("                                                                 ");

		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gridData.widthHint = 420;

		collectionTime.setEnabled(true);
		updateScanInfo();
	}

	public void updateScanInfo() {
		double rowDistance = Math.abs(xEnd.getNumericValue() - xStart.getNumericValue());
		int pointsPerRow = (int) (Math.round((rowDistance / xStepSize.getNumericValue()) + 1));
		int numberOfRows = (int) Math.round((Math.abs(yEnd.getNumericValue() - yStart.getNumericValue())
				/ yStepSize.getNumericValue() + 1));
		DecimalFormat df = new DecimalFormat("#.#####");
		rowDistanceLabel.setText("Row Distance : " + df.format(rowDistance));
		pointsPerRowLabel.setText("No. of points/row : " + pointsPerRow);
		numberOfRowsLabel.setText("No. of rows : " + numberOfRows);
		scanTypeLabel.setText("Step Map");
		timePerPointLabel.setText("Time per point : " + df.format(collectionTime.getNumericValue()));
		infoComposite.layout();
	}

	public FieldComposite getCollectionTime() {
		return collectionTime;
	}

	public FieldComposite getXStart() {
		return xStart;
	}

	public FieldComposite getYStart() {
		return yStart;
	}

	public FieldComposite getXEnd() {
		return xEnd;
	}

	public FieldComposite getYEnd() {
		return yEnd;
	}

	public FieldComposite getXStepSize() {
		return xStepSize;
	}

	public FieldComposite getYStepSize() {
		return yStepSize;
	}

	public FieldComposite getZValue() {
		return zValue;
	}

	public FieldComposite getEnergy() {
		return energy;
	}

	public FieldComposite getRowTime() {
		return rowTime;
	}

}
