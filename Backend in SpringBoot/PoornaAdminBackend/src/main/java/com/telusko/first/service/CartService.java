package com.telusko.first.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.telusko.first.model.Cart;
import com.telusko.first.model.Customer;
import com.telusko.first.model.Product;
import com.telusko.first.repo.CartRepo;
import com.telusko.first.repo.CustomerRepo;
import com.telusko.first.repo.ProductRepo;

import jakarta.servlet.http.HttpSession;

@Service
public class CartService {

	@Autowired
	CartRepo repo;
	
	@Autowired
	private CustomerRepo customerRepo;

	@Autowired
	private ProductRepo productRepo;
	
	@Autowired
    Cart cart;
	
	public ResponseEntity<?> getCartItems(HttpSession session) {
		String user_id = (String) session.getAttribute("user_id");
		
		if(user_id == null) {
			return new ResponseEntity<>("Log in to view Cart Items",HttpStatus.CONFLICT);
		}	
		
		else {
			return new ResponseEntity<>(repo.findAllByCustomer_Gmail(user_id),HttpStatus.OK);
		}
	}
	
	public ResponseEntity<?> getCartItemByID(int cid, HttpSession session) {
		String user_id = (String) session.getAttribute("user_id");
		
		if(user_id == null) {
			return new ResponseEntity<>("Log in to view Cart Items",HttpStatus.CONFLICT);
		}	
		
		else {
			return new ResponseEntity<>(repo.findById(cid),HttpStatus.OK);
		}
	}

	public ResponseEntity<?> addToCart(Cart cartRequest, HttpSession session) {
	    String user_id = (String) session.getAttribute("user_id");

	    if (user_id == null) {
	        return new ResponseEntity<>("Log in to add items to cart", HttpStatus.NOT_FOUND);
	    }

	    // Fetch the customer from DB
	    Customer customer = customerRepo.findById(user_id).orElse(null);
	    if (customer == null) {
	        return new ResponseEntity<>("Customer not found", HttpStatus.NOT_FOUND);
	    }

	    // Fetch the product from DB
	    if (cartRequest.getProduct() == null || cartRequest.getProduct().getPid() == 0) {
	        return new ResponseEntity<>("Product ID is missing", HttpStatus.BAD_REQUEST);
	    }
	    Product product = productRepo.findById(cartRequest.getProduct().getPid()).orElse(null);
	    if (product == null) {
	        return new ResponseEntity<>("Product not found", HttpStatus.NOT_FOUND);
	    }

	    // Create and save new Cart entry
	    cart.setCustomer(customer);
	    cart.setProduct(product);
	    cart.setPkg_quantity(cartRequest.getPkg_quantity());
	    cart.setPrice(cartRequest.getPrice());
	    cart.setNo_quantity(cartRequest.getNo_quantity());

	    repo.save(cart);

	    return new ResponseEntity<>("Item added to cart", HttpStatus.OK);
	}

	public ResponseEntity<?> updateCart(Cart cartRequest, HttpSession session) {
		String user_id = (String) session.getAttribute("user_id");

	    if (user_id == null) {
	        return new ResponseEntity<>("Log in to add items to cart", HttpStatus.NOT_FOUND);
	    }

	    // Fetch the customer from DB
	    Customer customer = customerRepo.findById(user_id).orElse(null);
	    if (customer == null) {
	        return new ResponseEntity<>("Customer not found", HttpStatus.NOT_FOUND);
	    }

	    // Fetch the product from DB
	    if (cartRequest.getProduct() == null || cartRequest.getProduct().getPid() == 0) {
	        return new ResponseEntity<>("Product ID is missing", HttpStatus.BAD_REQUEST);
	    }
	    Product product = productRepo.findById(cartRequest.getProduct().getPid()).orElse(null);
	    if (product == null) {
	        return new ResponseEntity<>("Product not found", HttpStatus.NOT_FOUND);
	    }

	    // Create and save new Cart entry
	    int cid = cartRequest.getCid();
	    Cart cart = repo.findById(cid).orElse(null);
	    
	    if(cart == null) {
	    	return new ResponseEntity<>("Cart item not found",HttpStatus.NOT_FOUND);
	    }
	    else {
	    	cart.setCustomer(customer);
		    cart.setProduct(product);
		    cart.setPkg_quantity(cartRequest.getPkg_quantity());
		    cart.setPrice(cartRequest.getPrice());
		    cart.setNo_quantity(cartRequest.getNo_quantity());

		    repo.save(cart);

		    return new ResponseEntity<>("Item updated", HttpStatus.OK);
	    }
	}

	public ResponseEntity<?> deleteCart(int cid, HttpSession session) {
		String user_id = (String) session.getAttribute("user_id");
		
		if (user_id == null) {
	        return new ResponseEntity<>("Log in to delete items from cart", HttpStatus.NOT_FOUND);
	    }
		
		Cart cart = repo.findById(cid).orElse(null);
		
		if(cart == null) {
	    	return new ResponseEntity<>("Cart item not found",HttpStatus.NOT_FOUND);
	    }
		else {
			repo.deleteById(cid);
			return new ResponseEntity<>("Cart item deleted",HttpStatus.OK);
		}
	}

	
}
