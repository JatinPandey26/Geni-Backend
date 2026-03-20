package com.geni.backend.integration.repository.specification;

import com.geni.backend.integration.Integration;
import org.springframework.data.jpa.domain.Specification;

public class MetadataSpecification {

    public static Specification<Integration> hasMetadataValue(String key, String value) {
        return (root, query, cb) ->
                cb.equal(
                        cb.function(
                                "jsonb_extract_path_text",
                                String.class,
                                root.get("metadata"),
                                cb.literal(key)
                        ),
                        value
                );
    }

}
