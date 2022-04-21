/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.ScannableMotion;
import gda.factory.Finder;
import uk.ac.diamond.daq.mapping.api.IScanModelWrapper;
import uk.ac.diamond.daq.mapping.impl.MappingScanDefinition;

/**
 * A section for configuring the outer scannables of a scan, e.g. temperature.
 * <p>
 * When only a subset of the configured scannables could be used as the outer axis in a scan,
 * they should be given as the {@code permittedOuterScannables} in the {@link MappingScanDefinition}.
 * <br>
 * If this list is not configured, then the user will be able to display and
 * configure any available scannable that implements {@link ScannableMotion} or a derived interface.
 */
public class OuterScannablesSection extends AbstractMappingSection {

	private static final Logger logger = LoggerFactory.getLogger(OuterScannablesSection.class);

	private OuterScannablesBlock outerScannablesBlock;

	@Override
	public void initialize(MappingExperimentView mappingView) {
		super.initialize(mappingView);

		outerScannablesBlock = new OuterScannablesBlock();
		outerScannablesBlock.setOuterScannables(getOuterScannables());
		outerScannablesBlock.setAvailableScannableNames(calculateAvailableScannableNames());
		outerScannablesBlock.setScannablesChangeListener(this::scannablesChanged);
		outerScannablesBlock.setPointsChangedListener(() -> getService(RegionAndPathController.class).updatePoints());
	}

	private List<IScanModelWrapper<IScanPointGeneratorModel>> getOuterScannables() {
		return getBean().getScanDefinition().getOuterScannables();
	}

	private Set<String> calculateAvailableScannableNames() {
		final List<String> permittedOuterScannableNames = getBean().getScanDefinition().getPermittedOuterScannables();
		if (permittedOuterScannableNames.isEmpty()) {
			try { // if permitted outer scannables is not set, add all Findables
				return Finder.getFindablesOfType(ScannableMotion.class).keySet();
			} catch (Exception e) {
				logger.error("Exception getting scannable names", e); // TODO should just propagate exception?
				return Collections.emptySet();
			}
		} else {
			// Ensure that the default scannable(s) are in the list, even if not explicitly set as "permitted"
			return concat(permittedOuterScannableNames.stream(), getBean().getScanDefinition().getDefaultOuterScannables().stream())
					.collect(toSet());
		}
	}

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);

		outerScannablesBlock.createControls(parent);
	}

	@SuppressWarnings("unused")
	private void handleScanPathUpdate(Object source, Object arg) {
		updatePoints();
	}

	private void scannablesChanged(List<IScanModelWrapper<IScanPointGeneratorModel>> outerScannables) {
		// update the bean from the UI
		getBean().getScanDefinition().setOuterScannables(outerScannables);
		updateMappingView();
	}

	private void updatePoints() {
		getService(RegionAndPathController.class).updatePoints();
	}

	@Override
	public void updateControls() {
		outerScannablesBlock.updateControls();
		updateMappingView();
	}

	protected void updateMappingView() {
		relayoutView();
		updateStatusLabel();
	}

	@Override
	public void dispose() {
		outerScannablesBlock.dispose();
		super.dispose();
	}

	/**
	 * Make a scannable visible in this section (if it is not already shown) and set the flag to say whether it should
	 * be included in a scan.
	 *
	 * @param scannableName name of the scannable
	 * @param includeInScan <code>true</code> if the scannable is to be included in scans, <code>false</code> otherwise
	 */
	public void showScannable(String scannableName, boolean includeInScan) {
		outerScannablesBlock.addScannable(scannableName, includeInScan);
	}

}
