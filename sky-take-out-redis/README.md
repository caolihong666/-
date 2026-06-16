# Redis 缓存服务

本目录包含餐饮业一体化管理系统所需的 **Redis 缓存服务（Windows 版）**，用于缓存热点数据、用户会话、分布式锁、消息队列等场景。

> 本项目整体借鉴自黑马程序员《苍穹外卖》教学项目，仅用于学习交流，黑马程序员为原创作者。

---

## 目录说明

```
sky-take-out-redis/
├── redis-server.exe              # Redis 服务端程序
├── redis-cli.exe                 # Redis 客户端程序
├── redis.windows.conf            # 默认配置文件（前台运行）
├── redis.windows-service.conf    # Windows 服务配置文件
├── redis-benchmark.exe           # 性能测试工具
├── redis-check-aof.exe           # AOF 文件修复工具
├── dump.rdb                      # RDB 持久化数据文件（如有）
└── Redis-x64-3.2.100.zip         # Redis 原始安装包备份
```

## 快速启动

### 前台启动（开发调试）

```bash
cd sky-take-out-redis
redis-server.exe redis.windows.conf
```

看到 `The server is now ready to accept connections on port 6379` 即表示启动成功。

### 注册为 Windows 服务（可选）

```bash
redis-server.exe --service-install redis.windows-service.conf --loglevel verbose
```

启动服务：

```bash
redis-server.exe --service-start
```

停止服务：

```bash
redis-server.exe --service-stop
```

卸载服务：

```bash
redis-server.exe --service-uninstall
```

## 连接测试

```bash
redis-cli.exe ping
```

正常返回：

```
PONG
```

## 后端配置

在 `sky-take-out/sky-server/src/main/resources/application-dev.yml` 中配置 Redis：

```yaml
sky:
  redis:
    host: localhost
    port: 6379
    database: 0
```

如需密码，请在 `redis.windows.conf` 中配置 `requirepass` 并在后端配置中同步。

## 默认配置

- 端口：`6379`
- 监听：`127.0.0.1`
- 持久化：默认开启 RDB（`dump.rdb`）
- 认证：默认无密码

## 注意事项

- 本目录提供的 Redis 为 Windows 版本（3.2.100），适用于本地开发；
- 生产环境建议部署 Linux 版 Redis 并启用密码认证、AOF/RDB 持久化、主从或哨兵高可用；
- 若启动失败提示端口被占用，请检查是否已有其他 Redis 实例在运行。
