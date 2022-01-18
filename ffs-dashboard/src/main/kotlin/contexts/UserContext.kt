package doist.ffs.contexts

import doist.ffs.models.User
import react.StateInstance
import react.createContext

val UserContext = createContext<StateInstance<User?>>()
