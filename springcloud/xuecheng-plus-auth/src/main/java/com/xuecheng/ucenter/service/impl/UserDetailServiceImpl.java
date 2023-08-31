package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcMenuMapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcMenu;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailServiceImpl implements UserDetailsService {
    @Autowired
    XcUserMapper xcUserMapper;
    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    XcMenuMapper xcMenuMapper;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        //先封装
        AuthParamsDto authParamsDto=null;
        try {
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        }catch (Exception e){
            throw  new RuntimeException("请求参数不符合要求");
        }
        String username = authParamsDto.getUsername();
        //将jason参数转化为我们的dto类
        //这里的S是我们输入的用户名
        LambdaQueryWrapper<XcUser> xcUserLambdaQueryWrapper = new LambdaQueryWrapper<>();
        xcUserLambdaQueryWrapper.eq(XcUser::getUsername,username);
        XcUser xcUser = xcUserMapper.selectOne(xcUserLambdaQueryWrapper);
        // 根据S拿到对象，不存在则直接返回null
        if(xcUser==null){
            throw new RuntimeException("账户或密码错误");
        }
        //拿到他的类ing
        String utype = authParamsDto.getAuthType();
        //拼接name
        String BeanName=utype+"_authService";
        //到这里有对象了
        AuthService bean = applicationContext.getBean(BeanName, AuthService.class);
        XcUserExt xcUserExt = bean.execute(authParamsDto);

        return getUserPrincipal(xcUserExt);
    }
    public UserDetails getUserPrincipal(XcUserExt user){
        //todo:在这个方法里授予权限
        //用户权限,如果不加报Cannot pass a null GrantedAuthority collection
        String[] authorities = {"p1"};
        String password = user.getPassword();
        //拿到权限根据id
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(user.getId());
        ArrayList<String> authoritie = new ArrayList<>();
        xcMenus.forEach(i->{
            authoritie.add(i.getCode());
        });
        authorities = authoritie.toArray(new String[0]);
        //为了安全在令牌中不放密码
        user.setPassword(null);
        //将user对象转json
        String userString = JSON.toJSONString(user);
        //创建UserDetails对象
        //他需要在userDetail
        UserDetails userDetails = User.withUsername(userString).password(password ).authorities(authorities).build();
        return userDetails;
    }

}
