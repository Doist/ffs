package doist.ffs.auth

import doist.ffs.db.Permission

data class Access(val resource: Resource, val permissions: Collection<Permission>)
