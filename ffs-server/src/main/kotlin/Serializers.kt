@file:OptIn(ExperimentalSerializationApi::class)

package doist.ffs

import doist.ffs.db.Flag
import doist.ffs.db.Organization
import doist.ffs.db.Project
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializer

@Serializer(forClass = Organization::class)
object OrganizationSerializer

@Serializer(forClass = Project::class)
object ProjectSerializer

@Serializer(forClass = Flag::class)
object FlagSerializer
