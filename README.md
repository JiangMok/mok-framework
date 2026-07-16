# Mok Framework

基于 **Spring Boot 3.4.5** 的 Java 多模块后端快速开发框架，集成了认证授权、用户角色管理、验证码、操作日志、系统监控、消息队列、文件管理、Excel 导入导出、AI 分析、邮件发送、限流防重等常用功能模块。

## 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 基础框架 | Spring Boot | 3.4.5 |
| JDK | Java | 17 |
| ORM | MyBatis-Plus | 3.5.7 |
| 数据库 | MySQL | 8.0.33 |
| 缓存 | Redis (Lettuce) | — |
| 消息队列 | RabbitMQ | — |
| 认证授权 | Sa-Token (JWT) | — |
| 搜索引擎 | Elasticsearch (可选) | — |
| AI 集成 | Spring AI + OkHttp | 1.0.0 / 4.12.0 |
| API 文档 | SpringDoc OpenAPI | 2.8.6 |
| 工具库 | Hutool | 5.8.42 |
| JSON | Fastjson2 | 2.0.60 |
| 动态数据源 | dynamic-datasource | 4.3.1 |
| 电子表格 | FESOD Sheet | 2.0.1-incubating |

## 项目结构

```
mok-framework/
├── mok-framework-app/           # 应用入口模块
├── mok-framework-common/        # 公共模块（异常、工具类、AOP、自动配置）
├── mok-framework-model/         # 数据模型（Entity、DTO、枚举）
├── mok-framework-auth/          # 认证授权模块（Sa-Token + JWT）
├── mok-framework-base/          # 基础管理模块（用户、角色、权限、部门）
├── mok-framework-captcha/       # 验证码模块
├── mok-framework-operationLog/  # 操作日志模块
├── mok-framework-monitor/       # 系统监控模块
├── mok-framework-mq/            # 消息队列模块（RabbitMQ）
├── mok-framework-task/          # 定时任务模块
├── mok-framework-file/          # 文件管理模块
├── mok-framework-excel/         # Excel 导入导出模块
├── mok-framework-ai/            # AI 分析模块
├── mok-framework-mail/          # 邮件发送模块
├── mok-framework-ratelimiter/   # 限流防重模块
└── mok-framework-test/          # 测试模块
```

## 模块说明

### 核心模块

#### mok-framework-app
应用启动入口，包含 `MokFrameworkApplication` 主类。项目启动时通过 `DataSourceCheckerRunner` 检查数据源状态，通过 `SystemHealthCheckRunner` 执行系统健康检查。

#### mok-framework-common
框架公共基础设施：
- **统一响应** — `R<T>` 通用响应体，`PageResult<T>` 分页结果
- **全局异常处理** — `GlobalExceptionHandler` 统一异常拦截
- **操作日志 AOP** — `@OperationLog` 注解自动记录操作日志
- **公共配置** — MyBatis-Plus、Redis、Swagger、动态数据源、文件存储等自动配置
- **工具类** — JSON 脱敏 (`JsonDesensitizationUtil`)、日志工具 (`LogUtils`)、响应工具 (`ResponseUtils`)
- **通用异常** — `BusinessException`、`FileNotFoundException`、`FileUploadException`

#### mok-framework-model
统一的数据模型层，包含：
- **实体类** — `UserEntity`、`RoleEntity`、`PermissionEntity`、`DepartmentEntity`、`FileEntity`、`OperationLogEntity`、`MailLog`、`MqFailedMessage` 等
- **DTO** — `LoginRequest`、`LoginResponse`、`UserDTO`、`RoleDTO`、`DepartmentDTO`、`OperationLogMessage`、`SystemCheckMailMessage` 等
- **枚举** — `MessageType`、`MailType`

### 功能模块

#### mok-framework-auth — 认证授权
基于 **Sa-Token** 实现的无侵入式认证授权：
- JWT 风格 Token 生成与校验
- 登录/登出/踢人下线
- 权限校验与角色校验
- Token 黑名单管理
- CORS 跨域配置
- 支持 `@SaCheckPermission` / `@SaCheckRole` 注解鉴权

#### mok-framework-base — 基础管理
提供后台管理系统的基础 CRUD 功能：
- **用户管理** — 用户增删改查、分页查询
- **角色管理** — 角色增删改查、角色-权限关联
- **权限管理** — 权限增删改查
- **部门管理** — 部门树形结构管理

#### mok-framework-captcha — 验证码
可插拔的验证码生成服务：
- **MOK 实现** — 框架自带验证码（含数学计算类型）
- **Hutool 实现** — 基于 Hutool 的验证码
- 通过 `captchaImpl.generate.type` 配置切换实现
- 验证码存入 Redis，支持过期时间配置

