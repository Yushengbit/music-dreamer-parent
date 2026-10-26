package com.musicdreamer.singer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.musicdreamer.singer.entity.Singer;
import com.musicdreamer.singer.vo.SingerDetailVO;

public interface SingerService extends IService<Singer> {

    /**
     * 根据 user_id 查询歌手
     */
    Singer getByUserId(Long userId);

    /**
     * 根据 user_id 查询歌手详情
     */
    SingerDetailVO getDetailByUserId(Long userId);

    SingerDetailVO getDetailBySingerId(Long singerId);
}
