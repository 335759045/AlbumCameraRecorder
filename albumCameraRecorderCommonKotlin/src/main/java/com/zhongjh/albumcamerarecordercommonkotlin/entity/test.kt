package com.zhongjh.albumcamerarecordercommonkotlin.entity

/**
 * @author 8
 * @date 2021/11/17
 */
class test(a: String?, b: String?) {
    init {
        checkNotNull(a) { "11" }
    }
}