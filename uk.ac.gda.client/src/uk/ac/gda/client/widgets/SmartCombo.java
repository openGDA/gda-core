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

package uk.ac.gda.client.widgets;

import static uk.ac.gda.ui.tool.WidgetUtilities.addWidgetDisposableListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientSWTElements;

/**
 * A composite which displays either a {@link Label} a {@link Combo} depending if is populated with one or more items.
 *
 * The available items are updated using {@link #populateCombo(List)}.
 * <p>
 * The selected item is available from {@link #getSelectedItem()}.
 * </p>
 *
 * @param <T>
 *            the class associated with the available items
 * @author Maurizio Nagni
 */
public class SmartCombo<T> extends Composite {

	/**
	 * The {@code combo tooltip}
	 */
	private final Optional<ClientMessages> tooltip;
	/**
	 * A listener to the {@code combo} {@link SWT#Selection} events
	 */
	private final Optional<Listener> listener;
	/**
	 * The container {@code combo}
	 */
	private Optional<Combo> combo = Optional.empty();
	/**
	 * The actual selected item
	 */
	private Optional<ImmutablePair<String, T>> selectedItem = Optional.empty();

	private static final Logger logger = LoggerFactory.getLogger(SmartCombo.class);

	/**
	 * @param parent
	 *            the widget where append this instance
	 * @param style
	 *            the style applied to the Combo/Label {@link Composite} container
	 * @param tooltip
	 *            the {@code combo} tooltip
	 * @param listener
	 *            the {@code combo}, if exists, {@link SWT#Selection} event listener
	 */
	public SmartCombo(Composite parent, int style, Optional<ClientMessages> tooltip, Optional<Listener> listener) {
		super(parent, style);
		GridLayoutFactory.fillDefaults().applyTo(this);
		ClientSWTElements.createClientGridDataFactory().align(SWT.LEFT, SWT.TOP).grab(true, true).applyTo(this);
		this.listener = listener;
		this.tooltip = tooltip;
	}

	/**
	 * Populates the instance with a new list of items. Each {@link ImmutablePair} is mapped to the {@link Combo} item
	 * using {@code ImmutablePair#getKey()} as label and {@code ImmutablePair#getValue()} as the data linked with the
	 * selected item.
	 *
	 * @param items
	 *            the {@code Combo} items. If contains a single element the {@code Combo} is replaced by a
	 *            {@link Label}.
	 */
	public void populateCombo(List<ImmutablePair<String, T>> items) {
		cleanContainer();
		Combo tmpCombo = ClientSWTElements.createCombo(this, SWT.READ_ONLY, new String[0],
				tooltip.orElse(ClientMessages.EMPTY_MESSAGE));

		Optional.ofNullable(items).ifPresent(i -> i.forEach(e -> {
			tmpCombo.add(e.getKey());
			tmpCombo.setData(e.getKey(), e.getValue());
		}));

		if (tmpCombo.getItemCount() > 1) {
			tmpCombo.select(0);
			listener.ifPresent(l -> addWidgetDisposableListener(tmpCombo, SWT.Selection, l));
			combo = Optional.of(tmpCombo);
		} else if (tmpCombo.getItemCount() == 1) {
			String uniqueElement = tmpCombo.getItem(0);
			selectedItem = Optional.ofNullable(getComboImmutablePair(tmpCombo, uniqueElement));
			cleanContainer();
			Label label = ClientSWTElements.createClientLabel(Composite.class.cast(this), SWT.NONE, uniqueElement);
			ClientSWTElements.createClientGridDataFactory().applyTo(label);
		} else if (tmpCombo.getItemCount() == 0) {
			selectedItem = Optional.empty();
			if (logger.isInfoEnabled()) {
				logger.info(String.format("Combo empty: %s", this.toString()));
			}
		}
		layout(true, true);
	}

	/**
	 * The value of the actual selection, if any
	 *
	 * @return the data associated with the actual selection, eventually {@link Optional#empty()}
	 */
	public final Optional<ImmutablePair<String, T>> getSelectedItem() {
		combo.ifPresent(this::getSelectedItem);
		return selectedItem;
	}

	/**
	 * The items available to be selected
	 *
	 * @return the combo items
	 */
	public final List<ImmutablePair<String, T>> getItems() {
		List<ImmutablePair<String, T>> items = new ArrayList<>();
		if (combo.isPresent()) {
			Arrays.stream(combo.get().getItems()).forEach(i -> items.add(getComboImmutablePair(combo.get(), i)));
		} else {
			selectedItem.ifPresent(items::add);
		}
		return Collections.unmodifiableList(items);
	}

	/**
	 * Selects the item at the given zero-relative index in the receiver's list. If the item at the index was already
	 * selected, it remains selected. Indices that are out of range are ignored.
	 *
	 * @param index
	 *            the index of the item to select
	 */
	public final void select(int index) {
		combo.ifPresent(c -> c.select(index));
	}

	private void getSelectedItem(Combo c) {
		String key = c.getItem(c.getSelectionIndex());
		selectedItem = Optional.of(getComboImmutablePair(c, key));
	}

	private void cleanContainer() {
		Arrays.stream(getChildren()).forEach(Widget::dispose);
		combo = Optional.empty();
	}

	@SuppressWarnings("unchecked")
	private ImmutablePair<String, T> getComboImmutablePair(Combo combo, String key) {
		return new ImmutablePair<>(key, (T) combo.getData(key));
	}

}
