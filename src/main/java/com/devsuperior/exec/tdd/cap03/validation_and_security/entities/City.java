package com.devsuperior.exec.tdd.cap03.validation_and_security.entities;

import java.util.ArrayList;
import java.util.List;

public class City {
	
	private Long id;
	private String name;	
	private List<Event> events = new ArrayList<>();
	
	public City() {}

	public City(Long id, String name) {
		this.id = id;
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Event> getEvents() {
		return events;
	}
}
