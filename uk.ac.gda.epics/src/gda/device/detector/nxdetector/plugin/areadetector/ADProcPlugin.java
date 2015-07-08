/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package gda.device.detector.nxdetector.plugin.areadetector;

import gda.device.detector.areadetector.v17.NDProcess;
import gda.device.detector.nxdetector.NXPluginBase;
import gda.scan.ScanInformation;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;

/**
 * An NXPlugin to configure the proc plugin for Area Detectors.
 * Configured values will be pushed at the start of a scan (prepareForCollection)
 */
public class ADProcPlugin implements NXPluginBase, InitializingBean {

	public enum DataType {
		INT8(NDProcess.DatatypeOut_Int8),
		UINT8(NDProcess.DatatypeOut_UInt8),
		INT16(NDProcess.DatatypeOut_Int16),
		UINT16(NDProcess.DatatypeOut_UInt16),
		INT32(NDProcess.DatatypeOut_Int32),
		UINT32(NDProcess.DatatypeOut_UInt32),
		FLOAT32(NDProcess.DatatypeOut_Float32),
		FLOAT64(NDProcess.DatatypeOut_Float64),
		AUTOMATIC(NDProcess.DatatypeOut_Automatic);

		private int value;

		public int getValue() {
			return value;
		}

		DataType(int value) {
			this.value = value;
		}
	}

	public enum FilterType {
		RECURSIVE_AVERAGE(NDProcess.FilterTypeV1_8_RecursiveAve),
		AVERAGE(NDProcess.FilterTypeV1_8_Average),
		SUM(NDProcess.FilterTypeV1_8_Sum),
		DIFFERENCE(NDProcess.FilterTypeV1_8_Diff),
		RECURSIVE_AVERAGE_DIFF(NDProcess.FilterTypeV1_8_RecursiveAveDiff),
		COPY_TO_FILTER(NDProcess.FilterTypeV1_8_CopyToFilter);

		private int value;

		public int getValue() {
			return value;
		}

		FilterType(int value) {
			this.value = value;
		}
	}

	public enum FilterCallback {
		EVERY_ARRAY(NDProcess.FilterCallback_EveryArray),
		ARRAY_N_ONLY(NDProcess.FilterCallback_ArrayNOnly);

		private int value;

		public int getValue() {
			return value;
		}

		FilterCallback(int value) {
			this.value = value;
		}
	}

	//We don't want to be pushing values that haven't been explicitly configured
	private NDProcess ndProcess;
	private String name;
	private String inputPort;
	private Boolean blocking;
	private DataType dataType;
	private Boolean enableOffsetScale;
	private Double offset;
	private Double scale;
	private Boolean enableLowClip;
	private Integer lowClip;
	private Boolean enableHighClip;
	private Integer highClip;
	private Boolean enableBackgroundSubtraction;
	private Boolean enableFlatField;
	private Boolean enableRecursiveFilter;
	private FilterType filterType;
	private FilterCallback filterCallback;
	private Integer numFilter;
	private Boolean autoResetFilter;
	private Double outputScale;
	private Double filterScale;
	private Double outputOffset;
	private Double filterOffset;
	private Double outputCoefficient1;
	private Double outputCoefficient2;
	private Double outputCoefficient3;
	private Double outputCoefficient4;
	private Double filterCoefficient1;
	private Double filterCoefficient2;
	private Double filterCoefficient3;
	private Double filterCoefficient4;
	private Double filterResetOffset;
	private Double filterResetCoefficient1;
	private Double filterResetCoefficient2;

	ADProcPlugin(String name) {
		this.name = name;
	}

	public void setInputPort(String inputPort) {
		this.inputPort = inputPort;
	}

	public String getInputPort() {
		return inputPort;
	}

	@Override
	public String getName() {
		return name;
	}

	public Boolean isBlocking() {
		return blocking;
	}

	public void setBlocking(boolean blocking) {
		this.blocking = blocking;
	}

