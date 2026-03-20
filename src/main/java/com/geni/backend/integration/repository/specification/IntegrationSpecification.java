package com.geni.backend.integration.repository.specification;

import com.geni.backend.integration.Integration;
import org.springframework.data.jpa.domain.Specification;

public class IntegrationSpecification {

    public static Specification<Integration> hasConnectorType(String connectorType) {
        return (root, query, cb) ->
                cb.equal(root.get("connectorType"), connectorType);
    }

    public static Specification<Integration> hasUserId(Long userId) {
        return (root, query, cb) ->
                cb.equal(root.get("userId"), userId);
    }

    public static Specification<Integration> isEnabled() {
        return (root, query, cb) ->
                cb.isTrue(root.get("enabled"));
    }

    public static Specification<Integration> isDisabled() {
        return (root, query, cb) ->
                cb.isFalse(root.get("enabled"));
    }
}
