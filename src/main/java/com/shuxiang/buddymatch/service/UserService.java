package com.shuxiang.buddymatch.service;

import com.shuxiang.buddymatch.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shuxiang.buddymatch.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户服务
 *
 * @author yupi
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码

     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    User getSafetyUser(User originUser);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);

    /**
     *  find all users containing specified tags
     * @param tags tags the users must have
     * @return
     */
    public List<User> searchUserByTags(List<String> tags);


    public boolean isAdmin(HttpServletRequest request);

    public int updateUser(User user, HttpServletRequest request);

    public User getLoginUser(HttpServletRequest request);

    List<User> getRecommendList();

    public boolean isAdmin(User loginUser);

    public List<User> matchUsers(long num, User loginUser);
}
