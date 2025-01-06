package org.opengda.detector.electronanalyser.api;
import java.util.List;
import java.util.Map;

import gda.factory.Findable;

/**
 * Service that provide global settings for SES api e.g default settings for {@link SESRegion}, {@link SESExcitationEnergySource}
 * and how the client display it.
 */
public interface SESSettingsService extends Findable {
	/**
	 * @return String defining the defaultExcitationEnergySourceForSESRegion value for a {@link SESRegion}.
	 */
	public String getDefaultExcitationEnergySourceForSESRegion();

	/**
	 * @parama Set the default String value for excitationEnergySource that a new {@link SESRegion} will use.
	 */
	public void setDefaultExcitationEnergySourceForSESRegion(String defaultExcitationEnergySourceForSESRegion);

	/**
	 *
	 * @return Map<String, String> excitationEnergySourceToLimits where limits is two numbers joined by "-" e.g "0-2100".
	 */
	public Map<String, List<Double>> getLegacyConversionExcitationEnergyForSESRegion();

	/**
	 * Set the legacy conversion of excitationEnergy to a excitationEnergySource {@link SESRegion}. The first String value of the map
	 * is the excitationEnergySource name, the second is a list of two values containing the excitationEnergy range it is valid at.
	 * Example configuration:
	 * <pre>
	 * {@code
	 * <bean id="sessettings" class="org.opengda.detector.electronanalyser.api.SESSettings">
	 * 	<property name="legacyConversionExcitationEnergyForSESRegion">
	 * 		<map>
	 * 			<!-- convert excitation energies in this range to source1-->
	 * 			<entry key="source1">
	 * 				<list value-type="java.lang.Double">
	 * 					<bean id="source1LowLimt" class="java.lang.Double">
	 * 						<constructor-arg value="2100"/>
	 * 					</bean>
	 * 					<bean id="source1HighLimt" class="java.lang.Double">
	 * 						<constructor-arg>
	 * 							<util:constant static-field="java.lang.Double.MAX_VALUE"/>
	 * 						</constructor-arg>
	 * 					</bean>
	 * 				</list>
	 * 			</entry>
	 * 		</map>
	 * 	</property>
	 * </bean>
	 * }
	 * </pre>
	 */
	public void setLegacyConversionExcitationEnergyForSESRegion(Map<String, List<Double>> excitationEnergySourceToLimits);

	/**
	 * Used when opening a file that is in legacy xml format and {@link #isLegacyFileFormatOverwrittenForSESSequenceJSONHanlder()} is set to false.
	 * @return the file extensions that this file is renamed to by adding this value at the end.
	 */
	public String getLegacyFileExtensionForSESSequenceJSONHanlder();

	/**
	 * Used when opening a file that is in legacy xml format and {@link #isLegacyFileFormatOverwrittenForSESSequenceJSONHanlder()} is set to false.
	 * @param legacyFileExtensionForSESSequenceJSONHanlder To preserve the legacy file, rename the file by adding this extension at the end.
	 */
	public void setLegacyFileExtensionForSESSequenceJSONHanlder(String legacyFileExtensionForSESSequenceJSONHanlder);

	/**
	 * @return boolean determining when opening a legacy file, if it is overwritten when converted.
	 */
	public boolean isLegacyFileFormatOverwrittenForSESSequenceJSONHanlder();

	/**
	 * @param boolean determining when opening a legacy file, if it is overwritten when converted.
	 */
	public void setLegacyFileFormatOverwrittenForSESSequenceJSONHanlder(boolean legacyFileExtensionForSESSequenceJSONHanlder);

	/**
	 * @param sesConfigExcitationEnergySourceList which defines the default {@link SESExcitationEnergySource} and how the client will
	 * display the excitation energy sources. Example configuration:
	 * <pre>
	 * {@code
	 * <bean id="sessettings" class="org.opengda.detector.electronanalyser.api.SESSettings">
	 * 	<property name="SESConfigExcitationEnergySourceList">
	 * 		<bean id="excitationEnergySourceConfig" class="java.util.ArrayList">
	 * 			<constructor-arg>
	 * 				<list>
	 * 					<bean id="dcmenergyConfig" class="org.opengda.detector.electronanalyser.api.SESConfigExcitationEnergySource">
	 * 						<constructor-arg name="name" value="source1"/>
	 * 						<constructor-arg name="displayName" value="Hard X-ray"/>
	 * 						<constructor-arg name="scannableName" value="dcmenergyEv"/>
	 * 					</bean>
	 * 					<bean id="pgmenergyConfig" class="org.opengda.detector.electronanalyser.api.SESConfigExcitationEnergySource">
	 * 						<constructor-arg name="name" value="source2"/>
	 * 						<constructor-arg name="displayName" value="Soft X-ray"/>
	 * 						<constructor-arg name="scannableName" value="pgmenergy"/>
	 * 					</bean>
	 * 				</list>
	 * 			</constructor-arg>
	 * 		</bean>
	 * 	</property>
	 * </bean>
	 * }
	 * </pre>
	 */
	public void setSESConfigExcitationEnergySourceList(List<SESConfigExcitationEnergySource> sesConfigExcitationEnergySource);

	/**
	 * @return List which defines the the default {@link SESExcitationEnergySource} and how client displays it.
	 */
	public List<SESConfigExcitationEnergySource> getSESConfigExcitationEnergySourceList();

	/**
	 * @return List of the default {@link SESExcitationEnergySource} which is used when creating new sequence files.
	 * Defined by {@link #setSESConfigExcitationEnergySourceList()}
	 */
	public List<SESExcitationEnergySource> getSESExcitationEnergySourceList();

	/**
	 * @return true if size of {@link #getSESExcitationEnergySourceList()} is greater than 1, else false.
	 */
	public boolean isExcitationEnergySourceSelectable();

	/**
	 * Helper function that will map an excitationEnergy value to a corresponding excitationEnergySourceName.
	 * Uses the configuration supplied by {@link #getSESConfigExcitationEnergySourceList()}.
	 * @param excitationEnergy
	 * @return
	 */
	public String convertLegacyExcitationEnergyToExcitationEnergySourceName(final double excitationEnergy);
}