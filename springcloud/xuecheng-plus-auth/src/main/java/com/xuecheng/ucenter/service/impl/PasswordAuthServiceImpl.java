package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.feignclient.CheckCodeClient;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service("password_authService")
@Slf4j
public class PasswordAuthServiceImpl implements AuthService {
@Autowired
    PasswordEncoder passwordEncoder;
@Autowired
    XcUserMapper xcUserMapper;
@Autowired
    CheckCodeClient checkCodeClient;
    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        //先校验的是验证码
        String checkcode = authParamsDto.getCheckcode();
        String checkcodekey = authParamsDto.getCheckcodekey();
        if(StringUtils.isEmpty(checkcode)||StringUtils.isEmpty(checkcodekey)){
            throw new RuntimeException("请输入验证码");
        }
        Boolean verify = checkCodeClient.verify(checkcodekey, checkcode);
        if(verify==null||!verify){
            throw new RuntimeException("验证码错误");
        }
        //开始校验
        String username = authParamsDto.getUsername();
        LambdaQueryWrapper<XcUser> xcUserLambdaQueryWrapper = new LambdaQueryWrapper<>();
        xcUserLambdaQueryWrapper.eq(XcUser::getUsername,username);
        XcUser xcUser = xcUserMapper.selectOne(xcUserLambdaQueryWrapper);
        if(xcUser==null){
            log.info("用户不存在:{}",username);
            throw new RuntimeException("用户不存在");
        }
        //
        //拿到数据库的信息
        String password = authParamsDto.getPassword();
        String passwordDb = xcUser.getPassword();
        boolean matches = passwordEncoder.matches(password, passwordDb);
        if(matches){
            XcUserExt xcUserExt = new XcUserExt();
            BeanUtils.copyProperties(xcUser,xcUserExt);
            return xcUserExt;
        }
        return null;
    }
}
