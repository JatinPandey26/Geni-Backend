package com.geni.backend.integration.repository;

import com.geni.backend.integration.Integration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface IntegrationRepository extends JpaRepository<Integration,Long> , JpaSpecificationExecutor<Integration> {
    List<Integration> findByConnectorType(String connectorType);
}
