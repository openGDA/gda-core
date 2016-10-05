// Metawidget
//
// This file is dual licensed under both the LGPL
// (http://www.gnu.org/licenses/lgpl-2.1.html) and the EPL
// (http://www.eclipse.org/org/documents/epl-v10.php). As a
// recipient of Metawidget, you may choose to receive it under either
// the LGPL or the EPL.
//
// Commercial licenses are also available. See http://metawidget.org
// for details.

package uk.ac.diamond.daq.guigenerator;

import org.eclipse.core.databinding.conversion.IConverter;
import org.metawidget.util.simple.ObjectUtils;

/**
 * This class is the same as the one provided with metawidget, its only required as the getConvertors method is
 * protected. Configures a DataBindingBindingProcessor prior to use. Once instantiated, WidgetProcessors are immutable.
 *
 * @author <a href="http://kennardconsulting.com">Richard Kennard</a>
 */
public class TwoWayDataBindingProcessorConfig {

	//
	// Private members
	//
	private IConverter[] mConverters;

	//
	// Public methods
	//

	/**
	 * Sets Converters for this DataBindingProcessor.
	 *
	 * @return this, as part of a fluent interface
	 */
	public TwoWayDataBindingProcessorConfig setConverters(IConverter... converters) {

		mConverters = converters;

		return this;
	}

	@Override
	public boolean equals(Object that) {

		if (this == that) {
			return true;
		}

		if (!ObjectUtils.nullSafeClassEquals(this, that)) {
			return false;
		}

		if (!ObjectUtils.nullSafeEquals(mConverters, ((TwoWayDataBindingProcessorConfig) that).mConverters)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {

		int hashCode = 1;
		hashCode = 31 * hashCode + ObjectUtils.nullSafeHashCode(mConverters);

		return hashCode;
	}

	//
	// Protected methods
	//
	protected IConverter[] getConverters() {

		return mConverters;
	}
}
