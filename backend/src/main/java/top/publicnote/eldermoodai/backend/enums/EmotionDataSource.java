package top.publicnote.eldermoodai.backend.enums;

/**
 * 情感数据来源枚举
 * 命名为 EmotionDataSource 以避免与 javax.sql.DataSource 命名冲突
 * Requirements: 5.5
 */
public enum EmotionDataSource {
    /**
     * 语音数据
     */
    VOICE,

    /**
     * 图像数据
     */
    IMAGE,

    /**
     * 视频数据
     */
    VIDEO,

    /**
     * 文本数据
     */
    TEXT,

    /**
     * 传感器数据
     */
    SENSOR
}
