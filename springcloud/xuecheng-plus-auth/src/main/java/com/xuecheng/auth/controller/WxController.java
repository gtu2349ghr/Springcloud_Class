//package com.xuecheng.auth.controller;
//
//import com.xuecheng.ucenter.model.dto.XcUserExt;
//import com.xuecheng.ucenter.model.po.XcUser;
//import com.xuecheng.ucenter.service.WxAuthService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.RequestMapping;
//
//@Controller
//@Slf4j
//public class WxController {
//    @Autowired
//    WxAuthService wxAuthService;
//    @RequestMapping("/wxLogin")
//    public String login(String code,String state){
//        //接收到code然后申请令牌
//        //申请完写入数据库
//        //返回重定向到页面
//
//        XcUser xcUser = wxAuthService.wxAuth(code);
//        if(xcUser==null){
//            return "redirect:http://www.51xuecheng.cn/error.html";
//        }
//        String username = xcUser.getUsername();
//        return "redirect:http://www.51xuecheng.cn/sign.html?username="+username+"&authType=wx";
//    }
//
//}
//
//
