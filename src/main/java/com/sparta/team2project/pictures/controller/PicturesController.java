package com.sparta.team2project.pictures.controller;

import com.sparta.team2project.commons.dto.MessageResponseDto;
import com.sparta.team2project.commons.security.UserDetailsImpl;
import com.sparta.team2project.pictures.dto.UploadResponseDto;
import com.sparta.team2project.pictures.service.PicturesService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PicturesController {

    private final PicturesService picturesService;

    @PostMapping("/schedules/{schedulesId}/pictures")
    public String uploadPictures(@PathVariable("schedulesId") Long schedulesId,
                                            @RequestParam("file") MultipartFile file,
                                            @AuthenticationPrincipal UserDetailsImpl userDetails
                                            ){
            return picturesService.uploadPictures(schedulesId, file, userDetails.getUsers());
    }

    @GetMapping("/schedules/{schedulesId}/pictures")
    public UploadResponseDto getPictures(@PathVariable("schedulesId") Long schedulesId){
        return picturesService.getPictures(schedulesId);
    }

    @GetMapping("/pictures/{picturesId}")
    public String getPicture(@PathVariable("picturesId") Long picturesId){
        return picturesService.getPicture(picturesId);
    }

    @PutMapping("/pictures/{picturesId}")
    public String updatePictures(@PathVariable("picturesId") Long picturesId,
                                                     @RequestParam("file") MultipartFile file,
                                                     @AuthenticationPrincipal UserDetailsImpl userDetails
                                          ){
        return picturesService.updatePictures(picturesId, file, userDetails.getUsers());
    }


    @DeleteMapping("/pictures/{picturesId}")
    public MessageResponseDto deletePictures(@PathVariable("picturesId") Long picturesId,
                                                   @AuthenticationPrincipal UserDetailsImpl userDetails
                                                   ){
        return picturesService.deletePictures(picturesId, userDetails.getUsers());
    }

}