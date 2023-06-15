package com.shuxiang.buddymatch.service;

import com.shuxiang.buddymatch.model.domain.UserTeam;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
* @author hushuxiang
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service
* @createDate 2023-06-06 23:30:04
*/
public interface UserTeamService extends IService<UserTeam> {

    public Map<Long, List<UserTeam>> getUserListByTeamId(List<Long> teamIdList);

    public Integer getUserCount(Long teamId);
}
