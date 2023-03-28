package com.sismics.rest.util;

public class UnvalUser {
	private String username;
	private String password;
	private String email;

	public UnvalUser() {
	}

	public String getUsername() {
		return username;
	}

	/**
	 * Setter of username.
	 *
	 * @param username username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Getter of password.
	 *
	 * @return password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Setter of password.
	 *
	 * @param password password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Getter of email.
	 *
	 * @return email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Setter of email.
	 *
	 * @param email email
	 */
	public void setEmail(String email) {
		this.email = email;
	}

}
