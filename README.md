# 基于组合推荐的电影推荐系统

该系统可根据用户的打分情况和类型偏好进行电影推荐， 实现了实时推荐与离线推荐两大功能， 分为推荐服务、后台服务、 可视化服务三大模块。 后台使用Spring MVC框架， 使用Scala和Spark进行推荐服务开发， 使用mongoDB作为数据库， 使用Redis作为业务缓存， 使用elasticsearch提供检索服务， 使用Flume和Kafka提供实时推荐的消息流。

## 功能模块

![推荐服务](https://user-images.githubusercontent.com/19551139/127156307-2d85f8e6-613b-4fc6-ade6-c70d0c19360d.png)

![后端服务](https://user-images.githubusercontent.com/19551139/127156349-81989e7d-a56f-4d27-89fd-cdb0955de5f8.png)

![可视化](https://user-images.githubusercontent.com/19551139/127156423-7fedd556-915a-493d-8a7e-1426a334d505.png)
  
## 系统架构

![系统架构](https://user-images.githubusercontent.com/19551139/127156527-de83260b-c225-4c56-8ff0-4268972c2de4.png)

