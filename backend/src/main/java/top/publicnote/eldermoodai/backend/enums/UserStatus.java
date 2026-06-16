package top.publicnote.eldermoodai.backend.enums;

/**
 * 用户状态枚举
 * Requirements: 2.3
 */
public enum UserStatus {
    /**
     * 活跃状态
     */
    ACTIVE,

    /**
     * 非活跃/禁用状态
     */
    INACTIVE,

    /**
     * 待审批状态（注册后默认状态）
     */
    PENDING_APPROVAL
}
