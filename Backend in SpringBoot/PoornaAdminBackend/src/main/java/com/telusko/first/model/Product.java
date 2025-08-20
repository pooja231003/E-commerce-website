package com.telusko.first.model;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
	
	@Id
	private int pid;
	private String name;
	private int price;
	private boolean quantity_half;
	private boolean quantity1;
	private boolean quantity2;
	private boolean quantity3;
	private boolean quantity5;
	private boolean available;
	
	@Column(length = 1000)
	private String description;

	
	private String imageName;
	private String imageType;
	@Lob
	private byte[] imageDate;
	
	
	public int getPid() {
		return pid;
	}
	public void setPid(int pid) {
		this.pid = pid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getPrice() {
		return price;
	}
	public void setPrice(int price) {
		this.price = price;
	}
	public boolean isQuantity_half() {
		return quantity_half;
	}
	public void setQuantity_half(boolean quantity_half) {
		this.quantity_half = quantity_half;
	}
	public boolean isQuantity1() {
		return quantity1;
	}
	public void setQuantity1(boolean quantity1) {
		this.quantity1 = quantity1;
	}
	public boolean isQuantity2() {
		return quantity2;
	}
	public void setQuantity2(boolean quantity2) {
		this.quantity2 = quantity2;
	}
	public boolean isQuantity3() {
		return quantity3;
	}
	public void setQuantity3(boolean quantity3) {
		this.quantity3 = quantity3;
	}
	public boolean isQuantity5() {
		return quantity5;
	}
	public void setQuantity5(boolean quantity5) {
		this.quantity5 = quantity5;
	}
	public boolean isAvailable() {
		return available;
	}
	public void setAvailable(boolean available) {
		this.available = available;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getImageName() {
		return imageName;
	}
	public void setImageName(String imageName) {
		this.imageName = imageName;
	}
	public String getImageType() {
		return imageType;
	}
	public void setImageType(String imageType) {
		this.imageType = imageType;
	}
	public byte[] getImageDate() {
		return imageDate;
	}
	public void setImageDate(byte[] imageDate) {
		this.imageDate = imageDate;
	}
	
	
}
