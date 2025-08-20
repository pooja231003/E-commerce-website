package com.telusko.first.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.telusko.first.service.LoginService;

@Controller
@CrossOrigin(origins = "http://localhost:3000")
public class LoginController {

	@Autowired
	private LoginService service;
	
	@PostMapping("/admin/login")
	public ResponseEntity<?> checkLoginAuth(@RequestParam String username, @RequestParam String password) {
		
		if(service.checkLoginAuth(username, password)) {
			return new ResponseEntity<>("success",HttpStatus.OK);
		}
		else {
			return new ResponseEntity<>("Invalid Username or Password",HttpStatus.NOT_FOUND);
		}
	}
}
