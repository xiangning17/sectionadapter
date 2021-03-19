package com.xiangning.sectionadapter.core

/**
 * 分组信息封装
 */
data class SectionInfo<T>(val section: T, var start: Int, var count: Int = 0) {
    val end: Int
        get() = start + count

    operator fun contains(fullPos: Int): Boolean = fullPos in start until start + count

    fun toSectionPos(fullPos: Int) = fullPos - start
    fun toFullPosition(sectionPos: Int) = start + sectionPos
}