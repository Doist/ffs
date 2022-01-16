# Users

## Register

```
POST /users/register
```

No prior authentication required.

| Query parameter | Type   | Required | Description           |
|-----------------|--------| -------- | --------------------- |
| `name`          | String | Yes      | Name of the user.     |
| `email`         | String | Yes      | Email of the user.    |
| `password`      | String | Yes      | Password of the user. |

On success, responds `201 Created` with an empty body.

## Login

```
POST /users/login
```

No prior authentication required.

| Query parameter | Type   | Required | Description           |
|-----------------|--------| -------- | --------------------- |
| `email`         | String | Yes      | Email of the user.    |
| `password`      | String | Yes      | Password of the user. |

On success, responds `200 OK` with a JSON object with the user data.

## Logout

No prior authentication required, although it's a no-op if the user is not authenticated.

```
POST /users/logout
```

Responds `302 Found` and redirects to `/`.

## Update user

Requires an authenticated user.

```
PUT /users/{id}
```

| Query parameter    | Type   | Required                              | Description               |
|--------------------|--------|---------------------------------------| ------------------------- |
| `name`             | String | No                                    | Name of the user.         |
| `email`            | String | No                                    | Email of the user.        |
| `password`         | String | No                                    | Password of the user.     |
| `current_password` | String | If `email` or `password` are provided | Current user password.    |

On success, responds `204 No Content` with an empty body.

## Delete user

Requires an authenticated user.

```
DELETE /users/{id}
```

| Query parameter    | Type   | Required | Description               |
|--------------------|--------|----------| ------------------------- |
| `current_password` | String | Yes      | Current user password.    |

On success, responds `302 Found` and redirects to `/`.
