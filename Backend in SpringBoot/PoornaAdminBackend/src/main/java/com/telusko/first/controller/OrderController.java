package com.telusko.first.controller;

import org.springframework.beans.factory.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.telusko.first.model.Order;
import com.telusko.first.service.OrderService;

@Controller
public class OrderController {

	@Autowired
	OrderService service;
	
	@GetMapping("/order")
	public ResponseEntity<?> getOrders(){
		return service.getOrders();
	}
	
	@GetMapping("/order/{gmail}")
	public ResponseEntity<?> getOrderByCustomer(@PathVariable String gmail){
		return service.getOrderByCustomer(gmail);
		
	}
	
	@PostMapping("/order")
	public ResponseEntity<?> addOrder(@RequestBody Order order){
		return service.addOrder(order);
	}
	
	// put and delete function should be implemented like in when ordered they can update  within 1hr and delete within 1 day
	
}
