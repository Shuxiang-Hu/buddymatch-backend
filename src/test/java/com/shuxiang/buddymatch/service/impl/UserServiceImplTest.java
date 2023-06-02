package com.shuxiang.buddymatch.service.impl;

import com.shuxiang.buddymatch.model.domain.User;
import com.shuxiang.buddymatch.service.UserService;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;


@SpringBootTest

class UserServiceImplTest {
    @Resource
    UserService userService;

    @Test
    void searchUserByTags() {
        List<String> tagNames = Arrays.asList("java", "python");
        List<User> users = userService.searchUserByTags(tagNames);
        System.out.println(users);
        Assert.assertNotNull(users);
    }
}