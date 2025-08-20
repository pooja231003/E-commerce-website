package com.telusko.first.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.telusko.first.model.Stock;

@Repository
public interface StockRepo extends JpaRepository<Stock, Integer>{

	Stock findByProductPid(int pid);
	
	void deleteByProductPid(int pid);

}
