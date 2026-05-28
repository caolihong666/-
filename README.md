# 餐饮商家一体化管理系统 (Sky Take Out)

一个基于 **Spring Boot + 微信小程序** 的在线外卖订餐平台，包含管理后台（B端）和微信小程序用户端（C端），支持菜品管理、订单处理、在线支付、实时消息推送等完整的外卖业务流程。

---

## 项目架构

```
餐饮商家一体化管理系统/
├── sky-take-out/          # Java 后端服务
│   ├── sky-common/        # 公共模块（工具类、常量、异常等）
│   ├── sky-pojo/          # 实体类、DTO、VO
│   └── sky-server/        # 业务服务层（Web、Service、Mapper）
└── mp-weixin/             # 微信小程序前端
```

---

## 技术栈

### 后端 (sky-take-out)

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.3 | 核心框架 |
| MyBatis | 2.2.0 | ORM 持久层框架 |
| MySQL | 8.x | 关系型数据库 |
| Druid | 1.2.1 | 数据库连接池 |
| Redis | - | 缓存 / 会话 / 消息队列 |
| Spring Cache | - | 方法级缓存 |
| WebSocket | - | 实时消息推送（来单提醒、催单） |
| JWT | 0.9.1 | 无状态身份认证 |
| Knife4j | 3.0.2 | API 文档（Swagger 增强） |
| 阿里云 OSS | 3.10.2 | 图片 / 文件云存储 |
| 微信支付 | 0.4.8 | 小程序微信支付 |
| PageHelper | 1.3.0 | 分页插件 |
| POI | 3.16 | Excel 报表导出 |
| Lombok | 1.18.20 | 简化代码 |

### 前端 (mp-weixin)

- **微信小程序**（原生 / uni-app 架构）

---

## 功能模块

### 管理端 (B端)

| 模块 | 功能说明 |
|------|---------|
| 🏪 店铺管理 | 店铺营业状态设置 |
| 👤 员工管理 | 员工账号的增删改查、登录认证 |
| 📂 分类管理 | 菜品分类 / 套餐分类的维护 |
| 🍜 菜品管理 | 菜品的增删改查、口味配置、图片上传 |
| 🍱 套餐管理 | 套餐的增删改查、包含菜品配置 |
| 📋 订单管理 | 订单查询、状态流转、接单拒单 |
| 📊 数据统计 | 营业额统计、用户统计、订单统计、销量排行 |
| 📈 工作台 | 今日数据概览、订单处理、订单提醒 |

### 用户端 (C端 / 微信小程序)

| 模块 | 功能说明 |
|------|---------|
| 🔐 微信登录 | 基于微信授权的一键登录 |
| 🏠 店铺浏览 | 查看店铺营业状态及菜品信息 |
| 🍽️ 菜品/套餐 | 分类浏览、菜品详情、口味选择 |
| 🛒 购物车 | 添加商品、修改数量、清空购物车 |
| 📍 地址簿 | 收货地址的增删改查、默认地址 |
| 📝 下单支付 | 订单确认、微信支付、订单状态跟踪 |
| 📦 订单管理 | 历史订单查询、再来一单 |

---

## 快速开始

### 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Redis 5.0+
- 微信开发者工具

### 1. 克隆项目

```bash
git clone <repository-url>
cd 餐饮商家一体化管理系统
```

### 2. 初始化数据库

```bash
# 创建数据库并导入初始 SQL（如有 sql 文件请执行）
mysql -u root -p -e "CREATE DATABASE sky_take_out CHARACTER SET utf8mb4;"
```

### 3. 配置后端

编辑 `sky-take-out/sky-server/src/main/resources/application-dev.yml`：

```yaml
sky:
  datasource:
    host: localhost
    port: 3306
    database: sky_take_out
    username: root
    password: your_password
  redis:
    host: localhost
    port: 6379
    database: 0
  alioss:
    endpoint: https://your-oss-endpoint.aliyuncs.com
    access-key-id: your_access_key_id
    access-key-secret: your_access_key_secret
    bucket-name: your-bucket-name
  wechat:
    appid: your_wechat_appid
    secret: your_wechat_secret
```

### 4. 启动后端服务

```bash
cd sky-take-out
mvn clean install
mvn spring-boot:run -pl sky-server
```

服务启动后访问：
- API 文档：http://localhost:8080/doc.html
- 服务端口：8080

### 5. 运行微信小程序

1. 打开 **微信开发者工具**
2. 导入项目目录 `mp-weixin`
3. 修改 `project.config.json` 中的 `appid` 为你自己的微信小程序 AppID
4. 修改小程序请求基地址指向本地或部署服务器
5. 编译预览

---

## 项目亮点

- ✅ **前后端分离**：后端提供 RESTful API，小程序独立部署
- ✅ **JWT 无状态认证**：管理端与用户端分别使用独立的 Token 机制
- ✅ **AOP 切面编程**：统一日志记录、权限校验、公共字段填充
- ✅ **Redis 缓存加速**：热点数据缓存，提升接口响应速度
- ✅ **WebSocket 实时通信**：来单语音播报、客户催单实时提醒
- ✅ **阿里云 OSS 存储**：图片资源云端管理，减轻服务器压力
- ✅ **微信支付集成**：完整的小程序支付闭环
- ✅ **Excel 报表导出**：运营数据一键导出

---

## 目录结构

```
sky-take-out/
├── sky-common/                    # 公共模块
│   ├── src/main/java/com/sky/
│   │   ├── constant/              # 常量
│   │   ├── context/               # 上下文
│   │   ├── exception/             # 全局异常
│   │   ├── json/                  # JSON 处理
│   │   ├── properties/            # 配置属性类
│   │   └── utils/                 # 工具类
│   └── pom.xml
├── sky-pojo/                      # 数据模型
│   ├── src/main/java/com/sky/
│   │   ├── dto/                   # 数据传输对象
│   │   ├── entity/                # 数据库实体
│   │   └── vo/                    # 视图对象
│   └── pom.xml
└── sky-server/                    # 业务服务
    ├── src/main/java/com/sky/
    │   ├── SkyApplication.java    # 启动类
    │   ├── annotation/            # 自定义注解
    │   ├── aspect/                # AOP 切面
    │   ├── config/                # 配置类
    │   ├── controller/            # 控制器
    │   │   ├── admin/             # 管理端接口
    │   │   ├── user/              # 用户端接口
    │   │   └── notify/            # 支付回调
    │   ├── handler/               # 处理器
    │   ├── interceptor/           # 拦截器
    │   ├── mapper/                # MyBatis Mapper
    │   ├── service/               # 业务逻辑
    │   ├── task/                  # 定时任务
    │   └── websocket/             # WebSocket 服务
    └── pom.xml
```

---

## 注意事项

1. **微信小程序 AppID**：需要自行注册微信小程序并配置合法域名，或在开发者工具中关闭「不校验合法域名」进行本地调试。
2. **微信支付**：需开通微信支付商户号，并配置正确的 API 密钥和证书。
3. **阿里云 OSS**：图片上传功能依赖阿里云对象存储，请提前创建 Bucket 并配置跨域规则。
4. **数据库**：请确保 MySQL 时区设置为 `Asia/Shanghai`，避免时间相关逻辑异常。

---

## License

本项目仅供学习交流使用。
