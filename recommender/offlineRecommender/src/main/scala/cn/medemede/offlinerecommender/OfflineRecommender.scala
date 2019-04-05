package cn.medemede.offlinerecommender

import org.apache.spark.SparkConf
import org.apache.spark.mllib.recommendation.{ALS, Rating}
import org.apache.spark.sql.SparkSession

/**
  * Movie数据集，数据集字段通过分割
  *
  * 151^                          电影的ID
  * Rob Roy (1995)^               电影的名称
  * In the highlands ....^        电影的描述
  * 139 minutes^                  电影的时长
  * August 26, 1997^              电影的发行日期
  * 1995^                         电影的拍摄日期
  * English ^                     电影的语言
  * Action|Drama|Romance|War ^    电影的类型
  * Liam Neeson|Jessica Lange...  电影的演员
  * Michael Caton-Jones           电影的导演
  *
  * tag1|tag2|tag3|....           电影的Tag
  **/

case class Movie(mid: Int, name: String, descri: String, timeLong: String, issue: String,
                 shoot: String, language: String, genres: String, actors: String, directors: String)

/**
  * Rating数据集，用户对于电影的评分数据集，用，分割
  *
  * 1,           用户的ID
  * 31,          电影的ID
  * 2.5,         用户对于电影的评分
  * 1260759144   用户对于电影评分的时间
  */
case class MovieRating(uid: Int, mid: Int, score: Double, timestamp: Int)

/**
  * MongoDB的连接配置
  * @param uri   MongoDB的连接
  * @param db    MongoDB要操作数据库
  */
case class MongoConfig(uri:String, db:String)

//推荐数组 rid即mid
case class Recommendation(rid:Int, r:Double)

// 用户的推荐 为用户uid推荐的电影数组
case class UserRecs(uid:Int, recs:Seq[Recommendation])

//电影的相似度
case class MovieRecs(uid:Int, recs:Seq[Recommendation])

object OfflineRecommender {

  val MONGODB_RATING_COLLECTION = "Rating"
  val MONGODB_MOVIE_COLLECTION = "Movie"
  val USER_MAX_RECOMMENDATION = 10  //推荐数量
  val USER_RECS = "UserRecs"

  //入口方法
  def main(args: Array[String]): Unit = {

    val config = Map(
      "spark.cores" -> "local[*]",
      "mongo.uri" -> "mongodb://localhost:27017/recommender",
      "mongo.db" -> "recommender"
    )

    //创建一个SparkConf配置
    val sparkConf = new SparkConf()
      .setAppName("OfflineRecommender")
      .setMaster(config("spark.cores"))
      .set("spark.executor.memory","6G")
      .set("spark.driver.memory","2G")

    //基于SparkConf创建一个SparkSession
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    //创建一个MongoDBConfig
    val mongoConfig = MongoConfig(config("mongo.uri"),config("mongo.db"))

    import spark.implicits._

    //读取mongoDB中的业务数据
    val ratingRDD = spark
      .read
      .option("uri",mongoConfig.uri)
      .option("collection",MONGODB_RATING_COLLECTION)
      .format("com.mongodb.spark.sql")
      .load()
      .as[MovieRating]
      .rdd
      .map(rating=> (rating.uid, rating.mid, rating.score))  //转化为三元组

    //用户的数据集 RDD[Int]
    val userRDD = ratingRDD.map(_._1).distinct() //提取uid，并去重

    //电影数据集 RDD[Int]
    val movieRDD = spark
      .read
      .option("uri",mongoConfig.uri)
      .option("collection",MONGODB_MOVIE_COLLECTION)
      .format("com.mongodb.spark.sql")
      .load()
      .as[Movie]
      .rdd
      .map(_.mid) //只需要mid

    //创建训练数据集
    val trainData = ratingRDD.map(x => Rating(x._1,x._2,x._3))

    val (rank,iterations,lambda) = (50, 5, 0.01) //训练参数 特征数量，迭代次数，正则化参数
    //训练ALS模型
    val model = ALS.train(trainData,rank,iterations,lambda)

    //计算用户推荐矩阵
    //需要构造一个usersProducts  RDD[(Int,Int)]
    val userMovies = userRDD.cartesian(movieRDD) //users与movies的笛卡尔积

    val preRatings = model.predict(userMovies) //用ALS模型预测

    val userRecs = preRatings
      .filter(_.rating > 0)
      .map(rating => (rating.user,(rating.product, rating.rating)))
      .groupByKey()
      .map{
        case (uid,recs) => UserRecs(uid,recs.toList.sortWith(_._2 > _._2) //降序排列
          .take(USER_MAX_RECOMMENDATION) //获取最多USER_MAX_RECOMMENDATION个
          .map(x => Recommendation(x._1,x._2)))
      }.toDF()

    userRecs.write
      .option("uri",mongoConfig.uri)
      .option("collection",USER_RECS)
      .mode("overwrite")
      .format("com.mongodb.spark.sql")
      .save()

    //计算电影相似度矩阵

    //关闭Spark
    spark.close()
  }

}
