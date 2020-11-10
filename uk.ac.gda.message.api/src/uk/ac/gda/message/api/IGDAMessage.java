package uk.ac.gda.message.api;

import java.io.Serializable;

public interface IGDAMessage extends Serializable {

	GDAMessageCategory getCategory();

	String getMessage();

	Serializable getSourceToken();

	void setCategory(GDAMessageCategory category);

	void setMessage(String message);

	void setSourceToken(Serializable sourceToken);

}