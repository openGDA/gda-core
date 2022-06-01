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

package uk.ac.diamond.daq.mapping.ui.position.summary;

import static uk.ac.gda.ui.tool.ClientMessages.DELETE_POSITION_TP;
import static uk.ac.gda.ui.tool.ClientMessages.MOVE_TO_POSITION_TP;
import static uk.ac.gda.ui.tool.ClientMessages.SAVE_POSITION_TP;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientCompositeWithGridLayout;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientLabel;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import uk.ac.diamond.daq.mapping.ui.controller.StageController;
import uk.ac.diamond.daq.mapping.ui.stage.event.UpdateStagePositionEvent;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument;
import uk.ac.gda.client.properties.stage.position.Position;
import uk.ac.gda.client.viewer.ThreeStateDisplay;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.ClientMessagesUtility;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.images.ClientImages;

/**
 * Displays in tabular one {@link Position} stored in the {@link StageController}.
 *
 * <p>
 * The actual implementations includes four actions (in the second column): save, remove, moveTo, info
 * </p>
 *
 * @author Maurizio Nagni
 *
 * @see PositionSummaryComposite
 */
class PositionSummaryRow {
	private static final Logger logger = LoggerFactory.getLogger(PositionSummaryRow.class);

	private final TableItem tableItem;
	private final Position position;

	private ThreeStateDisplay state;

	/**
	 * @param table
	 *            the {@link Table} where attach the {@link TableItem}
	 * @param position
	 *            the position handled by this row
	 */
	public PositionSummaryRow(Table table, Position position) {
		this.tableItem = new TableItem(table, SWT.NONE);
		this.position = position;
		addColumns();

		SpringApplicationContextFacade.addDisposableApplicationListener(this, updateStagePositionEventEventListener);
		table.addDisposeListener(event -> dispose()	);
	}

	private void addColumns() {
		Table table = tableItem.getParent();
		//------------------------
		TableEditor columnZero = new TableEditor(table);
		Label nameLabel = createClientLabel(table, SWT.NONE, position.getName());
		nameLabel.setToolTipText(ClientMessagesUtility.getMessage(position.getTooltip()));
		GridDataFactory.fillDefaults().applyTo(nameLabel);
		columnZero.grabHorizontal = true;
		columnZero.setEditor(nameLabel, tableItem, 0);

		//------------------------
		TableEditor columnOne = new TableEditor(table);
		columnOne.grabHorizontal = true;
		columnOne.horizontalAlignment = SWT.RIGHT;

		Composite columnOneContainer = createClientCompositeWithGridLayout(table, SWT.NONE, 1);
		state = new ThreeStateDisplay(columnOneContainer, "Set", "Not set", "");
		state.setYellow();
		columnOne.setEditor(columnOneContainer, tableItem, 1);

		//------------------------
		TableEditor columnTwo = new TableEditor(table);
		columnTwo.grabHorizontal = true;
		columnTwo.horizontalAlignment = SWT.RIGHT;

		Composite columnTwoContainer = createClientCompositeWithGridLayout(table, SWT.NONE, 1);
		ToolBar bar = new ToolBar (columnTwoContainer, SWT.BORDER);

		ToolItem item = new ToolItem (bar, SWT.PUSH);
		item.setImage(ClientSWTElements.getImage(ClientImages.SAVE));
		item.setToolTipText(ClientMessagesUtility.getMessage(SAVE_POSITION_TP));
		addSaveListener(item);

		item = new ToolItem (bar, SWT.PUSH);
		item.setImage(ClientSWTElements.getImage(ClientImages.DELETE));
		item.setToolTipText(ClientMessagesUtility.getMessage(DELETE_POSITION_TP));
		addDeleteListener(item);

		item = new ToolItem (bar, SWT.PUSH);
		item.setImage(ClientSWTElements.getImage(ClientImages.MOVE_TO));
		item.setToolTipText(ClientMessagesUtility.getMessage(MOVE_TO_POSITION_TP));
		addMoveToListener(item);

		columnTwo.setEditor(columnTwoContainer, tableItem, 2);
	}

	private void addSaveListener(ToolItem item) {
		Listener outOfBeamListener = e -> getStageController()
				.ifPresent(c -> {
					c.savePosition(position);
					state.setGreen();
					logger.debug("Saved position: {}", position);
				});
		item.addListener(SWT.Selection, outOfBeamListener);
	}

	private void addDeleteListener(ToolItem button) {
		Listener listener = e -> getStageController()
				.ifPresent(c -> {
					c.removePosition(position);
					state.setYellow();
					logger.debug("Deleted position: {}", position);
				});
		button.addListener(SWT.Selection, listener);
	}

	private void addMoveToListener(ToolItem button) {
		Listener listener = e -> getStageController()
				.ifPresent(c -> {
					logger.debug("Moved position: {}", position);
					c.moveToPosition(position);
				});
		button.addListener(SWT.Selection, listener);
	}

	private void dispose() {
		SpringApplicationContextFacade.removeApplicationListener(updateStagePositionEventEventListener);
	}

	private Optional<StageController> getStageController() {
		return SpringApplicationContextFacade.getOptionalBean(StageController.class);
	}

	/**
	 * This listener updates the summary when the camera control, publishing the event, and the detector control associated in this acquisition match.
	 * At the moment is not possible to use anonymous lambda expression because it generates a class cast exception
	 */
	private ApplicationListener<UpdateStagePositionEvent> updateStagePositionEventEventListener = new ApplicationListener<UpdateStagePositionEvent>() {
		@Override
		public void onApplicationEvent(UpdateStagePositionEvent event) {
			if (!event.getPosition().equals(position))
				return;
			updateSummary();
		}

		private void updateSummary() {
			Display.getDefault().asyncExec(() -> {
				state.setToolTipText(positionReport());
				tableItem.getParent().getShell().layout(true, true);
			});
		}

		private String positionReport() {
			return getPositionDocuments().stream()
				.map(DevicePositionDocument::toString)
				.collect(Collectors.joining("\n"));
		}

		private Set<DevicePositionDocument> getPositionDocuments() {
			return getStageController()
				.map(s -> s.getPositionDocuments(position))
				.orElseGet(HashSet::new);
		}
	};
}