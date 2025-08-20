package com.telusko.first.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


import com.telusko.first.model.Cart;

@Repository
public interface CartRepo extends JpaRepository<Cart, Integer> {

	@Query("SELECT c FROM Cart c WHERE c.customer.gmail = :gmail")
	List<Cart> findAllByCustomer_Gmail(String gmail);
	
}
