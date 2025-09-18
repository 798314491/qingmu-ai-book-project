package com.markdown.notes.controller;

import com.markdown.notes.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test Controller for debugging
 */
@RestController
@RequestMapping("/test")
public class TestController {
    
    @GetMapping("/public")
    public Result<String> publicEndpoint() {
        return Result.success("This is a public endpoint");
    }
    
    @GetMapping("/private")
    public Result<String> privateEndpoint() {
        return Result.success("This is a private endpoint");
    }
}
