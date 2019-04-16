package cn.medemede.server.rest;

import cn.medemede.server.model.request.LoginUserRequest;
import cn.medemede.server.model.request.RegisterUserRequest;
import cn.medemede.server.model.request.UpdateUserGenresRequest;
import cn.medemede.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//用于处理User相关的动作
@Controller
@RequestMapping("/rest/users")
public class UserRestApi {

    @Autowired
    private UserService userService;

    /**
     * 需要提供用户注册功能
     * 访问：url: /rest/users/register?username=abc&password=abc
     * 返回：{success：true}
     * @param username
     * @param password
     * @param model
     * @return
     */
    @RequestMapping(path = "/register", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model registerUser(@RequestParam("username") String username, @RequestParam("password")String password, Model model){
        model.addAttribute("success",userService.registerUser(new RegisterUserRequest(username,password)));
        return model;
    }

    /**
     * 需要提供用户登录功能
     * 访问：url: /rest/users/login?username=abc&password=abc
     * 返回：{success：true}
     * @param username
     * @param password
     * @param model
     * @return
     */
    @RequestMapping(path = "/login", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model loginUser(@RequestParam("username")String username,@RequestParam("password") String password, Model model){
        model.addAttribute("success",userService.loginUser(new LoginUserRequest(username,password)));
        return model;
    }

    /**
     * 需要能够添加用户偏爱的影片类别
     * 访问：url: /rest/users/genres?username=abc&genres=a|b|c|d
     * @param username
     * @param genres
     * @param model
     */
    @RequestMapping(path = "/genres", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public void addGenres(@RequestParam("username")String username,@RequestParam("genres") String genres,Model model){
        List<String> genresList = new ArrayList<>();
        Collections.addAll(genresList, genres.split("\\|"));
        userService.updateUserGenres(new UpdateUserGenresRequest(username,genresList));
    }

}
