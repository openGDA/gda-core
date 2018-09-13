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
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.device.Scannable;
import gda.factory.Finder;
public class ImageStatsDisplayPart {

	private static final Logger logger = LoggerFactory.getLogger(ImageStatsDisplayPart.class);

	private Composite parent;

	private Composite child;

	@Inject
	public ImageStatsDisplayPart() {
		logger.trace("Constructor called");
	}

	@PostConstruct
    public void postConstruct(Composite parent) {

		logger.trace("postConstruct called");
		this.parent = parent;
		ScrolledComposite scrollComp = new ScrolledComposite(parent, SWT.V_SCROLL);
		scrollComp.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		this.child = new Composite(scrollComp, SWT.NONE);
		child.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));

		GridDataFactory.fillDefaults().grab(true, true).applyTo(child);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(child);


		// Stats group1
		Group imageStatsGroup1 = new Group(child, SWT.CENTER);
		imageStatsGroup1.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		GridLayoutFactory.swtDefaults().numColumns(2).spacing(10, 20).applyTo(imageStatsGroup1);

		ScannableDisplayComposite maxCount = new ScannableDisplayComposite(imageStatsGroup1, SWT.NONE);
		maxCount.setScannable((Scannable) Finder.getInstance().find("eavImageMax"));
		maxCount.setTextWidth(265);
		maxCount.setDisplayName("Max Count:");
		maxCount.setValueSize(32);
		maxCount.setValueColour(SWT.COLOR_DARK_BLUE);
		maxCount.setLabelSize(10);

		// Stats group2
		Group imageStatsGroup2 = new Group(child, SWT.CENTER);
		imageStatsGroup2.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		GridLayoutFactory.swtDefaults().numColumns(2).spacing(10, 20).applyTo(imageStatsGroup2);

		ScannableDisplayComposite totalCount = new ScannableDisplayComposite(imageStatsGroup2, SWT.NONE);
		totalCount.setScannable((Scannable) Finder.getInstance().find("eavImageTotal"));
		totalCount.setTextWidth(260);
		totalCount.setDisplayName("Total Count:");
		totalCount.setValueSize(32);
		totalCount.setValueColour(SWT.COLOR_DARK_BLUE);
		totalCount.setLabelSize(10);

		// Stats group2
		Group imageStatsGroup3 = new Group(child, SWT.CENTER);
		imageStatsGroup3.setBackground(SWTResourceManager.getColor(SWT.COLOR_TRANSPARENT));
		GridLayoutFactory.swtDefaults().numColumns(2).spacing(10, 20).applyTo(imageStatsGroup3);

		ScannableDisplayComposite meanCount = new ScannableDisplayComposite(imageStatsGroup3, SWT.NONE);
		meanCount.setScannable((Scannable) Finder.getInstance().find("eavImageMean"));
		meanCount.setTextWidth(260);
		meanCount.setDisplayName("Count/Pixel:");
		meanCount.setValueSize(32);
		meanCount.setValueColour(SWT.COLOR_DARK_BLUE);
		meanCount.setLabelSize(10);
		
		// Set the child as the scrolled content of the ScrolledComposite
		scrollComp.setContent(child);
		scrollComp.setExpandHorizontal(true);
		scrollComp.setExpandVertical(true);
	}

	 @Focus
	public void onFocus() {
		parent.setFocus();
	}
}
