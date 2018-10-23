/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.gda.beamline.synoptics.composites;

import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.swt.widgets.Composite;

import gda.rcp.views.CompositeFactory;
import uk.ac.gda.beamline.synoptics.api.PlottingFileProcessor;
import uk.ac.gda.beamline.synoptics.utils.NewFileListener;

public class DetectorFileSelectionFactory implements CompositeFactory {

	private NewFileListener fileProvider;
	private Map<String, String> fileFilters = emptyMap();
	private PlottingFileProcessor plotter;
	private boolean updatingAtStart = true;
	private boolean clearingAtStart = true;

	@Override
	public Composite createComposite(Composite parent, int style) {
		requireNonNull(fileProvider, "NewFileListener is required");
		Map<String, Predicate<String>> filters = fileFilters.entrySet().stream()
				.collect(Collectors.toMap(
						Entry::getKey,
						e -> Pattern.compile(e.getValue()).asPredicate()));

		Function<DetectorFileSelection, LatestFileController> controllerFactory = view ->
				new LatestFileController(
					view,
					fileProvider,
					filters,
					updatingAtStart,
					clearingAtStart,
					plotter);

		return new DetectorFileSelection(parent, style, controllerFactory);
	}

	public void setFileListener(NewFileListener nfl) {
		this.fileProvider = nfl;
	}

	public void setFileFilters(Map<String, String> filters) {
		requireNonNull(filters, "File filters must not be null");
		fileFilters = filters;
	}

	public void setPlotter(PlottingFileProcessor plotter) {
		this.plotter = plotter;
	}

	public void setUpdatingAtStart(boolean updatingAtStart) {
		this.updatingAtStart = updatingAtStart;
	}

	public void setClearingAtStart(boolean clearingAtStart) {
		this.clearingAtStart = clearingAtStart;
	}


}
