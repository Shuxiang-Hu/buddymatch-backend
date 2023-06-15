package com.shuxiang.buddymatch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shuxiang.buddymatch.common.ErrorCode;
import com.shuxiang.buddymatch.exception.BusinessException;
import com.shuxiang.buddymatch.model.domain.UserTeam;
import com.shuxiang.buddymatch.service.UserTeamService;
import com.shuxiang.buddymatch.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author hushuxiang
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2023-06-06 23:30:04
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

    @Override
    public Map<Long, List<UserTeam>> getUserListByTeamId(List<Long> teamIdList) {
        QueryWrapper<UserTeam> qw = new QueryWrapper<>();
        qw.in("teamId",teamIdList);
        List<UserTeam> utListByTeamId = this.list();

        //队伍id =》 加入的UserTeam列表
        return utListByTeamId.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));


    }

    @Override
    public Integer getUserCount(Long teamId) {
        if(teamId == null || teamId <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<UserTeam> qw = new QueryWrapper<>();
        qw.eq("teamId",teamId);


        return Math.toIntExact(this.count(qw));


    }
}




