package com.telusko.first.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.telusko.first.model.Customer;
import com.telusko.first.repo.CustomerRepo;

import jakarta.servlet.http.HttpSession;

@Service
public class CustomerService {
	
	@Autowired
	CustomerRepo repo;

	public ResponseEntity<?> getCustomers() {
		return new ResponseEntity<>(repo.findAll(),HttpStatus.OK);
	}
	
	public ResponseEntity<?> signup(Customer customer) {
		Customer existingCustomer = repo.findById(customer.getGmail()).orElse(null);
		if(existingCustomer == null) {
			try {
				repo.save(customer);
				return new ResponseEntity<>("Customer account created", HttpStatus.OK);
			}
			catch(Exception e){
				return new ResponseEntity<>("Failed to create Customer account", HttpStatus.BAD_REQUEST);
			}
		}
		else {
			return new ResponseEntity<>("Account already exists, Log in to continue", HttpStatus.CONFLICT);
		}
	}
	
	public ResponseEntity<?> customerLogin(String gmail, String password, HttpSession session) {
		Customer customer = repo.findById(gmail).orElse(null);
		
		if(customer != null ) {
			if(customer.getGmail().equals(gmail) && customer.getPassword().equals(password)) {
				session.setAttribute("user_id", gmail);
				return new ResponseEntity<>("Logged in to account", HttpStatus.OK);
			}
			else {
				return new ResponseEntity<>("Incorrect gmail or password",HttpStatus.CONFLICT);
			}
		}
		else {
			return new ResponseEntity<>("Account not found, Create new Account", HttpStatus.NOT_FOUND);
		}
	}
	
	
	public ResponseEntity<?> getCustomer(String gmail, HttpSession session) {
		String user_id = (String) session.getAttribute("user_id");
		Customer customer = repo.findById(gmail).orElse(null);
		if(customer!=null) {
			return new ResponseEntity<>(customer,HttpStatus.OK);
		}
		if(user_id == null) {
			return new ResponseEntity<>("Log into your account", HttpStatus.UNAUTHORIZED);
		}
		else {
			return new ResponseEntity<>("Customer does not exists", HttpStatus.NOT_FOUND);
		}
	}

	public ResponseEntity<?> deleteCustomer(String gmail) {
		
		Customer existingCustomer = repo.findById(gmail).orElse(null);
		if(existingCustomer != null) {
			try {
				repo.deleteById(gmail);
				return new ResponseEntity<>("Account deleted", HttpStatus.OK);
			}
			catch(Exception e){
				return new ResponseEntity<>("Failed to delete account", HttpStatus.BAD_REQUEST);
			}
		}
		else {
			return new ResponseEntity<>("Account does not exists", HttpStatus.NOT_FOUND);
		}
	}

	public ResponseEntity<?> customerLogout(HttpSession session) {
		String user_id = (String) session.getAttribute("user_id");
		
		if(user_id != null) {
			session.invalidate();
			return new ResponseEntity<>("Logged out", HttpStatus.OK);
		}
		else {
			return new ResponseEntity<>("Log in to your account",HttpStatus.NOT_FOUND);
		}
	}
	

}
