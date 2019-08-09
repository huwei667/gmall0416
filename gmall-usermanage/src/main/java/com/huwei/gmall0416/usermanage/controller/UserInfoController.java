package com.huwei.gmall0416.usermanage.controller;

import com.huwei.gmall0416.bean.UserInfo;
import com.huwei.gmall0416.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
public class UserInfoController  {

    @Autowired
    private UserInfoService userInfoService;

    @RequestMapping("findAll")
    @ResponseBody
    public List<UserInfo> getAll(){
        return userInfoService.findAll();
    }

}
