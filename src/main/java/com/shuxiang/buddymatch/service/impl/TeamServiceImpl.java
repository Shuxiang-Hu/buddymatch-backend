package com.shuxiang.buddymatch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shuxiang.buddymatch.common.ErrorCode;
import com.shuxiang.buddymatch.contant.RedisKeyPrefix;
import com.shuxiang.buddymatch.exception.BusinessException;
import com.shuxiang.buddymatch.model.domain.Team;
import com.shuxiang.buddymatch.model.domain.User;
import com.shuxiang.buddymatch.model.domain.UserTeam;
import com.shuxiang.buddymatch.model.dto.TeamQuery;
import com.shuxiang.buddymatch.model.enums.TeamStatusEnum;
import com.shuxiang.buddymatch.model.request.TeamJoinRequest;
import com.shuxiang.buddymatch.model.request.TeamQuitRequest;
import com.shuxiang.buddymatch.model.request.TeamUpdateRequest;
import com.shuxiang.buddymatch.model.vo.TeamUserVO;
import com.shuxiang.buddymatch.model.vo.UserVO;
import com.shuxiang.buddymatch.service.TeamService;
import com.shuxiang.buddymatch.mapper.TeamMapper;
import com.shuxiang.buddymatch.service.UserService;
import com.shuxiang.buddymatch.service.UserTeamService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
* @author hushuxiang
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2023-06-06 23:28:17
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService{
    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {
        //1. 请求参数是否为空
        if(team == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //2.用户是否登录
        if(loginUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        //3. 最大人数应大于1，小于等于20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if(maxNum <=1 || maxNum > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍人数不满足要求");
        }

        //4. 队伍名称不能为空，也不能超过20个字符
        String teamName = team.getName();
        if(StringUtils.isBlank(teamName) || teamName.length() > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍名称不符合要求");
        }

        //5. 描述 <= 512
        String description = team.getDescription();
        if(StringUtils.isBlank(description ) && description.length() > 512){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍描述不符合要求");
        }

        //6.状态值不能小于0
        int status = team.getStatus();
        TeamStatusEnum teamStatus = TeamStatusEnum.getStatusByValue(status);
        if(teamStatus  == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍状态不满足要求");
        }

        //7. 加密队伍必须有密码且密码长度<=32
        String password = team.getPassword();
        if(teamStatus.equals(TeamStatusEnum.SECRET) && (StringUtils.isBlank(password) || password.length() > 32)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码不符合要求");
        }


        // 6. 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if (new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "超时时间 > 当前时间");
        }

        final long userId = loginUser.getId();
        // 7. 校验用户最多创建 5 个队伍
        RLock rLock = redissonClient.getLock(RedisKeyPrefix.ADD_TEAM +
                RedisKeyPrefix.USER_ID +
                loginUser.getId());

        try{
            while(true){
                boolean tried = rLock.tryLock(0,3000, TimeUnit.MILLISECONDS);
                if(!tried) continue;
                QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("userId", userId);
                long hasTeamNum = this.count(queryWrapper);
                if (hasTeamNum >= 5) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户最多创建 5 个队伍");
                }
                break;
            }
        } catch (InterruptedException e){
            throw new RuntimeException();
        } finally {
            rLock.unlock();
        }


        // 8. 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId = team.getId();
        if (!result || teamId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }
        // 9. 插入用户  => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "创建队伍失败");
        }

        return teamId;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<TeamUserVO> getTeamList(TeamQuery teamQuery, boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        // 组合查询条件
        if (teamQuery != null) {
            Long id = teamQuery.getId();
            if (id != null && id > 0) {
                queryWrapper.eq("id", id);
            }
            List<Long> idList = teamQuery.getIdList();
            if (CollectionUtils.isNotEmpty(idList)) {
                queryWrapper.in("id", idList);
            }
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(qw -> qw.like("name", searchText).or().like("description", searchText));
            }
            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like("name", name);
            }
            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)) {
                queryWrapper.like("description", description);
            }
            Integer maxNum = teamQuery.getMaxNum();
            // 查询最大人数相等的
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq("maxNum", maxNum);
            }
            Long userId = teamQuery.getUserId();
            // 根据创建人来查询
            if (userId != null && userId > 0) {
                queryWrapper.eq("userId", userId);
            }
            // 根据状态来查询
            Integer status = teamQuery.getStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getStatusByValue(status);
//            if (statusEnum == null) {
//                statusEnum = TeamStatusEnum.PUBLIC;
//            }
            if (!isAdmin && statusEnum.equals(TeamStatusEnum.PRIVATE)) {
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
            if(statusEnum != null){
                queryWrapper.eq("status", statusEnum.getValue());
            }
        }
        // 不展示已过期的队伍
        // expireTime is null or expireTime > now()
        queryWrapper.and(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));
        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        // 关联查询创建人的用户信息
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }
            User user = userService.getById(userId);
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            // 脱敏用户信息
            if (user != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                teamUserVO.setCreateUser(userVO);
            }
            teamUserVO.setHasJoinNum(userTeamService.getUserCount(team.getId()));
            teamUserVOList.add(teamUserVO);
        }
        return teamUserVOList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        if(teamUpdateRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = teamUpdateRequest.getId();
        if(id == null || id <0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"非法的队伍Id");
        }

        Team oldTeam = this.getById(id);
        if(oldTeam == null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍不存在");
        }

        if(!oldTeam.getId().equals(loginUser.getId()) && userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        Team team = new Team();
        BeanUtils.copyProperties(teamUpdateRequest,team);
        return this.updateById(team);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }


        Long userId = loginUser.getId();
        Long teamId = teamJoinRequest.getTeamId();
