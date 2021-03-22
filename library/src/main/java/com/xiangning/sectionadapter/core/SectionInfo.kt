package com.xiangning.sectionadapter.core

/**
 * 分组信息封装
 */
class SectionInfo<T>
@JvmOverloads
constructor(val section: T, start: Int, count: Int = 0) {
    var start: Int = start
        set(value) {
            end += value - start
            field = value
        }
    var end: Int = start + count
        private set

    var count: Int
        get() = end - start
        set(value) {
            end = start + value
        }

    operator fun contains(fullPos: Int): Boolean = fullPos in start until end

    fun toSectionPos(fullPos: Int) = fullPos - start
    fun toFullPosition(sectionPos: Int) = start + sectionPos
}