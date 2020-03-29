package dev.supergrecko.kllvm.core.typedefs

import dev.supergrecko.kllvm.contracts.Disposable
import dev.supergrecko.kllvm.contracts.Validatable
import dev.supergrecko.kllvm.utils.toBoolean
import dev.supergrecko.kllvm.utils.toInt
import org.bytedeco.javacpp.Pointer
import org.bytedeco.llvm.LLVM.LLVMContextRef
import org.bytedeco.llvm.LLVM.LLVMDiagnosticHandler
import org.bytedeco.llvm.LLVM.LLVMYieldCallback
import org.bytedeco.llvm.global.LLVM

/**
 * Higher level wrapper around llvm::LLVMContext
 *
 * - [llvm::LLVMContext](https://llvm.org/doxygen/classllvm_1_1LLVMContext.html)
 *
 * @throws IllegalArgumentException If any argument assertions fail. Most noticeably functions which involve a context ref.
 */
public class Context internal constructor(internal val llvmCtx: LLVMContextRef) : AutoCloseable, Validatable, Disposable {
    public override var valid: Boolean = true

    /**
     * A LLVM Context has a diagnostic handler. The receiving pointer will be passed to the handler.
     *
     * The C++ code for the DiagnosticHandler looks a little like this.
     *
     * struct DiagnosticHandler {
     *   void *DiagnosticContext = nullptr;
     *   DiagnosticHandler(void *DiagContext = nullptr)
     *     : DiagnosticContext(DiagContext) {}
     * }
     *
     * @param handler The diagnostic handler to use
     * @param diagnosticContext The diagnostic context. Pointer types: DiagnosticContext*
     *
     * - [LLVMContextSetDiagnosticHandler](https://llvm.org/doxygen/group__LLVMCCoreContext.html#gacbfc704565962bf71eaaa549a9be570f)
     *
     * @throws IllegalArgumentException If internal instance has been dropped.
     *
     * TODO: Find out how to actually call this thing from Kotlin/Java
     */
    public fun setDiagnosticHandler(handler: LLVMDiagnosticHandler, diagnosticContext: Pointer) {
        require(valid) { "This module has already been disposed." }

        LLVM.LLVMContextSetDiagnosticHandler(llvmCtx, handler, diagnosticContext)
    }

    /**
     * Sets the diagnostic handler without a specified context.
     *
     * This sets the context to be a nullptr.
     *
     * @param handler The diagnostic handler to use
     *
     * @throws IllegalArgumentException If internal instance has been dropped.
     *
     * TODO: Find out how to actually call this thing from Kotlin/Java
     */
    public fun setDiagnosticHandler(handler: LLVMDiagnosticHandler) {
        setDiagnosticHandler(handler, Pointer())
    }

    /**
     * Get the diagnostic handler for this context.
     *
     * - [LLVMContextGetDiagnosticHandler](https://llvm.org/doxygen/group__LLVMCCoreContext.html#ga4ecfc4310276f36557ee231e22d1b823)
     *
     * @throws IllegalArgumentException If internal instance has been dropped.
     *
     * TODO: Find out how to actually call this thing from Kotlin/Java
     */
    public fun getDiagnosticHandler(): LLVMDiagnosticHandler {
        require(valid) { "This module has already been disposed." }

        return LLVM.LLVMContextGetDiagnosticHandler(llvmCtx)
    }

    /**
     * Register a yield callback with the given context.
     *
     * @param callback Callback to register. C++ Type: void (*)(LLVMContext *Context, void *OpaqueHandle)
     * @param opaqueHandle Pointer types: void*
     *
     * - [LLVMContextSetYieldCallback](https://llvm.org/doxygen/group__LLVMCCoreContext.html#gabdcc4e421199e9e7bb5e0cd449468731)
     *
     * @throws IllegalArgumentException If internal instance has been dropped.
     *
     * TODO: Find out how to actually call this thing from Kotlin/Java
     */
    public fun setYieldCallback(callback: LLVMYieldCallback, opaqueHandle: Pointer) {
        require(valid) { "This module has already been disposed." }

        LLVM.LLVMContextSetYieldCallback(llvmCtx, callback, opaqueHandle)
    }

    /**
     * Retrieve whether the given context will be set to discard all value names.
     *
     * The underlying JNI function returns [Int] to be C compatible, so we will just turn
     * it into a kotlin [Boolean].
     *
     * - [LLVMContextShouldDiscardValueNames](https://llvm.org/doxygen/group__LLVMCCoreContext.html#ga537bd9783e94fa79d3980c4782cf5d76)
     *
     * @throws IllegalArgumentException If internal instance has been dropped.
     */
    public fun shouldDiscardValueNames(): Boolean {
        require(valid) { "This module has already been disposed." }

        val willDiscard = LLVM.LLVMContextShouldDiscardValueNames(llvmCtx)

        // Conversion from C++ bool to kotlin Boolean
        return willDiscard.toBoolean()
    }

    /**
     * Set whether the given context discards all value names.
     *
     * If true, only the names of GlobalValue objects will be available in the IR.
     * This can be used to save memory and runtime, especially in release mode.
     *
     * The underlying JNI function accepts [Int] to be C compatible, so we will just turn
     * it into a kotlin [Boolean].
     *
     * - [LLVMContextSetDiscardValueNames](https://llvm.org/doxygen/group__LLVMCCoreContext.html#ga0a07c702a2d8d2dedfe0a4813a0e0fd1)
     *
     * @throws IllegalArgumentException If internal instance has been dropped.
     */
    public fun setDiscardValueNames(discard: Boolean) {
        require(valid) { "This module has already been disposed." }

        // Conversion from kotlin Boolean to C++ bool
        val intValue = discard.toInt()

        LLVM.LLVMContextSetDiscardValueNames(llvmCtx, intValue)
    }

    /**
     * Dispose the current context reference.
     *
     * Note that after using this, the [context] should not be used again as
     * its LLVM reference has been disposed.
     *
     * Any calls referencing this context after it has been dropped will most likely fail
     * as the inner LLVM Context will be set to a null pointer after
     * this is called.
     *
     * @throws IllegalArgumentException If internal instance has been dropped.
     */
    public override fun dispose() {
        require(valid) { "This module has already been disposed." }

        valid = false

        LLVM.LLVMContextDispose(llvmCtx)
    }

    /**
     * Implementation for AutoCloseable for Context
     *
     * If the JVM ever does decide to auto-close this then
     * the module will be dropped to prevent memory leaks.
     *
     * @throws IllegalArgumentException If internal instance has been dropped.
     */
    public override fun close() = dispose()

    public companion object {
        /**
         * Create a new LLVM context
         *
         * - [LLVMContextCreate](https://llvm.org/doxygen/group__LLVMCCoreContext.html#gaac4f39a2d0b9735e64ac7681ab543b4c)
         */
        @JvmStatic
        public fun create(): Context {
            val llvmContext = LLVM.LLVMContextCreate()

            return Context(llvmContext)
        }

        /**
         * Obtain the global LLVM context
         */
        @JvmStatic
        public fun getGlobalContext(): Context {
            val ctx = LLVM.LLVMGetGlobalContext()

            return Context(ctx)
        }
    }
}