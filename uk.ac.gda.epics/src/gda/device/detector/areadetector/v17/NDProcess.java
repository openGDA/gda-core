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

package gda.device.detector.areadetector.v17;

import gda.observable.Observable;


/**
 * This maps to the "proc" plugin on the edm screen
 */
public interface NDProcess extends GetPluginBaseAvailable {

	/**
	 * List all the PVs
	 */

	static final String DataTypeOut = "DataTypeOut";

	static final String DataTypeOut_RBV = "DataTypeOut_RBV";

	static final String SaveBackground = "SaveBackground";

	static final String SaveBackground_RBV = "SaveBackground_RBV";

	static final String EnableBackground = "EnableBackground";

	static final String EnableBackground_RBV = "EnableBackground_RBV";

	static final String ValidBackground_RBV = "ValidBackground_RBV";

	static final String SaveFlatField = "SaveFlatField";

	static final String SaveFlatField_RBV = "SaveFlatField_RBV";

	static final String EnableFlatField = "EnableFlatField";

	static final String EnableFlatField_RBV = "EnableFlatField_RBV";

	static final String ValidFlatField_RBV = "ValidFlatField_RBV";

	static final String ScaleFlatField = "ScaleFlatField";

	static final String ScaleFlatField_RBV = "ScaleFlatField_RBV";

	static final String AutoOffsetScale = "AutoOffsetScale";

	static final String EnableOffsetScale = "EnableOffsetScale";

	static final String EnableOffsetScale_RBV = "EnableOffsetScale_RBV";

	static final String Offset = "Offset";

	static final String Offset_RBV = "Offset_RBV";

	static final String Scale = "Scale";

	static final String Scale_RBV = "Scale_RBV";

	static final String EnableLowClip = "EnableLowClip";

	static final String EnableLowClip_RBV = "EnableLowClip_RBV";

	static final String LowClip = "LowClip";

	static final String LowClip_RBV = "LowClip_RBV";

	static final String EnableHighClip = "EnableHighClip";

	static final String EnableHighClip_RBV = "EnableHighClip_RBV";

	static final String HighClip = "HighClip";

	static final String HighClip_RBV = "HighClip_RBV";

	static final String EnableFilter = "EnableFilter";

	static final String EnableFilter_RBV = "EnableFilter_RBV";

	static final String ResetFilter = "ResetFilter";

	static final String ResetFilter_RBV = "ResetFilter_RBV";

	static final String NumFilter = "NumFilter";

	static final String NumFilter_RBV = "NumFilter_RBV";

	static final String NumFilterRecip = "NumFilterRecip";

	static final String NumFiltered_RBV = "NumFiltered_RBV";

	static final String OOffset = "OOffset";

	static final String OOffset_RBV = "OOffset_RBV";

	static final String OScale = "OScale";

	static final String OScale_RBV = "OScale_RBV";

	static final String OC1 = "OC1";

	static final String OC1_RBV = "OC1_RBV";

	static final String OC2 = "OC2";

	static final String OC2_RBV = "OC2_RBV";

	static final String OC3 = "OC3";

	static final String OC3_RBV = "OC3_RBV";

	static final String OC4 = "OC4";

	static final String OC4_RBV = "OC4_RBV";

	static final String FOffset = "FOffset";

	static final String FOffset_RBV = "FOffset_RBV";

	static final String FScale = "FScale";

	static final String FScale_RBV = "FScale_RBV";

	static final String FC1 = "FC1";

	static final String FC1_RBV = "FC1_RBV";

	static final String FC2 = "FC2";

	static final String FC2_RBV = "FC2_RBV";

	static final String FC3 = "FC3";

	static final String FC3_RBV = "FC3_RBV";

	static final String FC4 = "FC4";

	static final String FC4_RBV = "FC4_RBV";

	static final String ROffset = "ROffset";

	static final String ROffset_RBV = "ROffset_RBV";

	static final String RC1 = "RC1";

	static final String RC1_RBV = "RC1_RBV";

