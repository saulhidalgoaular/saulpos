package com.saulpos.server.error;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestErrorController {
    @GetMapping("/test/base-exception")
    public void throwBase() {
        throw new BaseException(ErrorCode.RESOURCE_NOT_FOUND);
    }

    @GetMapping("/test/generic-exception")
    public void throwGeneric() {
        throw new RuntimeException("Unexpected error");
    }
}
