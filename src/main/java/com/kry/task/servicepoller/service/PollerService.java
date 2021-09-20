package com.kry.task.servicepoller.service;

import com.kry.task.servicepoller.model.ServiceUrl;
import com.kry.task.servicepoller.repository.PollerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@EnableScheduling
public class PollerService {
    private static final String INVALID_URL = "Invalid url: ";
    private static final String CANNOT_FOUND_URL = "Cannot be found url by ";
    private static final String NAME_AND_URL_CANNOT_BE_NULL = "Name and url cannot be null";
    private static final String GET = "GET";
    private static final String DELETED_SUCCESSFULLY = "\n is Deleted successfully";
    private static final String ALREADY_EXISTS = " already exists!";
    private static final String SERVICE_RUNNING = "Running";
    private static final String SERVICE_DOWN = "Down";
    private static final int STATUS_OK = 200;
    private static final int NO_STATUS = 0;
    private final PollerRepository pollerRepository;

    @Autowired
    public PollerService(PollerRepository pollerRepository) {
        this.pollerRepository = pollerRepository;
    }

    public ResponseEntity<Object> createNewService(ServiceUrl serviceUrl) {
        if (serviceUrl.getName() == null || serviceUrl.getUrl() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(NAME_AND_URL_CANNOT_BE_NULL);
        }

        Optional<ServiceUrl> oldUrl = pollerRepository.findByName(serviceUrl.getName());
        if (oldUrl.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(serviceUrl.getName() + ALREADY_EXISTS);
        }

        if (!isUrlValid(serviceUrl.getUrl())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(INVALID_URL + serviceUrl.getUrl());
        }

        pollerRepository.save(serviceUrl);
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceUrl);
    }

    public ResponseEntity<Object> updateServiceByName(String name, ServiceUrl modifiedUrl) {

        if (modifiedUrl.getName() == null || modifiedUrl.getUrl() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(NAME_AND_URL_CANNOT_BE_NULL);
        }

        if (!isUrlValid(modifiedUrl.getUrl())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(INVALID_URL + modifiedUrl.getUrl());
        }

        Optional<ServiceUrl> oldUrl = pollerRepository.findByName(name);
        if (!oldUrl.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(CANNOT_FOUND_URL + name);
        }

        pollerRepository.findByName(name)
                .map(url -> {
                    url.setName(modifiedUrl.getName());
                    url.setUrl(modifiedUrl.getUrl());
                    return pollerRepository.save(url);
                })
                .orElseGet(() -> {
                    modifiedUrl.setName(name);
                    return pollerRepository.save(modifiedUrl);
                });

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(modifiedUrl);
    }

    public ResponseEntity<Object> getServiceByName(String name) {
        Optional<ServiceUrl> url = pollerRepository.findByName(name);
        if (!url.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(CANNOT_FOUND_URL + name);
        }
        return ResponseEntity.status(HttpStatus.OK).body(url.get());
    }

    public ResponseEntity<Object> deleteServiceById(long id) {
        Optional<ServiceUrl> urlToBeDeleted = pollerRepository.findById(id);
        if (!urlToBeDeleted.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(CANNOT_FOUND_URL + id);
        }

        pollerRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).body(urlToBeDeleted.get() + DELETED_SUCCESSFULLY);
    }

    private boolean isUrlValid(String url) {
        try {
            URL obj = new URL(url);
            obj.toURI();
            return true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return false;
        }
    }

    private int getUrlResponseCode(String url) {
        try {
            URL obj = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
            conn.setRequestMethod(GET);
            return conn.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
            return NO_STATUS;
        }
    }

    public ResponseEntity<Object> getAllUrlResponseCodeMap() {
        Map<String, Integer> urlStatusMap = new HashMap();
        pollerRepository.findAll().forEach(serviceUrl -> {
            urlStatusMap.put(serviceUrl.getUrl(), getUrlResponseCode(serviceUrl.getUrl()));
        });
        return ResponseEntity.status(HttpStatus.OK).body(urlStatusMap);
    }

    @Scheduled(fixedRate = 5 * 60 * 1000) // Every 5 minutes
    public void scheduledStatusUpdate() {
        pollerRepository.findAll().forEach(serviceUrl -> {
            Optional<ServiceUrl> serviceOptional = pollerRepository.findById(serviceUrl.getId());
            serviceOptional.ifPresent(s -> {
                int statusCode = NO_STATUS;
                try {
                    statusCode = getUrlResponseCode(s.getUrl());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (statusCode == STATUS_OK) {
                    s.setStatus(SERVICE_RUNNING);
                } else {
                    s.setStatus(SERVICE_DOWN);
                }
                pollerRepository.save(s);
            });
        });
    }
}