	static final String RC2 = "RC2";

	static final String RC2_RBV = "RC2_RBV";

	static final String FilterType = "FilterType";

	static final String FilterTypeSeq = "FilterTypeSeq";

	static final String RecursiveAveSeq = "RecursiveAveSeq";

	static final String RecursiveSumSeq = "RecursiveSumSeq";

	static final String DifferenceSeq = "DifferenceSeq";

	static final String RecursiveAveDiffSeq = "RecursiveAveDiffSeq";

	static final String CopyToFilterSeq = "CopyToFilterSeq";
	
	static final String AutoResetFilter = "AutoResetFilter";
	static final String AutoResetFilter_RBV = "AutoResetFilter_RBV";
	static final String FilterCallbacks = "FilterCallbacks";
	static final String FilterCallbacks_RBV = "FilterCallbacks_RBV";
	
	static final int FilterCallback_EveryArray=0;
	static final int FilterCallback_ArrayNOnly=1;
	
	static final int FilterType_UserDefined=0;
	static final int FilterType_OffsetScale=1;
	static final int FilterType_RecursiveAve=2;
	static final int FilterType_RecursiveSum=3;
	static final int FilterType_Difference=4;
	static final int FilterType_RecursiveAveDiff=5;
	static final int FilterTypeV1_8_RecursiveAve=0;
	static final int FilterTypeV1_8_Average=1;
	static final int FilterTypeV1_8_Sum=2;
	static final int FilterTypeV1_8_Diff=3;
	static final int FilterTypeV1_8_RecursiveAveDiff=4;
	static final int FilterTypeV1_8_CopyToFilter=5;

	//DataTypeOutput types
	static final int DatatypeOut_Int8=0;
	static final int DatatypeOut_UInt8=1;
	static final int DatatypeOut_Int16=2;
	static final int DatatypeOut_UInt16=3;
	static final int DatatypeOut_Int32=4;
	static final int DatatypeOut_UInt32=5;
	static final int DatatypeOut_Float32=6;
	static final int DatatypeOut_Float64=7;
	static final int DatatypeOut_Automatic=8;
	
	/**
	 *
	 */
	short getDataTypeOut() throws Exception;

	/**
	 *
	 */
	void setDataTypeOut(int datatypeout) throws Exception;

	/**
	 *
	 */
	short getDataTypeOut_RBV() throws Exception;

	/**
	 *
	 */
	short getSaveBackground() throws Exception;

	/**
	 *
	 */
	void setSaveBackground(int savebackground) throws Exception;

	/**
	 *
	 */
	short getSaveBackground_RBV() throws Exception;

	/**
	 *
	 */
	short getEnableBackground() throws Exception;

	/**
	 *
	 */
	void setEnableBackground(int enablebackground) throws Exception;

	/**
	 *
	 */
	short getEnableBackground_RBV() throws Exception;

	/**
	 *
	 */
	short getValidBackground_RBV() throws Exception;

	/**
	 *
	 */
	short getSaveFlatField() throws Exception;

	/**
	 *
	 */
	void setSaveFlatField(int saveflatfield) throws Exception;

	/**
	 *
	 */
	short getSaveFlatField_RBV() throws Exception;

	/**
	 *
	 */
	short getEnableFlatField() throws Exception;

	/**
	 *
	 */
	void setEnableFlatField(int enableflatfield) throws Exception;

	/**
	 *
	 */
	short getEnableFlatField_RBV() throws Exception;

	/**
	 *
	 */
	short getValidFlatField_RBV() throws Exception;

	/**
	 *
	 */
	double getScaleFlatField() throws Exception;

	/**
	 *
	 */
	void setScaleFlatField(double scaleflatfield) throws Exception;

	/**
	 *
	 */
	double getScaleFlatField_RBV() throws Exception;
	/**
	 *
	 */
	short getAutoOffsetScale() throws Exception;

	/**
	 *
	 */
	void setAutoOffsetScale(int enableoffsetscale) throws Exception;

