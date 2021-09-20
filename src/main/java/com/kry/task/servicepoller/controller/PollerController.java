package com.kry.task.servicepoller.controller;

import com.kry.task.servicepoller.model.ServiceUrl;
import com.kry.task.servicepoller.service.PollerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/poller")
public class PollerController {

    private final PollerService service;

    @Autowired
    public PollerController(PollerService service) {
        this.service = service;
    }

    @GetMapping("/get/{name}")
    public ResponseEntity<Object> getUrlByName(@PathVariable (value = "name") String name) {
        return service.getServiceByName(name);
    }

    @GetMapping("/geturlstatusmap")
    public ResponseEntity<Object> getAllUrlStatusCode() {
        return service.getAllUrlResponseCodeMap();
    }

    @PostMapping("/create")
    public ResponseEntity<Object> newService(@RequestBody ServiceUrl serviceUrl) throws IOException {
        return service.createNewService(serviceUrl);
    }

    @PutMapping("/update/{name}")
    public ResponseEntity<Object> updateService(@RequestBody ServiceUrl serviceUrl,
                                                @PathVariable(value = "name") String name) {
        return service.updateServiceByName(name, serviceUrl);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> deleteEmployee(@PathVariable(value = "id") Long id) {
      return service.deleteServiceById(id);
    }
}
