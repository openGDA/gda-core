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

package uk.ac.gda.doe;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.richbeans.reflection.BeansFactory;

/**
 * IMPORTANT NOTE: All beans used with this class must have hashCode and equals
 * implemented correctly. All, including children and beans in collections.
 */
public class DOEUtils {

	private static final int MAX_RANGE_SIZE = 1000;


	/**
	 * Gets the RangeInfo for the objects passed in by constructing a
	 * recursive method with the objects in order first in list, outtermost.
	 * @param obs
	 * @return list
	 * @throws Exception
	 */
	public static List<RangeInfo> getInfoFromList(final List<Object> obs) throws Exception {

		final List<RangeInfo> exp = new ArrayList<RangeInfo>(31);
		getInfoFromList(obs, 0, exp);
		return exp;
	}

	private static void getInfoFromList(List<Object> obs, int index, List<RangeInfo> exp) throws Exception {

		if (index>=obs.size()) return;

		final Object          bean = obs.get(index);
		if(bean != null)
		{
			final List<RangeInfo> runs = DOEUtils.getInfo(bean);
			for (RangeInfo rangeInfo : runs) {
				if (!rangeInfo.isEmpty()) exp.add(rangeInfo);
				getInfoFromList(obs, index+1, exp);
			}
		}
		else
		{
			getInfoFromList(obs, index+1, exp);
		}
	}

	/**
	 * Reads the fields defined with a DOEField annotation and
	 * returns a list of objects used to describe the range.
	 *
	 * Each RangeInfo represents on experiment with the
	 *
	 * @param bean
	 * @return list of expanded
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public static List<RangeInfo> getInfo(final Object bean) throws Exception {


		final List<Collection<FieldContainer>> weightedFields = new ArrayList<Collection<FieldContainer>>(11);
		for (int i = 0; i < 11; i++) weightedFields.add(new LinkedHashSet<FieldContainer>(7));

		readAnnotations(null, bean, weightedFields, -1);

		List<FieldContainer> expandedFields = expandFields(weightedFields);

		final List<RangeInfo> ret = new ArrayList<RangeInfo>(31);
        getInfo(new RangeInfo(), expandedFields, 0, ret);

		return ret;
	}

	/**
	 *
	 * @param info
	 * @param orderedFields
	 * @param index
	 * @param ret
	 * @throws Exception
	 */
	protected static void getInfo(final RangeInfo            info,
			                      final List<FieldContainer> orderedFields,
			                      final int                  index,
			                      final List<RangeInfo>      ret) throws Exception {

		if (index>=orderedFields.size()) {
			// NOTE: You must implement hashCode and equals
			// on all beans. These are used to avoid adding
			// repeats.
			final RangeInfo clone = BeansFactory.deepClone(info);
			ret.add(clone);
			return;
		}

		final FieldContainer field      = orderedFields.get(index);

		final Object originalObject = field.getOriginalObject();
	    final String stringValue    = (String)BeansFactory.getBeanValue(originalObject, field.getName());
		if (stringValue==null) {
			getInfo(info, orderedFields, index+1, ret);
			return;
		}

		final String      range = stringValue.toString();
		final List<? extends Number> vals = DOEUtils.expand(range, field.getAnnotation().type());
		for (Number value : vals) {
			if (vals.size()>1) info.set(new FieldValue(field.getOriginalObject(), field.getName(), value.toString()));
			getInfo(info, orderedFields, index+1, ret);
		}

	}

	/**
	 * Reads the fields defined with a DOEField annotation and
	 * returns a list of expanded objects of the passed in type.
	 *
	 * Uses BeanUtils to clone beans.
	 *
	 * @param bean
	 * @return list of expanded
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public static List<? extends Object> expand(final Object bean) throws Exception {


		final List<Collection<FieldContainer>> weightedFields = new ArrayList<Collection<FieldContainer>>(11);
		for (int i = 0; i < 11; i++) weightedFields.add(new LinkedHashSet<FieldContainer>(7));

		readAnnotations(null, bean, weightedFields, -1);

		List<FieldContainer> expandedFields = expandFields(weightedFields);
		final List<Object> ret = new ArrayList<Object>(31);

		Object clone = BeansFactory.deepClone(bean);
        expand(clone, expandedFields, 0, ret);

		return ret;
	}

	/**
	 * Makes a 1D list from the weightedFields.
	 * @param weightedFields
	 * @return list
	 */
	private static List<FieldContainer> expandFields(final List<Collection<FieldContainer>> weightedFields) {
        final List<FieldContainer> ret = new ArrayList<FieldContainer>(31);
        for (Collection<FieldContainer> fields : weightedFields) {
			for (FieldContainer fc : fields) {
				if (!ret.contains(fc)) ret.add(0,fc);
			}
		}
        return ret;
	}

