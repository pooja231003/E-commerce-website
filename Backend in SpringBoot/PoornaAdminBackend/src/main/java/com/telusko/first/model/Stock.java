package com.telusko.first.model;

import jakarta.persistence.*;

@Entity
public class Stock {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@OneToOne
    @JoinColumn(name = "pid", referencedColumnName = "pid", unique = true)
    private Product product;

    private Double qty_500ml = 0.0;
    private Double qty_1l = 0.0;
    private Double qty_2l = 0.0;
    private Double qty_3l = 0.0;
    private Double qty_5l = 0.0;
    
    
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Product getProduct() {
		return product;
	}
	public void setProduct(Product product) {
		this.product = product;
	}
	public Double getQty_500ml() {
		return qty_500ml;
	}
	public void setQty_500ml(Double qty_500ml) {
		this.qty_500ml = qty_500ml;
	}
	public Double getQty_1l() {
		return qty_1l;
	}
	public void setQty_1l(Double qty_1l) {
		this.qty_1l = qty_1l;
	}
	public Double getQty_2l() {
		return qty_2l;
	}
	public void setQty_2l(Double qty_2l) {
		this.qty_2l = qty_2l;
	}
	public Double getQty_3l() {
		return qty_3l;
	}
	public void setQty_3l(Double qty_3l) {
		this.qty_3l = qty_3l;
	}
	public Double getQty_5l() {
		return qty_5l;
	}
	public void setQty_5l(Double qty_5l) {
		this.qty_5l = qty_5l;
	}
    
    
}
