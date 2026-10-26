package com.musicdreamer.singer.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.musicdreamer.common.dto.UserDTO;
import com.musicdreamer.common.result.R;
import com.musicdreamer.singer.client.UserClient;
import com.musicdreamer.singer.entity.Singer;
import com.musicdreamer.singer.mapper.SingerMapper;
import com.musicdreamer.singer.service.SingerService;
import com.musicdreamer.singer.vo.SingerDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SingerServiceImpl extends ServiceImpl<SingerMapper, Singer> implements SingerService {

    private final UserClient userClient;
    @Override
    public Singer getByUserId(Long userId) {
        return getOne(new LambdaQueryWrapper<Singer>()
                .eq(Singer::getUserId, userId)
                .last("limit 1"));
    }

    @Override
    public SingerDetailVO getDetailByUserId(Long userId) {
        Singer singer = getByUserId(userId);
        if (singer == null) {
            return null;
        }
        SingerDetailVO vo = new SingerDetailVO();
        vo.setSinger(singer);

        // 远程调用用户服务，补上歌手对应的注册用户信息（失败只记日志，不影响主流程）
        Long bindUserId = singer.getUserId();
        if (bindUserId != null) {
            try {
                R<UserDTO> userResult = userClient.getUserById(bindUserId);
                if (userResult != null && userResult.getData() != null) {
                    vo.setUser(userResult.getData());
                }
            } catch (Exception e) {
                log.error("调用用户服务查询用户信息失败,userId={}", bindUserId, e);
            }
        }
        return vo;
    }

    //根据singer_id查询歌手详情：程调用用户服务返回的注册用户信息
    @Override
    public SingerDetailVO getDetailBySingerId(Long singerId) {
        Singer singer = getById(singerId);
        if (singer == null) {
            return null;
        }
        SingerDetailVO vo = new SingerDetailVO();
        vo.setSinger(singer);
        Long bindUserId = singer.getUserId();
        if (bindUserId != null) {
            try {
                R<UserDTO> userResult = userClient.getUserById(bindUserId);
                if (userResult != null && userResult.getData() != null) {
                    vo.setUser(userResult.getData());
                }
            } catch (Exception e) {
                log.error("调用用户服务查询用户信息失败,userId={}", bindUserId, e);
            }
        }
        return vo;
    }
}
