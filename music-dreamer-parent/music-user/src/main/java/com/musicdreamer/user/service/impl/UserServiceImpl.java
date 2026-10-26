package com.musicdreamer.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.musicdreamer.user.entity.User;
import com.musicdreamer.user.mapper.UserMapper;
import com.musicdreamer.user.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}
