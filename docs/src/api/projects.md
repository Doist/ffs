# Projects

## Create project

```
POST /organizations/{id}/projects
```

Requires an authenticated user that is a user or admin in this project's organization.

| Query parameter | Type   | Required | Description          |
|-----------------|--------| -------- |----------------------|
| `name`          | String | Yes      | Name of the project. |

On success, responds `201 Created` with an empty body.

## List projects

```
GET /organizations/{id}/projects
```

Requires an authenticated user that is a reader, user, or admin, in this project's organization.

On success, responds `200 OK` with a JSON array containing all projects for the organization.

## Get project

```
GET /project/{id}
```

Requires an authenticated user that is a reader, user, or admin, in this project's organization.

On success, responds `200 OK` with a JSON object for the project.

## Update project

```
PUT /projects/{id}
```

Requires an authenticated user that is a user or admin in this project's organization.

| Query parameter | Type   | Required | Description               |
|-----------------|--------| -------- |---------------------------|
| `name`          | String | No       | Name of the organization. |

On success, responds `204 No Content` with an empty body.

## Delete project

Requires an authenticated user that is an admin in this project's organization.

```
DELETE /projects/{id}
```

On success, responds `204 No Content` with an empty body.
