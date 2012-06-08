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

package gda.rcp.util;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

/**
 * Utility class to provide some common, convenient methods for invoking Eclipse browser functionality
 */
public class BrowserUtil {

	/**
	 * Open a web browser given the url string in a workbench view. An optional browser id can be
	 * provided to reuse the same browser repeatedly. 
	 * 
	 * @param urlText url to open in browser, or null for empty browser
	 * @param browserId id of an existing browser to reuse, or null for a new one
	 * @param name name used for the presentation of the internal browser
	 * @param tooltip tooltip used for the presentation of the internal browser
	 * 
	 * @return the web browser instance
	 * @throws PartInitException if the browser fails to open for any reason
	 * @throws MalformedURLException if the string speficies an unknown protocol
	 */
	public static IWebBrowser openBrowserAsView(String urlText, String browserId, String name, String tooltip) throws PartInitException, MalformedURLException {
		URL url = null;
		if (urlText != null) {
			url = new URL(urlText);
		}

		return openBrowser(url, browserId, name, tooltip, false);
	}
	
	/**
	 * Open a web browser given the url string as an editor. An optional browser id can be
	 * provided to to reuse the same browser repeatedly. 
	 * 
	 * @param urlText url to open in browser, or null for empty browser
	 * @param browserId id of an existing browser to reuse, or null for a new one
	 * @param name name used for the presentation of the internal browser
	 * @param tooltip tooltip used for the presentation of the internal browser
	 *  
	 * @return the web browser instance
	 * @throws PartInitException if the browser fails to open for any reason
	 * @throws MalformedURLException if the string speficies an unknown protocol
	 */
	public static IWebBrowser openBrowserAsEditor(String urlText, String browserId, String name, String tooltip) throws PartInitException, MalformedURLException {
		URL url = null;
		if (urlText != null) {
			url = new URL(urlText);
		}

		return openBrowser(url, browserId, name, tooltip, true);
	}	

	/**
	 * Opens a web browser using the given parameters
	 * <p>
	 * Browser is opened according to the browser support provided in the 
	 * Platform (as defined by browserSupport extension points)
	 * <p>
	 * @param url url to open in browser, or null for empty browser	 * 
	 * @param browserId id of an existing browser to reuse, or null for a new one
	 * @param name name used for the presentation of the internal browser
	 * @param tooltip tooltip used for the presentation of the internal browser
	 * @param openAsEditor if internal browser is used, true opens as editor, false as view
	 * @return the web browser instance
	 * @throws PartInitException if the browser fails to open for any reason
	 */
	public static IWebBrowser openBrowser(URL url, String browserId, String name, String tooltip, boolean openAsEditor) throws PartInitException {
		IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
		
		int styleBit = IWorkbenchBrowserSupport.LOCATION_BAR | IWorkbenchBrowserSupport.NAVIGATION_BAR ;
		if (!openAsEditor)styleBit |=  IWorkbenchBrowserSupport.AS_VIEW;
			 
		IWebBrowser browser = browserSupport.createBrowser(styleBit, browserId, name, tooltip);
		browser.openURL(url);
		return browser;
	}

}
