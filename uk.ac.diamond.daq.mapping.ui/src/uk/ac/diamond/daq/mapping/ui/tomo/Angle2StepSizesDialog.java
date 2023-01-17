/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.tomo;

import static java.util.stream.Collectors.joining;
import static org.eclipse.jface.viewers.ColumnLabelProvider.createTextProvider;
import static uk.ac.diamond.daq.mapping.ui.tomo.TensorTomoScanSetupView.TomoAngle.ANGLE_1;
import static uk.ac.diamond.daq.mapping.ui.tomo.TensorTomoScanSetupView.TomoAngle.ANGLE_2;
import static uk.ac.diamond.daq.mapping.ui.tomo.TomoPathSection.formatDouble;
import static uk.ac.diamond.daq.mapping.ui.tomo.TomoPathSection.formatDoubles;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.IntStream;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.scanning.api.points.models.IAxialModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import uk.ac.diamond.daq.mapping.api.TensorTomoScanBean;
import uk.ac.diamond.daq.mapping.ui.tomo.TensorTomoPathInfo.StepSizes;
import uk.ac.diamond.daq.mapping.ui.tomo.TomoAngleEditorsBlock.TomoAnglePathModelChangeListener;
import uk.ac.diamond.daq.mapping.ui.tomo.TomoPathSection.AxialPathModelType;

/**
 * A dialog to show the calculated secondary angle step sizes and positions for tensor tomo scans.
 */
final class Angle2StepSizesDialog extends Dialog {

	private final TensorTomoScanBean scanBean;

	private Map<AxialPathModelType, IAxialModel> angle2PathModels;
	private IEclipseContext eclipseContext;

	private TensorTomoPathInfo pathInfo;

	private TomoAngleEditorsBlock tomoAngleEditorsBlock;

	private TomoAnglePathModelChangeListener angleModelChangeListener = null;

	private TableViewer angle2StepsAndPositionsTable;

