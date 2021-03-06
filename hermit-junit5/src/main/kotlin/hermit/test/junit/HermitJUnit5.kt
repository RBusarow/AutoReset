package hermit.test.junit

import hermit.test.*
import org.junit.jupiter.api.AfterEach
import hermit.test.resets as resetsExtension

public abstract class HermitJUnit5(
  delegates: MutableCollection<Resets> = mutableListOf()
) : Hermit(delegates),
  AutoReset {

  @Suppress("FunctionName")
  @AfterEach
  private fun _afterEach() {
    resetAll()
  }

  /**
   * Convenience function which delegates to [ResetManager.resets][resetsExtension] without needing the import.
   *
   * @see resetsExtension
   */
  public inline fun <reified T : Any> resets(
    noinline valueFactory: suspend () -> T = {
      val clazz = T::class

      try {
        clazz.java.getDeclaredConstructor().newInstance()
      } catch (illegal: IllegalAccessException) {
        val obj = clazz.objectInstance

        if (obj != null && obj is Resets) {
          obj
        } else {
          throw LazyResetDelegateObjectException(clazz)
        }
      } catch (abstract: InstantiationException) {
        when {
          clazz.java.isInterface -> throw LazyResetDelegateInterfaceException(clazz)
          clazz.isAbstract -> throw LazyResetDelegateAbstractException(clazz)
          else -> throw LazyResetDelegateNonDefaultConstructorException(clazz)
        }
      }
    }
  ): LazyResets<T> = resetsExtension(valueFactory)
}
