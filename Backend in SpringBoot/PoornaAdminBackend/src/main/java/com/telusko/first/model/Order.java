package com.telusko.first.model;

import java.math.BigDecimal;
import java.sql.Date;

import jakarta.persistence.*;

@Entity
@Table(name = "order_table") 
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int oid;
	
	@ManyToOne
    @JoinColumn(name = "gmail", referencedColumnName = "gmail")
    private Customer customer;
	
	@ManyToOne
	@JoinColumn(name = "pid", referencedColumnName = "pid")
	private Product product;
	
	private String pkg_quantity;
	private BigDecimal price;
	private int no_quantity;
	private Date date;
	private boolean delivery;
	
	
	public int getId() {
		return oid;
	}
	public void setId(int id) {
		this.oid = id;
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
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public boolean isDelivery() {
		return delivery;
	}
	public void setDelivery(boolean delivery) {
		this.delivery = delivery;
	}
	
	
	
	
}
