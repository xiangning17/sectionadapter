package com.xiangning.sectionadapter

import kotlin.math.sign

data class SectionInfo<T>(val section: T, val position: Int, val count: Int)

/**
 * 记录每个分组(Section)在总列表中的位置信息的工具类，根据访问数据的连续性，可高效的定位给定位置所在的分组
 */
class SectionListCounter<T : Any> {

    private data class HitInfo<T>(
        var hitPos: Int = 0,
        var index: Int = 0,
        var key: T? = null,
        var start: Int = 0,
        var end: Int = 0
    ) {
        val valid: Boolean
            get() = key != null
    }

    private var hitInfo = HitInfo<T>()

    private val indexes = LinkedHashMap<T, Int>()
    private val positionList = mutableListOf(0)
    private val keyList = mutableListOf<T>()

    fun register(key: T, size: Int = 0) {
        register(-1, key, size)
    }

    fun register(index: Int, key: T, size: Int = 0) {
        if (key !in indexes) {
            // 根据index选定将要插入到列表的位置，index或者末尾
            val insert = if (index in keyList.indices) index else keyList.size
            // 插入到指定位置
            keyList.add(insert, key)
            // 记录key的index到map，便于根据key索引快速查找其在列表的index
            indexes[key] = insert
            // 更新位于插入位置后的key在map中的存储值
            for (i in insert + 1 until keyList.size) {
                keyList[i].let { indexes[it] = indexes[it]!! + 1 }
            }

            // 插入占位节点，pos与前节点一致，代表size为0，后面setSize更新为正确的size
            positionList.add(insert + 1, positionList[insert])
        }

        setSize(key, size)
    }

    fun unregister(key: T) {
        indexes.remove(key)?.let { index ->
            keyList.removeAt(index)
            val size = positionList[index + 1] - positionList[index]
            positionList.removeAt(index + 1)
            if (size != 0) {
                for (i in index + 1 until positionList.size) {
                    positionList[i] -= size
                }

                hitInfo.key = null
            }
        }
    }

    fun setSize(key: T, size: Int) {
        val index = indexes[key] ?: throw RuntimeException("$key is not register!")
        val count = positionList[index + 1] - positionList[index]
        val delta = size - count
        if (delta != 0) {
            for (i in index + 1 until positionList.size) {
                positionList[i] += delta
            }
        }

        hitInfo.key = null
    }

    fun getSize(key: T) = indexes[key]?.let { positionList[it + 1] - positionList[it] } ?: 0

    fun getSection(findPos: Int): T? {
        if (findPos !in 0 until getCount()) {
            return null
        }

        return posInfo(findPos).key
    }

    fun getSectionInfo(key: T): SectionInfo<T>? {
        return indexes[key]?.let {
            SectionInfo(
                key,
                positionList[it],
                positionList[it + 1] - positionList[it]
            )
        }
    }

    fun getSectionInfo(findPos: Int): SectionInfo<T>? {
        if (findPos !in 0 until getCount()) {
            return null
        }

        return posInfo(findPos).run {
            SectionInfo(
                key!!,
                start,
                end - start
            )
        }
    }

    private fun posInfo(findPos: Int): HitInfo<T> {
        with(hitInfo) {
            if (hitInfo.valid) {
                if (findPos in start until end) {
                    return this
                }

                // 未命中，key置空
                key = null

                // 朝正向或者反向检查至多三个
                val step = (findPos - hitPos).sign
                for (i in 1..3) {
                    val findIndex = index + step * i
                    if (findIndex !in 0 until keyList.size) break
                    if (findPos in positionList[findIndex] until positionList[findIndex + 1]) {
                        // logd("hit cache $findPos: offset[${findIndex - index}]")
                        hitPos = findPos
                        index = findIndex
                        key = keyList[findIndex]
                        start = positionList[findIndex]
                        end = positionList[findIndex + 1]
                        return this
                    }
                }

            }

            var findIndex = positionList.binarySearch(findPos)
            if (findIndex < 0) {
                findIndex = -findIndex - 2
            }
            hitPos = findPos
            index = findIndex
            key = keyList[index]
            start = positionList[index]
            end = positionList[index + 1]
            return this
        }

    }

    fun getSectionCount() = keyList.size

    fun getCount() = positionList.last()
}