package com.hig.webrtc.hig;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecvDto {
    String event;
    String from;
    String to;
    JsonNode data;
}
