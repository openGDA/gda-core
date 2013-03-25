/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package org.diamond.util.io;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.gda.util.io.MacroSupplier;
import uk.ac.gda.util.io.StreamMacroSubstitutor;

/**
 *
 */
public class StreamMacroSubstitutorTest {

	/**
	 * Simple test to check substitution
	 * @throws IOException
	 */
	@Test
	public void testProcess() throws IOException {
		
		String f = "<?xml version='1.0' encoding='UTF-8'?>"
			+ "<configuration>"
			+ "<appender name='DebugFILE' class='ch.qos.logback.core.FileAppender'>"
			+ "<File>"
			+ "${my.file.macro}/log.txt"
			+ "</File>"
			+ "<layout class='ch.qos.logback.classic.PatternLayout'><pattern>%-5level %logger %ex - %m%n</pattern></layout></appender>"
			+ "${my.file.macro1}${my.file.macro}<logger name='gda'><level value='INFO'/></logger>"
			+ "<root><level value='ALL'/><appender-ref ref='DebugFILE'/></root></configuration>";
		final HashMap<String, String> map = new HashMap<String, String>();
		map.put("my.file.macro", "my_file_macro");
		map.put("my.file.macro1", "my_file_macro1");
		CharArrayWriter out = new CharArrayWriter();
		StreamMacroSubstitutor.process( new StringReader(f),out,  
				new MacroSupplier(){
					
					@Override
					public String get(String key) {
						return map.get(key);
							
					}});
		String s = out.toString();
		Assert.assertEquals(f.replace("${my.file.macro}", "my_file_macro").replace("${my.file.macro1}", "my_file_macro1"),s);
		
	}

}
