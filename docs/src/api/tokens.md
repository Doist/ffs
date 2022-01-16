# Tokens

## Create token for project

```
POST /projects/{id}/tokens
```

Requires an authenticated user that is a user or admin in this project's organization.

| Query parameter | Type             | Required | Description               |
|-----------------|------------------| -------- |---------------------------|
| `permission`    | "eval" or "read" | Yes      | Scope of the token.       |
| `description`   | String           | Yes      | Description of the token. |

On success, responds `201 Created` with the token in the body.

## Get tokens for project

```
GET /projects/{id}/tokens
```

Requires an authenticated user that is a reader, user, or admin, in this project's organization.

| Query parameter | Type                         | Required | Description                 |
|-----------------|------------------------------| -------- | --------------------------- |
| `role`          | "admin", "user", or "reader" | Yes      | Role to assign to the user. |

On success, responds `200 OK` with a JSON array containing non-sensitive token data.

## Update token description

```
PUT /tokens/{id}
```

Requires an authenticated user that is a user or admin in this project's organization.

| Query parameter | Type   | Required | Description               |
|-----------------|--------|----------|---------------------------|
| `description`   | String | No       | Description of the token. |

On success, responds `204 No Content` with an empty body.

## Delete token

```
DELETE /tokens/{id}
```

Requires an authenticated user that is a user or admin in this project's organization.

On success, responds `204 No Content` with an empty body.

