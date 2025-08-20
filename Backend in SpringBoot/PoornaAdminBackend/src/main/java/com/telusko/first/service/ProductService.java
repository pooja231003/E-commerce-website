package com.telusko.first.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.telusko.first.model.Product;
import com.telusko.first.repo.ProductRepo;

import jakarta.transaction.Transactional;

@Service
public class ProductService {
	
	@Autowired
	private ProductRepo repo;

	public List<Product> getProducts() {
		return repo.findAll();
	}

	public Product getProductById(int id) {
		return repo.findById(id).orElse(null);
	}

	@Transactional
	public ResponseEntity<?> addProduct(Product product, MultipartFile imageFile){
		Product existingProd = repo.findById(product.getPid()).orElse(null);
		if(existingProd == null) {
			product.setImageName(imageFile.getOriginalFilename());
			product.setImageType(imageFile.getContentType());
			try {
				product.setImageDate(imageFile.getBytes());
			} catch (IOException e) {
				return new ResponseEntity<>("Failed to add product", HttpStatus.INTERNAL_SERVER_ERROR);
			}
			repo.save(product);
			return new ResponseEntity<>("Product added", HttpStatus.OK);
		}
		else {
			return new ResponseEntity<>("Product already exists", HttpStatus.NOT_FOUND);
		}
	}

	public ResponseEntity<?> updateProduct(int id, Product product, MultipartFile imageFile) throws IOException {
		Product prod = repo.findById(id).orElse(null);
		if(prod!=null) {
				product.setImageName(imageFile.getOriginalFilename());
				product.setImageType(imageFile.getContentType());
				product.setImageDate(imageFile.getBytes());
				repo.save(product);
				return new ResponseEntity<>("Product updated", HttpStatus.OK);
		}
		else {
			return new ResponseEntity<>("Product not found", HttpStatus.OK);
		}
	}
	
	public ResponseEntity<?> updateProduct(int id, Product product) {
		Product prod = repo.findById(id).orElse(null);
		if(prod!=null) {
			product.setImageName(prod.getImageName());
			product.setImageType(prod.getImageType());
			product.setImageDate(prod.getImageDate());
			repo.save(product);
			System.out.print(product);
			return new ResponseEntity<>("Product updated", HttpStatus.OK);
		}
		else {
			return new ResponseEntity<>("Product not found", HttpStatus.OK);
		}
	}
	

	public void deleteProduct(int id) throws IOException{
		repo.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
		repo.deleteById(id);
	}

	public ResponseEntity<?> getPrices(int id) {
		Product product = repo.findById(id).orElse(null);
		
		if(product == null) {
			return new ResponseEntity<>("Product not found", HttpStatus.NOT_FOUND);
		}
		
		Map<String, Integer> prices = new HashMap<>();
		
		int cost = product.getPrice();
		
		if(product.isQuantity_half()) {
			prices.put("500ml", (int) (cost*0.5));
		}
		
		if(product.isQuantity1()) {
			prices.put("1l", cost);
		}
		
		if(product.isQuantity2()) {
			prices.put("2l", cost*2);
		}
		
		if(product.isQuantity3()) {
			prices.put("3l", cost*3);
		}
		
		if(product.isQuantity5()) {
			prices.put("5l", cost*5);
		}
		
		return new ResponseEntity<>(prices,HttpStatus.OK);
	}


}
