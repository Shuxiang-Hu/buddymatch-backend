package com.shuxiang.buddymatch.model.request;

import lombok.Data;


import java.io.Serializable;

@Data
public class TeamDeleteRequest implements Serializable {

    private static final long serialVersionUID = -5063657169722012724L;
    private Long id;

    // Getter and setter for the 'id' property
}