package com.telusko.first.model;

import java.math.BigDecimal;

import jakarta.persistence.*;

@Entity
public class Customer {

	@Id
	private String gmail;
	private String password;
	private String name;
	private String address;
	private String city;
	private String state;
	private BigDecimal pincode;
	private BigDecimal contact;
	
	
	public String getGmail() {
		return gmail;
	}
	public void setGmail(String gmail) {
		this.gmail = gmail;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public BigDecimal getPincode() {
		return pincode;
	}
	public void setPincode(BigDecimal pincode) {
		this.pincode = pincode;
	}
	public BigDecimal getContact() {
		return contact;
	}
	public void setContact(BigDecimal contact) {
		this.contact = contact;
	}
	
	
}
