package uk.ac.diamond.daq.guigenerator.test;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.richbeans.api.generator.RichbeansAnnotations.MaximumValue;
import org.eclipse.richbeans.api.generator.RichbeansAnnotations.MinimumValue;
import org.eclipse.richbeans.api.generator.RichbeansAnnotations.UiTooltip;
import org.eclipse.richbeans.api.generator.RichbeansAnnotations.Units;
import org.metawidget.inspector.annotation.UiReadOnly;

class TestBean {

	public enum ExampleEnum {
		FIRST_VALUE("Value 1"), SECOND_VALUE("Value 2");
		private String label;
		private ExampleEnum(String label) {
			this.label = label;
		}
		@Override
		public String toString() {
			return label;
		}
	}

	public static final String STRING_FIELD_TOOLTIP = "String field tooltip";
	public static final String INT_FIELD_MAX_VALUE_STRING = "25";
	public static final String INT_FIELD_MIN_VALUE_STRING = "-10";
	private static final String DOUBLE_FIELD_MIN_VALUE_STRING = "345.5";
	private static final String DOUBLE_FIELD_MAX_VALUE_STRING = "711.3";

	private String hiddenStringField;
	private String stringField;
	private String uiReadOnlyStringField;
	private int intField;
	private double doubleField = 450.3;
	private ExampleEnum type;

	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(listener);
	}

	// This field should be hidden by XML metadata - do not add @UiHidden!
	public String getHiddenStringField() {
		return hiddenStringField;
	}
	public void setHiddenStringField(String hiddenStringField) {
		this.hiddenStringField = hiddenStringField;
	}

	public String getStringFieldWithGetterOnly() {
		return "String field with getter only";
	}

	@UiTooltip(STRING_FIELD_TOOLTIP)
	public String getStringField() {
		return stringField;
	}
	public void setStringField(String stringField) {
		Object oldValue = this.stringField;
		this.stringField = stringField;
		pcs.firePropertyChange("stringField", oldValue, this.stringField);
	}

	@UiReadOnly
	public String getUiReadOnlyStringField() {
		return uiReadOnlyStringField;
	}
	public void setUiReadOnlyStringField(String uiReadOnlyStringField) {
		Object oldValue = this.uiReadOnlyStringField;
		this.uiReadOnlyStringField = uiReadOnlyStringField;
		pcs.firePropertyChange("uiReadOnlyStringField", oldValue, this.uiReadOnlyStringField);
	}

	@MinimumValue(INT_FIELD_MIN_VALUE_STRING)
	@MaximumValue(INT_FIELD_MAX_VALUE_STRING)
	@Units("eV")
	public int getIntField() {
		return intField;
	}
	public void setIntField(int intField) {
		Object oldValue = this.intField;
		this.intField = intField;
		pcs.firePropertyChange("intField", oldValue, this.intField);
	}

	@MinimumValue(DOUBLE_FIELD_MIN_VALUE_STRING)
	@MaximumValue(DOUBLE_FIELD_MAX_VALUE_STRING)
	@Units("Hz")
	public double getDoubleField() {
		return doubleField;
	}
	public void setDoubleField(double doubleField) {
		Object oldValue = this.doubleField;
		this.doubleField = doubleField;
		pcs.firePropertyChange("doubleField", oldValue, this.doubleField);
	}

	public ExampleEnum getType() {
		return type;
	}
	public void setType(ExampleEnum type) {
		Object oldValue = this.type;
		this.type = type;
		pcs.firePropertyChange("type", oldValue, this.type);
	}
}