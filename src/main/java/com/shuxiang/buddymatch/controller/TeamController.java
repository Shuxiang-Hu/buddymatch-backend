package com.shuxiang.buddymatch.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shuxiang.buddymatch.common.BaseResponse;
import com.shuxiang.buddymatch.common.ErrorCode;
import com.shuxiang.buddymatch.common.ResultUtils;
import com.shuxiang.buddymatch.exception.BusinessException;
import com.shuxiang.buddymatch.model.domain.Team;
import com.shuxiang.buddymatch.model.domain.User;
import com.shuxiang.buddymatch.model.domain.UserTeam;
import com.shuxiang.buddymatch.model.dto.TeamQuery;
import com.shuxiang.buddymatch.model.request.*;
import com.shuxiang.buddymatch.model.vo.TeamUserVO;
import com.shuxiang.buddymatch.service.TeamService;
import com.shuxiang.buddymatch.service.UserService;
import com.shuxiang.buddymatch.service.UserTeamService;
import org.apache.ibatis.io.ResolverUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/team")
@CrossOrigin(origins = {"http://localhost:5173"},allowCredentials = "true")
//@CrossOrigin
public class TeamController {

    @Resource
    private TeamService teamService;
    @Resource
    private UserService userService;
    @Resource
    private UserTeamService userTeamService;

    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request){
        if(teamAddRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest,team);
        User loginUser = userService.getLoginUser(request);
        long saveId = teamService.addTeam(team,loginUser);

        return ResultUtils.success(saveId);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody TeamDeleteRequest deleteRequest, HttpServletRequest request){

        long  id = deleteRequest.getId();
        if(id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getLoginUser(request);
        boolean delete = teamService.deleteTeam(id,loginUser);

        if(!delete){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除队伍失败");
        }

        return ResultUtils.success(true);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest,HttpServletRequest request){
        if(teamUpdateRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean update = teamService.updateTeam(teamUpdateRequest,loginUser);

        if(!update){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新队伍失败");
        }

        return ResultUtils.success(true);
    }

    @GetMapping("/get")
    public BaseResponse<Team> getTeamById(long id){
        if(id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);


        if(team == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"队伍不存在");
        }

        return ResultUtils.success(team);
    }

    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> getTeamList(TeamQuery teamQuery,HttpServletRequest request){
        if(teamQuery ==  null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if(userService.getLoginUser(request) == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }


        List<TeamUserVO> teamUserVOList = teamService.getTeamList(teamQuery,userService.isAdmin(request));
        List<Long> teamIdList = teamUserVOList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        try{
            User loginUser = userService.getLoginUser(request);
            queryWrapper.eq("userId",loginUser.getId());
            queryWrapper.in("teamId",teamIdList);
            List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
            Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            teamUserVOList.forEach(teamUserVO ->{
                boolean hasJoin = hasJoinTeamIdSet.contains(teamUserVO.getId());
                teamUserVO.setHasJoin(hasJoin);
            });
        } catch (Exception e){
            return ResultUtils.success(teamUserVOList);
        }
        Map<Long, List<UserTeam>> userListByTeamId = userTeamService.getUserListByTeamId(teamIdList);

        teamUserVOList.forEach(teamUserVO ->{
            Integer hasJoinCount = Optional.
                    ofNullable(userListByTeamId.get(teamUserVO.getId())).
                    orElse(Collections.EMPTY_LIST).
                    size();
            teamUserVO.setHasJoinNum(hasJoinCount);
        });

        return ResultUtils.success(teamUserVOList);
    }


    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest,HttpServletRequest request){
        if(teamJoinRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.joinTeam(teamJoinRequest,loginUser);

        return ResultUtils.success(result);

    }

    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request){
        if(teamQuitRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.quitTeam(teamQuitRequest,loginUser);

        return ResultUtils.success(result);

    }

    /**
     * 获取当前用户创建的队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVO>> getCreateTeamList(TeamQuery teamQuery,HttpServletRequest request){
        if(teamQuery ==  null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        teamQuery.setUserId(loginUser.getId());

        List<TeamUserVO> teamUserVOList = teamService.getTeamList(teamQuery,true);

        return ResultUtils.success(teamUserVOList);
    }

    /**
     * 获取当前用户加入的队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVO>> getJoinTeamList(TeamQuery teamQuery,HttpServletRequest request){
        System.out.println("get join list !!!!!!!!");
        if(teamQuery ==  null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<UserTeam> qw = new QueryWrapper<>();
        qw.eq("userId",loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(qw);
        System.out.println("userTeamList: " + userTeamList);
        Map<Long, List<UserTeam>> longListMap = userTeamList.stream().
                collect(Collectors.groupingBy(UserTeam::getTeamId));

        List<Long> teamIdList = new ArrayList<>(longListMap.keySet());
        System.out.println("Team Id list: " + teamIdList);
        teamQuery.setIdList(teamIdList);
        List<TeamUserVO> teamUserVOList = teamService.getTeamList(teamQuery,true);

        Map<Long, List<UserTeam>> userListByTeamId = userTeamService.getUserListByTeamId(teamIdList);

        teamUserVOList.forEach(teamUserVO ->{
            Integer hasJoinCount = Optional.
                    ofNullable(userListByTeamId.get(teamUserVO.getId())).
                    orElse(Collections.EMPTY_LIST).
                    size();
            teamUserVO.setHasJoinNum(hasJoinCount);
        });
        return ResultUtils.success(teamUserVOList);
    }
}
