package com.musicdreamer.singer.vo;

import com.musicdreamer.common.dto.UserDTO;
import com.musicdreamer.singer.entity.Singer;
import lombok.Data;

/**
 * 歌手详情：歌手信息 + 注册用户信息
 */
@Data
public class SingerDetailVO {

    /**
     * 歌手信息
     */
    private Singer singer;

    /**
     * 该歌手对应的注册用户信息（用户服务调用失败或用户不存在时为 null）
     */
    private UserDTO user;
}
