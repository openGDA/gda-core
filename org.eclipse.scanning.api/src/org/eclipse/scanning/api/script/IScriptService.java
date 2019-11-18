/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.api.script;

/**
 *
 * A service to run scripts, python, javascript, r etc.
 *
 * This service may be backed by a custom engine which supports a subset
 * of the scripting languages. It might be implemented by an OSGi Jython
 * or by an eclipse EASE environment.
 *
 * @author Matthew Gerring
 *
 */
public interface IScriptService {

	public static final String VAR_NAME_SCAN_BEAN = "scanBean";
	public static final String VAR_NAME_SCAN_REQUEST = "scanRequest";
	public static final String VAR_NAME_SCAN_REQUEST_JSON = "scanRequestJson";
	public static final String VAR_NAME_SCAN_MODEL = "scanModel";
	public static final String VAR_NAME_SCAN_PATH = "scanPath";
	public static final String VAR_NAME_XANES_EDGE_PARAMS_JSON = "xanesEdgeParamsJson";
	public static final String VAR_NAME_TOMO_PARAMS_JSON = "tomoParamsJson";

	/**
	 * For DAQ server version 8 and 9 this will probably be {JYTHON, SPEC_PASTICHE}
	 * @return the available supported scripting languages that this service can execute.
	 */
	ScriptLanguage[] supported();

	/**
	 * Sets the variable with the given name to the given value, if the implementation supports this.
	 * @param name name
	 * @param value value
	 */
	void setNamedValue(String name, Object value);

	/**
	 * Execute a script on the server. This can be used for instance inside scanning to run
	 * a script before and after a scan.
	 *
	 * @param req the script request
	 * @throws UnsupportedLanguageException if the script language specified by
	 *   {@link ScriptRequest#getLanguage()} is not supported by this script service.
	 * @throws ScriptExecutionException if an error occurred running the script
	 */
	void execute(ScriptRequest req) throws UnsupportedLanguageException, ScriptExecutionException;

	/**
	 * Aborts any currently executing scripts.
	 */
	void abortScripts();

}
