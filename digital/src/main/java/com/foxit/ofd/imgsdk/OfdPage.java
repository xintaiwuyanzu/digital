package com.foxit.ofd.imgsdk;

/**
 * @brief OfdPage
 */
public class OfdPage {
    /**
     * @return 无.
     * @brief 设置页面大小
     * @param[in] page            页面对象。
     * @param[in] x                页面的X坐标。
     * @param[in] y                页面的Y坐标。
     * @param[in] width            页面的宽度。
     * @param[in] height            页面的高度。
     */
    public static native void setSize(long page, float x, float y, float width, float height);

}
