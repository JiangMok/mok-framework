# Mok Framework

Mok Framework 是供小团队内部复用的 Java 多模块项目脚手架。新项目以本仓库为基础，保留 MySQL、Redis、认证和基础权限能力，再按需裁剪消息队列、邮件、AI、文件、Excel、监控等模块。

操作日志和限流能力已经提取为独立 Spring Boot Starter，脚手架只保留项目级适配代码。

## 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 基础框架 | Spring Boot | 3.4.5 |
| JDK | Java | 17 |
| ORM | MyBatis-Plus | 3.5.7 |
| 数据库 | MySQL | 8.0.33 |
| 缓存 | Redis / Lettuce | Spring Boot 管理 |
| 消息队列 | RabbitMQ | Spring Boot 管理 |
| 认证授权 | Sa-Token + JWT | 1.45.0 |
| 搜索引擎 | Elasticsearch（可选） | 3.5.9 |
| AI | Spring AI + OkHttp | 1.0.0 / 4.12.0 |
| API 文档 | SpringDoc OpenAPI | 2.8.6 |
| 工具库 | Hutool | 5.8.42 |
| JSON | Fastjson2 | 2.0.60 |
| 动态数据源 | dynamic-datasource | 4.3.1 |
| 电子表格 | FESOD Sheet | 2.0.1-incubating |

## 独立 Starter

当前开发基线使用：

| Starter | 版本 | 作用 |
|---------|------|------|
| `top.jiangmok:mok-ratelimiter-spring-boot-starter` | `1.0.0-SNAPSHOT` | Local / Redis 限流与防重复提交 |
| `top.jiangmok:mok-operation-log-spring-boot-starter` | `1.1.0-SNAPSHOT` | 操作日志采集、异步处理和多后端存储 |

SNAPSHOT 尚未发布到 Maven Central。其他电脑或 CI 构建脚手架前，需要先在同一 Maven 本地仓库安装两个 Starter：

```bash
cd mok-ratelimiter-spring-boot-starter
mvn clean install

cd ../mok-operation-log-spring-boot-starter
mvn clean install
```

正式版本发布后，应将父 POM 中的 Starter 版本改为 Maven Central 对应版本。

## 项目结构

```text
mok-framework/
├── mok-framework-app/           # 应用入口和环境配置
├── mok-framework-common/        # 统一响应、异常、Redis、Swagger、公共工具
├── mok-framework-model/         # 项目 Entity、DTO 和枚举
├── mok-framework-auth/          # Sa-Token 认证授权
├── mok-framework-base/          # 用户、角色、权限、部门
├── mok-framework-captcha/       # 验证码
├── mok-framework-operationLog/  # 操作日志后台管理与项目适配
├── mok-framework-monitor/       # 数据库、Redis、RabbitMQ 等健康检查
├── mok-framework-mq/            # RabbitMQ 公共消息能力与失败消息管理
├── mok-framework-task/          # 定时任务
├── mok-framework-file/          # 文件管理
├── mok-framework-excel/         # FESOD Sheet 导入导出
├── mok-framework-ai/            # AI 分析与系统提示词配置
├── mok-framework-mail/          # 邮件配置、发送和日志
└── mok-framework-test/          # 示例/联调接口，不是自动化测试模块
```

### 核心模块

- `mok-framework-app`：应用启动、Profile 配置、启动检查。
- `mok-framework-common`：`R<T>`、`PageResult<T>`、全局异常、Redis、Swagger 和公共工具。
- `mok-framework-model`：脚手架业务实体、DTO 和枚举。
- `mok-framework-auth`：登录、刷新 Token、登出、权限校验。
- `mok-framework-base`：用户、角色、权限和部门管理。
- `mok-framework-captcha`：Redis 验证码。

### 可选模块

- `mok-framework-operationLog`：Starter 的权限、分页、操作人和独立数据源适配；不需要后台日志页面时可删除，Starter 仍可单独保留。
- `mok-framework-monitor`：系统信息和健康检查。
- `mok-framework-mq`：RabbitMQ 失败消息管理。
- `mok-framework-task`：健康检查等定时任务。
- `mok-framework-file`：文件上传、下载和管理。
- `mok-framework-excel`：FESOD Sheet 导入导出。
- `mok-framework-ai`：DeepSeek/OpenAI 分析和提示词配置。
- `mok-framework-mail`：邮件账户、收件人、发送和日志。
- `mok-framework-test`：示例接口，正式项目通常删除。

详细裁剪步骤见 [新项目初始化清单](docs/NEW_PROJECT_CHECKLIST.md)。

## 环境要求

团队约定所有新项目固定使用：

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

当前默认 `dev` Profile 使用 Elasticsearch，操作日志采用 RabbitMQ 异步策略，因此默认启动还需要：

- RabbitMQ 3.9+
- Elasticsearch

不使用 Elasticsearch 时选择 `dev-no-es`。若不使用 RabbitMQ，还需要将操作日志 `async-strategy` 改为 `async`，并裁剪依赖 RabbitMQ 的业务模块。

## 快速开始

### 1. 准备敏感配置

在后端根目录创建 `env/dev-env.properties`。该文件不能提交到 Git：

