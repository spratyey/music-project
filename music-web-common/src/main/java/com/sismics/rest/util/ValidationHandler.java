package com.sismics.rest.util;

import com.google.common.base.Strings;
import com.sismics.rest.exception.ClientException;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import java.text.MessageFormat;
import java.util.Date;
import java.util.regex.Pattern;
import com.sismics.rest.util.UnvalUser;

public abstract class ValidationHandler {
	private ValidationHandler nextHandler;

	public ValidationHandler setNextHandler(ValidationHandler handler) {
		this.nextHandler = handler;
		return handler;
	}

	public void handle(UnvalUser u) throws ClientException {
		if (nextHandler != null) {
			nextHandler.handle(u);
		}
	}

	protected String length(String s, String name, Integer lengthMin, Integer lengthMax, boolean nullable)
			throws ClientException {
		s = StringUtils.strip(s);
		if (nullable && StringUtils.isEmpty(s)) {
			return s;
		}
		if (s == null) {
			throw new ClientException("ValidationError", MessageFormat.format("{0} must be set", name));
		}
		if (lengthMin != null && s.length() < lengthMin) {
			throw new ClientException("ValidationError",
					MessageFormat.format("{0} must be more than {1} characters", name, lengthMin));
		}
		if (lengthMax != null && s.length() > lengthMax) {
			throw new ClientException("ValidationError",
					MessageFormat.format("{0} must be more than {1} characters", name, lengthMax));
		}
		return s;
	}

	protected void email(String s, String name) throws ClientException {
		if (!Pattern.compile(".+@.+").matcher(s).matches()) {
			throw new ClientException("ValidationError", MessageFormat.format("{0} must be an email", "email"));
		}
	}

	protected void alphanumeric(String s, String name) throws ClientException {
		if (!Pattern.compile("[a-zA-Z0-9_]+").matcher(s).matches()) {
			throw new ClientException("ValidationError",
					MessageFormat.format("{0} must have only alphanumeric or underscore characters", name));
		}
	}
}