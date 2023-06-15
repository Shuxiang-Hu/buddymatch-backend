package com.shuxiang.buddymatch.once;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.Gson;
import com.shuxiang.buddymatch.mapper.UserMapper;
import com.shuxiang.buddymatch.model.domain.User;
import com.shuxiang.buddymatch.service.UserService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.*;

/**
 * 导入用户任务
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Component
public class InsertUsers {

    @Resource
    private UserService userService;

    /**
     * 批量插入用户
     */
    //@Scheduled(initialDelay = 5000, fixedRate = Long.MAX_VALUE)
    public void doInsertUsers() {
//        QueryWrapper<User> qw = new QueryWrapper<>();
//        qw.eq("userAccount","fakeUser");
//        userService.remove(qw);
        StopWatch stopWatch = new StopWatch();
        System.out.println("goodgoodgood");
        stopWatch.start();
        final int INSERT_NUM = 1000000;
        List<User> users = new LinkedList<>();
        for (int i = 0; i < INSERT_NUM; i++) {
            Random random = new Random();
            User user = new User();
            user.setUsername("假用户");
            user.setUserAccount("fakeUser"+ UUID.randomUUID() + "i");
            user.setAvatarUrl("https://636f-codenav-8grj8px727565176-1256524210.tcb.qcloud.la/img/logo.png");
            user.setGender(0);
            user.setUserPassword("12345678");
            user.setPhone("123");
            user.setEmail("123@qq.com");
            user.setProfile("我是一个测试用的假用户");

            user.setUserStatus(0);
            user.setUserRole(0);
            String[] tags = new String[]{"java","Python","c++","c"};
            HashSet<String> tagSet = new HashSet<>();

            for(String tag:tags){
                if(random.nextDouble() > 0.5){
                    tagSet.add(tag);
                }
            }
            String gender = random.nextDouble() > 0.5 ? "男" :  "女";
            tagSet.add(gender);
            Gson gson = new Gson();
            user.setTags(gson.toJson(tagSet));


            users.add(user);
            System.out.println("已创建的用户数量："+(i+1));

        }
        userService.saveBatch(users,100000);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}
