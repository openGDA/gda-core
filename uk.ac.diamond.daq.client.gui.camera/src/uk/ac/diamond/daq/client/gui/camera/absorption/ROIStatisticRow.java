/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.client.gui.camera.absorption;

import static org.eclipse.january.dataset.DatasetUtils.convertToDataset;
import static uk.ac.gda.ui.tool.ClientMessages.EMPTY_MESSAGE;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientButton;
import static uk.ac.gda.ui.tool.WidgetUtilities.addWidgetDisposableListener;

import java.util.function.BiConsumer;
import java.util.function.LongSupplier;
import java.util.stream.IntStream;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import uk.ac.gda.ui.tool.ClientTextFormats;

/**
 * Calculates, from a region shape, and displays, as table row, an image
 * intensity.
 * <p>
 * To dynamically calculate the intensity, the class exposes
 * {@link #getProcessEvent()} to external instances handling a region area on
 * the plotting system.
 * </p>
 *
 * <p>
 * To dynamically calculate the intensity ration with another column, the
 * instance exposes {@link #getIntensity()} so that another instance can use it
 * calling {@link #calculateRatioWith(LongSupplier, String)}.
 * </p>
 *
 * <p>
 * Locking the row value, selecting the {@code lockIntensity} button causes the
 * row to stop updating the intensity value. However region and its dataset are
 * updated are always updated , so unlocking a row causes its immediate update.
 * </p>
 *
 * @author Maurizio Nagni
 *
 */
class ROIStatisticRow {
	private final TableItem tableItem;

	private Label intensityLabel;
	private Text ratioText;
	private Button lockIntensity;

	private IDataset dataset;
	private IROI roi;
	private long intensity;

	private LongSupplier otherIntensity;
	private String otherIntensityName;

	/**
	 * @param table the {@link Table} where attach the {@link TableItem}
	 * @param name  the name of the row
	 */
	public ROIStatisticRow(Table table, String name) {
		this.tableItem = new TableItem(table, SWT.NONE);
		addColumns(name);
	}

	public void calculateRatioWith(LongSupplier otherIntensity, String otherIntensityName) {
		this.otherIntensity = otherIntensity;
		this.otherIntensityName = otherIntensityName;
	}

	private BiConsumer<IROI, IDataset> processEvent = (flux, data) -> {
		this.roi = flux;
		this.dataset = data;
		if (!lockIntensity.getSelection()) {
			updateValue();
		}
	};

	/**
	 * @return the process event (as defined above)
	 */
	BiConsumer<IROI, IDataset> getProcessEvent() {
		return processEvent;
	}

	private LongSupplier intensitySupplier = () -> this.intensity;

	public LongSupplier getIntensity() {
		return intensitySupplier;
	}

	/**
	 * Calculate the ration of this region intensity
	 */
	public void processRatio() {
		if (otherIntensity != null && otherIntensity.getAsLong() != 0) {
			double ratio = this.intensity / (double) otherIntensity.getAsLong();
			String ratioString = String.format("Ratio with %s: %.3f", otherIntensityName, ratio);
			this.ratioText.setText(ratioString);
		}
	}

	private void addColumns(String name) {
		Table table = tableItem.getParent();

		TableEditor editor = new TableEditor(table);
		lockIntensity = createClientButton(table, SWT.CHECK, EMPTY_MESSAGE, EMPTY_MESSAGE);

		editor.grabHorizontal = true;
		editor.setEditor(lockIntensity, tableItem, 0);
		addWidgetDisposableListener(lockIntensity, SWT.Selection, event -> updateValue());

		editor = new TableEditor(table);
		Label nameLabel = new Label(table, SWT.NONE);
		nameLabel.setText(name);

		editor.grabHorizontal = true;
		editor.setEditor(nameLabel, tableItem, 1);

		editor = new TableEditor(table);
		intensityLabel = new Label(table, SWT.NONE);

		editor.grabHorizontal = true;
		editor.setEditor(intensityLabel, tableItem, 2);

		editor = new TableEditor(table);
		ratioText = new Text(table, SWT.NONE);
		ratioText.setEnabled(false);
		ratioText.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));

		editor.grabHorizontal = true;
		editor.setEditor(ratioText, tableItem, 3);
	}

	private void updateValue() {
		// Takes the bounding box containing whatever shape
		IRectangularROI boundingBox = roi.getBounds();

		int xRoiStart = boundingBox.getIntPoint()[0];
		int yRoiStart = boundingBox.getIntPoint()[1];
		int xRoiLength = boundingBox.getIntLengths()[0];
		int yRoiLength = boundingBox.getIntLengths()[1];

		int[] start = new int[] { yRoiStart, xRoiStart };
		int[] end = new int[] { yRoiStart + yRoiLength, xRoiStart + xRoiLength };
		int[] step = new int[] { 1, 1 };

		//resets the intensity
		this.intensity = 0;

		// yx datashape
		Dataset intDataset = convertToDataset(dataset.getSliceView(start, end, step));

		Mean mean = new Mean();
		IntStream.range(0, yRoiLength)
			.forEach(y -> IntStream.range(0, xRoiLength)
			.filter(x -> roi.containsPoint((double) x + xRoiStart, (double) y + yRoiStart))
			.forEach(x -> mean.increment(intDataset.getLong(y, x))));

		double meanResult = mean.getResult();
		this.intensity = (long) meanResult;
		this.intensityLabel.setText(ClientTextFormats.formatDecimal(intensity));
		processRatio();
	}

	public Text getRatioText() {
		return ratioText;
	}

}
