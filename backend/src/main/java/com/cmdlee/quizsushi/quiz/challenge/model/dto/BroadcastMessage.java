package com.cmdlee.quizsushi.quiz.challenge.model.dto;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BroadcastMessage implements Serializable {
    private String id;
    private String message;
    private long timestamp;

    public BroadcastMessage(String message) {
        this.id = NanoIdUtils.randomNanoId();
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

}
