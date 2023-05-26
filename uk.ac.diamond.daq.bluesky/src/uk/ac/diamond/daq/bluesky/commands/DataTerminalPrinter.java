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

package uk.ac.diamond.daq.bluesky.commands;

import static java.lang.Double.parseDouble;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Stream;

import gda.jython.InterfaceProvider;
import io.blueskyproject.TaggedDocument;
import io.blueskyproject.documents.DescriptorDocument;
import io.blueskyproject.documents.EventDocument;

/**
 * Listener for bluesky data events to print to terminal
 *
 * The fields to print are determined from the hint on the {@link DescriptorDocument}.
 *
 * Currently limited to numbers only.
 */
public class DataTerminalPrinter implements Consumer<TaggedDocument> {

	private Collection<String> dataFields;
	private Map<String, Format> formats;

	@Override
	public void accept(TaggedDocument doc) {
		if (doc.doc() instanceof DescriptorDocument desc) {
			handleDescriptorDocument(desc);
		}
		if (doc.doc() instanceof EventDocument event) {
			if (dataFields == null) {
				throw new IllegalStateException("Descriptor document not yet received");
			}
			handleEventDocument(event);
		}
	}

	private void handleDescriptorDocument(DescriptorDocument desc) {
		// Assume the ordering of hints map
		Stream<List<?>> fields = desc.getHints().entrySet().stream().map(Entry::getValue).map(Map.class::cast)
				.map(m -> m.get("fields")).map(List.class::cast);
		dataFields = fields.flatMap(List::stream).map(Object::toString).toList();
		var dataKeys = new LinkedHashMap<>(desc.getDataKeys());
		dataKeys.keySet().retainAll(dataFields);

		formats = dataKeys.entrySet().stream().filter(e -> dataFields.contains(e.getKey()))
				.collect(toMap(Entry::getKey, e -> {
					var precision = Integer.parseInt(e.getValue().getMetadata().get("precision").toString());
					var f = new DecimalFormat();
					f.setMaximumFractionDigits(precision);
					return f;
				}));
		var header = dataKeys.keySet().stream().collect(joining("\t"));
		InterfaceProvider.getTerminalPrinter().print(header);
	}

	private void handleEventDocument(EventDocument event) {
		var dataKeys = new LinkedHashMap<>(event.getData());
		dataKeys.keySet().retainAll(dataFields);
		String line = dataKeys.entrySet().stream().map(e -> {
			var f = formats.get(e.getKey());
			return f.format(parseDouble(e.getValue().toString()));
		}).collect(joining("\t"));
		InterfaceProvider.getTerminalPrinter().print(line);
	}

}
