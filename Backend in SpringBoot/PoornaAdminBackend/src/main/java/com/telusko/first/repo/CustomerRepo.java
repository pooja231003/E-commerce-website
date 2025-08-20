package com.telusko.first.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.telusko.first.model.Customer;

@Repository
public interface CustomerRepo extends JpaRepository<Customer, String>{

}