	/**
	 *
	 */
	/**
	 *
	 */
	short getEnableOffsetScale() throws Exception;

	/**
	 *
	 */
	void setEnableOffsetScale(int enableoffsetscale) throws Exception;

	/**
	 *
	 */
	short getEnableOffsetScale_RBV() throws Exception;

	/**
	 *
	 */
	double getOffset() throws Exception;

	/**
	 *
	 */
	void setOffset(double offset) throws Exception;

	/**
	 *
	 */
	double getOffset_RBV() throws Exception;

	/**
	 *
	 */
	double getScale() throws Exception;

	/**
	 *
	 */
	void setScale(double scale) throws Exception;

	/**
	 *
	 */
	double getScale_RBV() throws Exception;

	/**
	 *
	 */
	short getEnableLowClip() throws Exception;

	/**
	 *
	 */
	void setEnableLowClip(int enablelowclip) throws Exception;

	/**
	 *
	 */
	short getEnableLowClip_RBV() throws Exception;

	/**
	 *
	 */
	double getLowClip() throws Exception;

	/**
	 *
	 */
	void setLowClip(double lowclip) throws Exception;

	/**
	 *
	 */
	double getLowClip_RBV() throws Exception;

	/**
	 *
	 */
	short getEnableHighClip() throws Exception;

	/**
	 *
	 */
	void setEnableHighClip(int enablehighclip) throws Exception;

	/**
	 *
	 */
	short getEnableHighClip_RBV() throws Exception;

	/**
	 *
	 */
	double getHighClip() throws Exception;

	/**
	 *
	 */
	void setHighClip(double highclip) throws Exception;

	/**
	 *
	 */
	double getHighClip_RBV() throws Exception;

	/**
	 *
	 */
	short getEnableFilter() throws Exception;

	/**
	 *
	 */
	void setEnableFilter(int enablefilter) throws Exception;

	/**
	 *
	 */
	short getEnableFilter_RBV() throws Exception;

	/**
	 *
	 */
	short getResetFilter() throws Exception;

	/**
	 *
	 */
	void setResetFilter(int resetfilter) throws Exception;

	/**
	 *
	 */
	short getResetFilter_RBV() throws Exception;

	/**
	 *
	 */
	int getNumFilter() throws Exception;

	/**
	 *
	 */
	void setNumFilter(int numfilter) throws Exception;

	/**
	 *
	 */
	int getNumFilter_RBV() throws Exception;

	/**
	 *
	 */
	int getNumFilterRecip() throws Exception;

	/**
	 *
	 */
	void setNumFilterRecip(int numfilterrecip) throws Exception;

	/**
	 *
	 */
	int getNumFiltered_RBV() throws Exception;

	/**
	 *
	 */
	double getOOffset() throws Exception;

	/**
	 *
	 */
	void setOOffset(double ooffset) throws Exception;

	/**
	 *
	 */
	double getOOffset_RBV() throws Exception;

	/**
	 *
	 */
	double getOScale() throws Exception;

	/**
	 *
	 */
	void setOScale(double oscale) throws Exception;

	/**
	 *
	 */
	double getOScale_RBV() throws Exception;

	/**
	 *
	 */
	double getOC1() throws Exception;

	/**
	 *
	 */
	void setOC1(double oc1) throws Exception;

	/**
	 *
	 */
	double getOC1_RBV() throws Exception;

	/**
	 *
	 */
	double getOC2() throws Exception;

	/**
	 *
	 */
	void setOC2(double oc2) throws Exception;

	/**
	 *
	 */
	double getOC2_RBV() throws Exception;

	/**
	 *
	 */
	double getOC3() throws Exception;

	/**
	 *
	 */
	void setOC3(double oc3) throws Exception;

	/**
	 *
	 */
	double getOC3_RBV() throws Exception;

	/**
	 *
	 */
	double getOC4() throws Exception;

	/**
	 *
	 */
	void setOC4(double oc4) throws Exception;

	/**
	 *
	 */
	double getOC4_RBV() throws Exception;

