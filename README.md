# 伙伴匹配后端
130w+数据
没加索引
1180 ms （登录时的查询）
加入账号和密码的索引 <br/>
首页查询前段耗时1.25s，使用缓存之后40.55ms

环境改造：<br/>
1.搭建git将代码上传到git管理。<br/>
2.搭建jenkins，通过jenkins进行打包部署。<br/>
3.搭建docker，通过jenkins将工程发布到docker，实现自动化部署全流程。<br/>


工程改造：<br/>
1.引入spring cloud框架。<br/>
2.把现有单体工程按照微服务拆分(最少2个微服务，可独立部署)。<br/>
3.微服务之间使用feign调用，使用hystirx作为调用方的熔断控制。<br/>
4.搭建服务注册中心nacos，用于服务发现以及配置管理。<br/>
5.搭建gateway微服务网关。 <br/>
 A：实现鉴权功能(比如请求头中带key=xxx的才让通过)<br/>
 B：实现动态路由路由，网关通过从nacos获取微服务的地址，进行转发。<br/>
 C：实现限流功能，比如1秒最多可以请求3次，超过的请求直接返回错误。<br/>
 D：实现黑白名单功能，比如请求头中带有tag=zhangsan(白名单)，的才能访问，反之tag=lisi(黑名单)的不能访问<br/>
6.实现调用链追踪。搭建zipkin、sleuth(spring cloud组件)，实现微服务之间调用关系的展现。<br/>
7.搭建elk，将微服务日志吐到e里面，用k展现出来。<br/><br/>

这些下来你机器资源未必够用，如果想全套演练，可以去阿里或者腾讯云看下，现在学生买机器有优惠，一个月大概几十块钱，你可以买2台。其实花这点钱到无所谓，重要的是你经历过在公有云上的操作，这比有些在职场中工作过好些年的人都强，将来也是出去面试出牛的点。以后云计算少不了，早点接触云，有好处。
