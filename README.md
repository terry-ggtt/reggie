# 安理点餐管理系统

这是一个基于 Spring Boot 的餐饮外卖/点餐管理系统，包含后台管理端和用户点餐端。前端页面以静态资源形式放在 Spring Boot 项目内，由后端服务统一托管。

## 技术栈

后端：

- Java 17
- Spring Boot 2.4.5
- Spring MVC
- MyBatis-Plus 3.4.2
- MySQL
- Druid 数据库连接池
- Lombok
- Maven

前端：

- Vue 2
- Element UI
- Vant
- Axios
- ECharts
- HTML / CSS / JavaScript

## 项目结构

```text
src/main/java/com/itheima/reggie/
├── controller/      # 接口控制层
├── service/         # 业务接口
├── service/impl/    # 业务实现
├── mapper/          # MyBatis-Plus 数据访问层
├── entity/          # 数据实体
├── DTO/             # 数据传输对象
├── common/          # 通用返回、异常处理、自动填充、静态资源配置
├── config/          # MyBatis-Plus 配置
├── filter/          # 登录校验过滤器
└── utils/           # 工具类

src/main/resources/
├── application.yml
├── db/
└── static/
    ├── backend/     # 后台管理端页面
    └── front/       # 用户点餐端页面
```

## 功能模块

后台管理端：

- 员工登录与退出
- 员工管理
- 分类管理
- 菜品管理
- 套餐管理
- 订单管理
- 用户评价管理
- 售后管理
- 销售统计分析

用户点餐端：

- 手机号验证码登录
- 菜品和套餐浏览
- 购物车
- 地址管理
- 下单
- 再来一单
- 订单查看
- 订单评价
- 售后申请

## 统计分析实现

统计分析由后端接口聚合数据，前端使用 ECharts 渲染图表。

后端入口：

```text
src/main/java/com/itheima/reggie/controller/ReportController.java
```

统计接口：

| 接口 | 功能 |
| --- | --- |
| `GET /report/sales/date` | 按日统计订单数和营业额 |
| `GET /report/sales/month` | 按月统计订单数和营业额 |
| `GET /report/sales/year` | 按年统计订单数和营业额 |
| `GET /report/dish/top10` | 统计热销菜品 TOP10 |
| `GET /report/category/sales` | 统计分类销售额 |

实现方式：

- 使用 `LambdaQueryWrapper` 按时间范围查询订单。
- 只统计已完成订单，条件为 `Orders.status = 4`。
- 订单数量使用 `orders.size()` 计算。
- 营业额使用 Java Stream 对 `Orders.amount` 求和。
- 热销菜品通过订单明细按 `dishId` 分组，并汇总 `number`。
- 分类销售额通过 `dishId -> categoryId` 映射后，用 `Map.merge` 累加金额。
- 当前实现主要在 Java 内存中聚合，没有使用 SQL `GROUP BY`。

前端页面：

```text
src/main/resources/static/backend/page/report/sales.html
```

前端实现方式：

- Vue 管理页面状态。
- Axios 并发请求销售趋势、热销菜品、分类销售接口。
- ECharts 渲染柱状图、折线图、横向柱状图和环形饼图。

## 本地运行

环境要求：

- JDK 17
- Maven 3.6+
- MySQL 8.0+

创建数据库：

```sql
CREATE DATABASE reggie CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

也可以直接导入完整建库和演示数据脚本：

```bash
mysql -u root -p < src/main/resources/db/reggie_full.sql
```

修改数据库配置：

```yaml
spring:
  datasource:
    druid:
      driver-class-name: com.mysql.cj.jdbc.Driver
      url: jdbc:mysql://localhost:3306/reggie?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true
      username: root
      password: your_password
```

启动项目：

```bash
mvn spring-boot:run
```

或使用 Maven Wrapper：

```bash
./mvnw spring-boot:run
```

Windows：

```bash
mvnw.cmd spring-boot:run
```

访问地址：

```text
后台管理端：http://localhost:8080/backend/index.html
用户点餐端：http://localhost:8080/front/index.html
```

## 静态资源映射

静态页面由 Spring Boot 提供访问：

```text
/backend/** -> classpath:/static/backend/
/front/**   -> classpath:/static/front/
```

配置位置：

```text
src/main/java/com/itheima/reggie/common/WebMvcConfig.java
```

## 部署说明

该项目不是纯前端项目，不能直接作为普通静态站点部署到 Vercel。

原因：

- 项目依赖 Spring Boot 后端服务。
- 页面中的接口请求需要 Java 后端处理。
- 数据需要连接 MySQL。
- `/backend/**` 和 `/front/**` 路径映射只有在 Spring Boot 运行时才生效。

推荐部署方式：

- 后端部署到支持 Java 服务的平台，例如云服务器、Docker、Render、Railway、Fly.io 等。
- 数据库使用云 MySQL 或独立 MySQL 服务。
- 前端静态页面可以继续由 Spring Boot 托管。

安全要求：

- 不要把真实数据库账号、密码、短信密钥提交到仓库。
- 生产环境应使用环境变量或外部配置注入敏感信息。
- 如果仓库曾公开部署过包含 `application.yml` 的源码，应立即更换数据库密码和相关密钥。
