package com.telusko.first.service;

import org.springframework.stereotype.Service;

@Service
public class LoginService {

	public boolean checkLoginAuth(String username, String password) {
		String	HARD_CODED_USERNAME = "admin";
		String 	HARD_CODED_PASSWORD = "admin";
		
		return username.equals(HARD_CODED_USERNAME) && password.equals(HARD_CODED_PASSWORD);
	}


}
