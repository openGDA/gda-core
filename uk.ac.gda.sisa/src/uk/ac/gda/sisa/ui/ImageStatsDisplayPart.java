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

package uk.ac.gda.sisa.ui;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.device.Scannable;
import gda.factory.Finder;
public class ImageStatsDisplayPart {

	private static final Logger logger = LoggerFactory.getLogger(ImageStatsDisplayPart.class);

	private Composite parent;
	
	private AlignmentConfiguration alignmentConfig;

	@Inject
	public ImageStatsDisplayPart() {
		logger.trace("Constructor called");
		
		try {
			alignmentConfig = Finder.getInstance().findSingleton(AlignmentConfiguration.class);
		} catch (IllegalArgumentException exception) {
			String msg = "No AlignmentConfiguration was found! (Or more than 1)";
			logger.error(msg);
			throw new RuntimeException(msg);
		}
	}
	

	@PostConstruct
    public void postConstruct(Composite parent) {

		logger.trace("postConstruct called");
		this.parent = parent;
		ScrolledComposite scrollComp = new ScrolledComposite(parent, SWT.V_SCROLL);
		scrollComp.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		Composite child = new Composite(scrollComp, SWT.NONE);
		child.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		GridDataFactory.fillDefaults().grab(true, true).applyTo(child);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(child);
		
		alignmentConfig.getAlignmentStats().stream().forEachOrdered(stat -> addAlignmentStat(child, stat));
		
		// Set the child as the scrolled content of the ScrolledComposite
		scrollComp.setContent(child);
		scrollComp.setExpandHorizontal(true);
		scrollComp.setExpandVertical(true);
	}
	
	private void addAlignmentStat(Composite composite, AlignmentStat alignmentStat) {
		Group group = new Group(composite, SWT.CENTER);
		group.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		GridLayoutFactory.swtDefaults().numColumns(2).spacing(10, 20).applyTo(group);
		
		ScannableDisplayComposite meanCount = new ScannableDisplayComposite(group, SWT.NONE);
		meanCount.setScannable(alignmentStat.getScannable());
		meanCount.setTextWidth(260);
		meanCount.setDisplayName(alignmentStat.getLabel());
		meanCount.setValueSize(24);
		meanCount.setValueColour(SWT.COLOR_DARK_BLUE);
		meanCount.setLabelSize(24);
	}

	 @Focus
	public void onFocus() {
		parent.setFocus();
	}
}
