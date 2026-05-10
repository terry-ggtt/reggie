# 安理点餐管理系统

这是一个用于餐饮外卖的点餐管理后台系统，系统包含用户端和管理端。

## 技术栈

- **后端**：Spring Boot + MyBatis Plus + MySQL
- **前端**：Vue 2 + Element UI + ECharts

## 项目结构

```
├── src/main/java/com/itheima/reggie/
│   ├── controller/     # 控制器
│   ├── service/        # 业务逻辑
│   ├── mapper/        # 数据访问
│   ├── entity/        # 实体类
│   ├── common/        # 公共类
│   └── filter/       # 过滤器
├── src/main/resources/
│   ├── static/backend/  # 管理端前端
│   └── static/front/    # 用户端前端
```

## 快速开始

### 1. 环境要求

- JDK 1.8+
- MySQL 8.0+
- Maven 3.6+

### 2. 配置数据库

创建数据库 `reggie` 并导入数据：

```sql
CREATE DATABASE reggie CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

配置 `application.yml` 中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/reggie?serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

### 3. 启动后端

```bash
mvn spring-boot:run
```

或导入 IDEA 后运行 `ReggeApplication.java`

### 4. 访问系统

- **管理端**：`http://localhost:8080/backend/`
- **用户端**：`http://localhost:8080/front/index.html`

默认管理员账号：`admin` / `123456`

## 功能模块

### 管理端

- **员工管理**：员工账号管理
- **分类管理**：菜品/套餐分类
- **菜品管理**：菜品 CRUD
- **套餐管理**：套餐管理
- **订单管理**：订单查看
- **统计分析**：销售数据统计

### 核心 API

| 接口 | 说明 |
|------|------|
| `GET /report/sales/date` | 按日统计 |
| `GET /report/sales/month` | 按月统计 |
| `GET /report/sales/year` | 按年统计 |
| `GET /report/dish/top10` | 热销菜品 |
| `GET /report/category/sales` | 分类销售占比 |

## 统计分析功能

支持按日/月/年三个维度查看：

- **总订单量** / **总销售额**
- **销售趋势图**（折线+柱状）
- **热销菜品 TOP10**（横向柱状图）
- **分类销售占比**（饼图）

## 配置说明

### 文件上传目录

配置 `application.yml`：

```yaml
reggie:
  path: D:/reggie_take_out/upload/
```

确保目录存在且有读写权限。
