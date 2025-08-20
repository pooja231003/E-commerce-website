package com.telusko.first.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.telusko.first.model.Stock;
import com.telusko.first.service.StockService;

@Controller
@CrossOrigin(origins = "http://localhost:3000")
public class StockController {

	@Autowired
	private StockService service;
	
	
	@GetMapping("/stocks")
	public ResponseEntity<List<Stock>> getStocks() {
		List<Stock> stocks = service.getStocks();
		return new ResponseEntity<>(stocks, HttpStatus.OK);
	}
	
	@GetMapping("/stock/{pid}")
	public ResponseEntity<?> getStockByPid(@PathVariable int pid) {
		Stock stock = service.getStockByPid(pid);
		if(stock!=null) {
			return new ResponseEntity<>(stock, HttpStatus.OK);
		}
		else {
			return new ResponseEntity<>("Stock not found" , HttpStatus.NOT_FOUND);
		}
	}
	
	@PostMapping("/stock/{pid}")
	public ResponseEntity<?> addStock(@PathVariable int pid,@RequestBody Stock stock){
		return service.addStock(pid, stock);
	}
	
	@PutMapping("/stock/{pid}")
	public ResponseEntity<?> updateStock(@PathVariable int pid, @RequestBody Stock stock){
		return service.updateStock(pid,stock);
	}
	
	@DeleteMapping("/stock/{pid}")
	public ResponseEntity<?> deleteStock(@PathVariable int pid){
		return service.deleteStock(pid);
	}
	
}
