/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class wraps a StackLayout of sections which allow the user to submit the scan parameters to different forms of
 * scanning, for example a standard mapping scan, or a XANES scanning script with drift correction.
 */
public class SubmitScanSelector extends AbstractMappingSection {
	private static final Logger logger = LoggerFactory.getLogger(SubmitScanSelector.class);

	private Composite mainComposite;
	private Composite optionsComposite;
	private StackLayout optionsStack;

	private List<SubmitScanSection> sections;
	private List<Composite> sectionsComposites;
	private int currentSection;

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		mainComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(mainComposite);
		GridLayoutFactory.fillDefaults().applyTo(mainComposite);

		// Stack of the available submit sections
		optionsComposite = new Composite(mainComposite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(optionsComposite);
		GridLayoutFactory.fillDefaults().applyTo(optionsComposite);
		optionsStack = new StackLayout();
		optionsComposite.setLayout(optionsStack);

		// Create the various sections
		sectionsComposites = new ArrayList<>(sections.size());
		for (SubmitScanSection section : sections) {
			final String sectionName = section.getClass().getSimpleName();
			logger.debug("Creating mapping section {}", sectionName);
			try {
				section.initialize(getMappingView());
				section.createControls(optionsComposite);
				sectionsComposites.add(section.getComposite());
			} catch (Exception e) {
				logger.error("Error creating mapping section {}", sectionName, e);
			}
		}
		showSection(0);
	}

	public void showSection(int section) {
		sections.get(currentSection).onHide();
		optionsStack.topControl = sectionsComposites.get(section);
		sections.get(section).onShow();
		currentSection = section;

		optionsComposite.layout();
		mainComposite.layout();
	}

	public void setSections(List<SubmitScanSection> sections) {
		this.sections = sections;
	}

	public int getNumberOfSections() {
		return sections.size();
	}

	public int getCurrentSectionIndex() {
		return currentSection;
	}

	public List<String> getDescriptions() {
		return sections.stream().map(SubmitScanSection::getDescription).collect(toList());
	}

}
