package com.geni.backend.Connector;

import lombok.Getter;


@Getter
public enum ConnectorType {
    GITHUB("GITHUB","github");

    String type;
    String name;

    ConnectorType(String type, String name) {
        this.type=type;
        this.name=name;
    }


}
