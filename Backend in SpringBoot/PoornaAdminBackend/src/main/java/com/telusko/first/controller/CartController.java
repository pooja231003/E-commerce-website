package com.telusko.first.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.telusko.first.model.Cart;
import com.telusko.first.service.CartService;

import jakarta.servlet.http.HttpSession;

@Controller
public class CartController {
	
	@Autowired
	CartService service;

	@GetMapping("/cart")
	public ResponseEntity<?> getCartItems(HttpSession session){
		return service.getCartItems(session);
	}
	
	@GetMapping("/cart/id")
	public ResponseEntity<?> getCartItem(@PathVariable int cid, HttpSession session){
		return service.getCartItemByID(cid, session);
	}
	
	@PostMapping("/cart")
	public ResponseEntity<?> addToCart(@RequestBody Cart cart,HttpSession session){
		return service.addToCart(cart,session);
	}
	
	@PutMapping("/cart")
	public ResponseEntity<?> updateCart(@RequestBody Cart cart,HttpSession session){
		return service.updateCart(cart,session);
	}
	
	@DeleteMapping("/cart/id")
	public ResponseEntity<?> deleteCart(@PathVariable int cid, HttpSession session ){
		return service.deleteCart(cid,session);
	}
}
