package uk.ac.gda.eventbus.api.message;

import java.io.Serializable;

public interface IGDAMessage extends Serializable {

	GDAMessageCategory getCategory();

	String getMessage();

	Object getSourceToken();

	void setCategory(GDAMessageCategory category);

	void setMessage(String message);

	void setSourceToken(Object sourceToken);

}