package com.devsuperior.exec.tdd.cap03.validation_and_security.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.exec.tdd.cap03.validation_and_security.dto.CityDTO;
import com.devsuperior.exec.tdd.cap03.validation_and_security.tests.TokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CityControllerIntegrationTests {

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
		CityDTO cityDTO = new CityDTO(null, "Recife");
		String jsonBody = objectMapper.writeValueAsString(cityDTO);
		
		mockMvc.perform(post("/cities")
				.content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isUnauthorized());		
	}
	
	@Test
	public void insertShouldReturnForbiddenWhenClientLogged() throws Exception {	
		
		String accessToken = tokenUtil.obtainAccessToken(mockMvc, clientUsername, clientPassword);	
		
		CityDTO cityDTO = new CityDTO(null, "Recife");
		String jsonBody = objectMapper.writeValueAsString(cityDTO);
		
		mockMvc.perform(post("/cities")
				.header("Authorization", "Bearer " + accessToken)
				.content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isForbidden());
	}
	
	@Test
	public void insertShouldCreatedResourceWhenAdminLoggedAndCorrectData() throws Exception {

		String accessToken = tokenUtil.obtainAccessToken(mockMvc, adminUsername, adminPassword);

		CityDTO cityDTO = new CityDTO(null, "Recife");
		String jsonBody = objectMapper.writeValueAsString(cityDTO);		
		
		mockMvc.perform(post("/cities")				
				.header("Authorization", "Bearer " + accessToken)
				.content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
		.andExpect(status().isCreated())
		.andExpect(jsonPath("$.id").exists())
		.andExpect(jsonPath("$.name").value("Recife"));
	}
	
	@Test
	public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndBlankName() throws Exception {

		String accessToken = tokenUtil.obtainAccessToken(mockMvc, adminUsername, adminPassword);

		CityDTO cityDTO = new CityDTO(null, "    ");
		String jsonBody = objectMapper.writeValueAsString(cityDTO);		
		
		mockMvc.perform(post("/cities")
				.header("Authorization", "Bearer " + accessToken)
				.content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))		
		.andExpect(status().isUnprocessableEntity())
		.andExpect(jsonPath("$.errors[0].fieldName").value("name"))
		.andExpect(jsonPath("$.errors[0].message").value("Campo requerido"));
	}
	
	@Test
	public void findAllShouldReturnAllResourcesSortedByName() throws Exception {
		
		mockMvc.perform(get("/cities")
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$[0].name").value("Belo Horizonte"))
		.andExpect(jsonPath("$[1].name").value("Belém"))
		.andExpect(jsonPath("$[2].name").value("Brasília"));
	}
}
