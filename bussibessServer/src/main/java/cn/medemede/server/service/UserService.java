package cn.medemede.server.service;

import cn.medemede.server.model.core.User;
import cn.medemede.server.model.request.LoginUserRequest;
import cn.medemede.server.model.request.RegisterUserRequest;
import cn.medemede.server.model.request.UpdateUserGenresRequest;
import cn.medemede.server.utils.Constant;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.util.JSON;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

//对于用户具体处理业务服务的服务类

@Service
public class UserService {

    @Autowired
    private MongoClient mongoClient;

    @Autowired
    private ObjectMapper objectMapper;

    private MongoCollection<Document> userCollection;

    //用于获取User表连接
    private MongoCollection<Document> getUserCollection(){
        if(null == userCollection)
            this.userCollection = mongoClient.getDatabase(Constant.MONGO_DATABASE).getCollection(Constant.MONGO_USER_COLLECTION);
        return this.userCollection;
    }

    //将User装换成一个Document
    private Document userToDocument(User user){
        try {
            return Document.parse(objectMapper.writeValueAsString(user));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    //将Document装换成为User
    private User documentToUser(Document document){
        try {
            return objectMapper.readValue(JSON.serialize(document),User.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 用于提供注册用户的服务
     * @param request
     * @return
     */
    public boolean registerUser(RegisterUserRequest request){
        //判断是否有相同的用户名已经注册
        if(getUserCollection().find(new Document("username",request.getUsername())).first() != null)
            return false;

        //创建一个用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setFirst(true);

        //插入一个用户
        Document document = userToDocument(user);
        if(null == document)
            return false;
        getUserCollection().insertOne(document);
        return true;
    }

    /**
     * 用于提供用户的登录
     * @param request
     * @return
     */
    public boolean loginUser(LoginUserRequest request){
        //需要找到这个用户
        Document document = getUserCollection().find(new Document("username",request.getUsername())).first();
        if(null == document)
            return false;
        User user = documentToUser(document);
        //验证密码
        if(null == user)
            return false;
        return user.getPassword().compareTo(request.getPassword()) == 0;
    }

    /**
     * 用于更新用户第一次登录选择的电影类别
     * @param request
     * @return
     */
    public void updateUserGenres(UpdateUserGenresRequest request){
        getUserCollection().updateOne(new Document("username",request.getUsername()),new Document().append("$set",new Document("$genres",request.getGenres())));
        getUserCollection().updateOne(new Document("username",request.getUsername()),new Document().append("$set",new Document("$first",false)));
    }

    //通过用户名查询一个用户
    public User findUserByUsername(String username){
        Document document = getUserCollection().find(new Document("username",username)).first();
        if(null == document || document.isEmpty())
            return null;
        return documentToUser(document);
    }

}
