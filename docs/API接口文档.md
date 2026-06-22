# ElderMoodAI 后端API接口文档

> 文档生成日期：2026年6月17日
> 后端服务基础路径：`/api`（需根据实际部署配置）

---

## 目录

1. [通用说明](#通用说明)
2. [认证管理模块](#认证管理模块)
   - [发送注册验证码](#发送注册验证码)
   - [用户注册](#用户注册)

---

## 通用说明

### 响应格式

所有接口均返回统一的JSON响应格式：

```json
{
  "success": true,
  "message": "操作成功",
  "data": { }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| success | boolean | 请求是否成功 |
| message | string | 响应消息 |
| data | object | 响应数据（成功时返回，失败时可能为null） |

### HTTP状态码说明

| 状态码 | 说明 |
|--------|------|
| 200 OK | 请求成功 |
| 201 Created | 资源创建成功 |
| 400 Bad Request | 请求参数错误 |
| 500 Internal Server Error | 服务器内部错误 |

---

## 认证管理模块

基础路径：`/auth`

### 发送注册验证码

向用户邮箱发送注册验证码。

**请求信息**

| 项目 | 内容 |
|------|------|
| URL | `POST /auth/send-verification-code` |
| Content-Type | `application/json` |

**请求参数**

```json
{
  "email": "user@example.com"
}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| email | string | 是 | 用户邮箱地址，需符合邮箱格式 |

**成功响应**

```json
{
  "success": true,
  "message": "验证码已发送，请查收邮箱",
  "data": null
}
```

**失败响应**

```json
{
  "success": false,
  "message": "该邮箱已被注册",
  "data": null
}
```

**错误码说明**

| 错误信息 | 说明 |
|----------|------|
| 邮箱不能为空 | 未提供邮箱参数 |
| 邮箱格式不正确 | 邮箱格式验证失败 |
| 该邮箱已被注册 | 邮箱已被其他用户使用 |
| 发送验证码失败，请稍后重试 | 邮件发送服务异常 |

---

### 用户注册

使用邮箱和验证码进行用户注册。

**请求信息**

| 项目 | 内容 |
|------|------|
| URL | `POST /auth/register` |
| Content-Type | `application/json` |

**请求参数**

```json
{
  "username": "testuser",
  "email": "user@example.com",
  "password": "password123",
  "verificationCode": "123456"
}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | string | 是 | 用户名，长度3-50字符 |
| email | string | 是 | 用户邮箱地址，需符合邮箱格式 |
| password | string | 是 | 密码，长度6-50字符 |
| verificationCode | string | 是 | 邮箱验证码 |

**成功响应**

HTTP状态码：201

```json
{
  "success": true,
  "message": "注册成功",
  "data": {
    "id": 1,
    "username": "testuser",
    "phone": null,
    "email": "user@example.com",
    "role": "GUARDIAN",
    "status": "PENDING_APPROVAL",
    "createdAt": "2026-06-17T10:30:00",
    "updatedAt": "2026-06-17T10:30:00"
  }
}
```

**用户数据说明**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | number | 用户唯一标识 |
| username | string | 用户名 |
| phone | string | 手机号（可为null） |
| email | string | 邮箱地址 |
| role | string | 用户角色：GUARDIAN（家属/监护人）、CAREGIVER（护理员）、ADMIN（管理员） |
| status | string | 用户状态：ACTIVE（活跃）、INACTIVE（禁用）、PENDING_APPROVAL（待审批） |
| createdAt | string | 创建时间，ISO 8601格式 |
| updatedAt | string | 更新时间，ISO 8601格式 |

> 注意：响应中不包含密码字段，密码以BCrypt加密存储。

**失败响应**

```json
{
  "success": false,
  "message": "验证码错误或已过期",
  "data": null
}
```

**错误码说明**

| 错误信息 | 说明 |
|----------|------|
| 用户名不能为空 | 未提供用户名参数 |
| 用户名长度必须在3-50字符之间 | 用户名长度不符合要求 |
| 邮箱不能为空 | 未提供邮箱参数 |
| 邮箱格式不正确 | 邮箱格式验证失败 |
| 密码不能为空 | 未提供密码参数 |
| 密码长度必须在6-50字符之间 | 密码长度不符合要求 |
| 验证码不能为空 | 未提供验证码参数 |
| 验证码错误或已过期 | 验证码无效 |
| 用户名已被使用 | 用户名已被其他用户注册 |
| 该邮箱已被注册 | 邮箱已被其他用户使用 |
| 注册失败，请稍后重试 | 服务器内部错误 |

---

## 附录

### 用户角色说明

| 角色值 | 角色 | 说明 |
|--------|------|------|
| GUARDIAN | 家属/监护人 | 老年人的家庭成员，可查看关联老人的情绪数据 |
| CAREGIVER | 护理员 | 养老机构工作人员，负责日常照护 |
| ADMIN | 管理员 | 系统管理员，拥有最高权限 |

### 用户状态说明

| 状态值 | 状态 | 说明 |
|--------|------|------|
| ACTIVE | 活跃 | 正常使用的账户 |
| INACTIVE | 禁用 | 被禁用的账户 |
| PENDING_APPROVAL | 待审批 | 新注册账户，等待管理员审批 |

---

*文档由Kiro自动生成*
