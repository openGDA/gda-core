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

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.richbeans.widgets.menu.MenuAction;
import org.eclipse.swt.graphics.Color;

import uk.ac.diamond.daq.client.gui.camera.absorption.AbsorptionAction.ActionState;
import uk.ac.diamond.daq.client.gui.camera.liveview.CameraPlotter;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;

/**
 * Allows the user to select a geometric shape and draw a regions on a camera {@link IPlottingSystem}.
 *
 * @author Maurizio Nagni
 * @see AbsorptionAction
 */
class AbsorptionElement extends MenuAction implements IPropertyChangeListener {

	private final CameraPlotter cameraPlotter;

	/**
	 * @param name the menu action name
	 * @param regionColor the default region color
	 * @param cameraPlotter the underlying plotting system
	 * @param roiEventConsumer the elements consuming any change, position or shape, in the region
	 */
	AbsorptionElement(ClientMessages name, Color regionColor, CameraPlotter cameraPlotter, BiConsumer<IROI, IDataset> roiEventConsumer) {
		super(ClientMessagesUtility.getMessage(name));
		this.cameraPlotter = cameraPlotter;
		initialize(regionColor, roiEventConsumer);
	}

	private void initialize(Color regionColor, BiConsumer<IROI, IDataset> roiEventConsumer) {
		ActionRegionROIListenerBuilder roiListenerBuilder = new ActionRegionROIListenerBuilder(roiEventConsumer, cameraPlotter::getImageTrace);
		String regionName = String.format("%s Region", getText());

		addAction(new AbsorptionAction(ClientMessages.BOX, RegionType.BOX, regionColor, regionName,
				roiListenerBuilder, cameraPlotter::getPlottingSystem));
		addAction(new AbsorptionAction(ClientMessages.CIRCLE, RegionType.CIRCLE, regionColor, regionName,
				roiListenerBuilder, cameraPlotter::getPlottingSystem));
	}

	private void addAction(AbsorptionAction action) {
		super.add(action);
		// so each action, when executed, informs this instance
		action.addPropertyChangeListener(this);
		// so each action knows when its region is created or removed
		cameraPlotter.getPlottingSystem().addRegionListener(action);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		Arrays.stream(ActionState.values()).filter(state -> state.equals(event.getNewValue())).findFirst()
				.ifPresent(updateActionState);
	}

	private Consumer<ActionState> updateActionState = state -> {
		// disable the action menu when one of its actions has been selected
		if (ActionState.CREATED.equals(state)) {
			this.setEnabled(false);
		// enable the action menu when one of its action moves to the READY state
		} else if (ActionState.READY.equals(state)) {
			this.setEnabled(true);
		}
	};

	/**
	 * Allows to pre-assemble a parametrized listener to be instantiated when the region is draw.
	 *
	 * @see AbsorptionAction
	 */
	class ActionRegionROIListenerBuilder {
		private final BiConsumer<IROI, IDataset> eventConsumer;
		private final Supplier<IImageTrace> imageTrace;

		public ActionRegionROIListenerBuilder(BiConsumer<IROI, IDataset> eventConsumer, Supplier<IImageTrace> imageTrace) {
			super();
			this.eventConsumer = eventConsumer;
			this.imageTrace = imageTrace;
		}

		public IROIListener build(IRegion region) {
			return new ActionRegionROIListener(eventConsumer, region, imageTrace);
		}

		class ActionRegionROIListener implements IROIListener {
			private final BiConsumer<IROI, IDataset> eventConsumer;
			private final IRegion region;
			private final Supplier<IImageTrace> imageTrace;

			public ActionRegionROIListener(BiConsumer<IROI, IDataset> eventConsumer, IRegion region,
					Supplier<IImageTrace> imageTrace) {
				super();
				this.eventConsumer = eventConsumer;
				this.region = region;
				this.imageTrace = imageTrace;
			}

			@Override
			public void roiDragged(ROIEvent evt) {
				if (evt.getSource().equals(region) && imageTrace.get() != null) {
					eventConsumer.accept(evt.getROI(), imageTrace.get().getData());
				}
			}

			@Override
			public void roiChanged(ROIEvent evt) {
				if (evt.getSource().equals(region) && imageTrace.get() != null) {
					eventConsumer.accept(evt.getROI(), imageTrace.get().getData());
				}
			}

			@Override
			public void roiSelected(ROIEvent evt) {
				//Not used
			}
		}
	}
}
