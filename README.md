# Spring Boot 多环境配置示例

这是一个 Spring Boot 项目，演示了多环境配置和各种信息接口的实现。

## 功能特性

- 多环境配置 (dev, test, prod)
- 应用信息接口 (/api/info)
- 健康检查接口 (/healthz)
- Git 信息集成

## 多环境配置

项目支持三个环境：

- 开发环境 (dev)
- 测试环境 (test)
- 生产环境 (prod)

切换环境的方式：

1. 修改 `application.properties` 中的 `spring.profiles.active` 属性
2. 通过命令行参数: `java -jar demo.jar --spring.profiles.active=prod`

## 信息接口

### 应用信息接口

访问 `/api/info` 可以获取应用的详细信息，包括：

- 应用基本信息（名称、版本等）
- 环境信息
- Git 信息（分支、提交 ID 等）
- Java 和操作系统信息

### 健康检查接口

项目提供了两个健康检查接口：

1. `/healthz` - 详细的健康检查接口

   - 返回应用的详细健康状态，包括存活状态和就绪状态
   - 正常时返回 HTTP 200，异常时返回 HTTP 503

2. `/healthz/simple` - 简化的健康检查接口
   - 主要用于 Kubernetes 等容器平台的存活探针
   - 正常时返回 HTTP 200 和"OK"，异常时返回 HTTP 503 和"NOT_AVAILABLE"

### Actuator 接口

项目集成了 Spring Boot Actuator，提供了以下端点：

- `/actuator/info` - 应用信息
- `/actuator/health` - 健康状态

## Git 信息集成

项目支持通过 `git-commit-id-maven-plugin` 插件自动收集和显示 Git 信息：

### 插件配置

在 `pom.xml` 中已配置 `git-commit-id-maven-plugin` 插件，用于在构建时自动收集 Git 仓库信息：

```xml
<plugin>
    <groupId>io.github.git-commit-id</groupId>
    <artifactId>git-commit-id-maven-plugin</artifactId>
    <version>5.0.0</version>
    <executions>
        <execution>
            <id>get-the-git-infos</id>
            <goals>
                <goal>revision</goal>
            </goals>
            <phase>initialize</phase>
        </execution>
    </executions>
    <configuration>
        <generateGitPropertiesFile>true</generateGitPropertiesFile>
        <generateGitPropertiesFilename>${project.build.outputDirectory}/git.properties</generateGitPropertiesFilename>
        <includeOnlyProperties>
            <includeOnlyProperty>git.branch</includeOnlyProperty>
            <includeOnlyProperty>git.commit.id</includeOnlyProperty>
            <includeOnlyProperty>git.commit.time</includeOnlyProperty>
            <includeOnlyProperty>git.build.time</includeOnlyProperty>
        </includeOnlyProperties>
        <failOnNoGitDirectory>false</failOnNoGitDirectory>
    </configuration>
</plugin>
```

该插件会在构建时生成 `git.properties` 文件，包含指定的 Git 信息。

### 信息读取方式

项目使用 `ClassPathResource` 直接从 `git.properties` 文件读取 Git 信息：

```java
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
            // 设置默认值
            // ...
        }
    } catch (IOException e) {
        // 异常处理
        // ...
    }

    return gitInfo;
}
```

### 构建和运行

使用 Maven 构建项目时，插件会自动生成 Git 信息：

```bash
mvn clean package
```

或者直接运行：

```bash
mvn spring-boot:run
```

构建完成后，可通过访问 `/api/info` 接口查看 Git 信息。
