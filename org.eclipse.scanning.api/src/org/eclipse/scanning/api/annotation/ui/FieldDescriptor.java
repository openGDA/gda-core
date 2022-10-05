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

package org.eclipse.scanning.api.annotation.ui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for a field in a model to control how that field should be handled in an editor e.g. whether it should be
 * visible, the associated unit, minimum/maximum permitted value etc.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldDescriptor {

	/**
	 * @return true if the field is visible in the UI
	 */
	public boolean visible() default true;

	/**
	 * @return true if the field is editable or false for read only.
	 */
	public boolean editable() default true;

	/**
	 * The label attribute. If unset, uses the name of the field for the label.
	 */
	public String label() default "";

	/**
	 * If scannable is set, this value overrides the maximum of the scannble, otherwise the scannable is used.
	 *
	 * @return maximum allowed legal value for field
	 */
	public double maximum() default Double.POSITIVE_INFINITY;

	/**
	 * If scannable is set, this value overrides the minimum of the scannble, otherwise the scannable is used.
	 *
	 * @return minimum allowed legal value for field
	 */
	public double minimum() default Double.NEGATIVE_INFINITY;

	/**
	 * If scannable is set, this value overrides the unit of the scannble, otherwise the scannable unit is used.
	 *
	 * @return the unit that the fields value should be in.
	 */
	public String unit() default "";

	/**
	 * @return the string hint which is shown to the user when they first edit the value.
	 */
	public String hint() default "";

	/**
	 * If the field is a String, java.io.File, java.nio.file.Path or IResource you may use this annotation to define the
	 * type of checking which will be done.
	 * <p>
	 * If this field is not used and your field is a File for instance, the NEW_FILE option will be the default, rather
	 * than NONE
	 *
	 * @return the file type.
	 */
	public FileType file() default FileType.NONE;

	/**
	 * The number format to format a field editing a number
	 */
	public String numberFormat() default "";
}
