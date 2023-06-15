package com.shuxiang.buddymatch.service;

import com.shuxiang.buddymatch.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shuxiang.buddymatch.model.domain.User;
import com.shuxiang.buddymatch.model.dto.TeamQuery;
import com.shuxiang.buddymatch.model.request.TeamJoinRequest;
import com.shuxiang.buddymatch.model.request.TeamQuitRequest;
import com.shuxiang.buddymatch.model.request.TeamUpdateRequest;
import com.shuxiang.buddymatch.model.vo.TeamUserVO;

import java.util.List;

/**
* @author hushuxiang
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2023-06-06 23:28:17
*/
public interface TeamService extends IService<Team> {


    /**
     * 新增队伍
     * @param team
     * @param loginUser
     * @return
     */
    public long addTeam(Team team, User loginUser);

    public List<TeamUserVO> getTeamList(TeamQuery teamQuery,boolean isAdmin);

    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    public boolean deleteTeam(long id, User loginUser);
}
