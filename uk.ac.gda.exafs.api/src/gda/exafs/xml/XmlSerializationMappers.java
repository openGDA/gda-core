/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package gda.exafs.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import com.ctc.wstx.api.WstxOutputProperties;
import com.ctc.wstx.stax.WstxInputFactory;
import com.ctc.wstx.stax.WstxOutputFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator.Feature;

public class XmlSerializationMappers {

	private XmlSerializationMappers() {
		// No constructor for utility class
	}

	/**
	 * Return new XmlMapper object that can serialize/deserialize objects to/from XML
	 * @return XmlMapper
	 */
	public static XmlMapper getXmlMapper() {
		XMLInputFactory ifactory = new WstxInputFactory();
		XMLOutputFactory ofactory = new WstxOutputFactory();
		// enable double quotes in the XML header declaration
		ofactory.setProperty(WstxOutputProperties.P_USE_DOUBLE_QUOTES_IN_XML_DECL, true);

		XmlFactory xf = XmlFactory.builder().inputFactory(ifactory).outputFactory(ofactory).build();
		XmlMapper mapper = new XmlMapper(xf);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.enable(Feature.WRITE_XML_DECLARATION);
		mapper.setDefaultUseWrapper(false);
		return mapper;
	}

	/**
	 * Return String or Double value from JsonNode object
	 * (to have proper value for map value)
	 * @param node
	 * @return
	 */
	private static Object getNodeValue(JsonNode node) {
		try {
			return Double.parseDouble(node.asText());
		} catch(NumberFormatException nfe) {
		}
		return node.asText();
	}

	public static class MapSerializer extends JsonSerializer<Map<Object, Object>> {

		protected String keyFieldName = "scannableName";
		protected String valueFieldName = "pv";
		protected String entryFieldName = "";

		public MapSerializer() {
			super();
		}

	    @Override
	    public void serialize(Map<Object,Object> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
	        ToXmlGenerator xmlGen = (ToXmlGenerator) gen;
	        xmlGen.writeStartObject();
	        for (Map.Entry<?, ?> entry : value.entrySet()) {

//	        	<scannableName>scannable1</scannableName>
//	        	<pv>pvForScannable1</pv>
//	        	<scannableName>scannable2</scannableName>
//	        	<pv>pv:for:scannable2</pv>
	        	if (!entryFieldName.isEmpty()) {
	        		xmlGen.writeObjectFieldStart(entryFieldName);
	        	}
	            xmlGen.writeObjectFieldStart(keyFieldName);
	            xmlGen.setNextIsAttribute(false);
	            xmlGen.writeRaw(entry.getKey().toString());
	            xmlGen.writeEndObject();

	            xmlGen.writeObjectFieldStart(valueFieldName);
	            xmlGen.setNextIsAttribute(false);
	            xmlGen.writeRaw(entry.getValue().toString());
	            xmlGen.writeEndObject();
	            if (!entryFieldName.isEmpty()) {
	            	xmlGen.writeEndObject();
	            }
	        }
	        xmlGen.writeEndObject();
	    }

	}

	public static class MapDeserializer extends JsonDeserializer<Map<Object,Object>> {

		protected String keyFieldName = "scannableName";
		protected String valueFieldName = "pv";
		protected String entryFieldName = "";

		public MapDeserializer() {
			super();
		}

		@Override
		public Map<Object, Object> deserialize(JsonParser j, DeserializationContext ctxt)
				throws IOException {

			JsonNode node = j.getCodec().readTree(j);
			if (node.get(entryFieldName) != null) {
				return getMapFromEntry(node.get(entryFieldName));
			}

			// Iterate through list of key value pairs and add each to the map
	        var scnNameIter = node.get(keyFieldName).elements();
	        var pvIter = node.get(valueFieldName).elements();

			Map<Object,Object> map = new LinkedHashMap<>();
	        while(scnNameIter.hasNext() && pvIter.hasNext()) {
	        	map.put(scnNameIter.next().asText(), getNodeValue(pvIter.next()));
	        }
			return map;
		}