	protected Angle2StepSizesDialog(Shell parentShell, IEclipseContext eclipseContext,
			TensorTomoScanBean scanBean, TensorTomoPathInfo pathInfo) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.APPLICATION_MODAL);
		this.eclipseContext = eclipseContext;
		this.scanBean = scanBean;
		this.pathInfo = pathInfo;

		angle2PathModels = new EnumMap<>(AxialPathModelType.class);
		final IAxialModel angle2Model = scanBean.getAngle2Model().getModel();
		angle2PathModels.put(AxialPathModelType.forModel(angle2Model), angle2Model);
	}

	public void addAngleModelChangeListener(TomoAnglePathModelChangeListener listener) {
		angleModelChangeListener = listener;
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Tensor Tomography Path");
	}

	@Override
	public boolean close() {
		tomoAngleEditorsBlock.dispose();

		return super.close();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);

		createTomoAngleEditorsArea(composite);
		createTable(composite);

		return composite;
	}

	private void createTomoAngleEditorsArea(Composite parent) {
		tomoAngleEditorsBlock = new TomoAngleEditorsBlock(scanBean, eclipseContext);
		tomoAngleEditorsBlock.addAngleModelChangeListener(this::anglePathsChanged);
		tomoAngleEditorsBlock.createControls(parent);
	}

	private void createTable(Composite parent) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText("The table below shows the calculated " + ANGLE_2 +
				" step sizes and positions for each " + ANGLE_1 + " position");
		GridDataFactory.swtDefaults().applyTo(label);

		angle2StepsAndPositionsTable = new TableViewer(parent);
		angle2StepsAndPositionsTable.setContentProvider((IStructuredContentProvider) input -> getTableContents());
		angle2StepsAndPositionsTable.getTable().setLinesVisible(true);
		angle2StepsAndPositionsTable.getTable().setHeaderVisible(true);
		GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, 250).applyTo(angle2StepsAndPositionsTable.getTable());

		final TableViewerColumn angle1PosColumn = new TableViewerColumn(angle2StepsAndPositionsTable, SWT.LEAD);
		angle1PosColumn.getColumn().setText(ANGLE_1 + " position");
		angle1PosColumn.getColumn().setWidth(120);
		angle1PosColumn.getColumn().setMoveable(true);

		angle1PosColumn.setLabelProvider(createTextProvider(this::getAngle1PositionText));

		final TableViewerColumn angle2StepSizeColumn = new TableViewerColumn(angle2StepsAndPositionsTable, SWT.LEAD);
		angle2StepSizeColumn.getColumn().setText(ANGLE_2 + " step size");
		angle2StepSizeColumn.getColumn().setWidth(120);
		angle2StepSizeColumn.getColumn().setMoveable(true);
		angle2StepSizeColumn.setLabelProvider(createTextProvider(this::getAngle2StepSizeText));

		final TableViewerColumn angle2PositionsColumn = new TableViewerColumn(angle2StepsAndPositionsTable, SWT.LEAD);
		angle2PositionsColumn.getColumn().setText(ANGLE_2 + " positions");
		angle2PositionsColumn.getColumn().setWidth(400);
		angle2PositionsColumn.getColumn().setMoveable(true);
		angle2PositionsColumn.setLabelProvider(createTextProvider(this::getAngle2PositionsText));

		angle2StepsAndPositionsTable.setInput(pathInfo);
	}

	private Object[] getTableContents() {
		// just return the angle1 position indices, the label providers use that to look up the column text
		return IntStream.range(0, pathInfo.getAngle1Positions().length).mapToObj(Integer::valueOf).toArray();
	}

	private String getAngle1PositionText(Object indexObj) {
		// parameter type is Object to match ColumnLabelProvider.createTextProvider method signature
		final int index = (Integer) indexObj;
		final double angle1Pos = pathInfo.getAngle1Positions()[index];
		return formatDouble(angle1Pos);
	}

	@SuppressWarnings("null")
	private String getAngle2StepSizeText(Object indexObj) {
		// parameter type is Object to match ColumnLabelProvider.createTextProvider method signature
		final int index = (Integer) indexObj;
		final StepSizes angle2StepSizesObj = pathInfo.getAngle2StepSizes();
		final StepSizes angle2StepSizeForAngle1Pos = angle2StepSizesObj.getRank() == 0 ?
				null : pathInfo.getAngle2StepSizes().getStepSizeForIndex(index);

		return switch (angle2StepSizesObj.getRank()) {
			case 0 -> "-"; // no step size
			case 1 -> formatDouble(angle2StepSizeForAngle1Pos.getStepSize());
			case 2 -> formatDoubles(angle2StepSizeForAngle1Pos.getOneDStepSizes());
			default -> throw new IllegalArgumentException("Unexpected step size rank: " + angle2StepSizeForAngle1Pos.getRank());
		};
	}

	private String getAngle2PositionsText(Object indexObj) {
		// parameter type is Object to match ColumnLabelProvider.createTextProvider method signature
		final int index = (Integer) indexObj;
		final double[] angle2Positions = pathInfo.getAngle2Positions()[index];
		return Arrays.stream(angle2Positions).mapToObj(TomoPathSection::formatDouble).collect(joining(", "));
	}

	private void anglePathsChanged(boolean modelTypeChange) {
		if (angleModelChangeListener != null) {
			angleModelChangeListener.angleModelChanged(modelTypeChange);
		}

		if (modelTypeChange) {
			relayoutDialog();
		}
	}

	private void relayoutDialog() {
		((Composite) getDialogArea()).layout(true, true);
	}

	public void updatePathInfo(TensorTomoPathInfo pathInfo) {
		this.pathInfo = pathInfo;
		angle2StepsAndPositionsTable.setInput(pathInfo);
		angle2StepsAndPositionsTable.refresh();
	}

}