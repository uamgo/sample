package com.esgyn.kafka.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kafka.serializer.Encoder;
import kafka.utils.VerifiableProperties;

public class JsonEncoder implements Encoder<Object> {
    /*private static final Logger logger = Logger.getLogger(JsonEncoder.class);*/
    // instantiating ObjectMapper is expensive. In real life, prefer injecting the value.
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public JsonEncoder(VerifiableProperties verifiableProperties) {
        /* This constructor must be present for successful compile. */
    }
    public byte[] toBytes(Object object) {
        try {
            return objectMapper.writeValueAsString(object).getBytes();
        } catch (JsonProcessingException e) {
            /*logger.error(String.format("Json processing failed for object: %s", object.getClass().getName()), e);*/
        	e.printStackTrace();
        }
        return "".getBytes();
    }
}