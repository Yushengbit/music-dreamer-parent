package com.musicdreamer.singer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.musicdreamer.singer.entity.Singer;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SingerMapper extends BaseMapper<Singer> {

}
