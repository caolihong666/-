# Java 后端服务

本目录包含餐饮业一体化管理系统的 **Java 后端服务**，基于 Spring Boot 构建，为 Web 管理端和微信小程序端提供统一的数据接口与业务处理能力。

> 本项目整体借鉴自黑马程序员《苍穹外卖》教学项目，仅用于学习交流，黑马程序员为原创作者。

---

## 模块说明

```
sky-take-out/
├── sky-common/        # 公共模块：工具类、常量、异常、上下文、配置属性等
├── sky-pojo/          # 数据模型：实体类（entity）、DTO、VO
└── sky-server/        # 业务服务：Controller、Service、Mapper、WebSocket、定时任务等
```

## 主要技术

- Spring Boot 2.7.3
- MyBatis + MySQL 8.x + Druid
- Redis（缓存 / 会话）
- JWT（管理端与用户端双 Token）
- Knife4j / Swagger（API 文档）
- WebSocket（来单提醒、催单）
- 阿里云 OSS（图片存储）
- 微信支付（小程序支付）

## 快速启动

1. 确保 MySQL、Redis 已启动；
2. 复制/编辑 `sky-server/src/main/resources/application-dev.yml`，配置数据库、Redis、OSS、微信等参数；
3. 执行：

```bash
cd sky-take-out
mvn clean install
mvn spring-boot:run -pl sky-server
```

启动后访问：

- API 文档：http://localhost:8080/doc.html
- 服务基地址：http://localhost:8080

## 接口说明

- `/admin/**`：Web 管理端接口
- `/user/**`：微信小程序用户端接口
- `/ws/**`：WebSocket 实时通信接口
- `/notify/**`：微信支付回调接口

## 注意事项

- 生产环境请修改 `application.yml` 中的 JWT 密钥；
- 微信支付、阿里云 OSS 等第三方服务需要替换为真实账号信息；
- 数据库时区建议设置为 `Asia/Shanghai`。
