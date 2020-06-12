package dev.supergrecko.vexe.llvm.unit.ir

import dev.supergrecko.vexe.llvm.ir.PassRegistry
import dev.supergrecko.vexe.test.TestSuite
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

internal class PassRegistryTest : TestSuite({
    describe("Pass Registry acts as a singleton") {
        val p1 = PassRegistry()
        val p2 = PassRegistry()

        assertEquals(p1.ref, p2.ref)
    }
})