	public DataType getDataType() {
		return dataType;
	}

	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}

	public Boolean getEnableOffsetScale() {
		return enableOffsetScale;
	}

	public void setEnableOffsetScale(boolean enableOffsetScale) {
		this.enableOffsetScale = enableOffsetScale;
	}

	public Double getOffset() {
		return offset;
	}

	public void setOffset(double offset) {
		this.offset = offset;
	}

	public Double getScale() {
		return scale;
	}

	public void setScale(double scale) {
		this.scale = scale;
	}

	public Boolean getEnableLowClip() {
		return enableLowClip;
	}

	public void setEnableLowClip(boolean enableLowClip) {
		this.enableLowClip = enableLowClip;
	}

	public Integer getLowClip() {
		return lowClip;
	}

	public void setLowClip(int lowClip) {
		this.lowClip = lowClip;
	}

	public Boolean getEnableHighClip() {
		return enableHighClip;
	}

	public void setEnableHighClip(boolean enableHighClip) {
		this.enableHighClip = enableHighClip;
	}

	public Integer getHighClip() {
		return highClip;
	}

	public void setHighClip(int highClip) {
		this.highClip = highClip;
	}

	public Boolean getEnableBackgroundSubtraction() {
		return enableBackgroundSubtraction;
	}

	public void setEnableBackgroundSubtraction(Boolean enableBackgroundSubtraction) {
		this.enableBackgroundSubtraction = enableBackgroundSubtraction;
	}

	public Boolean getEnableFlatField() {
		return enableFlatField;
	}

	public void setEnableFlatField(Boolean enableFlatField) {
		this.enableFlatField = enableFlatField;
	}

	public Boolean getEnableRecursiveFilter() {
		return enableRecursiveFilter;
	}

	public void setEnableRecursiveFilter(Boolean enableRecursiveFilter) {
		this.enableRecursiveFilter = enableRecursiveFilter;
	}

	public FilterType getFilterType() {
		return filterType;
	}

	public void setFilterType(FilterType filterType) {
		this.filterType = filterType;
	}

	public FilterCallback getFilterCallback() {
		return filterCallback;
	}

	public void setFilterCallback(FilterCallback filterCallback) {
		this.filterCallback = filterCallback;
	}

	public Integer getNumFilter() {
		return numFilter;
	}

	public void setNumFilter(Integer numFilter) {
		this.numFilter = numFilter;
	}

	public Boolean getAutoResetFilter() {
		return autoResetFilter;
	}

	public void setAutoResetFilter(Boolean autoResetFilter) {
		this.autoResetFilter = autoResetFilter;
	}

	public Double getOutputScale() {
		return outputScale;
	}

	public void setOutputScale(Double outputScale) {
		this.outputScale = outputScale;
	}

	public Double getFilterScale() {
		return filterScale;
	}

	public void setFilterScale(Double filterScale) {
		this.filterScale = filterScale;
	}

	public Double getOutputOffset() {
		return outputOffset;
	}

	public void setOutputOffset(Double outputOffset) {
		this.outputOffset = outputOffset;
	}

	public Double getFilterOffset() {
		return filterOffset;
	}

	public void setFilterOffset(Double filterOffset) {
		this.filterOffset = filterOffset;
	}

	public Double getOutputCoefficient1() {
		return outputCoefficient1;
	}

	public void setOutputCoefficient1(Double outputCoefficient1) {
		this.outputCoefficient1 = outputCoefficient1;
	}

	public Double getOutputCoefficient2() {
		return outputCoefficient2;
	}

	public void setOutputCoefficient2(Double outputCoefficient2) {
		this.outputCoefficient2 = outputCoefficient2;
	}

	public Double getOutputCoefficient3() {
		return outputCoefficient3;
	}

	public void setOutputCoefficient3(Double outputCoefficient3) {
		this.outputCoefficient3 = outputCoefficient3;
	}

	public Double getOutputCoefficient4() {
		return outputCoefficient4;
	}

	public void setOutputCoefficient4(Double outputCoefficient4) {
		this.outputCoefficient4 = outputCoefficient4;
	}

	public Double getFilterCoefficient1() {
		return filterCoefficient1;
	}

	public void setFilterCoefficient1(Double filterCoefficient1) {
		this.filterCoefficient1 = filterCoefficient1;
	}

	public Double getFilterCoefficient2() {
		return filterCoefficient2;
	}

	public void setFilterCoefficient2(Double filterCoefficient2) {
		this.filterCoefficient2 = filterCoefficient2;
	}

	public Double getFilterCoefficient3() {
		return filterCoefficient3;
	}

	public void setFilterCoefficient3(Double filterCoefficient3) {
		this.filterCoefficient3 = filterCoefficient3;
	}

	public Double getFilterCoefficient4() {
		return filterCoefficient4;
	}

	public void setFilterCoefficient4(Double filterCoefficient4) {
		this.filterCoefficient4 = filterCoefficient4;
	}

	public Double getFilterResetOffset() {
		return filterResetOffset;
	}

	public void setFilterResetOffset(Double filterResetOffset) {
		this.filterResetOffset = filterResetOffset;
	}

	public Double getFilterResetCoefficient1() {
		return filterResetCoefficient1;
	}

	public void setFilterResetCoefficient1(Double filterResetCoefficient1) {
		this.filterResetCoefficient1 = filterResetCoefficient1;
	}

	public Double getFilterResetCoefficient2() {
		return filterResetCoefficient2;
	}

	public void setFilterResetCoefficient2(Double filterResetCoefficient2) {
		this.filterResetCoefficient2 = filterResetCoefficient2;
	}

	public NDProcess getNdProcess() {
		return ndProcess;
	}

	public void setNdProcess(NDProcess ndProcess) {
		this.ndProcess = ndProcess;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (getNdProcess() == null) {
			throw new RuntimeException("NDProcess must be set");
		}
	}

	@Override
	public boolean willRequireCallbacks() {
		return true;
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		ndProcess.getPluginBase().enableCallbacks();

		if (isBlocking() != null) {
			ndProcess.getPluginBase().setBlockingCallbacks(isBlocking() ? 1 : 0);
		}

		String port = getInputPort();
		if (port != null && !port.isEmpty()) {
			ndProcess.getPluginBase().setNDArrayPort(port);
		}

		if (getDataType() != null) {
			ndProcess.setDataTypeOut(getDataType().getValue());
		}

		if (getEnableOffsetScale() != null) {
			ndProcess.setEnableOffsetScale(getEnableOffsetScale() ? 1 : 0);
		}

		if (getOffset() != null) {
			ndProcess.setOffset(getOffset());
		}

		if (getScale() != null) {
			ndProcess.setScale(getScale());
		}

		if (getEnableHighClip() != null) {
			ndProcess.setEnableHighClip(getEnableHighClip() ? 1 : 0);
		}

		if (getHighClip() != null) {
			ndProcess.setHighClip(getHighClip());
		}

		if (getEnableLowClip() != null) {
			ndProcess.setEnableLowClip(getEnableLowClip() ? 1 : 0);
		}

		if (getLowClip() != null) {
			ndProcess.setLowClip(getLowClip());
		}

		if (getEnableBackgroundSubtraction() != null) {
			ndProcess.setEnableBackground(getEnableBackgroundSubtraction() ? 1 : 0);
		}

		if (getEnableFlatField() != null) {
			ndProcess.setEnableFlatField(getEnableFlatField() ? 1 : 0);
		}

		if (getEnableRecursiveFilter() != null) {
			ndProcess.setEnableFilter(getEnableRecursiveFilter() ? 1 : 0);
		}

		if (getFilterType() != null) {
			ndProcess.setFilterType(getFilterType().getValue());
		}

		if (getFilterCallback() != null) {
			ndProcess.setFilterCallbacks(getFilterCallback().getValue());
		}

		if (getNumFilter() != null) {
			ndProcess.setNumFilter(getNumFilter());
		}

		if (getAutoResetFilter() != null) {
			ndProcess.setAutoResetFilter(getAutoResetFilter() ? 1 : 0);
		}

		if (getOutputOffset() != null) {
			ndProcess.setOOffset(getOutputOffset());
		}

		if (getOutputScale() != null) {
			ndProcess.setOScale(getOutputScale());
		}

		if (getFilterOffset() != null) {
			ndProcess.setFOffset(getFilterOffset());
		}

		if (getFilterScale() != null) {
			ndProcess.setFScale(getFilterScale());
		}

		if (getOutputCoefficient1() != null) {
			ndProcess.setOC1(getOutputCoefficient1());
		}

		if (getOutputCoefficient2() != null) {
			ndProcess.setOC2(getOutputCoefficient2());
		}

		if (getOutputCoefficient3() != null) {
			ndProcess.setOC3(getOutputCoefficient3());
		}

		if (getOutputCoefficient4() != null) {
			ndProcess.setOC4(getOutputCoefficient4());
		}

		if (getFilterCoefficient1() != null) {
			ndProcess.setFC1(getFilterCoefficient1());
		}

		if (getFilterCoefficient2() != null) {
			ndProcess.setFC2(getFilterCoefficient2());
		}

		if (getFilterCoefficient3() != null) {
			ndProcess.setFC3(getFilterCoefficient3());
		}

		if (getFilterCoefficient4() != null) {
			ndProcess.setFC4(getFilterCoefficient4());
		}

		if (getFilterResetOffset() != null) {
			ndProcess.setROffset(getFilterResetOffset());
		}

		if (getFilterResetCoefficient1() != null) {
			ndProcess.setRC1(getFilterResetCoefficient1());
		}

		if (getFilterResetCoefficient2() != null) {
			ndProcess.setRC2(getFilterResetCoefficient2());
		}
	}

	@Override
	public void prepareForLine() throws Exception {
	}

	@Override
	public void completeLine() throws Exception {
	}

	@Override
	public void completeCollection() throws Exception {
		ndProcess.getPluginBase().disableCallbacks();
	}

	@Override
	public void atCommandFailure() throws Exception {
		completeCollection();
	}

	@Override
	public void stop() throws Exception {
		completeCollection();
	}

	@Override
	public List<String> getInputStreamNames() {
		return Arrays.asList();
	}

	@Override
	public List<String> getInputStreamFormats() {
		return Arrays.asList();
	}

}
