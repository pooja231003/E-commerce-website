package com.telusko.first.model;

import java.math.BigDecimal;

import jakarta.persistence.*;

@Entity
public class Cart {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int cid;
	
	@ManyToOne
	@JoinColumn(name = "gmail", referencedColumnName = "gmail")
	private Customer customer;
	
	@ManyToOne
	@JoinColumn(name = "pid", referencedColumnName = "pid")
	private Product product;
	
	private String pkg_quantity;
	private BigDecimal price;
	private int no_quantity;
	
	
	public int getCid() {
		return cid;
	}
	public void setCid(int cid) {
		this.cid = cid;
	}
	public Customer getCustomer() {
		return customer;
	}
	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
	public Product getProduct() {
		return product;
	}
	public void setProduct(Product product) {
		this.product = product;
	}
	public String getPkg_quantity() {
		return pkg_quantity;
	}
	public void setPkg_quantity(String pkg_quantity) {
		this.pkg_quantity = pkg_quantity;
	}
	public BigDecimal getPrice() {
		return price;
	}
	public void setPrice(BigDecimal price) {
		this.price = price;
	}
	public int getNo_quantity() {
		return no_quantity;
	}
	public void setNo_quantity(int no_quantity) {
		this.no_quantity = no_quantity;
	}
}
