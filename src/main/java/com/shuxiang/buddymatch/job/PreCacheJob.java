package com.shuxiang.buddymatch.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shuxiang.buddymatch.common.ResultUtils;
import com.shuxiang.buddymatch.contant.RedisKeyPrefix;
import com.shuxiang.buddymatch.mapper.UserMapper;
import com.shuxiang.buddymatch.model.domain.User;
import com.shuxiang.buddymatch.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 缓存预热
 */
@Component
@Slf4j
public class PreCacheJob {


    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    /**
     * 为推荐用户做缓存预热
     */
    @Scheduled(cron = "0 0 4 * * *")
    public void doPreCacheRecommend(){
        String lockKey = RedisKeyPrefix.PRE_CACHE_LOCK;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if(lock.tryLock(0,3000, TimeUnit.MILLISECONDS)){
                Long userId = 3L;
                String cacheKey = RedisKeyPrefix.RECOMMEND + userId;
                QueryWrapper<User> qw = new QueryWrapper<>();
                Page<User> users = userService.page(new Page<>(1,20),qw);
                redisTemplate.opsForValue().set(cacheKey,users,24,TimeUnit.HOURS);
            }


        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            //如果持有锁，则释放锁
            if(lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }

    }
}
