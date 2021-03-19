package com.xiangning.sectionadapter

inline fun <T1, T2> isNotNull(t1: T1?, t2: T2?, block: (T1, T2) -> Unit) {
    if (t1 != null && t2 != null) {
        block(t1, t2)
    }
}