//        RLock userLock = redissonClient.getLock(RedisKeyPrefix.ADD_TEAM +
//                RedisKeyPrefix.USER_ID +
//                loginUser.getId());
//
//        RLock teamLock = redissonClient.getLock(RedisKeyPrefix.ADD_TEAM +
//                RedisKeyPrefix.USER_ID +
//                teamId);
        QueryWrapper<UserTeam> qw;


//        boolean triedUserLock = userLock.tryLock(0, 3000, TimeUnit.MILLISECONDS);
//        boolean triedTeamLock = teamLock.tryLock(0, 3000, TimeUnit.MILLISECONDS);
//        if (!(triedUserLock && triedTeamLock)){
//            userLock.unlock();
//            teamLock.unlock();
//            continue;
//        }
        qw = new QueryWrapper<>();
        qw.eq("userId", userId);
        long count = userTeamService.count(qw);
        if (count > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多加入五个队伍");
        }


        Team team = this.getById(teamId);

        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }

        Date expireTime = team.getExpireTime();
        if (expireTime != null && expireTime.before(new Date())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已过期");
        }

        Integer teamStatus = team.getStatus();
        if (teamStatus.equals(TeamStatusEnum.PRIVATE.getValue())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无法加入私有队伍");
        }

        String password = teamJoinRequest.getPassword();
        if (teamStatus.equals(TeamStatusEnum.SECRET.getValue())) {
            if (StringUtils.isBlank(password) || !password.equals(team.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不正确");
            }
        }

        if (getTeamSize(team) >= team.getMaxNum()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已满");
        }

        qw = new QueryWrapper<>();
        qw.eq("teamId", team.getId());
        qw.eq("userId", userId);
        long hasJoinedTeam = userTeamService.count(qw);

        if (hasJoinedTeam > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已经加入队伍");
        }

        UserTeam userTeam = new UserTeam();
        userTeam.setJoinTime(new Date());
        userTeam.setCreateTime(new Date());
        userTeam.setTeamId(team.getId());
        userTeam.setUserId(userId);
        boolean res = userTeamService.save(userTeam);
        return res;
    }

    /**
     * 查询已经加入队伍的人数
     * @param team
     * @return
     */
    private long getTeamSize(Team team) {
        QueryWrapper<UserTeam> qw;
        qw = new QueryWrapper<>();
        qw.eq("teamId", team.getId());
        return userTeamService.count(qw);


    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Long teamId = teamQuitRequest.getTeamId();
        if(teamId == null || teamId <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //检察队伍是否存在

        Team team = this.getById(teamId);
        System.out.println("team =>>>> " + team);
        if(team == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍不存在");
        }

//        检查用户是否已经加入队伍
        Long userId = loginUser.getId();
        UserTeam queryUserTeam = new UserTeam();
        queryUserTeam.setUserId(userId);
        queryUserTeam.setTeamId(teamId);
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>(queryUserTeam);
        long count = userTeamService.count(userTeamQueryWrapper);
        if(count == 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户未加入队伍");
        }

//        检查当前队伍人数，若为1,解散
        //若人数不为1，且退出的用户为队长，则
        long teamSize = getTeamSize(team);
        if(teamSize == 1){
            this.removeById(teamId);
            return userTeamService.remove(userTeamQueryWrapper);
        }

        if(team.getUserId().equals(userId)){
            QueryWrapper<UserTeam> qw = new QueryWrapper<>();
            qw.eq("teamId",teamId);
            qw.last("order by id asc limit 2");
            List<UserTeam> userTeamList = userTeamService.list(qw);
            if(CollectionUtils.isEmpty(userTeamList) || userTeamList.size() < 2){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }

            UserTeam nextTeamLeader = userTeamList.get(1);
            Team newTeam = new Team();
            newTeam.setUserId(nextTeamLeader.getUserId());
            newTeam.setId(teamId);
            if(!this.updateById(newTeam)){
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }


        }


        return userTeamService.remove(userTeamQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(long id, User loginUser) {
        // 校验队伍是否存在
        Team team = this.getById(id);

        if(team == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍不存在");
        }
        long teamId = team.getId();
        // 校验你是不是队伍的队长
        if (!Objects.equals(team.getUserId(), loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH, "无访问权限");
        }
        // 移除所有加入队伍的关联信息
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        boolean result = userTeamService.remove(userTeamQueryWrapper);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍关联信息失败");
        }
        // 删除队伍
        return this.removeById(teamId);
    }


}




