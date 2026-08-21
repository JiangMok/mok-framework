# 新项目初始化清单

本项目定位为团队内部全量脚手架。创建新项目时按以下顺序裁剪，避免遗漏依赖或配置。

## 1. 基础信息

- 修改 Maven `groupId`、应用名称和包名。
- 修改前端 `package.json` 名称和页面标题。
- 修改数据库名称、文件目录和日志文件名称。
- 生成新的 JWT 密钥，不复用脚手架开发密钥。
- 创建项目自己的 Git 仓库和首个基线标签。

## 2. 固定基础设施

团队约定 MySQL 和 Redis 为必选基础设施：

- 配置主数据源。
- 配置 Redis 连接和独立数据库编号。
- 执行核心用户、角色、权限和部门表 SQL。

## 3. Starter 选择

### 限流 Starter

脚手架默认使用 Redis：

```yaml
mok:
  rate-limiter:
    backend: redis
```

单实例项目也可以改为 `local`，但多实例下计数不会共享。

### 操作日志 Starter

可选择：

- `file + async`：开发或轻量项目。
- `mysql + async`：不使用 RabbitMQ 的中小项目。
- `mysql + rabbitmq`：需要异步重试的项目。
- `es + rabbitmq`：日志量大且需要全文检索的项目。

使用 MySQL 时执行 `mok-framework-operationLog/src/main/resources/sql/mok_operation_log.sql`。

## 4. 可选模块裁剪

删除模块时同时检查四处：

1. 父 POM `<modules>`。
2. `mok-framework-app/pom.xml` 组装依赖。
3. 其他模块 POM 的直接依赖。
4. 对应 YAML 配置和前端菜单/API/页面。

| 模块 | 可以删除的场景 |
|------|----------------|
| `mok-framework-ai` | 项目不调用大模型 |
| `mok-framework-mail` | 项目不发送邮件 |
| `mok-framework-mq` | 不使用 RabbitMQ，且操作日志采用 async |
| `mok-framework-file` | 不提供文件管理 |
| `mok-framework-excel` | 不提供表格导入导出 |
| `mok-framework-task` | 没有定时任务 |
| `mok-framework-monitor` | 不需要框架健康检查页面 |
| `mok-framework-operationLog` | 不需要操作日志后台管理接口；Starter 仍可单独保留 |
| `mok-framework-test` | 正式业务项目通常删除示例接口模块 |

## 5. 删除演示业务

前端 `order` 下的订单、商品、优惠券、发货和秒杀页面属于演示业务。新项目不需要时同时删除：

- `src/views/order/`
- 对应 `src/api/modules/`
- 对应 `src/types/`
- 数据库菜单和权限记录

## 6. 交付门禁

后端：

```powershell
& 'D:\Develop\Softwares\Maven\apache-maven-3.9.12\bin\mvn.cmd' `
  -s 'D:\Develop\Softwares\Maven\apache-maven-3.9.12\conf\settings.xml' `
  clean test
```

前端：

```bash
npm run build
```

前端构建会依次执行 TypeScript 检查、ESLint 和 Vite 生产构建。

## 7. 版本维护原则

- 脚手架升级主要服务后续新项目，不强制旧项目持续合并模板。
- 多项目共同需要的修复优先沉淀到独立 Starter 或内部依赖。
- 每次创建项目记录使用的脚手架提交和 Starter 版本。

