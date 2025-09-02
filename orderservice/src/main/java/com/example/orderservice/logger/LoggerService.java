package com.example.orderservice.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggerService {
    private final Logger logger = LoggerFactory.getLogger(LoggerService.class);

    public void info(String message, Object... args){
        logger.info(message);
    }

    public void debug(String message, Object... args){
        logger.debug(message);
    }

    public void warn(String message, Object... args){
        logger.warn(message);
    }
    public void error(String message, Object... args){
        logger.error(message);
    }
}