package com.kry.task.servicepoller.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kry.task.servicepoller.model.ServiceUrl;
import com.kry.task.servicepoller.service.PollerService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(PollerController.class)
class PollerControllerTest {

    private static final String POLLER = "/poller";
    private static ObjectMapper objectMapper = new ObjectMapper();


    @MockBean
    private PollerService pollerService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetItemWhenItemExists() throws Exception {
        RequestBuilder requestLinkByDocIdBuilder = MockMvcRequestBuilders.get(POLLER + "/get/geek").accept(MediaType.ALL);
        // given
        when(pollerService.getServiceByName("geek")).thenReturn(getResponseJson("geek"));

        // when
        final MvcResult result = mockMvc.perform(requestLinkByDocIdBuilder).andReturn();

        // then
        assertThat(objectMapper.readTree(result.getResponse().getContentAsString()))
                .isEqualTo(objectMapper.readTree(getResponseJson("geek").getBody().toString()));
        assertThat(HttpStatus.OK.value()).isEqualTo(result.getResponse().getStatus());
    }

    @Test
    public void testGetItemWhenItemNotExists() throws Exception {
        RequestBuilder requestLinkByDocIdBuilder = MockMvcRequestBuilders.get(POLLER + "/get/nothing").accept(MediaType.ALL);
        // given
        when(pollerService.getServiceByName("nothing")).thenReturn(getResponseJson("nothing"));

        // when
        final MvcResult result = mockMvc.perform(requestLinkByDocIdBuilder).andReturn();

        // then
        assertThat(result.getResponse().getContentAsString()).isEqualTo(getResponseJson("nothing").getBody().toString());
        assertThat(HttpStatus.NOT_FOUND.value()).isEqualTo(result.getResponse().getStatus());
    }

    @Test
    public void testGetAllUrlStatusWhenItemExists() throws Exception {
        RequestBuilder requestLinkByDocIdBuilder = MockMvcRequestBuilders.get(POLLER + "/geturlstatusmap").accept(MediaType.ALL);
        // given
        when(pollerService.getAllUrlResponseCodeMap()).thenReturn(getResponseJson("getMap"));

        // when
        final MvcResult result = mockMvc.perform(requestLinkByDocIdBuilder).andReturn();

        // then
        assertThat(objectMapper.readTree(result.getResponse().getContentAsString()))
                .isEqualTo(objectMapper.readTree(getResponseJson("getMap").getBody().toString()));
        assertThat(HttpStatus.OK.value()).isEqualTo(result.getResponse().getStatus());
    }

    @Test
    public void testGetAllUrlStatusWhenItemNotExists() throws Exception {
        RequestBuilder requestLinkByDocIdBuilder = MockMvcRequestBuilders.get(POLLER + "/geturlstatusmap").accept(MediaType.ALL);
        // given
        when(pollerService.getAllUrlResponseCodeMap()).thenReturn(getResponseJson("nothing"));

        // when
        final MvcResult result = mockMvc.perform(requestLinkByDocIdBuilder).andReturn();

        // then
        assertThat(result.getResponse().getContentAsString()).isEqualTo(getResponseJson("nothing").getBody().toString());
        assertThat(HttpStatus.NOT_FOUND.value()).isEqualTo(result.getResponse().getStatus());
    }

    @Test
    public void testWhenItemCreated() throws Exception {

        // given
        ServiceUrl serviceUrl = getServiceUrlObject(43L, "non", Mockito.anyString(), "created",
                LocalDateTime.of(2021, 9, 15, 22, 00),
                LocalDateTime.of(2021, 9, 15, 23, 00));

        // when
        when(pollerService.createNewService(serviceUrl))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED)
                        .body(serviceUrl));

        // then
        mockMvc.perform(MockMvcRequestBuilders
                        .post(POLLER + "/create")
                        .content(getInputJson())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void testWhenItemUpdated() throws Exception {

        // given
        ServiceUrl serviceUrl = getServiceUrlObject(43L, "non", Mockito.anyString(), "updated",
                LocalDateTime.of(2021, 9, 15, 22, 00),
                LocalDateTime.of(2021, 9, 15, 23, 00));

        // when
        when(pollerService.updateServiceByName(Mockito.anyString(), serviceUrl))
                .thenReturn(ResponseEntity.status(HttpStatus.OK)
                        .body(serviceUrl));

        // then
        mockMvc.perform(MockMvcRequestBuilders
                        .put(POLLER + "/update/name")
                        .content(getInputJson())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void testWhenItemDeleted() throws Exception {

        // given
        ServiceUrl serviceUrl = getServiceUrlObject(43L, "non", Mockito.anyString(), "updated",
                LocalDateTime.of(2021, 9, 15, 22, 00),
                LocalDateTime.of(2021, 9, 15, 23, 00));

        // when
        when(pollerService.deleteServiceById(33L))
                .thenReturn(ResponseEntity.status(HttpStatus.OK)
                        .body(serviceUrl));

        // then
        mockMvc.perform(MockMvcRequestBuilders
                        .delete(POLLER + "/delete/33")
                        .content(getInputJson())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    private String getInputJson() {
        return String.format("{\n" +
                "\t\"name\" : \"non\",\n" +
                "\t\"url\": \"http://www.tanya123.com/\",\n" +
                "\t\"status\": \"created\"\n" +
                "}");
    }

    private ServiceUrl getServiceUrlObject(Long id, String name, String url, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new ServiceUrl(id, name, url, status, createdAt, updatedAt);
    }

    private ResponseEntity<Object> getResponseJson(String name) {
        if (name.equals("geek")) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(String.format("{\"id\":40,\"name\":\"geek\",\"url\":\"http://www.geeksforgeeks.org/\",\"status\":\"update\",\"createdAt\":\"2021-09-15T00:34:22.545588\",\"updatedAt\":\"2021-09-15T00:34:22.545665\"}"));
        } else if (name.equals("nothing")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(String.format("Cannot be found url by nothing"));
        } else if (name.equals("getMap")) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(String.format("{\"https://spring.io/guides/tutorials/rest/\":200,\"http://www.geeksforgeeks.org/\":301}"));
        }
        return null;
    }
}