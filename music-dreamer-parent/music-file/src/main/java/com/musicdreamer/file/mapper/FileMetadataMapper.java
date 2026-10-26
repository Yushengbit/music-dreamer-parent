package com.musicdreamer.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.musicdreamer.file.entity.FileMetadata;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件元数据 Mapper
 *
 * <p>继承 BaseMapper 后，单表的增删改查方法（selectById / insert / updateById / page 等）
 * MyBatis-Plus 已经帮我们写好了，这里不用再写任何方法。
 */
@Mapper
public interface FileMetadataMapper extends BaseMapper<FileMetadata> {

}