	/**
	 * Recursive method reads the annotations of all non-null fields.
	 *
	 * This algorithm is not perfect and there is probably a simpler one
	 * that deals with more cases. All the test cases are in @see DOETest.
	 *
	 * The complexity comes with dealing with fields which are lists of beans
	 * that may have fields which are ranges.
	 *
	 *
	 * @param fieldObject
	 * @param weightedFields
	 * @throws Exception
	 */
	protected static void readAnnotations(final FieldContainer             parent,
			                             final Object                     fieldObject,
			                             final List<Collection<FieldContainer>> weightedFields,
			                             final int                        index) throws Exception {

		// A few common fields that we can rule out as objects which have DOEField fields.
		if (fieldObject.getClass().getName().startsWith("java.lang.")) return;

		Field[] ff     = null;
		if (fieldObject instanceof List<?>) {
			final List<?> vals = (List<?>)fieldObject;
			if (!vals.isEmpty()) {
				ff = vals.get(0).getClass().getDeclaredFields();
			}
		} else {
			ff = fieldObject.getClass().getDeclaredFields();
		}
		if (ff == null) return;

		final List<Field> controlledFields = getControlledFields(fieldObject, ff);
		for (int i = 0; i < ff.length; i++) {

			final Field    f   = ff[i];
			if (controlledFields!=null&&controlledFields.contains(f)) continue;

			final DOEField doe = f.getAnnotation(DOEField.class);
			FieldContainer fc = new FieldContainer();
			fc.setField(f);
			fc.setOriginalObject(fieldObject);
			fc.setParent(parent);
			fc.setListIndex(index);
			fc.setAnnotation(doe);
			if (doe!=null) {
				final Collection<FieldContainer> list = weightedFields.get(doe.value());
				if (fieldObject instanceof List<?>) {
					final List<?> values = (List<?>)fieldObject;
					for (int j = 0; j < values.size(); j++) {
						list.add(fc.clone(values.get(j), j));
					}

				} else {
					list.add(fc);
				}

			} else {
				try {
					if (fieldObject instanceof List<?>) {
						final List<?> values = (List<?>)fieldObject;
						for (int j = 0; j < values.size(); j++) {
							readAnnotations(fc, values.get(j), weightedFields, j);
						}

					} else {
						final Object value = BeansFactory.getBeanValue(fieldObject, f.getName());
						if (value!=null) readAnnotations(fc, value, weightedFields, -1);
					}
				} catch (Throwable ignored) {

				}
			}
		}
	}


	private static List<Field> getControlledFields(Object fieldObject, Field[] ff) throws Exception {

        if (fieldObject instanceof List<?>) return null;

        final List<Field> controlled = new ArrayList<Field>(7);
        for (int i = 0; i < ff.length; i++) {
			final Field    f   = ff[i];
			final DOEControl control = f.getAnnotation(DOEControl.class);
			if (control!=null) {
				final Object value  = BeansFactory.getBeanValue(fieldObject, f.getName());
				if (value!=null) {
					final String[] vals = control.values();
					if (!Arrays.asList(vals).contains(value)) continue;
					final String[] ffs  = control.fields();
					for (int j = 0; j < vals.length; j++) {
						if (vals[j].equals(value)) continue; // why this line?
						controlled.add(fieldObject.getClass().getDeclaredField(ffs[j]));
					}
				}
			}

		}
		return controlled;
	}

