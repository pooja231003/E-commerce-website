package com.telusko.first.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.telusko.first.model.Customer;
import com.telusko.first.service.CustomerService;

import jakarta.servlet.http.HttpSession;

@RestController
public class CustomerController {
	
	@Autowired
	CustomerService service;

	@PostMapping("/signup")
	public ResponseEntity<?> signup(@RequestBody Customer customer){
		return service.signup(customer);
	}
	
	@GetMapping("/login")
	public ResponseEntity<?> customerLogin(@RequestParam String gmail, @RequestParam String password, HttpSession session){
		return service.customerLogin(gmail, password, session);
	}
	
	@GetMapping("/logout")
	public ResponseEntity<?> customerLogout(HttpSession session){
		return service.customerLogout(session);
	}
	
	@GetMapping("/customer/{gmail}")
	public ResponseEntity<?> getCustomer(@PathVariable String gmail, HttpSession session){
		return service.getCustomer(gmail, session);
	}
	
	@GetMapping("/customers")
	public ResponseEntity<?> getCustomers(){
		return service.getCustomers();
	}
	
	@DeleteMapping("/customer/{gmail}")
	public ResponseEntity<?> deleteCustomer(@PathVariable String gmail){
		return service.deleteCustomer(gmail);
	}
	
	
}
