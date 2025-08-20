package com.telusko.first.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


import com.telusko.first.model.Order;
import com.telusko.first.model.Product;
import com.telusko.first.model.Customer;
import com.telusko.first.repo.CustomerRepo;
import com.telusko.first.repo.OrderRepo;
import com.telusko.first.repo.ProductRepo;

@Service
public class OrderService {
	
	@Autowired
	OrderRepo repo;
	
	@Autowired
	CustomerRepo crepo;
	
	@Autowired
	ProductRepo prepo;
	
	@Autowired
	Order o;

	public ResponseEntity<?> getOrders() {

		List<Order> orders = repo.findAll();
		
		if(orders == null) {
			return new ResponseEntity<>("No Orders to view",HttpStatus.OK);
		}
		else {
			return new ResponseEntity<>("All Orders fetched",HttpStatus.OK);
		}
		
	}

	public ResponseEntity<?> getOrderByCustomer(String gmail) {
		
		Customer customer = crepo.findById(gmail).orElse(null);
		
		if(customer == null) {
			return new ResponseEntity<>("No customer found", HttpStatus.NOT_FOUND);
		}
		else {
			return new ResponseEntity<>(repo.getOrdersbyCustomer_Gmail(gmail), HttpStatus.OK);
			}
	}

	public ResponseEntity<?> addOrder(Order order) {
		
		Customer customer = crepo.findById(order.getCustomer().getGmail()).orElse(null);
		Product product = prepo.findById(order.getProduct().getPid()).orElse(null);
		
		if(customer == null) {
			return new ResponseEntity<>("Account not found", HttpStatus.NOT_FOUND);
		}
		if(product == null) {
			return new ResponseEntity<>("Product not found", HttpStatus.NOT_FOUND);
		}
		
		o.setCustomer(customer);
		o.setDate(order.getDate());
		o.setDelivery(false);
		o.setNo_quantity(order.getNo_quantity());
		o.setPkg_quantity(order.getPkg_quantity());
		o.setPrice(order.getPrice());
		o.setProduct(product);
		return new ResponseEntity<>("Order Placed", HttpStatus.OK);
	}

}
