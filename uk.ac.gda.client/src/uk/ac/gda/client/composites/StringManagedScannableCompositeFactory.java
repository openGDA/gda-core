/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.client.composites;

import static uk.ac.gda.ui.tool.ClientMessagesUtility.getClientMessageByString;
import static uk.ac.gda.ui.tool.ClientMessagesUtility.getMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.EnumPositioner;
import gda.rcp.views.CompositeFactory;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.client.properties.stage.ManagedScannable;
import uk.ac.gda.client.properties.stage.ScannableProperties;
import uk.ac.gda.client.widgets.SmartCombo;

/**
 * A combo box representing a {@code String} based ManagedScannable, i.e. {@link EnumPositioner}
 *
 * @author Maurizio Nagni
 * @author Douglas Winter
 */
public class StringManagedScannableCompositeFactory implements CompositeFactory {

	private static final Logger logger = LoggerFactory.getLogger(StringManagedScannableCompositeFactory.class);

	private SmartCombo<String> combo;

	/**
	 * The combo will be populated with a list of <Key, Value> where the key represent the label while the value is the motor position (String)
	 * this map allows an easy way to get the key from the value (getManagedScannable().getPosition())
	 */
	private final Map<String, Integer> valueToLabel = new HashMap<>();

	private final ManagedScannable<Object> managedScanable;

	public StringManagedScannableCompositeFactory(ManagedScannable<Object> managedScanable) {
		super();
		this.managedScanable = managedScanable;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		combo = new SmartCombo<>(parent, style, null, Optional.of(selectionListener()));
		combo.populateCombo(createImmutablePairsCollection(getManagedScannable()));
		initialize();
		return combo;
	}

	/**
	 * Reads the managedScannable position to correctly set the combo at start
	 */
	private void initialize() {
		try {
			combo.select(valueToLabel.get(getManagedScannable().getPosition()));
		} catch (GDAClientException e) {
			logger.error("Cannot initialize {} ", this, e);
		}
	}

	/**
	 * Uses the {@link ScannableProperties#getEnumsMap()} to build the combo data
	 * @param managedScanable
	 * @return
	 */
	private List<ImmutablePair<String, String>> createImmutablePairsCollection(ManagedScannable<Object> managedScanable) {
		List<ImmutablePair<String, String>> positions = new ArrayList<>();
		Map<String, String> enumMap = managedScanable.getScannablePropertiesDocument().getEnumsMap();
		var index = 0;
		for (Entry<String, String> pair : enumMap.entrySet()) {
			// Uses the ScannableProperties#getEnumsMap() keys to create the combo label from the ClientMessages
			positions.add(new ImmutablePair<>(getMessage(getClientMessageByString(pair.getKey())), pair.getValue()));
			// populate the inverse mapping
			valueToLabel.put(pair.getValue(), index);
			index++;
		}
		return positions;
	}

	private Listener selectionListener() {
		return e -> {
			Optional<String> position = combo.getSelectedItem()
				.map(ImmutablePair::getValue);
			if (position.isPresent()) {
				try {
					getManagedScannable().moveTo(position.get());
				} catch (GDAClientException e1) {
					logger.error("Error moving {} ", getManagedScannable(), e1);
				}
			}
		};
	}

	private ManagedScannable<Object> getManagedScannable() {
		return managedScanable;
	}
}
