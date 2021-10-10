@file:OptIn(ExperimentalSerializationApi::class)

package doist.ffs

import doist.ffs.db.Organization
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializer

@Serializer(forClass = Organization::class)
object OrganizationSerializer