		/**
		 * Iterate over entry nodes, extract the key-value pairs and generate a map
		 * @param node
		 * @return
		 */
		private Map<Object, Object> getMapFromEntry(JsonNode node) {
			var vals = node.elements();
			var iter = node.iterator();
			Map<Object, Object> map = new LinkedHashMap<>();
			while(iter.hasNext()) {
				var nodeValue = iter.next();
				if (nodeValue.isArray() && nodeValue.size()>1) {
					// map with same key and value type
					map.put(nodeValue.get(0).asText(), getNodeValue(nodeValue.get(1)));
				} else if (nodeValue.get(keyFieldName)==null) {
					// map with single entry
					map.put(nodeValue.asText(), getNodeValue(iter.next()));
				} else {
					var keyNode = nodeValue.get(keyFieldName);
					if (keyNode.isArray()) {
						// If 2 key nodes, then use these for key and value : key = 1st item, value = 2nd item
						map.put(keyNode.get(0).asText(), getNodeValue(keyNode.get(1)));
					} else {
						map.put(nodeValue.get(keyFieldName).asText(), getNodeValue(nodeValue.get(valueFieldName)));
					}
				}
			}
			return map;
		}
	}

	public static class ListSerializer extends JsonSerializer<List<Object>> {

		public ListSerializer() {
			super();
		}

	    @Override
	    public void serialize(List<Object> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
	        ToXmlGenerator xmlGen = (ToXmlGenerator) gen;
	        xmlGen.writeStartObject();
	        for(var v : value) {
	        	xmlGen.writeObjectFieldStart(v.getClass().getSimpleName().toLowerCase());
	        	xmlGen.setNextIsAttribute(false);
	        	xmlGen.writeRaw(v.toString());
	        	xmlGen.writeEndObject();
	        }
	        xmlGen.writeEndObject();
	    }
	}

	public static class ListDeserializer extends JsonDeserializer<List<Object>> {

		@Override
		public List<Object> deserialize(JsonParser j, DeserializationContext ctxt)
				throws IOException {

			JsonNode node = j.getCodec().readTree(j);
			if (node == null) {
				return null;
			}

			var entry = node.fields().next();
			List<Object> nestedList = new ArrayList<>();
			JsonNode value = entry.getValue();
			// Node is a single text value
			if (value.isValueNode()) {
				nestedList.add(entry.getValue().textValue());
			} else {
				// Node has 1 or more elements that can be iterated
				JsonNode nodeValues = value.elements().next();
				if (nodeValues.isValueNode()) {
					// Add single value to the list
					nestedList.add(nodeValues.textValue());
				} else {
					// Iterate over all the values and add to list
					var iterator = nodeValues.elements();
					while(iterator.hasNext()) {
						var els = iterator.next();
						nestedList.add(getNodeValue(els));
					}
				}
			}
			return nestedList;
		}
	}

	/**
	 * {@link JsonSerializer} to convert {@code List<List<Double>>} to a
	 * 'list-of-lists' XML structure.
	 * e.g. Given : (1.0, 2.3, 4.5), (11.0, 23.0, 45.0) , XML produced is :
	 * <pre><{@code
	 * <list>
	 *  <double>1.0</double>
	 *  <double>2.3</double>
	 *  <double>4.5</double>
	 * </list>
	 * <list>
	 *   <double>11.0</double>
	 *   <double>23.0</double>
	 *   <double>45.0</double>
	 * </list>
	 *}</pre>
	 */
	public static class NestedListSerializer extends JsonSerializer<List<List<Double>>> {

		public NestedListSerializer() {
			super();
		}

	    @Override
	    public void serialize(List<List<Double>> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
	        ToXmlGenerator xmlGen = (ToXmlGenerator) gen;
	        xmlGen.writeStartObject();
	        for (var vals : value) {
	            xmlGen.writeObjectFieldStart("list");
	        	for(Double v : vals) {
		            xmlGen.writeObjectFieldStart("double");
		            xmlGen.setNextIsAttribute(false);
		            xmlGen.writeRaw(v.toString());
		            xmlGen.writeEndObject();
	        	}
	            xmlGen.writeEndObject();
	        }
	        xmlGen.writeEndObject();
	    }
	}

	/**
	 * {@link JsonDeserializer} to convert XML produced by {@link NestedListSerializer}
	 * back to a {@code List<List<Double>>}.
	 */
	public static class NestedListDeserializer extends JsonDeserializer<List<List<Double>>> {

		@Override
		public List<List<Double>> deserialize(JsonParser j, DeserializationContext ctxt)
				throws IOException {

			JsonNode node = j.getCodec().readTree(j);
			if (node == null) {
				return null;
			}

			var dblIter = node.findValues("double");
			List<List<Double>> nestedList = new ArrayList<>();
			for(var n : dblIter) {
				// Collect doubles from iterator into a list :
				var els = n.elements();
				List<Double> valList = new ArrayList<>();
				els.forEachRemaining(v -> valList.add(v.asDouble()));
				nestedList.add(valList);
			}
			return nestedList;
		}
	}

}
