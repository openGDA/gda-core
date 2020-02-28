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

package org.eclipse.scanning.points.serialization;

import java.util.Collection;

import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class ScanRequestMixIn {

	@JsonProperty abstract Collection<IScanPointGeneratorModel> getModels();

	@JsonProperty abstract void setModels(Collection<IScanPointGeneratorModel> models);

	@JsonIgnore abstract void setModels(IScanPointGeneratorModel... models);

	@JsonProperty abstract Collection<String> getMonitorNames();

	@JsonProperty abstract void setMonitorNames(Collection<String> monitorNames);

	@JsonIgnore abstract void setMonitorNames(String... monitorNames);

	@JsonProperty abstract void setMetadataScannableNames(Collection<String> metadataScannableNames);

	@JsonIgnore abstract void setMetadataScannableNames(String... metadataScannableNames);


}
