package com.xiangning.sectionadapter.core

/**
 * 记录每个分组(Section)在总列表中的位置信息的工具类，根据访问数据的连续性，可高效的定位给定位置所在的分组
 */
class SectionListCounter<T : Any> {

    private data class HitInfo<T>(
        var sectionInfo: SectionInfo<T>? = null,
        var index: Int = -1,
        var advance: Boolean = true
    )

    private val hitInfo = HitInfo<T>()

    private val indexes = LinkedHashMap<T, Int>()
    private val sectionInfoList = mutableListOf<SectionInfo<T>>()

    fun register(section: T, size: Int = 0) {
        register(-1, section, size)
    }

    fun register(index: Int, section: T, size: Int = 0) {
        if (section in indexes) {
            setSize(section, size)
            return
        }

        // 根据index选定将要插入到列表的位置，index或者末尾
        val insert = if (index in sectionInfoList.indices) index else sectionInfoList.size

        // 记录key的index到map，便于根据key索引快速查找其在列表的index
        indexes[section] = insert
        // 插入位置后面的元素位置加1，更新start
        for (i in insert until sectionInfoList.size) {
            indexes[sectionInfoList[i].section] = i + 1
            sectionInfoList[i].start += size
        }

        // 根据前项计算开始位置
        val pos = sectionInfoList.getOrNull(insert - 1)?.end ?: 0
        // 插入占位节点，但不设置size，后面使用setSize更新
        sectionInfoList.add(insert, SectionInfo(section, pos, size))
    }

    fun unregister(section: T) {
        indexes.remove(section)?.let { index ->
            val info = sectionInfoList.removeAt(index)
            val size = info.count

            for (i in index until sectionInfoList.size) {
                indexes[sectionInfoList[i].section] = i
                sectionInfoList[i].start -= size
            }

            if (info === hitInfo.sectionInfo) {
                hitInfo.sectionInfo = null
            }
        }
    }

    fun setSize(section: T, size: Int) {
        val index = indexes[section] ?: throw RuntimeException("$section is not register!")
        val info = sectionInfoList[index]
        val delta = size - info.count
        if (delta != 0) {
            for (i in index + 1 until sectionInfoList.size) {
                sectionInfoList[i].start += delta
            }
        }
        info.count = size
    }

    fun getSize(section: T) = getSectionInfo(section)?.count ?: 0

    fun getSectionInfo(section: T): SectionInfo<T>? {
        return indexes[section]?.let { sectionInfoList[it] }
    }

    fun getSectionByPos(pos: Int) = getSectionInfoByPos(pos)?.section

    fun getSectionInfoByPos(pos: Int): SectionInfo<T>? {
        if (pos !in 0 until getCount()) {
            return null
        }

        return findSectionInfoByPos(pos)
    }

    private fun findSectionInfoByPos(pos: Int): SectionInfo<T> {
        hitInfo.sectionInfo?.let { info ->
            if (pos in info) {
                return info
            }

            // 读取Section在列表中的位置
            val base = hitInfo.index
            // 根据方向选中查找顺序
            val neighbours = if (hitInfo.advance) intArrayOf(1, -1) else intArrayOf(-1, 1)
            for (i in neighbours) {
                val curIndex = base + i
                val neighbour = sectionInfoList.getOrNull(curIndex) ?: continue
                if (pos in neighbour) {
                    hitInfo.apply {
                        sectionInfo = neighbour
                        index = curIndex
                        advance = curIndex > base
                    }
                    return neighbour
                }
            }
        }

        // 二分查找
        var low = 0
        var high = sectionInfoList.size - 1

        while (low <= high) {
            val mid = (low + high).ushr(1) // safe from overflows
            val midVal = sectionInfoList[mid]
            if (pos in midVal) {
                hitInfo.apply {
                    sectionInfo = midVal
                    index = mid
                }
                return midVal
            } else if (pos < midVal.start) {
                high = mid - 1
            } else {
                low = mid + 1
            }
        }

        throw RuntimeException("section not found! pos = $pos")
    }

    fun getSectionListSize() = sectionInfoList.size

    fun getCount() = sectionInfoList.last().end
}