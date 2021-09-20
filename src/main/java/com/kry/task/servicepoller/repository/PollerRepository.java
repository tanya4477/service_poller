package com.kry.task.servicepoller.repository;

import com.kry.task.servicepoller.model.ServiceUrl;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PollerRepository extends CrudRepository<ServiceUrl, Long> {
    Optional<ServiceUrl> findByName(String name);
}
