package com.sismics.rest.util;

import java.io.*;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.util.UnvalUser;

public class UserValidation extends ValidationHandler {
	public static void validate(UnvalUser u) throws ClientException {
		ValidationHandler emailValidator = new ValidationHandler() {
			@Override
			public void handle(UnvalUser u) throws ClientException {
				String name = "email";
				u.setEmail(length(u.getEmail(), name, 3, 50, false));
				email(u.getEmail(), name);
				System.out.println("email handled");
				super.handle(u);
			}
		};
		ValidationHandler usernameValidator = new ValidationHandler() {
			@Override
			public void handle(UnvalUser u) throws ClientException {
				String name = "username";
				u.setUsername(length(u.getUsername(), name, 3, 50, false));
				alphanumeric(u.getUsername(), "username");
				System.out.println("username handled");
				super.handle(u);
			}
		};

		ValidationHandler passwordValidator = new ValidationHandler() {
			@Override
			public void handle(UnvalUser u) throws ClientException {
				u.setPassword(length(u.getPassword(), "password", 3, 50, false));
				System.out.println("password handled");
				super.handle(u);
			}
		};
		usernameValidator.setNextHandler(passwordValidator);
		passwordValidator.setNextHandler(emailValidator);

		usernameValidator.handle(u);

	}

}
