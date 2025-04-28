package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@RestController
@RequestMapping("/api")
public class InfoController {

    @Autowired
    private Environment environment;

    @Value("${spring.application.name:未知应用}")
    private String applicationName;

    @Value("${info.app.version:1.0.0}")
    private String applicationVersion;

    @Value("${info.app.description:Spring Boot应用}")
    private String applicationDescription;

    @GetMapping("/info")
    public Map<String, Object> getAppInfo() {
        Map<String, Object> info = new HashMap<>();
        
        // 应用基本信息
        Map<String, Object> appInfo = new HashMap<>();
        appInfo.put("name", applicationName);
        appInfo.put("description", applicationDescription);
        appInfo.put("version", applicationVersion);
        
        // 环境信息
        String[] activeProfiles = environment.getActiveProfiles();
        appInfo.put("activeProfiles", activeProfiles);
        appInfo.put("environment", environment.getProperty("info.app.environment"));
        
        // Java信息
        Map<String, Object> javaInfo = new HashMap<>();
        javaInfo.put("version", System.getProperty("java.version"));
        javaInfo.put("vendor", System.getProperty("java.vendor"));
        
        // 系统信息
        Map<String, Object> osInfo = new HashMap<>();
        osInfo.put("name", System.getProperty("os.name"));
        osInfo.put("version", System.getProperty("os.version"));
        osInfo.put("arch", System.getProperty("os.arch"));
        
        // Git信息
        Map<String, Object> gitInfo = getGitInfo();
        
        // 组装最终信息
        info.put("application", appInfo);
        info.put("java", javaInfo);
        info.put("os", osInfo);
        info.put("git", gitInfo);
        
        return info;
    }
    
    /**
     * 从git.properties文件中读取Git信息
     * @return Git信息的Map
     */
    private Map<String, Object> getGitInfo() {
        Map<String, Object> gitInfo = new HashMap<>();
        Properties properties = new Properties();
        
        try {
            ClassPathResource resource = new ClassPathResource("git.properties");
            if (resource.exists()) {
                try (InputStream inputStream = resource.getInputStream()) {
                    properties.load(inputStream);
                    
                    // 读取git信息
                    gitInfo.put("branch", properties.getProperty("git.branch", "未知"));
                    gitInfo.put("commitId", properties.getProperty("git.commit.id", "未知"));
                    gitInfo.put("commitTime", properties.getProperty("git.commit.time", "未知"));
                    gitInfo.put("buildTime", properties.getProperty("git.build.time", "未知"));
                }
            } else {
                // 如果文件不存在，设置默认值
                gitInfo.put("branch", "未知");
                gitInfo.put("commitId", "未知");
                gitInfo.put("commitTime", "未知");
                gitInfo.put("buildTime", "未知");
            }
        } catch (IOException e) {
            // 处理异常情况
            gitInfo.put("error", "读取Git信息出错: " + e.getMessage());
            
            // 设置默认值
            gitInfo.put("branch", "未知");
            gitInfo.put("commitId", "未知");
            gitInfo.put("commitTime", "未知");
            gitInfo.put("buildTime", "未知");
        }
        
        return gitInfo;
    }
} 