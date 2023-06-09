package com.shuxiang.buddymatch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shuxiang.buddymatch.common.ErrorCode;
import com.shuxiang.buddymatch.contant.UserConstant;
import com.shuxiang.buddymatch.exception.BusinessException;
import com.shuxiang.buddymatch.mapper.UserMapper;
import com.shuxiang.buddymatch.model.domain.User;
import com.shuxiang.buddymatch.service.UserService;
import com.shuxiang.buddymatch.util.AlgorithmUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    /**
     * salt for pwd
     */
    private static final String SALT = "huhuhu";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. vlaidate
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }

        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return -1;
        }
        // if two pws are the same
        if (!userPassword.equals(checkPassword)) {
            return -1;
        }
        // no duplicate account
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Account already exists");
        }

        // 2. encrypt
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 3. add user
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);

        boolean saveResult = this.save(user);
        if (!saveResult) {
            return -1;
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. validate
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        if (userAccount.length() < 4) {
            return null;
        }
        if (userPassword.length() < 8) {
            return null;
        }
        // account can not contain special char
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return null;
        }
        // 2. encrypt
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // check if user exists
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // user doesnt exist
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            return null;
        }
        // 3. remove sensitive user info
        User safetyUser = getSafetyUser(user);
        // 4. add login state
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }

    /**
     * remove sensitive user info
     *
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser)  {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setProfile(originUser.getProfile());
        safetyUser.setTags(originUser.getTags());

        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        return safetyUser;
    }

    /**
     * user log out
     * @param request
     */
    @Override
    public int userLogout(HttpServletRequest request) {

        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return 1;
    }

    /**
     *  find all users containing specified tags
     * @param tagNames tags the users must have
     * @return
     */
    @Override
    public List<User> searchUserByTags(List<String> tagNames) {
        if(CollectionUtils.isEmpty(tagNames)){
           throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //search and filter by sql
//        QueryWrapper<User> qw = new QueryWrapper<User>();
//        for(String tagName:tagNames){
//            qw.like("tags",tagName);
//        }
//        List<User> users = userMapper.selectList(qw);
        //return users.stream().map(this::getSafetyUser).collect(Collectors.toList());
        //search and filter in memory
        tagNames = tagNames.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        Set<String> tagSet = new HashSet<>(tagNames);

        //System.out.println("Target tags:  " + tagSet);
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.last("limit 20");
        List<User> users = userMapper.selectList(qw);
        List<User> res = new ArrayList<>();
        Gson gson = new Gson();

        for(User user: users){

            String tags = user.getTags();
            if(tags == null) continue;;
            tags = tags.toLowerCase();
            //System.out.println("User: " + user.getId() );

            Set<String> userTags = gson.fromJson(tags, new TypeToken<Set<String>>() {
            }.getType());
            userTags = Optional.ofNullable(userTags).orElse(new HashSet<>());
            userTags.retainAll(tagSet);

            if(userTags.size() == tagSet.size()){
                res.add(getSafetyUser(user));
            }
            //System.out.println("User tags: " + userTags );
        }

        //System.out.println("search by tag result: " + res);
        return res;


    }

    /**
     * is admin sending request
     *
     * @param request
     * @return
     */
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User user = (User) userObj;
        return isAdmin(user);
    }

    @Override
    public int updateUser(User user, HttpServletRequest request) {
        //1.判断用户权限，只有管理员和用户自己可以修改用户信息
        User loginUser = getLoginUser(request);
        if(loginUser == null){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
//        System.out.println("is admin: " + isAdmin(request));
//        System.out.println("user id: " + user.getId());
//        System.out.println("login user id: " + loginUser.getId());
        if(!isAdmin(request) && !Objects.equals(user.getId(), loginUser.getId())){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        //2. 判断用户id是否合法
        Long id = user.getId();
        if(id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //3.查询旧用户
        User oldUser = userMapper.selectById(id);
        if(oldUser == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }


        return userMapper.updateById(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        if(request == null){
            return null;
        }

        return (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
    }


    @Override
    public List<User> getRecommendList() {
        QueryWrapper<User> qw = new QueryWrapper<>();
        List<User> users = userMapper.selectList(qw);
        users = users.stream().map((this::getSafetyUser)).collect(Collectors.toList());
        return users;
    }

    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getUserRole() == UserConstant.ADMIN_ROLE;
    }

    /**
     *
     * @return 与当前登录用户匹配度高的用户列表
     */
    @Override
    public List<User> matchUsers(long num, User loginUser) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "tags");
        queryWrapper.isNotNull("tags");
        //queryWrapper.last("limit " +  40);
        List<User> userList = this.list(queryWrapper);
        String  tags = loginUser.getTags();
        if(tags == null || tags.equals("")){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户未设置标签");
        }
        tags = tags.toLowerCase();
        //System.out.println("Tags: " + tags);
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        // 用户列表的下标 => 相似度
        List<Pair<User, Double>> list = new ArrayList<>();
        // 依次计算所有用户和当前用户的相似度
        for (User user : userList) {
            String userTags = user.getTags().toLowerCase();
            //System.out.println("Current user: " + user);
            // 无标签或者为当前用户自己
            if (StringUtils.isBlank(userTags) || Objects.equals(user.getId(), loginUser.getId())) {
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            // 计算分数
            double dice = AlgorithmUtils.dice(tagList, userTagList);
            //System.out.println(dice);
            list.add(new Pair<>(user, dice));
        }
        // 按dice由大到小排序
        List<Pair<User, Double>> topUserPairList = list.stream()
                .sorted(Comparator.comparingDouble((Pair<User, Double> pair) -> pair.getValue()).reversed())
                .limit(num)
                .collect(Collectors.toList());
        // 原本顺序的 userId 列表
        List<Long> userIdList = topUserPairList.stream().map(pair -> pair.getKey().getId()).collect(Collectors.toList());
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userIdList);
        // 1, 3, 2
        // User1、User2、User3
        // 1 => User1, 2 => User2, 3 => User3
        Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper)
                .stream()
                .map(this::getSafetyUser)
                .collect(Collectors.groupingBy(User::getId));
        List<User> finalUserList = new ArrayList<>();
        for (Long userId : userIdList) {
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
        return finalUserList;
    }

}




