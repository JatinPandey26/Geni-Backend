package com.geni.backend.integration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "integrations")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Integration {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        long id;
        String name;
        String connectorType;
        Long userId;
        String credentialRef;
        @JdbcTypeCode(SqlTypes.JSON)
        @Column(columnDefinition = "jsonb")
        Map<String, Object> metadata;
        boolean enabled;

        @Column(unique = true)
        String externalId;
        @CreatedDate
        @Column(updatable = false)
        Instant createdAt;


        @LastModifiedDate
        private Instant updatedAt;
}
