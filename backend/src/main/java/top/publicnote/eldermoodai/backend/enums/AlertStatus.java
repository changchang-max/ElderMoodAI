package top.publicnote.eldermoodai.backend.enums;

/**
 * 预警处理状态枚举
 * Requirements: 6.6, 6.12, 6.13
 */
public enum AlertStatus {
    /**
     * 待处理
     */
    PENDING,

    /**
     * 已处理
     */
    HANDLED,

    /**
     * 已忽略
     */
    IGNORED
}
