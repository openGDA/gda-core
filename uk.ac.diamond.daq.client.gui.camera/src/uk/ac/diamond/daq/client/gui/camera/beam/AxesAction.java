package uk.ac.diamond.daq.client.gui.camera.beam;

import java.util.function.Supplier;

import org.apache.commons.math3.linear.RealVector;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;

/**
 * Allows the user to display alternative axes
 *
 * @author Maurizio Nagni
 *
 */
class AxesAction extends Action {

	/**
	 * The action label
	 */
	private final ClientMessages actionLabel;

	private final RealVector topLeft;
	private final RealVector bottomRight;

	private IAxis xAxis;
	private IAxis yAxis;

	private String xAxisLabel;
	private String yAxisLabel;

	private boolean visible = false;

	/**
	 * Using a supplier allows a dynamic link to the plotting system, i.e. the
	 * stream still not started
	 */
	private final Supplier<IPlottingSystem<Composite>> plottingSystem;

	/**
	 * The action to create new axes on the plotting system
	 *
	 * @param actionLabel    the text for the context menu element
	 * @param plottingSystem where find the context menu
	 * @param topLeft        the top left corner of the new plotting system area
	 * @param bottomRight    the bottom right corner of the new plotting system area
	 */
	AxesAction(ClientMessages actionLabel, Supplier<IPlottingSystem<Composite>> plottingSystem, RealVector topLeft,
			RealVector bottomRight, String xAxisLabel, String yAxisLabel) {
		this.actionLabel = actionLabel;
		this.plottingSystem = plottingSystem;
		this.topLeft = topLeft;
		this.bottomRight = bottomRight;
		this.xAxisLabel = xAxisLabel;
		this.yAxisLabel = yAxisLabel;
		setText(ClientMessagesUtility.getMessage(actionLabel));
		setEnabled(true);
	}

	@Override
	public void run() {
		if (!visible) {
			displayAxes();
			visible = true;
			setText("Removes " + ClientMessagesUtility.getMessage(actionLabel));
		} else {
			hideAxes();
			setText(ClientMessagesUtility.getMessage(actionLabel));
			visible = false;
		}
	}

	private void displayAxes() {
		getPlottingSystem().setSelectedXAxis(getXAxis());
		getPlottingSystem().setSelectedYAxis(getYAxis());
	}

	private void hideAxes() {
		getPlottingSystem().removeAxis(getXAxis());
		getPlottingSystem().removeAxis(getYAxis());
	}

	private IAxis createXAxis() {
		IAxis axis = getPlottingSystem().createAxis(xAxisLabel, false, SWT.BOTTOM);
		if (getPlottingSystem().getSelectedXAxis().isInverted()) {
			axis.setRange(bottomRight.getEntry(0), topLeft.getEntry(0));
		} else {
			axis.setRange(topLeft.getEntry(0), bottomRight.getEntry(0));
		}
		axis.setAxisAutoscaleTight(true);
		axis.setVisible(true);
		return axis;
	}

	private IAxis getXAxis() {
		if (xAxis == null) {
			xAxis = createXAxis();
		}
		return xAxis;
	}

	private IAxis getYAxis() {
		if (yAxis == null) {
			yAxis = createYAxis();
		}
		return yAxis;
	}

	private IAxis createYAxis() {
		IAxis axis = getPlottingSystem().createAxis(yAxisLabel, true, SWT.LEFT);
		if (getPlottingSystem().getSelectedYAxis().isInverted()) {
			axis.setRange(bottomRight.getEntry(1), topLeft.getEntry(1));
		} else {
			axis.setRange(topLeft.getEntry(1), bottomRight.getEntry(1));
		}
		axis.setVisible(true);
		return axis;
	}

	private IPlottingSystem<Composite> getPlottingSystem() {
		return plottingSystem.get();
	}
}
