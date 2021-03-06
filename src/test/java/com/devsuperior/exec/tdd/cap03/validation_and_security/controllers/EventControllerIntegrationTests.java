package com.devsuperior.exec.tdd.cap03.validation_and_security.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.exec.tdd.cap03.validation_and_security.dto.EventDTO;
import com.devsuperior.exec.tdd.cap03.validation_and_security.tests.TokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class EventControllerIntegrationTests {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;	
	
	@Autowired
	private TokenUtil tokenUtil;

	private String clientUsername;
	private String clientPassword;
	private String adminUsername;
	private String adminPassword;
	
	@BeforeEach
	void setUp() throws Exception {
		
		clientUsername = "ana@gmail.com";
		clientPassword = "123456";
		adminUsername = "bob@gmail.com";
		adminPassword = "123456";
	}

	@Test
	public void insertShouldReturnUnauthorizedWhenNoUserLogged() throws Exception {

		EventDTO eventDTO = new EventDTO(null, "Expo XP", LocalDate.of(2021, 5, 18), "https://expoxp.com.br", 1L);
		String jsonBody = objectMapper.writeValueAsString(eventDTO);
		
		mockMvc.perform(post("/events")
				.content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isUnauthorized());
	}
	
	@Test
	public void insertShouldCreatedResourceWhenClientLoggedAndCorrectData() throws Exception {

		String accessToken = tokenUtil.obtainAccessToken(mockMvc, clientUsername, clientPassword);
		LocalDate nextMonth = LocalDate.now().plusMonths(1L);
		
		EventDTO eventDTO = new EventDTO(null, "Expo XP", nextMonth, "https://expoxp.com.br", 1L);
		String jsonBody = objectMapper.writeValueAsString(eventDTO);
		
		mockMvc.perform(post("/events")
				.header("Authorization", "Bearer " + accessToken)
				.content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))		
		.andExpect(status().isCreated())
		.andExpect(jsonPath("$.id").exists())
		.andExpect(jsonPath("$.name").value("Expo XP"))
		.andExpect(jsonPath("$.date").value(nextMonth.toString()))
		.andExpect(jsonPath("$.url").value("https://expoxp.com.br"))
		.andExpect(jsonPath("$.cityId").value(1L));
	}
	
	@Test
	public void insertShouldInsertResourceWhenAdminLoggedAndCorrectData() throws Exception {

		String accessToken = tokenUtil.obtainAccessToken(mockMvc, adminUsername, adminPassword);
		LocalDate nextMonth = LocalDate.now().plusMonths(1L);
		
		EventDTO eventDTO = new EventDTO(null, "Expo XP", nextMonth, "https://expoxp.com.br", 1L);
		String jsonBody = objectMapper.writeValueAsString(eventDTO);
		
		mockMvc.perform(post("/events")
				.header("Authorization", "Bearer " + accessToken)
				.content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))		
		.andExpect(status().isCreated())
		.andExpect(jsonPath("$.id").exists())
		.andExpect(jsonPath("$.name").value("Expo XP"))
		.andExpect(jsonPath("$.date").value(nextMonth.toString()))
		.andExpect(jsonPath("$.url").value("https://expoxp.com.br"))
		.andExpect(jsonPath("$.cityId").value(1L));
	}
	
	@Test
	public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndBlankName() throws Exception {

		String accessToken = tokenUtil.obtainAccessToken(mockMvc, adminUsername, adminPassword);
		LocalDate nextMonth = LocalDate.now().plusMonths(1L);
		
		EventDTO eventDTO = new EventDTO(null, "      ", nextMonth, "https://expoxp.com.br", 1L);
		String jsonBody = objectMapper.writeValueAsString(eventDTO);
		
		mockMvc.perform(post("/events")
				.header("Authorization", "Bearer " + accessToken)
				.content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isUnprocessableEntity())
		.andExpect(jsonPath("$.errors[0].fieldName").value("name"))
		.andExpect(jsonPath("$.errors[0].message").value("Campo requerido"));
	}
	
	@Test
	public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndPastDate() throws Exception {

		String accessToken = tokenUtil.obtainAccessToken(mockMvc, adminUsername, adminPassword);
		LocalDate pastMonth = LocalDate.now().minusMonths(1L);
		
		EventDTO eventDTO = new EventDTO(null, "Expo XP", pastMonth, "https://expoxp.com.br", 1L);
		String jsonBody = objectMapper.writeValueAsString(eventDTO);
		
		mockMvc.perform(post("/events")
				.header("Authorization", "Bearer " + accessToken)
				.content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))	
		.andExpect(status().isUnprocessableEntity())
		.andExpect(jsonPath("$.errors[0].fieldName").value("date"))
		.andExpect(jsonPath("$.errors[0].message").value("A data do evento n??o pode ser passada"));
	}
	
	@Test
	public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndNullCity() throws Exception {

		String accessToken = tokenUtil.obtainAccessToken(mockMvc, adminUsername, adminPassword);
		LocalDate nextMonth = LocalDate.now().plusMonths(1L);
		
		EventDTO eventDTO = new EventDTO(null, "Expo XP", nextMonth, "https://expoxp.com.br", null);
		String jsonBody = objectMapper.writeValueAsString(eventDTO);
		
		mockMvc.perform(post("/events")
				.header("Authorization", "Bearer " + accessToken)
				.content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isUnprocessableEntity())
		.andExpect(jsonPath("$.errors[0].fieldName").value("cityId"))
		.andExpect(jsonPath("$.errors[0].message").value("Campo requerido"));
	}
	
	@Test
	public void findAllShouldReturnPagedResources() throws Exception {
		
		mockMvc.perform(get("/events")
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.content").exists());
	}	
}