#### mok-framework-operationLog — 操作日志
完善的操作日志记录方案：
- **AOP 自动记录** — 通过 `@OperationLog` 注解标记
- **双存储策略** — MySQL 存储（`OperationLogMySqlServiceImpl`）和 Elasticsearch 存储（`OperationLogESServiceImpl`），可通过 `operationlogImpl.save-location.type` 切换
- **异步处理** — 通过 RabbitMQ 异步写入日志，降低对主业务的影响
- 支持敏感字段脱敏、请求参数/响应结果记录

#### mok-framework-monitor — 系统监控
系统健康检查与监控：
- 数据库连接状态检查
- Redis 连接状态检查
- RabbitMQ 连接状态检查
- 健康检查 REST API 端点
- 异常时自动发送告警邮件

#### mok-framework-mq — 消息队列
RabbitMQ 消息队列集成：
- 消息发送确认（Publisher Confirm）与消息返回（Publisher Return）
- 消费者手动确认（Manual Ack）与重试机制
- **失败消息记录** — 消费失败的消息持久化到数据库，支持后续补发
- 预置队列：操作日志队列、系统健康检查邮件队列

#### mok-framework-task — 定时任务
基于 `@EnableScheduling` 的定时任务：
- **系统健康检查** — `HealthCheckTask` 定时检查各组件状态

#### mok-framework-file — 文件管理
文件上传下载管理：
- 文件上传（支持 MIME 类型校验）
- 文件下载
- 文件物理存储路径与 URL 映射
- 文件记录持久化到数据库

#### mok-framework-excel — Excel 操作
Excel 导入导出功能：
- 基于 EasyExcel 的大数据量 Excel 导入
- 上传监听器模式处理

#### mok-framework-ai — AI 分析
AI 大模型集成模块：
- **多 Provider 支持** — DeepSeek、OpenAI（通过 `mok.ai.provider` 切换）
- **双通道实现** — Spring AI 框架 + OkHttp 直连
- **策略模式** — `AIService` 接口，`DeepSeekAIService` / `OpenAIAIService` 实现
- **异步分析** — 自定义线程池，支持流式与非流式调用
- 预置场景：系统日志分析助手

#### mok-framework-mail — 邮件服务
邮件发送与日志记录：
- 支持 SSL / STARTTLS
- 邮件发送日志持久化
- 健康检查异常邮件告警

#### mok-framework-ratelimiter — 限流防重
基于注解的声明式限流与防重提交：
- **限流策略** — 固定窗口 (`FixedWindowStrategy`)、滑动窗口 (`SlidingWindowStrategy`)、令牌桶 (`TokenBucketStrategy`)
- **限流范围** — 支持全局限流和 IP 级别限流
- **防重提交** — `@PreventDuplicate` 注解防止重复提交
- **SpEL 表达式** — 支持动态解析限流 Key
- **Actuator 端点** — 提供限流监控端点
- 基于 Redis 实现，支持分布式环境

## 快速开始

### 环境要求

- **JDK** 17+
- **Maven** 3.6+
- **MySQL** 8.0+
- **Redis** 6.0+
- **RabbitMQ** 3.9+（可选，用于异步操作日志和邮件）

### 1. 克隆项目

```bash
git clone <your-repo-url>
cd mok-framework
```

### 2. 配置环境变量

在项目根目录创建 `env/dev-env.properties` 文件，配置敏感信息：

```properties
# 数据库密码
spring.datasource.dynamic.datasource.master.password=your_db_password
spring.datasource.dynamic.datasource.operationLog.password=your_db_password

# Redis 密码
spring.data.redis.password=your_redis_password

# RabbitMQ 密码
spring.rabbitmq.password=your_rabbitmq_password

# Sa-Token JWT 密钥
sa-token.jwt-secret-key=your_jwt_secret_key

# AI API Key
spring.ai.openai.api-key=your_ai_api_key
mok.ai.api-key=your_ai_api_key
```

### 3. 初始化数据库

创建数据库并执行初始化 SQL：

```sql
CREATE DATABASE IF NOT EXISTS mf_master_dev DEFAULT CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS mf_operation_log_dev DEFAULT CHARACTER SET utf8mb4;
```

执行 `mok-framework-app/src/main/resources/sql/` 下的 SQL 脚本。

### 4. 启动项目

```bash
mvn clean install
cd mok-framework-app
mvn spring-boot:run
```

或者指定 Profile：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 5. 访问

- **API 服务**: `http://localhost:8080/api`
- **Swagger UI**: `http://localhost:8080/api/swagger-ui.html`
- **OpenAPI 文档**: `http://localhost:8080/api/v3/api-docs`

