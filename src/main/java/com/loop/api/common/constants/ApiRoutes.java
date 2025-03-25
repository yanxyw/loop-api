package com.loop.api.common.constants;

public final class ApiRoutes {

	private ApiRoutes() {
	}

	public static final class Admin {
		public static final String BASE = "/admin";
		public static final String USERS = BASE + "/users";
	}

	public static final class Auth {
		public static final String BASE = "/auth";
		public static final String LOGIN = BASE + "/login";
		public static final String SIGNUP = BASE + "/signup";
	}

	public static final class User {
		public static final String BASE = "/users";
		public static final String ME = BASE + "/me";
	}
}
