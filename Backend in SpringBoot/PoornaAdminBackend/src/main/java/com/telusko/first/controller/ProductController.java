package com.telusko.first.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.telusko.first.model.Product;
import com.telusko.first.service.ProductService;

import jakarta.servlet.annotation.MultipartConfig;

@Controller
@MultipartConfig
@CrossOrigin(origins = "http://localhost:3000")
public class ProductController {
	
	@Autowired
	private ProductService service;
	
	@GetMapping("/products")
	public ResponseEntity<List<Product>> getProducts() {
		List<Product> prod = service.getProducts(); 
		return new ResponseEntity<>(prod, HttpStatus.OK);
	}
	
	@GetMapping("/product/{id}")
	public ResponseEntity<?> getProductById(@PathVariable int id){
		Product prod =service.getProductById(id);
		
		if(prod!=null) {
			return new ResponseEntity<>(prod, HttpStatus.OK);
		}
		else {
			return new ResponseEntity<>("Product not found", HttpStatus.NOT_FOUND);
		}
	}
	
	@GetMapping("/product/{productID}/image")
	public ResponseEntity<?> getImageByProdID(@PathVariable int productID){
		
		Product product = service.getProductById(productID);
		if(product!=null) {
			byte[] imageFile = product.getImageDate();
			
			return ResponseEntity
					.ok()
					.contentType(MediaType.valueOf(product.getImageType()))
					.body(imageFile);
		}
		else {
			return new ResponseEntity<>("Product not found", HttpStatus.NOT_FOUND);
		}
		
	}
	
	@GetMapping("/product/get_prices/{id}")
	public ResponseEntity<?> getPrices(@PathVariable int id){
		return service.getPrices(id);
	}
	
	@PostMapping(value = "/product")
	public ResponseEntity<?> addProduct(@RequestPart Product product, @RequestPart MultipartFile imageFile) {
	
	    if (imageFile.isEmpty()) {
	        return new ResponseEntity<>("Image is required", HttpStatus.BAD_REQUEST);
	    }
	    
		return service.addProduct(product, imageFile);
	}
	
	@PutMapping("/product/{id}")
	public ResponseEntity<?> updateProduct(@PathVariable int id, @RequestPart("product") Product product, @RequestPart(value = "imageFile", required = false) MultipartFile imageFile){
			try {
				if(imageFile.isEmpty()) {
					return service.updateProduct(id, product);
				}
				return service.updateProduct(id, product, imageFile);
			} catch (IOException e) {
				return new ResponseEntity<>("Product not found", HttpStatus.NOT_FOUND);
			}
	}
	
	@DeleteMapping("/product/{id}")
	public ResponseEntity<?> deleteProduct(@PathVariable int id){
		try {
			service.deleteProduct(id);
			return new ResponseEntity<>("Product deleted",HttpStatus.OK);
		} 
		catch (IOException e) {
			return new ResponseEntity<>("Product not found",HttpStatus.NOT_FOUND);
		}
	}
}