## 项目配置

### 多环境支持

| Profile | 配置文件 | 说明 |
|---------|---------|------|
| `dev` | `application-dev.yml` | 开发环境（含 ES） |
| `dev-no-es` | `application-dev-no-es.yml` | 开发环境（不含 ES，默认） |
| `prod` | `application-prod.yml` | 生产环境 |

### 核心配置项

```yaml
# 验证码实现切换
captchaImpl:
  generate:
    type: mok-framework   # mok-framework / hutool

# 操作日志存储切换
operationlogImpl:
  save-location:
    type: mysql           # mysql / es

# AI Provider 切换
mok:
  ai:
    provider: deepseek    # deepseek / openai
    model: deepseek-v4-flash
```

### 外部配置覆盖

项目支持从外部路径加载配置覆盖：

- `optional:file:./config/` — 当前目录下的 config 文件夹
- `optional:file:/etc/mok-framework/` — Linux 标准配置路径

生产环境可通过环境变量注入敏感配置，如 `${PROD_DB_MASTER_URL}`。

## API 接口概览

### 认证相关
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/auth/login` | POST | 用户登录 |
| `/api/auth/logout` | POST | 用户登出 |
| `/api/auth/kickout` | POST | 踢人下线 |

### 用户管理
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/user/page` | GET | 分页查询用户 |
| `/api/user/{id}` | GET | 查询用户详情 |
| `/api/user` | POST | 新增用户 |
| `/api/user/{id}` | PUT | 修改用户 |
| `/api/user/{id}` | DELETE | 删除用户 |

### 角色管理
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/role/page` | GET | 分页查询角色 |
| `/api/role` | POST | 新增角色 |
| `/api/role/{id}` | PUT | 修改角色 |
| `/api/role/{id}` | DELETE | 删除角色 |

### 权限管理
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/permission/tree` | GET | 查询权限树 |
| `/api/permission` | POST | 新增权限 |

### 部门管理
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/department/tree` | GET | 查询部门树 |
| `/api/department` | POST | 新增部门 |

### 验证码
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/captcha` | GET | 获取验证码 |

### 文件管理
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/file/upload` | POST | 上传文件 |
| `/api/file/{id}` | GET | 下载文件 |

### 系统监控
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/monitor/health` | GET | 系统健康检查 |

### AI 分析
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/ai/analysis` | POST | AI 日志分析 |
| `/api/ai/chat` | POST | AI 对话 |

### 操作日志
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/operation-log/page` | GET | 分页查询操作日志 |

### Excel 操作
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/excel/upload` | POST | 上传并导入 Excel |

## 开发指南

### 模块依赖关系

```
mok-framework-app
    ├── mok-framework-auth
    ├── mok-framework-base
    ├── mok-framework-captcha
    ├── mok-framework-operationLog
    ├── mok-framework-monitor
    ├── mok-framework-mq
    ├── mok-framework-task
    ├── mok-framework-file
    ├── mok-framework-excel
    ├── mok-framework-ai
    ├── mok-framework-mail
    ├── mok-framework-ratelimiter
    └── mok-framework-test
            ↓
    mok-framework-common
    mok-framework-model
```

`mok-framework-common` 和 `mok-framework-model` 是基础模块，其他功能模块均依赖它们。

### 策略模式的使用

框架中多处使用策略模式，便于扩展：

- **验证码** — 实现 `CaptchaService` 接口即可扩展新的验证码类型
- **操作日志存储** — 实现 `OperationLogService` 接口切换存储方式
- **AI Provider** — 实现 `AIService` 接口接入新的大模型
- **限流策略** — 实现 `RateLimitStrategy` 接口自定义限流算法

### 操作日志注解

```java
@OperationLog(
    title = "用户管理",
    businessType = BusinessType.UPDATE,
    isSaveRequestData = true
)
@PutMapping("/{id}")
public R<Void> update(@PathVariable Long id, @RequestBody UserDTO dto) {
    userService.update(id, dto);
    return R.ok();
}
```

### 限流注解

```java
@RateLimit(
    key = "#userId",
    count = 10,
    time = 60,
    type = RateLimitType.SLIDING_WINDOW
)
@GetMapping("/{userId}")
public R<UserDTO> getUser(@PathVariable String userId) {
    // ...
}
```

### 防重提交注解

```java
@PreventDuplicate(
    key = "#order.orderNo",
    expire = 10,
    message = "请勿重复提交订单"
)
@PostMapping("/order")
public R<Void> createOrder(@RequestBody OrderDTO order) {
    // ...
}
```

## License

[MIT](LICENSE)