	/**
	 * Recursive method which expands out all the simulations into
	 * a 1D list from the ranges specified. This reads the annotation
	 * weightings to construct the loops based on parameter weighting.
	 *
	 * For instance temperature might be in an outer loop to process all
	 * experiments at a given temperature together.
	 *
	 * @param clone
	 * @param orderedFields
	 * @param index
	 * @param ret
	 * @throws Exception
	 */
	protected static void expand(      Object            clone,
			                     final List<FieldContainer> orderedFields,
			                     final int               index,
			                     final List<Object>      ret) throws Exception {

		if (index>=orderedFields.size()) {
			// NOTE: You must implement hashCode and equals
			// on all beans. These are used to avoid adding
			// repeats.
			if (!ret.contains(clone)) ret.add(clone);
			return;
		}

		final FieldContainer field      = orderedFields.get(index);

		final Object originalObject = field.getOriginalObject();
	    final String stringValue    = (String)BeansFactory.getBeanValue(originalObject, field.getName());
		if (stringValue==null) {
			expand(clone, orderedFields, index+1, ret);
			return;
		}

		final String      range = stringValue.toString();
		final List<? extends Number> vals = DOEUtils.expand(range, field.getAnnotation().type());
		for (Number value : vals) {
			clone = BeansFactory.deepClone(clone);
			setBeanValue(clone, field, value.toString(), field.getListIndex());
			expand(clone, orderedFields, index+1, ret);
		}

	}

	protected static boolean setBeanValue(final Object clone, final FieldContainer field, final String value, final int index) throws Exception {

		final List<FieldContainer> fieldPath = new ArrayList<FieldContainer>(3);

		FieldContainer f = field.getParent();
		while (f!=null) {
			fieldPath.add(0,f);
			f = f.getParent();
		}

		Object cloneObject = clone;
		for (FieldContainer fc : fieldPath) {
			if (cloneObject instanceof List<?>) {
				final int     listIndex = field.getParent().getListIndex();
				final List<?> cloneList = (List<?>)cloneObject;
				if (listIndex>-1) {
					cloneObject = cloneList.get(listIndex);
				} else {
					return false;
				}
			} else {
			    cloneObject = BeansFactory.getBeanValue(cloneObject, fc.getName());
			}
		}

		if (cloneObject instanceof List<?> && index>-1) {
			cloneObject = ((List<?>)cloneObject).get(index);
		}

		if (value!=null && value.equals(BeansFactory.getBeanValue(cloneObject, field.getName()))) {
			return false;
		}

	    BeansFactory.setBeanValue(cloneObject, field.getName(), value);
	    return true;
	}

	/**
	 * Translates a doe string encoded for the possible range types
	 * into a list of Double values.
	 *
	 * @param range
	 * @return expanded values
	 */
	public static List<? extends Number> expand(final String range) {
		return expand(range, (String)null);
	}

	/**
	 * Expand values defined in a range
	 * @param range
	 * @param clazz
	 * @return list of double values
	 */
	public static <T extends Number> List<T> expand(final String range, Class<T> clazz) {
		return expand(range,null,clazz);
	}

	/**
	 * Expand values defined in a range
	 * @param range
	 * @param unit
	 * @return list of double values
	 */
	public static List<? extends Number> expand(final String range, final String unit) {
		return expand(range,unit,Double.class);
	}

	/**
	 *
	 * @param range
	 * @param unit
	 * @param clazz
	 * @return list of values
	 */
	private static <T extends Number> List<T> expand(String range, String unit, Class<T> clazz) {

		final List<T> ret  = new ArrayList<T>(7);

		if (DOEUtils.isList(range, unit)) {
			final String value   = DOEUtils.removeUnit(range, unit);
			final String[] items = value.split(",");
            for (String val : items)  ret.add(getValue(val.trim(), clazz));

		} else if (DOEUtils.isRange(range, unit)) {
			final double[] ran = DOEUtils.getRange(range, unit);
			if (ran[0]>ran[1]) {
			    for (double i = ran[0]; i >= ran[1]; i-=ran[2]) {
			    	if (ret.size()>MAX_RANGE_SIZE) break;
			    	ret.add(getValue(i, clazz));
			    }
		    } else {
			    for (double i = ran[0]; i <= ran[1]; i+=ran[2]) {
			    	if (ret.size()>MAX_RANGE_SIZE) break;
			    	ret.add(getValue(i, clazz));
			    }
		    }

		} else {
			final String value   = DOEUtils.removeUnit(range, unit);
			ret.add(getValue(value.trim(), clazz));

		}

		return ret;
	}


	public static double[] getRange(String range, String unit) {
		if (!DOEUtils.isRange(range, unit)) return null;
		final String value   = DOEUtils.removeUnit(range, unit);
		final String[] item  = value.split(";");
		final double   start = Double.parseDouble(item[0].trim());
		final double   end   = Double.parseDouble(item[1].trim());
		      double   inc   = Double.parseDouble(item[2].trim());
		return new double[]{start,end,inc};
	}


