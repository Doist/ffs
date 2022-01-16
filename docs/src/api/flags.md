# Flags

## Create flag

```
POST /projects/{id}/flags
```

Requires an authenticated user that is a user or admin in this flag's organization.

| Query parameter | Type   | Required | Description        |
|-----------------|--------| -------- | ------------------ |
| `name`          | String | Yes      | Name of the flag.  |
| `rule`          | String | Yes      | Rule of the flag.  |

On success, responds `201 Created` with an empty body.

## List flags

```
GET /projects/{id}/flags
```

Requires an authenticated user that is a reader, user, or admin, in this flag's organization.

On success, responds `200 OK` with a JSON array containing all flags for the project.

!!! info

    This endpoint can be used without the project `{id}`, as long it is inferred from the authentication token. Tokens are always associated with a specific project.

## Get flag

```
GET /flags/{id}
```

Requires an authenticated user that is a reader, user, or admin, in this flag's organization.

On success, responds `200 OK` with a JSON object for the flag.

## Update flag

```
PUT /flags/{id}
```

Requires an authenticated user that is a user or admin in this flag's organization.

| Query parameter | Type   | Required | Description       |
|-----------------|--------| -------- | ----------------- |
| `name`          | String | No       | Name of the flag. |
| `rule`          | String | No       | Rule of the flag. |

On success, responds `204 No Content` with an empty body.

## Evaluate flags

```
GET /flags/eval
```

Requires an token access with eval permissions to this flag's project.

| Query parameter | Type   | Required | Description                      |
|-----------------|--------| -------- |----------------------------------|
| `env`           | String | Yes      | JSON-encoded environment string. |

On success, responds `200 OK` with a JSON object mapping flag names to their evaluation.

!!! info

    This endpoint is used without the project `{id}`, since it is inferred from the authentication token. Tokens are always associated with a specific project.

## Archive flag

```
PUT /flags/{id}/archive
```

Requires an authenticated user that is a user or admin in this flag's organization.

On success, responds `204 No Content` with an empty body.


## Unarchive flag

```
DELETE /flags/{id}/archive
```

Requires an authenticated user that is a user or admin in this flag's organization.

On success, responds `204 No Content` with an empty body.
