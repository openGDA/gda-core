/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.client;

import gda.jython.InterfaceProvider;
import gda.rcp.util.BrowserUtil;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class HelpHandlerImpl implements HelpHandler{


	String filePath;
	
	public String getFilePath() {
		return filePath;
	}

	/**
	 * 
	 * @param filePath - path to file containing topics help entries. Follows structure 
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	PropertiesConfiguration config;
	
	private void loadAddressMap() throws ConfigurationException{
		config = new PropertiesConfiguration(filePath);
	}

	
	@Override
	public boolean handle(String text, StringBuffer buf) throws IOException, PartInitException, ConfigurationException {
		
		String[] parts = text.trim().split(" ",2); //remove whitespace before or after
		String topic = parts.length>1 ? parts[1] : "default";
		if( topic.equals("reloadHelp")){
			config=null;
			return true;
		}
		if( config == null){
			loadAddressMap( );
		}
		String address = null;
		String val[];
		String topicToFind=topic;
		while(true){
			val = config.getStringArray(topicToFind);
			if( val.length>0){
				address=val[0];
				break;
			}
			int lastIndexOf = topicToFind.lastIndexOf(".");
			if( lastIndexOf<=0)
				break;
			topicToFind = topicToFind.substring(0, lastIndexOf);
		}
		if(address != null){
			if (address.startsWith("http")) {
				URL url = new URL(address);
				BrowserUtil.openBrowser(url, "GDA Help", "GDA", topic, false);
			} else if( address.startsWith("{cheatSheetTopic}")){
				String substring = address.substring((new String("{cheatSheetTopic}")).length());
				(new org.eclipse.ui.cheatsheets.OpenCheatSheetAction(substring)).run();
			} else if( address.startsWith("{cheatSheetFile}")){
				String substring = address.substring((new String("{cheatSheetFile}")).length());
				(new org.eclipse.ui.cheatsheets.OpenCheatSheetAction(text,text, new URL(substring))).run();
			} else if( address.startsWith("{helpResource}")){
				String substring = address.substring((new String("{helpResource}")).length());
				PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(substring);
			} else if( address.startsWith("{helpId}")){
				String substring = address.substring((new String("{helpId}")).length());
				PlatformUI.getWorkbench().getHelpSystem().displayHelp(substring);
			} else if( address.startsWith("{gda_command}")){
				String substring = address.substring((new String("{gda_command}")).length());
				String resp = InterfaceProvider.getCommandRunner().evaluateCommand(substring);
				if( resp != null && !resp.equals("None"))
					buf.append(resp);
			}
			else {
				buf.append("Available " + topic + ":");
				for( String s : val){
					buf.append("\n\t" + s );
				}
			}
			return true;
		}
		return false; 
	}

}
