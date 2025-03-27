package sample.routes

import jstack.di.DiContext
import jstack.rpc.Router
import jstack.rpc.procedure
import sample.internal.storeInventory

object Store: Router<DiContext>() {
    val inventory by procedure { _: Unit -> storeInventory() }
}