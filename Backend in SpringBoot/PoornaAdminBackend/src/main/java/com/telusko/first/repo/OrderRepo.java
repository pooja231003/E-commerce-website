package com.telusko.first.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.telusko.first.model.Order;

@Repository
public interface OrderRepo extends JpaRepository<Order,Integer>{

	@Query("SELECT o FROM Order o WHERE o.customer.gmail = :gmail")
	public List<Order> getOrdersbyCustomer_Gmail(String gmail);
}
