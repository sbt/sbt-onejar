package com.github.retronym

import java.io.File

private[retronym] object Compat {
  def cacheStoreFactory(base: File) = sbt.util.CacheStoreFactory(base)
}
