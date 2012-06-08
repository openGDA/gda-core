/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.client.actions;

import gda.rcp.util.BrowserUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.browser.IWebBrowser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DataDispenserAbstractHandler extends AbstractHandler implements IHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(DataDispenserAbstractHandler.class);
	private static final String DATA_DISPENSER_BROWSER_ID = "data.dispenser.browser";
	
	public static final String DATA_DISPENSER_CONTROLLER_URL_PREF= "datadispenser.controller.url";
	public static final String DATA_DISPENSER_MEDIA_URL_PREF = "datadispenser.media.url";
	public static final String DATA_DISPENSER_DEFAULT_PREF = "data.dispenser.default";
	
	/**
	 * Open the given url in the data dispenser browser
	 * 
	 * @param url
	 *            destination link
	 */
	protected void openInBrowser(String url) {
		String title = "Data Dispenser ";
		String tooltip = title + ": " + url;

		try {
			IWebBrowser browser = BrowserUtil.openBrowserAsView(url,DATA_DISPENSER_BROWSER_ID
					+ Calendar.getInstance().getTimeInMillis(), title, tooltip);
			browser.openURL(new URL(url));
		} catch (PartInitException e1) {
			logger.error("Error opening browser" + e1);
		} catch (MalformedURLException e1) {
			logger.error("Error with URL provided" + e1);
		}
	}

}
