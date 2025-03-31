package com.devcv.event.domain.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventResponseRequest {
    private Long memberId;
    private String responseJson;
}