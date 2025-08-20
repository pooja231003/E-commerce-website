package com.telusko.first.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.telusko.first.model.Product;
import com.telusko.first.model.Stock;
import com.telusko.first.repo.ProductRepo;
import com.telusko.first.repo.StockRepo;

import jakarta.transaction.Transactional;

@Service
public class StockService {
	
	@Autowired
	private StockRepo repo;
	@Autowired
	private ProductRepo prepo;

	public List<Stock> getStocks() {
		return repo.findAll();
	}
	
	public Stock getStockByPid(int pid) {
		return repo.findByProductPid(pid);
	}

	public ResponseEntity<?> addStock(int pid, Stock stock) { //pid should be fixed not entered
		Product product = prepo.findById(pid).orElse(null);
		Stock existingStock = repo.findByProductPid(pid);
		
		if (product == null) {
	        return new ResponseEntity<>("Product not found", HttpStatus.NOT_FOUND);
	    }
		
		if(existingStock!=null) {
			return new ResponseEntity<>("Stock already exists", HttpStatus.CONFLICT);
		}
		
		stock.setProduct(product);
		repo.save(stock);
		return new ResponseEntity<>("Stock added", HttpStatus.OK);
	}

	public ResponseEntity<?> updateStock(int pid, Stock stock) {
		Product product = prepo.findById(pid).orElse(null);
		Stock existingStock = repo.findByProductPid(pid);
		
		if(product==null) {
			return new ResponseEntity<>("Product not found", HttpStatus.NOT_FOUND);
		}
		
		if(existingStock==null) {
			stock.setProduct(product);
			repo.save(stock);
			return new ResponseEntity<>("Stock added", HttpStatus.OK);
		}
		
		existingStock.setQty_500ml(stock.getQty_500ml());
		existingStock.setQty_1l(stock.getQty_1l());
		existingStock.setQty_2l(stock.getQty_2l());
		existingStock.setQty_3l(stock.getQty_3l());
		existingStock.setQty_5l(stock.getQty_5l());
		repo.save(existingStock);
		return new ResponseEntity<>("Stock updated", HttpStatus.OK);
	}

	@Transactional // required when you write custom methods to save or delete which modifies the data
	public ResponseEntity<?> deleteStock(int pid) {
		Stock existingStock = repo.findByProductPid(pid);
		
		if(existingStock==null) {
			return new ResponseEntity<>("Stock not found", HttpStatus.NOT_FOUND);
		}
		repo.deleteByProductPid(pid);
		return new ResponseEntity<>("Stock deleted", HttpStatus.OK);
	}

	
}
