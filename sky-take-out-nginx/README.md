# Nginx 网关 & Web 管理端

本目录包含餐饮业一体化管理系统的 **Nginx 网关/静态服务器** 以及已构建好的 **Web 管理端（B 端）** 静态资源。

---

## 目录说明

```
sky-take-out-nginx/
└── nginx-1.20.2/                 # Nginx 1.20.2（Windows 版）
    ├── conf/nginx.conf           # 主配置文件
    ├── html/                     # 静态资源根目录
    │   ├── sky/                  # Web 管理端编译产物（Vue + Element UI）
    │   │   ├── index.html
    │   │   ├── css/
    │   │   ├── js/
    │   │   └── ...
    │   └── 50x.html              # 错误页面
    ├── logs/                     # 访问日志 / 错误日志
    └── nginx.exe                 # Windows 启动程序
```

## 主要作用

1. **Web 管理端入口**：浏览器访问 `http://localhost` 时，Nginx 返回 `html/sky/index.html`，加载 Vue 管理后台。
2. **反向代理后端接口**：
   - `/api/` → `http://localhost:8080/admin/`
   - `/user/` → `http://localhost:8080/user/`
   - `/ws/` → `http://localhost:8080/ws/`（WebSocket 长连接）
3. **负载均衡（预留）**：配置中已定义 `upstream webservers`，可扩展多台 Java 后端服务。

## 快速启动（Windows）

```bash
cd sky-take-out-nginx/nginx-1.20.2
nginx.exe
```

启动后即可访问：

- Web 管理端：http://localhost
- 后端 API 文档：http://localhost:8080/doc.html（需先启动 Java 后端）

## 停止 Nginx

```bash
nginx -s stop
```

或强制结束进程：

```bash
taskkill /f /im nginx.exe
```

## 常用配置说明

`conf/nginx.conf` 核心片段：

```nginx
upstream webservers{
  server 127.0.0.1:8080 weight=90;
}

server {
    listen       80;
    server_name  localhost;

    location / {
        root   html/sky;
        index  index.html index.htm;
    }

    location /api/ {
        proxy_pass   http://localhost:8080/admin/;
    }

    location /user/ {
        proxy_pass   http://webservers/user/;
    }

    location /ws/ {
        proxy_pass   http://webservers/ws/;
        proxy_http_version 1.1;
        proxy_read_timeout 3600s;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "$connection_upgrade";
    }
}
```

## 自定义建议

- 修改 `server_name` 可绑定自定义域名；
- 如需 HTTPS，可取消 `443 ssl` 配置的注释并配置证书；
- 生产环境建议开启 `gzip`、`access_log` 与缓存策略；
- 多实例部署时，在 `upstream webservers` 中增加后端服务器地址。

## 注意事项

- 启动 Nginx 前请确保 Java 后端服务已正常启动，否则 Web 管理端的动态数据将无法加载；
- 默认监听 80 端口，若端口被占用，请修改 `conf/nginx.conf` 中的 `listen` 端口。
