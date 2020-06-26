package uk.ac.diamond.daq.client.gui.camera.absorption;

import java.util.function.BiConsumer;
import java.util.function.LongSupplier;
import java.util.stream.IntStream;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.roi.IRectangularROI;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.WidgetUtilities;

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
 * calling {@link #calculateRatioWith(LongSupplier, ClientMessages)}.
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
	private Label ratioLabel;
	private Button lockIntensity;

	private IDataset dataset;
	private IROI roi;
	private long intensity;

	private LongSupplier otherIntensity;
	private ClientMessages otherIntensityName;

	/**
	 * @param table the {@link Table} where attach the {@link TableItem}
	 * @param name  the name of the row
	 */
	public ROIStatisticRow(Table table, ClientMessages name) {
		this.tableItem = new TableItem(table, SWT.NONE);
		addColumns(name);
	}

	public void calculateRatioWith(LongSupplier otherIntensity, ClientMessages otherIntensityName) {
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
	 * Updates the row values using the actual region/image. 
	 * @return
	 */
	BiConsumer<IROI, IDataset> getProcessEvent() {
		return processEvent;
	}

	private LongSupplier intensitySupplier = () -> this.intensity;

	public LongSupplier getIntensity() {
		return intensitySupplier;
	}

	/**
	 * Calculate the ration of this region intensity com
	 * 
	 * @param valueA
	 */
	private void processRatio() {
		if (otherIntensity != null && otherIntensity.getAsLong() != 0) {
			String ratioString = String.format("Ratio with %s: %.3f",
					ClientMessagesUtility.getMessage(otherIntensityName),
					(double) this.intensity / otherIntensity.getAsLong());
			this.ratioLabel.setText(ratioString);
		}
	}

	private void addColumns(ClientMessages name) {
		Table table = tableItem.getParent();

		TableEditor editor = new TableEditor(table);
		lockIntensity = ClientSWTElements.createButton(table, SWT.CHECK, ClientMessages.EMPTY_MESSAGE,
				ClientMessages.EMPTY_MESSAGE);
		editor.grabHorizontal = true;
		editor.setEditor(lockIntensity, tableItem, 0);

		WidgetUtilities.addWidgetDisposableListener(lockIntensity, SWT.Selection, event -> updateValue());

		editor = new TableEditor(table);
		Label nameLabel = ClientSWTElements.createLabel(table, SWT.NONE, name);
		editor.grabHorizontal = true;
		editor.setEditor(nameLabel, tableItem, 1);

		editor = new TableEditor(table);
		intensityLabel = ClientSWTElements.createLabel(table, SWT.NONE, ClientMessages.EMPTY_MESSAGE);
		editor.grabHorizontal = true;
		editor.setEditor(intensityLabel, tableItem, 2);

		editor = new TableEditor(table);
		ratioLabel = ClientSWTElements.createLabel(table, SWT.NONE, ClientMessages.EMPTY_MESSAGE);
		editor.grabHorizontal = true;
		editor.setEditor(ratioLabel, tableItem, 3);
	}

	private void updateValue() {
		// Takes the bounding box containing whatever shape
		IRectangularROI boundingBox = roi.getBounds();
		int[] xy = boundingBox.getIntPoint();
		int[] length = boundingBox.getIntLengths();

		int[] start = new int[] { xy[0], xy[1] };
		int[] end = new int[] { xy[0] + length[0], xy[1] + length[1] };
		int[] step = new int[] { 1, 1 };

		// resets the intensity
		this.intensity = 0;
		// crop the image using the bounding box
		Dataset intDataset = DatasetUtils.convertToDataset(dataset.getSliceView(start, end, step));

		// filters the points contained in the roi, sum up their intensities
		IntStream.range(0, length[1])
				.forEach(y -> IntStream.range(0, length[0])
						.filter(x -> roi.containsPoint((double) x + xy[0], (double) y + xy[1]))
						.forEach(x -> sumIntensities(x, y, intDataset)));
		this.intensityLabel.setText(Long.toString(this.intensity));

		processRatio();
	}

	private void sumIntensities(int x, int y, Dataset intDataset) {
		this.intensity += intDataset.getLong(x, y);
	}
}
