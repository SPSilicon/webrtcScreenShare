package com.hig.webrtc.hig;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown =  true)
public class AnswerDto {
    String to;
    String data;
}
