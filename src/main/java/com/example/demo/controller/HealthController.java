package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 提供/healthz端点用于应用健康状态检查
 */
@RestController
public class HealthController {

    @Autowired
    private ApplicationAvailability applicationAvailability;

    /**
     * 健康检查接口
     * 用于检查应用是否正常运行
     * 
     * @return 健康状态信息
     */
    @GetMapping("/healthz")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> healthStatus = new HashMap<>();
        
        // 获取应用存活状态
        LivenessState livenessState = applicationAvailability.getLivenessState();
        // 获取应用就绪状态
        ReadinessState readinessState = applicationAvailability.getReadinessState();
        
        // 设置健康检查结果
        healthStatus.put("status", "UP");
        healthStatus.put("timestamp", System.currentTimeMillis());
        
        // 应用状态详情
        Map<String, Object> details = new HashMap<>();
        details.put("liveness", livenessState.toString());
        details.put("readiness", readinessState.toString());
        healthStatus.put("details", details);
        
        // 根据应用状态决定HTTP响应码
        HttpStatus httpStatus = HttpStatus.OK;
        if (livenessState != LivenessState.CORRECT) {
            httpStatus = HttpStatus.SERVICE_UNAVAILABLE;
            healthStatus.put("status", "DOWN");
        } else if (readinessState != ReadinessState.ACCEPTING_TRAFFIC) {
            httpStatus = HttpStatus.SERVICE_UNAVAILABLE;
            healthStatus.put("status", "DOWN");
        }
        
        return new ResponseEntity<>(healthStatus, httpStatus);
    }
    
    /**
     * 简化版健康检查接口
     * 主要用于Kubernetes等容器平台的存活探针
     * 返回200表示应用正常运行，503表示应用不可用
     */
    @GetMapping("/healthz/simple")
    public ResponseEntity<String> simpleHealthCheck() {
        LivenessState livenessState = applicationAvailability.getLivenessState();
        ReadinessState readinessState = applicationAvailability.getReadinessState();
        
        if (livenessState == LivenessState.CORRECT && readinessState == ReadinessState.ACCEPTING_TRAFFIC) {
            return ResponseEntity.ok("OK");
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("NOT_AVAILABLE");
        }
    }
} 