	/**
	 * There must be a better way of doing this
	 * @param val
	 * @param clazz
	 * @return number
	 */
	private static <T extends Number> T getValue(String val, Class<T> clazz) {
		return getValue(new Double(val), clazz);
	}
	/**
	 *
	 * @param <T>
	 * @param val
	 * @param clazz
	 * @return number
	 */
	@SuppressWarnings("unchecked")
	private static <T extends Number> T getValue(double val, Class<T> clazz) {
		if (clazz==Integer.class) {
			return (T)new Integer(Math.round(Math.round(val)));
		} else if (clazz==Double.class) {
			return (T)new Double(val);
		}
		throw new ClassCastException("DOEUtils cannot expand with class "+clazz+" yet.");
	}

	/**
	 * Used to test a value to see if it is legal syntax for a doe value.
	 * @param value
	 * @return true if doe value
	 */
	public static boolean isDOE(final String value) {
		return isRange(value, null) || isList(value, null);
	}

	/**
	 * Returns true if the value is a range of numbers. The decimal
	 * places must be eight or less.
	 *
	 * @param value
	 * @param unit -  may be null
	 * @return true of the value is a list of values
	 */
	public static boolean isRange(final String value, final String unit) {
		return isRange(value, 8, unit);
	}

	/**
	 *
	 * @param value
	 * @param decimalPlaces
	 * @param unit
	 * @return true of the value is a list of values
	 */
	public static boolean isRange(String value, int decimalPlaces, String unit) {
		final Pattern rangePattern = getRangePattern(decimalPlaces, unit);
		return rangePattern.matcher(value.trim()).matches();
	}

	/**
     * A regular expression to match a range.
     * @param decimalPlaces for numbers matched
     * @param unit - may be null if no unit in the list.
     * @return Pattern
     */
	public static Pattern getRangePattern(final int decimalPlaces, final String unit) {

		final String ndec = decimalPlaces>0
		                  ? "\\.?\\d{0,"+decimalPlaces+"})"
		                  : ")";
		final String digitExpr = "(\\-?\\d+"+ndec;
		final String rangeExpr = "("+digitExpr+";\\ ?"+digitExpr+";\\ ?"+digitExpr+")";
		if (unit==null) {
			return Pattern.compile(rangeExpr);
		}
	    return Pattern.compile(rangeExpr+"\\ {1}\\Q"+unit+"\\E");
	}

	/**
	 * Returns true if the value is a list of numbers. The decimal
	 * places must be eight or less.
	 *
	 * @param value
	 * @param unit -  may be null
	 * @return true of the value is a list of values
	 */
	public static boolean isList(final String value, final String unit) {
		return isList(value,8,unit);
	}

	/**
	 *
	 * @param value
	 * @param decimalPlaces
	 * @param unit
	 * @return true of the value is a list of values
	 */
	public static boolean isList(String value, int decimalPlaces, String unit) {
		final Pattern listPattern = getListPattern(decimalPlaces, unit);
		return listPattern.matcher(value.trim()).matches();
	}

	/**
     * A regular expression to match a
     * @param decimalPlaces for numbers matched
     * @param unit - may be null if no unit in the list.
     * @return Pattern
     */
	public static Pattern getListPattern(final int decimalPlaces, final String unit) {

		final String ndec = decimalPlaces>0
		                  ? "\\.?\\d{0,"+decimalPlaces+"})"
		                  : ")";
		final String digitExpr = "(\\-?\\d+"+ndec;
		final String listExpr = "(("+digitExpr+",\\ ?)+"+digitExpr+")";
		if (unit==null) {
			return Pattern.compile(listExpr);
		}
	    return Pattern.compile(listExpr+"\\ {1}\\Q"+unit+"\\E");
	}

	/**
	 * Strips the unit, should only be called on strings that are known to match a
	 * value pattern.
	 *
	 * @param value
	 * @param unit
	 * @return value without unit.
	 */
	public static String removeUnit(String value, String unit) {
		if (unit==null) return value;
        final Pattern pattern = Pattern.compile("(.+)\\ ?\\Q"+unit+"\\E");
        final Matcher matcher = pattern.matcher(value);
        if (matcher.matches()) return matcher.group(1);
        return value;
	}

}
