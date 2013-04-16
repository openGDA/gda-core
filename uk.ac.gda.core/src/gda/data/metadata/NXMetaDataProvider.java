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

package gda.data.metadata;

import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableBase;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.scan.IScanDataPoint;
import gda.scan.ScanInformation;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.python.core.PyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class NXMetaDataProvider extends ScannableBase implements NexusTreeProvider, Map<String, String>, Findable {

	private String name;

	private static final Logger logger = LoggerFactory.getLogger(NXMetaDataProvider.class);

	public NXMetaDataProvider() {
		super();
		reset();
	}

	@Override
	public INexusTree getNexusTree() {
		INexusTree nexusTree = new NexusTreeNode("NXCollection", "NXCollection", null);
		for (Entry<String, String> e : map.entrySet()) {
			nexusTree.addChildNode(new NexusTreeNode(e.getKey(), "NX" + e.getKey(), nexusTree, new NexusGroupData(e
					.getValue())));
		}

		try {
			createMetaScannableMap();
		} catch (DeviceException e1) {
			// TODO Auto-generated catch block
			logger.error("TODO put description of error here", e1);
		}
		System.out.println("***size = " + metaScannableMap.size());
		for (Entry<String, String> e : metaScannableMap.entrySet()) {
			System.out.println("***key = " + e.getKey());
			System.out.println("***val = " + e.getValue());
			nexusTree.addChildNode(new NexusTreeNode(e.getKey(), "NX" + e.getKey(), nexusTree, new NexusGroupData(e
					.getValue())));
		}
		return nexusTree;
	}

	Map<String, String> map;

	public void reset() {
		this.map = new HashMap<String, String>();
		this.metaScannableMap = new HashMap<String, String>();
	}

	public String listAsString(String fmt, String delimiter) {
		// TODO Auto-generated method stub
		String total = "";
		for (Entry<String, String> e : map.entrySet()) {
			total += "," + e.getKey() + ":" + e.getValue();
		}
		// String x = concatenateKeyAndValueForListAsString("%s:%x", ",");
		String x = concatenateKeyAndValueForListAsString(fmt, delimiter);
		System.out.println("***x = " + x);
		total = x;
		return total;
	}

	public String concatenateKeyAndValueForListAsString(String fmt, String delimiter) {
		String concatenated = "";
		int sanityCheck = StringUtils.countOccurrencesOf(fmt, "%s");
		System.out.println("***sanityCheck = " + sanityCheck);
		if (sanityCheck == 2) {
			for (Entry<String, String> e : map.entrySet()) {
				// concatenated += "," + e.getKey() + ":" + e.getValue();
				concatenated += String.format(fmt, e.getKey(), e.getValue());
				concatenated += delimiter;
			}
			// remove the unnecessary last delimiter
			concatenated = concatenated.substring(0, concatenated.length() - 1);
		} else {
			String defaultFmt = "%s:%s";
			String defaultDelimiter = ",";
			logger.warn("Bad input format: " + "\"" + fmt + "\"" + " is " + "replaced by default format: " + "\""
					+ defaultFmt + "\"");
			concatenated = concatenateKeyAndValueForListAsString(defaultFmt, defaultDelimiter);
		}
		// remove the unnecessary last delimiter, if present
		// if ( concatenated.lastIndexOf(delimiter)==(concatenated.length()-1)){
		// concatenated = concatenated.substring(0, concatenated.length()-1);
		// }
		return concatenated;
	}

	public void setMetaTexts(Map<String, String> metaTexts) {
		for (String key : metaTexts.keySet()) {
			add(key, metaTexts.get(key));
		}
	}

	public Map<String, String> getMetaTexts() {
		Map<String, String> outMap = new HashMap<String, String>(map);
		return outMap;
	}
	
	public void add(String key, String value) {
		put(key, value);
	}

	public int size() {
		return map.size();
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	public String get(Object key) {
		return map.get(key);
	}

	public String put(String key, String value) {
		return map.put(key, value);
	}

	public String remove(Object key) {
		return map.remove(key);
	}

	public void putAll(Map<? extends String, ? extends String> m) {
		map.putAll(m);
	}

	public void clear() {
		map.clear();
	}

	public Set<String> keySet() {
		return map.keySet();
	}

	public Collection<String> values() {
		return map.values();
	}

	public Set<java.util.Map.Entry<String, String>> entrySet() {
		return map.entrySet();
	}

	public boolean equals(Object o) {
		return map.equals(o);
	}

	public int hashCode() {
		return map.hashCode();
	}

	List<Scannable> metaScannables = new Vector<Scannable>();
	Map<String, String> metaScannableMap;

	private INexusTree startNexusTree;

	private ScanInformation startScanInformation;

	public void setMetaScannables(List<Scannable> metaScannables) {
		this.metaScannables.addAll(metaScannables);
	}
	
	public List<Scannable> getMetaScannables() {
		List<Scannable> outLst = new Vector<Scannable>(this.metaScannables);
		return outLst;
	}

	public Map<String, String> getMetaScannableMap() {
		Map<String, String> outMap = new HashMap<String, String>(metaScannableMap);
		return outMap;
	}
	
	public void add(Scannable scannable) {
		System.out.println("***adding = " + scannable.getName());
		metaScannables.add(scannable);
	}

	public void remove(ScannableBase scannable) {
		System.out.println("***removing = " + scannable.getName());
		metaScannables.remove(scannable);
	}

	public void createMetaScannableMap() throws DeviceException {
		metaScannableMap.clear();
		for (Scannable scn : metaScannables) {
			List<scannableNamePositionEntry> metas = new Vector<scannableNamePositionEntry>();

			List<String> scnNames = new Vector<String>();

			int len = scn.getInputNames().length;
			String[] inputNames = scn.getInputNames();
			for (int i = 0; i < len; i++) {
				scnNames.add(inputNames[i]);
			}

			len = scn.getExtraNames().length;
			String[] extraNames = scn.getExtraNames();
			for (int i = 0; i < len; i++) {
				scnNames.add(extraNames[i]);
			}

			List<String> scnFormats = new Vector<String>();
			len = scn.getOutputFormat().length;
			String[] outFormats = scn.getOutputFormat();
			for (int i = 0; i < len; i++) {
				scnFormats.add(outFormats[i]);
			}

			Object scnPos = null;
			try {
				scnPos = scn.getPosition();
			} catch (PyException e) {
				throw new DeviceException("Error fetching " + scn.getName() + " position: " + e.toString());
			} catch (Exception e) {
				throw new DeviceException("Error fetching " + scn.getName() + " position: " + e.getMessage(), e);
			}

			// Object pos = null;
			// try {
			// pos = scn.getPosition();
			// } catch (DeviceException e1) {
			// // TODO Auto-generated catch block
			// logger.error("TODO put description of error here", e1);
			// }
			String posAsString = scnPos.toString();

			metas.add(new scannableNamePositionEntry(scn.getName(), posAsString));
			// metas = createNamePositionPairs(scn);
			for (scannableNamePositionEntry e : metas) {
				metaScannableMap.put(e.key, e.value);
			}
		}
	}

	class scannableNamePositionEntry {
		public String key;
		public String value;

		public scannableNamePositionEntry(String key, String value) {
			super();
			this.key = key;
			this.value = value;
		}

	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		if ((this.name == null || this.name.isEmpty())) {
			logger.warn("getName() called on NXMetaDataProvider when the name has not been set. This may cause problems in the system and should be fixed.");
		}
		return this.name;
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		return null;
	}

	@Override
	public void configure() throws FactoryException {
		super.configure();
		setInputNames(new String[0]);
		setExtraNames(new String[0]);
		setOutputFormat(new String[0]);
	}

	@Override
	public void atScanStart() throws DeviceException {
		startNexusTree=getNexusTree();
		
	}
	
	public INexusTree getAtScanStartNexusTree(){
		return startNexusTree;
		
	}
	
}
