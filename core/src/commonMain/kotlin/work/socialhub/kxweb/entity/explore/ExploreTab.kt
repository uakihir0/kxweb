package work.socialhub.kxweb.entity.explore

import kotlin.js.JsExport

@JsExport
enum class ExploreTab(
    val timelineId: String
) {
    FOR_YOU("VGltZWxpbmU6DAC2CwABAAAAB2Zvcl95b3UAAA=="),
    TRENDING("VGltZWxpbmU6DAC2CwABAAAACHRyZW5kaW5nAAA="),
    NEWS("VGltZWxpbmU6DAC2CwABAAAABG5ld3MAAA=="),
    SPORTS("VGltZWxpbmU6DAC2CwABAAAABnNwb3J0cwAA"),
    ENTERTAINMENT("VGltZWxpbmU6DAC2CwABAAAADWVudGVydGFpbm1lbnQAAA=="),
}
