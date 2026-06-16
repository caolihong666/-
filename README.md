# 餐饮业一体化管理系统（Sky Take Out）

一个基于 **Spring Boot + Vue + 微信小程序** 的餐饮业一体化管理系统。

本项目将传统的“外卖/订餐”能力升级为面向餐饮行业的综合管理平台，包含：

- **Web 管理端（B 端）**：面向商家/运营人员，基于 Vue + Element UI 构建，由 Nginx 提供静态资源服务与反向代理；
- **微信小程序端（C 端）**：面向顾客，提供浏览、下单、支付、订单跟踪等能力；
- **Java 后端服务**：基于 Spring Boot，提供 RESTful API、WebSocket 实时推送、微信支付对接、数据报表等；
- **Nginx 网关/代理**：统一入口，静态资源托管 + 反向代理 + WebSocket 支持；
- **Redis 缓存**：缓存、会话、热点数据加速。

---

## 项目架构

```
sky-take-out/                 # 餐饮业一体化管理系统
├── sky-take-out/             # Java 后端服务
│   ├── sky-common/           # 公共模块（工具类、常量、异常等）
│   ├── sky-pojo/             # 实体类、DTO、VO
│   └── sky-server/           # 业务服务层（Web、Service、Mapper）
├── sky-take-out-nginx/       # Nginx 网关 & Web 管理端静态资源
│   └── nginx-1.20.2/
│       ├── conf/nginx.conf   # Nginx 配置文件
│       ├── html/sky/         # Web 管理端编译产物
│       └── nginx.exe         # Windows 启动程序
├── sky-take-out-redis/       # Redis（Windows 版）
│   ├── redis-server.exe      # Redis 服务
│   ├── redis-cli.exe         # Redis 客户端
│   └── redis.windows.conf    # 默认配置文件
└── mp-weixin/                # 微信小程序用户端（C 端）
```

### 端口与角色说明

| 服务 | 默认端口 | 说明 |
|------|----------|------|
| Nginx | 80 | Web 管理端入口；反向代理 `/api/`、`/user/`、`/ws/` 到后端 |
| Java 后端 | 8080 | 管理端/用户端业务 API、WebSocket |
| Redis | 6379 | 缓存、会话、热点数据 |
| MySQL | 3306 | 关系型数据库 |

---

## 技术栈

### 后端（sky-take-out）

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.3 | 核心框架 |
| MyBatis | 2.2.0 | ORM 持久层框架 |
| MySQL | 8.x | 关系型数据库 |
| Druid | 1.2.1 | 数据库连接池 |
| Redis | 3.2.100+ | 缓存 / 会话 / 消息队列 |
| Spring Cache | - | 方法级缓存 |
| WebSocket | - | 实时消息推送（来单提醒、催单） |
| JWT | 0.9.1 | 无状态身份认证 |
| Knife4j | 3.0.2 | API 文档（Swagger 增强） |
| 阿里云 OSS | 3.10.2 | 图片 / 文件云存储 |
| 微信支付 | 0.4.8 | 小程序微信支付 |
| PageHelper | 1.3.0 | 分页插件 |
| POI | 3.16 | Excel 报表导出 |
| Lombok | 1.18.20 | 简化代码 |

### Web 管理端（sky-take-out-nginx/html/sky）

- **Vue 2.x + Element UI**：管理后台界面
- **Nginx 1.20.2**：静态资源托管 + 反向代理

### 微信小程序端（mp-weixin）

- **原生微信小程序 / uni-app 架构**

---

## 功能模块

### Web 管理端（B 端）

访问地址：`http://localhost`（默认 80 端口）

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

### 微信小程序端（C 端）

在微信开发者工具中导入 `mp-weixin` 目录运行。

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
- Redis 3.2+（本项目已内置 Windows 版 Redis）
- Nginx 1.20.2（本项目已内置 Windows 版 Nginx）
- 微信开发者工具

### 1. 克隆项目

```bash
git clone <repository-url>
cd sky-take-out
```

### 2. 初始化数据库

```bash
# 创建数据库并导入初始 SQL（如有 sql 文件请执行）
mysql -u root -p -e "CREATE DATABASE sky_take_out CHARACTER SET utf8mb4;"
```

### 3. 启动 Redis

进入 `sky-take-out-redis` 目录，执行：

```bash
redis-server.exe redis.windows.conf
```

### 4. 配置并启动后端

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

启动后端：

```bash
cd sky-take-out
mvn clean install
mvn spring-boot:run -pl sky-server
```

服务启动后访问：
- API 文档：http://localhost:8080/doc.html
- 服务端口：8080

### 5. 启动 Nginx & 访问 Web 管理端

进入 `sky-take-out-nginx/nginx-1.20.2` 目录，执行：

```bash
nginx.exe
```

访问 Web 管理端：
- 地址：http://localhost
- 默认登录账号/密码以前端页面提示或初始化数据为准

Nginx 已配置：
- `/` → 返回 `html/sky` 中的 Web 管理端页面；
- `/api/` → 代理到后端 `/admin/`；
- `/user/` → 代理到后端 `/user/`；
- `/ws/` → 代理到后端 `/ws/`（WebSocket）。

停止 Nginx：

```bash
nginx -s stop
```

### 6. 运行微信小程序

1. 打开 **微信开发者工具**；
2. 导入项目目录 `mp-weixin`；
3. 修改 `project.config.json` 中的 `appid` 为你自己的微信小程序 AppID；
4. 修改小程序请求基地址指向本地或部署服务器；
5. 编译预览。

---

## 项目亮点

- ✅ **前后端分离**：后端提供 RESTful API，Web 端与小程序独立部署；
- ✅ **一体化管理**：Web 端面向商家运营，小程序端面向顾客消费；
- ✅ **Nginx 统一入口**：静态资源加速、反向代理、WebSocket 支持；
- ✅ **Redis 缓存加速**：热点数据缓存，提升接口响应速度；
- ✅ **JWT 无状态认证**：管理端与用户端分别使用独立的 Token 机制；
- ✅ **AOP 切面编程**：统一日志记录、权限校验、公共字段填充；
- ✅ **WebSocket 实时通信**：来单语音播报、客户催单实时提醒；
- ✅ **阿里云 OSS 存储**：图片资源云端管理，减轻服务器压力；
- ✅ **微信支付集成**：完整的小程序支付闭环；
- ✅ **Excel 报表导出**：运营数据一键导出。

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

1. **Web 端与小程序端区分**
   - Web 端是商家管理后台，通过浏览器访问 `http://localhost`；
   - 微信小程序端是顾客使用的小程序，需要导入 `mp-weixin` 到微信开发者工具。
2. **微信小程序 AppID**：需要自行注册微信小程序并配置合法域名，或在开发者工具中关闭「不校验合法域名」进行本地调试。
3. **微信支付**：需开通微信支付商户号，并配置正确的 API 密钥和证书。
4. **阿里云 OSS**：图片上传功能依赖阿里云对象存储，请提前创建 Bucket 并配置跨域规则。
5. **数据库**：请确保 MySQL 时区设置为 `Asia/Shanghai`，避免时间相关逻辑异常。
6. **Redis / Nginx 路径**：项目内置的 Redis、Nginx 均为 Windows 版本，部署到 Linux 生产环境时请替换为对应平台二进制并调整配置文件。

---

## License

本项目仅供学习交流使用。
