package com.project.cook_mate.recipe.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class LogHelper2 {
    private static final Logger logger = LoggerFactory.getLogger(LogHelper2.class);

    public void processRecipeRequest(String content, String userId) {
        logger.info("[REQUEST] 유저 {} - {} 요청 중", userId, content);
    }

    public void requestSuccess(String content, String userId) {
        logger.info("[INFO] 유저:{}, {}", userId, content);
    }

    public void requestFail(String content, String userId) {
        logger.info("[WARN] 유저:{}, {}", userId, content);
    }

    public void handleException(Exception e) { logger.error("[ERROR]-recipe Exception occurred: {}", e.getMessage());
    }
}
