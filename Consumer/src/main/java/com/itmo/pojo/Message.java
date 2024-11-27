package com.itmo.pojo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;

@AllArgsConstructor
@Getter
@Setter
public class Message {
    private String message;
    private Integer uniqueId;

    public Message() {
    }
}
