package doist.ffs.contexts

import react.StateInstance
import react.createContext

val SessionContext = createContext<StateInstance<String?>>()
