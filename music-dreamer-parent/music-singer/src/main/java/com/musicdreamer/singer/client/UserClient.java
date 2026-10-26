package com.musicdreamer.singer.client;

import com.musicdreamer.common.dto.UserDTO;
import com.musicdreamer.common.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "music-user",path = "/user")
public interface UserClient {

    @GetMapping("get/{id}")
    R<UserDTO> getUserById(@PathVariable("id") Long id);
}
