# Organizations

## Create organization

Requires an authenticated user. They become an admin of the organization.

```
POST /organizations
```

| Query parameter | Type   | Required | Description               |
|-----------------|--------| -------- |---------------------------|
| `name`          | String | Yes      | Name of the organization. |

On success, responds `201 Created` with an empty body.

## List organizations

Requires an authenticated user.

```
GET /organizations
```

On success, responds `200 OK` with a JSON array containing all organizations for the user.

## Get organization

Requires an authenticated user that is a reader, user, or admin in this organization.

```
GET /organizations/{id}
```

On success, responds `200 OK` with a JSON object for the organization.

## Update organization

Requires an authenticated user that is a user or admin in this organization.

```
PUT /organizations/{id}
```

| Parameter | Type   | Required | Description               |
| --------- |--------| -------- |---------------------------|
| `name`    | String | No       | Name of the organization. |

On success, responds `204 No Content` with an empty body.

## Delete organization

```
DELETE /organizations/{id}
```

Requires an authenticated user that is an admin in this organization.

On success, responds `204 No Content` with an empty body.

## Add user to organization

```
POST /organizations/{id}/users/{user_id}
```

Requires an authenticated user that is an admin in this organization.

| Query parameter | Type                         | Required | Description                 |
|-----------------|------------------------------| -------- | --------------------------- |
| `role`          | "admin", "user", or "reader" | Yes      | Role to assign to the user. |

On success, responds `201 Created` with an empty body.

## Update user role in organization

```
PUT /organizations/{id}/users/{user_id}
```

Requires an authenticated user that is an admin in this organization.

| Query parameter | Type                         | Required | Description                 |
|-----------------|------------------------------| -------- | --------------------------- |
| `role`          | "admin", "user", or "reader" | Yes      | Role to assign to the user. |

On success, responds `204 No Content` with an empty body.

## Remove user from organization

```
DELETE /organizations/{id}/users/{user_id}
```

Requires an authenticated user that is an admin in this organization.

On success, responds `204 No Content` with an empty body.
