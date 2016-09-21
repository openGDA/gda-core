/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.guigenerator;

import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableColumnModel {
	private static final Logger logger = LoggerFactory.getLogger(TableColumnModel.class);

	private final String name;
	private final String label;
	private final String hidden;
	private String min;
	private String max;

	public TableColumnModel(String name, String label, String hidden, String min, String max) {
		this.name = name;
		this.label = label;
		this.hidden = hidden;
		this.min = min;
		this.max = max;
	}

	public String getName() {
		return name;
	}
	public String getLabel() {
		return label;
	}
	public boolean isHidden() {
		return parseOrElse(hidden, Boolean::parseBoolean, Boolean.FALSE);
	}
	public double getMinDouble() {
		return parseOrElse(min, Double::parseDouble, Double.MIN_VALUE);
	}
	public double getMaxDouble() {
		return parseOrElse(max, Double::parseDouble, Double.MAX_VALUE);
	}
	public int getMinInt() {
		return parseOrElse(min, Integer::parseInt, Integer.MIN_VALUE);
	}
	public int getMaxInt() {
		return parseOrElse(max, Integer::parseInt, Integer.MAX_VALUE);
	}

	private <T> T parseOrElse(String text, Function<String, T> parser, T defaultValue) {
		if (text != null){
			try{
				return parser.apply(text);
			} catch (Exception e) {
				logger.info("failed to parse {}", text, e);
			}
		}
		return defaultValue;
	}
}
