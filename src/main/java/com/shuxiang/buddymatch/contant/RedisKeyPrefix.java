package com.shuxiang.buddymatch.contant;


import lombok.Data;

@Data
public class RedisKeyPrefix {
    public final static String PROJECT = "bm:";

    public final static String USER_MODULE = PROJECT+"user:";

    public final static String RECOMMEND = USER_MODULE + "recommend:";

    public final static String PRE_CACHE_LOCK = USER_MODULE + "preCacheLock:";

    public final static String TEAM_MODULE = PROJECT+"team:";

    public final static String ADD_TEAM = TEAM_MODULE + "add:";
    public final static String JOIN_TEAM = TEAM_MODULE + "join:";

    public final static String USER_ID = "userId:";

    public final static String TEAM_ID = "teamId:";
}
