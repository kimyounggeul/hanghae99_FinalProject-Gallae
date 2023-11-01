package com.sparta.team2project.pictures.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.sparta.team2project.commons.dto.MessageResponseDto;
import com.sparta.team2project.commons.entity.UserRoleEnum;
import com.sparta.team2project.commons.exceptionhandler.CustomException;
import com.sparta.team2project.commons.exceptionhandler.ErrorCode;
import com.sparta.team2project.pictures.dto.PicturesResponseDto;
import com.sparta.team2project.pictures.dto.UploadResponseDto;
import com.sparta.team2project.pictures.entity.Pictures;
import com.sparta.team2project.pictures.repository.PicturesRepository;
import com.sparta.team2project.s3.AmazonS3ResourceStorage;
import com.sparta.team2project.s3.CustomMultipartFile;
import com.sparta.team2project.s3.FileDetail;
import com.sparta.team2project.schedules.entity.Schedules;
import com.sparta.team2project.schedules.repository.SchedulesRepository;
import com.sparta.team2project.users.UserRepository;
import com.sparta.team2project.users.Users;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import marvin.image.MarvinImage;
import org.marvinproject.image.transform.scale.Scale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PicturesService {
    private final AmazonS3ResourceStorage amazonS3ResourceStorage;
    private final AmazonS3Client amazonS3Client;
    private final PicturesRepository picturesRepository;
    private final SchedulesRepository schedulesRepository;
    private final UserRepository userRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public FileDetail save(MultipartFile multipartFile) {
        FileDetail fileDetail = FileDetail.multipartOf(multipartFile);
        amazonS3ResourceStorage.store(fileDetail.getPath(), multipartFile);
        return fileDetail;
    }

    // 사진 등록
    @SneakyThrows
    public String uploadPictures(Long schedulesId, MultipartFile file, Users users) {
        Users existUser = checkUser(users); // 유저 확인
        checkAuthority(existUser, users);         // 권한 확인
        // 기 존재하는 사진이 있는지 확인
        Schedules checkSchedules = schedulesRepository.findById(schedulesId).orElseThrow(
                () -> new CustomException(ErrorCode.ID_NOT_MATCH)
        );
        List<Pictures> checkPicturesList = checkSchedules.getPicturesList();
        // 기 존재하는 사진 모음이 3개인지 확인
        if (checkPicturesList.size() == 3) {
            throw new CustomException(ErrorCode.EXCEED_PICTURES_LIMIT);
        }
        // 그외의 경우 사진 등록
        else {
            // 1. 파일 정보를 picturesResponseDto에 저장
            String picturesName = file.getOriginalFilename();
            String picturesURL = "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com" + "/" + "schedulesPictures" + "/" + picturesName;
            String pictureContentType = file.getContentType();
            String fileFormatName = file.getContentType().substring(file.getContentType().lastIndexOf("/") + 1);
            // 2. 이미지 리사이즈 함수 호출
//            MultipartFile resizedImage = resizer(picturesName, fileFormatName, file, 250);
            Long pictureSize = file.getSize();  // 단위: KBytes
            // 3. Repository에 파일 정보를 저장하기 위해 PicturesList에 저장(schedulesId 필요)
            Schedules schedules = schedulesRepository.findById(schedulesId).orElseThrow(
                    () -> new CustomException(ErrorCode.ID_NOT_MATCH)
            );
            Pictures pictures = new Pictures(schedules, picturesURL, picturesName, pictureContentType, pictureSize);
            checkPicturesList.add(pictures);
            // 4. 사진을 메타데이터 및 정보와 함께 S3에 저장
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(pictureContentType);
            metadata.setContentLength(pictureSize);
            try (InputStream inputStream = file.getInputStream()) {
                amazonS3Client.putObject(new PutObjectRequest(bucket + "/schedulesPictures", picturesName, inputStream, metadata)
                        .withCannedAcl(CannedAccessControlList.PublicRead));
            } catch (IOException e) {
                throw new CustomException(ErrorCode.S3_NOT_UPLOAD);
            }
            // 4. Repository에 Pictures리스트를 저장
            picturesRepository.save(pictures);// 5. 성공 메시지 DTO와 함께 picturesResponseDtoList를 반환
            return picturesURL;
        }
    }



    public UploadResponseDto getPictures(Long schedulesId) {
        // 1. Schedules 객체를 찾아 연결된 Pictures 불러오기
        Schedules schedules = schedulesRepository.findById(schedulesId).orElseThrow(
                () -> new CustomException(ErrorCode.ID_NOT_MATCH)
        );
        // 2. 불러온 Pictures의 리스트를 DTO의 리스트로 변환
        List<Pictures> picturesList = schedules.getPicturesList();
        List<PicturesResponseDto> picturesResponseDtoList = new ArrayList<>(3);
        for (Pictures pictures : picturesList) {
            // 3. 파일 불러오기
            try {
                S3Object s3Object = amazonS3Client.getObject(bucket + "/schedulesPictures", pictures.getPicturesName());
                S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent();
                FileOutputStream fileOutputStream = new FileOutputStream(new File(pictures.getPicturesName()));
                byte[] read_buf = new byte[1024];
                int read_len = 0;
                while ((read_len = s3ObjectInputStream.read(read_buf)) > 0) {
                    fileOutputStream.write(read_buf, 0, read_len);
                }
                s3ObjectInputStream.close();
                fileOutputStream.close();
            } catch (AmazonServiceException e) {
                throw new AmazonServiceException(e.getErrorMessage());
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e.getMessage());
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            }
            // 4. 각 사진 파일 정보(Pictures)를 DTO리스트에 저장
            PicturesResponseDto picturesResponseDto = new PicturesResponseDto(pictures);
            picturesResponseDtoList.add(picturesResponseDto);
        }
        // 5. 성공 메시지와 함께 사진 정보 반환
        MessageResponseDto messageResponseDto = new MessageResponseDto("요청한 파일을 반환하였습니다.", 200);
        UploadResponseDto uploadResponseDto = new UploadResponseDto(picturesResponseDtoList, messageResponseDto);
        return uploadResponseDto;
    }

    public String getPicture(Long picturesId) {
        try {
            // 1. 파일을 찾아 열기
            Pictures pictures = picturesRepository.findById(picturesId).orElseThrow(
                    () -> new CustomException(ErrorCode.ID_NOT_MATCH)
            );
            S3Object s3Object = amazonS3Client.getObject(bucket + "/schedulesPictures", pictures.getPicturesName());
            S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent();
            FileOutputStream fileOutputStream = new FileOutputStream(new File(pictures.getPicturesName()));
            byte[] read_buf = new byte[1024];
            int read_len = 0;
            while ((read_len = s3ObjectInputStream.read(read_buf)) > 0) {
                fileOutputStream.write(read_buf, 0, read_len);
            }
            s3ObjectInputStream.close();
            fileOutputStream.close();
            // 2. 사진 파일 정보(Pictures) 반환
            return pictures.getPicturesURL();
        } catch (AmazonServiceException e) {
            throw new AmazonServiceException(e.getErrorMessage());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String updatePictures(Long picturesId, MultipartFile file, Users users) {
        Users existUser = checkUser(users); // 유저 확인
        checkAuthority(existUser, users);         // 권한 확인
        Pictures pictures = picturesRepository.findById(picturesId).orElseThrow(
                () -> new CustomException(ErrorCode.ID_NOT_MATCH));
        // 1. 파일 기본 정보 추출
        String picturesName = file.getOriginalFilename();
        String picturesURL = "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com" + "/" + "schedulesPictures" + "/" + picturesName;
        String pictureContentType = file.getContentType();
        String fileFormatName = file.getContentType().substring(file.getContentType().lastIndexOf("/") + 1);
        // 2. 이미지 사이즈 재조정
//        MultipartFile resizedImage = resizer(picturesName, fileFormatName, file, 250);
        Long pictureSize = file.getSize();  // 단위: KBytes
        pictures.updatePictures(picturesURL, picturesName, pictureContentType, pictureSize);
        picturesRepository.save(pictures);
        // 3. 사진을 메타데이터 및 정보와 함께 S3에 저장
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(pictureContentType);
        metadata.setContentLength(pictureSize);
        try (InputStream inputStream = file.getInputStream()) {
            amazonS3Client.putObject(new PutObjectRequest(bucket + "/schedulesPictures", picturesName, inputStream, metadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (IOException e) {
            throw new CustomException(ErrorCode.S3_NOT_UPLOAD);
        }
        return picturesURL;
    }

    public MessageResponseDto deletePictures(Long picturesId, Users users) {
        Users existUser = checkUser(users); // 유저 확인
        checkAuthority(existUser, users);         // 권한 확인
        Pictures pictures = picturesRepository.findById(picturesId).orElseThrow(
                () -> new CustomException(ErrorCode.ID_NOT_MATCH)
        );
        try {
            amazonS3Client.deleteObject(bucket + "/schedulesPictures", pictures.getPicturesName());
        } catch (AmazonServiceException e) {
            throw new AmazonServiceException(e.getErrorMessage());
        }
        picturesRepository.delete(pictures);
        MessageResponseDto messageResponseDto = new MessageResponseDto("사진이 삭제되었습니다.", 200);
        return messageResponseDto;
    }

    // 이미지 사이즈 변경 메서드
    @Transactional
    public MultipartFile resizer(String fileName, String fileFormat, MultipartFile originalImage, int height) {

        try {
            BufferedImage image = ImageIO.read(originalImage.getInputStream());// MultipartFile -> BufferedImage Convert

            int originWidth = image.getWidth();
            int originHeight = image.getHeight();

            // origin 이미지가 400보다 작으면 패스
            if (originHeight < height)
                return originalImage;

            MarvinImage imageMarvin = new MarvinImage(image);

            Scale scale = new Scale();
            scale.load();
            scale.setAttribute("newWidth", height * originWidth / originHeight); //비율유지를 위해 너비와 높이 비율 계산
            scale.setAttribute("newHeight", height);
            scale.process(imageMarvin.clone(), imageMarvin, null, null, false);

            BufferedImage imageNoAlpha = imageMarvin.getBufferedImageNoAlpha();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(imageNoAlpha, fileFormat, baos);
            baos.flush();

            return new CustomMultipartFile(fileName, fileFormat, originalImage.getContentType(), baos.toByteArray());

        } catch (IOException e) {
            throw new CustomException(ErrorCode.UNABLE_TO_CONVERT);
        }
    }

    // 사용자 조회 메서드
    private Users checkUser(Users users) {
        return userRepository.findByEmail(users.getEmail()).
                orElseThrow(() -> new CustomException(ErrorCode.ID_NOT_MATCH));

    }

    // ADMIN 권한 및 이메일 일치여부 메서드
    private void checkAuthority(Users existUser, Users users) {
        if (!existUser.getUserRole().equals(UserRoleEnum.ADMIN) && !existUser.getEmail().equals(users.getEmail())) {
            throw new CustomException(ErrorCode.NOT_ALLOWED);
        }
    }
}
