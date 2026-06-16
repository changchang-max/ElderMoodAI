package top.publicnote.eldermoodai.backend.service;

import top.publicnote.eldermoodai.backend.dto.RegisterRequest;
import top.publicnote.eldermoodai.backend.entity.User;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 注册后的用户
     */
    User register(RegisterRequest request);

    /**
     * 发送注册验证码
     *
     * @param email 邮箱
     */
    void sendRegistrationCode(String email);
}