	/**
	 *
	 */
	double getFOffset() throws Exception;

	/**
	 *
	 */
	void setFOffset(double foffset) throws Exception;

	/**
	 *
	 */
	double getFOffset_RBV() throws Exception;

	/**
	 *
	 */
	double getFScale() throws Exception;

	/**
	 *
	 */
	void setFScale(double fscale) throws Exception;

	/**
	 *
	 */
	double getFScale_RBV() throws Exception;

	/**
	 *
	 */
	double getFC1() throws Exception;

	/**
	 *
	 */
	void setFC1(double fc1) throws Exception;

	/**
	 *
	 */
	double getFC1_RBV() throws Exception;

	/**
	 *
	 */
	double getFC2() throws Exception;

	/**
	 *
	 */
	void setFC2(double fc2) throws Exception;

	/**
	 *
	 */
	double getFC2_RBV() throws Exception;

	/**
	 *
	 */
	double getFC3() throws Exception;

	/**
	 *
	 */
	void setFC3(double fc3) throws Exception;

	/**
	 *
	 */
	double getFC3_RBV() throws Exception;

	/**
	 *
	 */
	double getFC4() throws Exception;

	/**
	 *
	 */
	void setFC4(double fc4) throws Exception;

	/**
	 *
	 */
	double getFC4_RBV() throws Exception;

	/**
	 *
	 */
	double getROffset() throws Exception;

	/**
	 *
	 */
	void setROffset(double roffset) throws Exception;

	/**
	 *
	 */
	double getROffset_RBV() throws Exception;

	/**
	 *
	 */
	double getRC1() throws Exception;

	/**
	 *
	 */
	void setRC1(double rc1) throws Exception;

	/**
	 *
	 */
	double getRC1_RBV() throws Exception;

	/**
	 *
	 */
	double getRC2() throws Exception;

	/**
	 *
	 */
	void setRC2(double rc2) throws Exception;

	/**
	 *
	 */
	double getRC2_RBV() throws Exception;

	/**
	 *
	 */
	short getFilterType() throws Exception;

	/**
	 *
	 */
	void setFilterType(int filtertype) throws Exception;

/*	void setFilterType(FilterTypeEnum filterType) throws Exception;

	FilterTypeEnum getFilterTypeEx() throws Exception;
*/	/**
	 *
	 */
	int getFilterTypeSeq() throws Exception;

	/**
	 *
	 */
	void setFilterTypeSeq(int filtertypeseq) throws Exception;

	/**
	 *
	 */
	int getRecursiveAveSeq() throws Exception;

	/**
	 *
	 */
	void setRecursiveAveSeq(int recursiveaveseq) throws Exception;

	/**
	 *
	 */
	int getRecursiveSumSeq() throws Exception;

	/**
	 *
	 */
	void setRecursiveSumSeq(int recursivesumseq) throws Exception;

	/**
	 *
	 */
	int getDifferenceSeq() throws Exception;

	/**
	 *
	 */
	void setDifferenceSeq(int differenceseq) throws Exception;

	/**
	 *
	 */
	int getRecursiveAveDiffSeq() throws Exception;

	/**
	 *
	 */
	void setRecursiveAveDiffSeq(int recursiveavediffseq) throws Exception;

	/**
	 *
	 */
	int getCopyToFilterSeq() throws Exception;

	/**
	 *
	 */
	void setCopyToFilterSeq(int copytofilterseq) throws Exception;

	/**
	 * @throws Exception
	 */
	void reset() throws Exception;

	/**
	 * See Area Detector NDProcess FilterCallbacks doc. for version 1.8 and above
	 */
	void setFilterCallbacks(int filterCallback) throws Exception;
	
	int getFilterCallbacks() throws Exception;
	
	void setAutoResetFilter(int enable) throws Exception;
	
	int getAutoResetFilter() throws Exception;

	Observable<Double> createScaleObservable() throws Exception;	
	Observable<Double> createOffsetObservable() throws Exception;	
}