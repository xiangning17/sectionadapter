package com.xiangning.sectionadapter.core

data class Linker<T : Any>(val clazz: Class<T>, val binder: ItemBinder<T, *>)