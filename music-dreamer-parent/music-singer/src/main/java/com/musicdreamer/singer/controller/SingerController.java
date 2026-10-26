package com.musicdreamer.singer.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.musicdreamer.common.exception.GlobalException;
import com.musicdreamer.common.result.R;
import com.musicdreamer.common.result.ResultCodeEnum;
import com.musicdreamer.singer.dto.SingerDTO;
import com.musicdreamer.singer.entity.Singer;
import com.musicdreamer.singer.service.SingerService;
import com.musicdreamer.singer.vo.SingerDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/singer")
@RequiredArgsConstructor

public class SingerController {

    private final SingerService singerService;
    /**
     * 根据id查询歌手
     */
    @GetMapping("/get/{id}")
    public R<Singer> getSingerById(@PathVariable("id") Long id) {
        Singer singer = singerService.getById(id);
        if (singer == null) {
            throw new GlobalException(ResultCodeEnum.SINGER_NOT_EXIST);
        }
        return R.ok(singer);
    }

    /**
     * 根据user_id查询歌手
     */
    @GetMapping("/getByUserId/{userId}")
    public R<Singer> getSingerByUserId(@PathVariable("userId") Long userId) {
        Singer singer = singerService.getByUserId(userId);
        if (singer == null) {
            throw new GlobalException(ResultCodeEnum.SINGER_NOT_EXIST);
        }
        return R.ok(singer);
    }

    /**
     * 根据user_id查询歌手详情：歌手信息 + 远程调用用户服务返回的注册用户信息
     */
    @GetMapping("/detail/{id}")
    public R<SingerDetailVO> getSingerDetail(@PathVariable("userId") Long userId) {
        SingerDetailVO detail = singerService.getDetailByUserId(userId);
        if (detail == null) {
            throw new GlobalException(ResultCodeEnum.SINGER_NOT_EXIST);
        }
        return R.ok(detail);
    }


    //根据singer_id查询歌手详情：程调用用户服务返回的注册用户信息
    @GetMapping("detailBySingerId/{singerId}")
    public R<SingerDetailVO> getSingerDetailBySingerId(@PathVariable("singerId") Long singerId) {
        SingerDetailVO detail = singerService.getDetailBySingerId(singerId);
        if (detail == null) {
            throw new GlobalException(ResultCodeEnum.SINGER_NOT_EXIST);
        }
        return R.ok(detail);
    }



    /**
     * 新增歌手（一个用户只能有一个歌手身份）
     */
    @PostMapping("/save")
    public R<Void> saveSinger(@RequestBody SingerDTO singerDTO) {
        if (singerDTO.getUserId() == null) {
            throw new GlobalException(ResultCodeEnum.PARAM_EMPTY);
        }
        if (singerService.getByUserId(singerDTO.getUserId()) != null) {
            throw new GlobalException(ResultCodeEnum.SINGER_EXIST);
        }
        Singer singer = new Singer();
        BeanUtils.copyProperties(singerDTO, singer);
        singer.setId(null);
        singerService.save(singer);
        return R.ok();
    }

    /**
     * 修改歌手
     */
    @PutMapping("/update")
    public R<Void> updateSinger(@RequestBody SingerDTO singerDTO) {
        if (singerDTO.getId() == null) {
            throw new GlobalException(ResultCodeEnum.PARAM_EMPTY);
        }
        if (singerService.getById(singerDTO.getId()) == null) {
            throw new GlobalException(ResultCodeEnum.SINGER_NOT_EXIST);
        }
        Singer singer = new Singer();
        BeanUtils.copyProperties(singerDTO, singer);
        singerService.updateById(singer);
        return R.ok();
    }

    /**
     * 删除歌手（逻辑删除）
     */
    @DeleteMapping("/delete/{id}")
    public R<Void> deleteSinger(@PathVariable("id") Long id) {
        if (singerService.getById(id) == null) {
            throw new GlobalException(ResultCodeEnum.SINGER_NOT_EXIST);
        }
        singerService.removeById(id);
        return R.ok();
    }

    /**
     * 分页查询（支持按歌手名模糊查询）
     */
    @GetMapping("/page")
    public R<Page<Singer>> pageSinger(@RequestParam(value = "current", defaultValue = "1") long current,
                                      @RequestParam(value = "size", defaultValue = "10") long size,
                                      @RequestParam(value = "singerName", required = false) String singerName) {
        Page<Singer> page = new Page<>(current, size);
        LambdaQueryWrapper<Singer> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(singerName), Singer::getSingerName, singerName);
        Page<Singer> resPage = singerService.page(page, wrapper);
        return R.ok(resPage);
    }


}
