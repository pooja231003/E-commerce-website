package com.telusko.first.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.telusko.first.model.Product;

public interface ProductRepo extends JpaRepository<Product, Integer> {

}
