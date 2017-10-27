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

// this text is in file gda/util/converters/package-info.java
/**
 *      <p>
 *      The design philosophy is that within the ObjectServer of GDA there will be instances of classes that implement
 *      interfaces IReloadableQuantitiesConverter and Findable. These instances are findable by any other classes using
 *      the Finder object given the Name member of the class.
 *      <p>
 *      One such class is the CombinedDOF which has the name of the IReloadableQuantitiesConverter object stored in its
 *      ConverterName field. The CombinedDOF then uses the IReloadableQuantitiesConverter object to convert the position
 *      of the dof to the position of the associated moveables and vice versa.
 *      <p>
 *      Two important implementations of IReloadableQuantitiesConverter and Findable are CoupledConverterHolder and
 *      SplitConverterHolder.
 *      <p>
 *      These classes do no actual conversion themselves but can be used to create a complex converter from other
 *      converters to any required level of nesting.
 *      <p>
 *      Current implementations of IReloadableQuantitiesConverter and Findable that actually perform conversion are
 *      JEPConverterHolder and LookupTableConverterHolder. Both of these implementation perform the same conversion for
 *      all entries in the Source or Target lists passed to it. This is done by such classes creating a simple converter
 *      that implements IQuantityConverter and using that to construct a GenQuantitiesConverter which simple re-uses the
 *      single conversion provided by the IQuantityConverter object over all entries in the Source and Target lists
 *      presented to it in the toSource and calculateMoveables methods.
 *      <p>
 *      LookupTableQuantityConverter
 *      <p>
 *      The lookup tables must be constant in direction (either ascending or descending) in the X values. A table that is
 *      constant in direction in both X and Y can be used to convert in both directions. If a convert is to be used for converting
 *      for both source to target and target to source and the Y values change direction within a table, a coupled converter must
 *      be formed with another LookupTableQuantityConverter object. We use a mode flag to indicate how the object is to be
 *      used to control which checks of the data in the table are performed. For example InterpolationFunction will throw
 *      an exception if the xvalues change direction *
 *
 */
package gda.util.converters;