```properties
# 主业务数据库
master.mysql.host=127.0.0.1
master.mysql.port=3306
master.mysql.password=your_db_password

# 独立操作日志数据库（dev-no-es + mysql 存储使用）
operationLog.mysql.host=127.0.0.1
operationLog.mysql.port=3306
operationLog.mysql.password=your_db_password

# Redis
spring.data.redis.password=your_redis_password

# RabbitMQ
spring.rabbitmq.host=127.0.0.1
spring.rabbitmq.password=your_rabbitmq_password

# Sa-Token
sa-token.jwt-secret-key=replace_with_a_long_random_secret

# AI
spring.ai.openai.api-key=your_ai_api_key
mok.ai.api-key=your_ai_api_key
```

### 2. 初始化数据库

```sql
CREATE DATABASE IF NOT EXISTS mf_master_dev DEFAULT CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS mf_operation_log_dev DEFAULT CHARACTER SET utf8mb4;
```

按项目需要执行：

- 核心业务 SQL：`mok-framework-app/src/main/resources/sql/`
- 操作日志 MySQL 表：`mok-framework-operationLog/src/main/resources/sql/mok_operation_log.sql`

操作日志 SQL 不会删除旧的 `sys_operation_log` 表。

### 3. 构建后端

确认两个 SNAPSHOT Starter 已安装后执行：

```bash
mvn clean test
```

### 4. 启动

默认启动 `dev`（含 ES）：

```bash
cd mok-framework-app
mvn spring-boot:run
```

不使用 ES：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev-no-es
```

### 5. 访问

- API：`http://localhost:8080/api`
- Swagger UI：`http://localhost:8080/api/swagger-ui.html`
- OpenAPI：`http://localhost:8080/api/v3/api-docs`

## Profile

| Profile | 配置文件 | 操作日志存储 | 说明 |
|---------|----------|--------------|------|
| `dev`（默认） | `application-dev.yml` | Elasticsearch + RabbitMQ | 完整开发环境 |
| `dev-no-es` | `application-dev-no-es.yml` | MySQL + RabbitMQ | 不使用 ES |
| `prod` | `application-prod.yml` | Elasticsearch + RabbitMQ | 生产环境模板 |

## 核心配置

```yaml
# 验证码实现
captchaImpl:
  generate:
    type: mok-framework           # mok-framework / hutool

mok:
  operation-log:
    enabled: true
    save-location: mysql          # file / mysql / es
    async-strategy: rabbitmq      # async / rabbitmq
    record-get: true

  rate-limiter:
    enabled: true
    backend: redis                # local / redis
    failure-policy: allow         # allow / reject

  ai:
    provider: deepseek            # deepseek / openai
    model: deepseek-v4-flash
```

## 主要 API

完整接口以运行时 Swagger/OpenAPI 为准。以下为当前主要接口：

| 模块 | 接口 | 方法 |
|------|------|------|
| 登录 | `/api/auth/login` | POST |
| 刷新 Token | `/api/auth/refresh` | POST |
| 登出 | `/api/auth/logout` | GET |
| 用户分页 | `/api/user/page` | POST |
| 用户详情 | `/api/user/{id}` | GET |
| 新增用户 | `/api/user/add` | POST |
| 修改用户 | `/api/user/update` | POST |
| 删除用户 | `/api/user/delete/{id}` | DELETE |
| 角色分页 | `/api/role/page` | POST |
| 权限树 | `/api/permission/tree` | GET |
| 部门树 | `/api/dept/tree` | GET |
| 部门分页 | `/api/dept/list` | POST |
| 生成验证码 | `/api/captcha/generate` | GET |
| 校验验证码 | `/api/captcha/validate` | POST |
| 文件分页 | `/api/files/page` | POST |
| 上传文件 | `/api/files/upload` | POST |
| 系统信息 | `/api/system/info` | GET |
| 系统健康 | `/api/system/health` | GET |
| 操作日志分页 | `/api/operation-log/page` | POST |
| 操作日志详情 | `/api/operation-log/{id}` | GET |

## Starter 使用示例

### 操作日志

```java
@OperationLog(
    title = "修改用户",
    businessType = BusinessType.UPDATE,
    saveRequestParam = true,
    saveResponseData = false
)
@PostMapping("/update")
public R<Void> update(@RequestBody UserDTO dto) {
    userService.update(dto);
    return R.ok();
}
```

### 限流

```java
@RateLimit(
    key = "#userId",
    limit = 10,
    window = 60,
    type = RateLimitType.SLIDING_WINDOW,
    scope = RateLimitScope.USER
)
@GetMapping("/{userId}")
public R<UserDTO> getUser(@PathVariable String userId) {
    return R.ok(userService.getById(userId));
}
```

### 防重复提交

```java
@PreventDuplicate(
    key = "#order.orderNo",
    lockTime = 10,
    message = "请勿重复提交订单"
)
@PostMapping("/order")
public R<Void> createOrder(@RequestBody OrderDTO order) {
    orderService.create(order);
    return R.ok();
}
```

`@PreventDuplicate` 只防止短时间重复请求，不能代替数据库唯一索引或业务幂等表。

## 构建门禁

后端：

```bash
mvn clean test
```

前端项目 `mok-vue-admin`：

```bash
npm run build
```

前端构建会依次执行 TypeScript 检查、ESLint 和 Vite 生产构建。

## 外部配置覆盖

- `optional:file:./config/`
- `optional:file:/etc/mok-framework/`

生产环境应使用环境变量或外部配置注入密码、JWT 密钥和 API Key。

## License

[MIT](LICENSE)
