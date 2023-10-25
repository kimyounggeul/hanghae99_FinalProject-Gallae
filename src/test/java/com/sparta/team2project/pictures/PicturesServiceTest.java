//package com.sparta.team2project.pictures;
//
//import com.amazonaws.services.s3.AmazonS3Client;
//import com.amazonaws.services.s3.model.GetObjectRequest;
//import com.amazonaws.services.s3.model.PutObjectRequest;
//import com.amazonaws.services.s3.model.PutObjectResult;
//import com.amazonaws.services.s3.model.S3Object;
//import com.sparta.team2project.commons.dto.MessageResponseDto;
//import com.sparta.team2project.commons.entity.UserRoleEnum;
//import com.sparta.team2project.pictures.dto.PicturesMessageResponseDto;
//import com.sparta.team2project.pictures.dto.PicturesResponseDto;
//import com.sparta.team2project.pictures.dto.UploadResponseDto;
//import com.sparta.team2project.pictures.entity.Pictures;
//import com.sparta.team2project.pictures.repository.PicturesRepository;
//import com.sparta.team2project.pictures.service.PicturesService;
//import com.sparta.team2project.posts.dto.TripDateOnlyRequestDto;
//import com.sparta.team2project.posts.entity.PostCategory;
//import com.sparta.team2project.posts.entity.Posts;
//import com.sparta.team2project.s3.AmazonS3ResourceStorage;
//import com.sparta.team2project.s3.CustomMultipartFile;
//import com.sparta.team2project.s3.FileDetail;
//import com.sparta.team2project.s3.MultipartUtil;
//import com.sparta.team2project.schedules.dto.CreateSchedulesRequestDto;
//import com.sparta.team2project.schedules.dto.SchedulesRequestDto;
//import com.sparta.team2project.schedules.entity.Schedules;
//import com.sparta.team2project.schedules.repository.SchedulesRepository;
//import com.sparta.team2project.tripdate.entity.TripDate;
//import com.sparta.team2project.users.UserRepository;
//import com.sparta.team2project.users.Users;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.jupiter.api.BeforeEach;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.mockito.stubbing.OngoingStubbing;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//import static com.sparta.team2project.schedules.entity.SchedulesCategory.카페;
//import static org.junit.Assert.assertEquals;
//import static org.mockito.Mockito.*;
//
//public class PicturesServiceTest {
//
//    private PicturesService picturesService;
//    private AmazonS3ResourceStorage amazonS3ResourceStorage;
//
//    private AmazonS3Client amazonS3Client;
//
//    private SchedulesRepository schedulesRepository;
//
//    private UserRepository userRepository;
//
//
//    @Value("${cloud.aws.s3.bucket}")
//    private String bucket;
//
//    private CustomMultipartFile customMultipartFile;
//
//    private PicturesRepository picturesRepository;
//
//    @BeforeEach
//    public void setUp() {
//        customMultipartFile = mock(CustomMultipartFile.class);
//        amazonS3ResourceStorage = mock(AmazonS3ResourceStorage.class);
//        amazonS3Client = mock(AmazonS3Client.class);
//        picturesRepository = mock(PicturesRepository.class);
//        userRepository = mock(UserRepository.class);
//        schedulesRepository = mock(SchedulesRepository.class);
//        picturesService = new PicturesService(amazonS3ResourceStorage, amazonS3Client, picturesRepository, schedulesRepository, userRepository);
//    }
//
//    public TripDateOnlyRequestDto MockTripDateOnlyRequestDto(){
//        TripDateOnlyRequestDto tripDateOnlyRequestDto = mock(TripDateOnlyRequestDto.class);
//        when(tripDateOnlyRequestDto.getChosenDate()).thenReturn(LocalDate.of(2023, 10, 10));
//        return tripDateOnlyRequestDto;
//    }
//
//    public SchedulesRequestDto MockSchedulesRequestDto(){
//        // Mock the MenuRequestDto
//        SchedulesRequestDto schedulesRequestDto = mock(SchedulesRequestDto.class);
//        // Set up the behavior of the mock DTO
//        when(schedulesRequestDto.getSchedulesCategory()).thenReturn(카페);
//        when(schedulesRequestDto.getCosts()).thenReturn(10000);
//        when(schedulesRequestDto.getPlaceName()).thenReturn("정동진 카페");
//        when(schedulesRequestDto.getContents()).thenReturn("해돋이 카페");
//        when(schedulesRequestDto.getTimeSpent()).thenReturn("3시간");
//        when(schedulesRequestDto.getReferenceURL()).thenReturn("www.blog.com");
//        return schedulesRequestDto;
//    }
//
//    public CreateSchedulesRequestDto MockCreateSchedulesRequestDto(){
//        CreateSchedulesRequestDto createSchedulesRequestDto = mock(CreateSchedulesRequestDto.class);
//        List<SchedulesRequestDto> schedulesRequestDtoList = new ArrayList<>();
//        schedulesRequestDtoList.add(MockSchedulesRequestDto());
//        when(createSchedulesRequestDto.getSchedulesList()).thenReturn(schedulesRequestDtoList);
//        return createSchedulesRequestDto;
//    }
//
//    public TripDate MockTripDate(){
//        return new TripDate(MockTripDateOnlyRequestDto(), MockPosts());
//    }
//
//    public Posts MockPosts(){
//        return new Posts("해돋이 보러간다", "정동진 해돋이", PostCategory.가족, "동해안 해돋이", MockUsers());
//    }
//
//    public Users MockUsers(){
//        return new Users("test@email.com", "test", "test123!", UserRoleEnum.USER, "image/profileImg.png");
//    }
//
//    public Schedules MockSchedules(){
//        return new Schedules(MockTripDate(), MockSchedulesRequestDto());
//    }
//
//    public Pictures MockPicturesOne(){
//        return new Pictures(MockSchedules(), "image/test.png", "test.png", "image/png", 100000L);
//    }
//
//    public Pictures MockPicturesTwo(){
//        return new Pictures(MockSchedules(), "image/example.png", "example.png", "image/png", 50000L);
//    }
//
//    public MultipartFile MockFile(String picturesName, String filename, String contentType, Long pictureSize){
//        byte[] content = new byte[Math.toIntExact(pictureSize)];
//        return new CustomMultipartFile(picturesName, filename, contentType, content);
//    }
//
//
//    @Test
//    public void testUploadPictures() {
//        // Mock user and schedules
//        Users user = MockUsers();
//        Schedules schedules = MockSchedules();
//        Pictures picturesOne = MockPicturesOne();
//        Pictures picturesTwo = MockPicturesTwo();
//        MultipartFile file = MockFile(picturesOne.getPicturesName(), picturesOne.getPicturesName(), picturesOne.getPictureContentType(), picturesOne.getPictureSize());
//        List<Pictures> picturesList = new ArrayList<>();
//        picturesList.add(picturesOne);
//        picturesList.add(picturesTwo);
//        List<MultipartFile> fileList = new ArrayList<>();
//        fileList.add(file);
//        PutObjectResult putObjectResult = mock(PutObjectResult.class);
//        when(userRepository.findByEmail(anyString())).thenReturn(java.util.Optional.of(user));
//        when(schedulesRepository.findById(anyLong())).thenReturn(java.util.Optional.of(schedules));
//
//        // Mock Amazon S3 behavior
////        doNothing().when(amazonS3ResourceStorage).store(anyString(), any(MultipartFile.class));
//        when(amazonS3Client.putObject(bucket, picturesOne.getPicturesName(), picturesOne.getPicturesName())).thenReturn(putObjectResult);
//
//        // Mock repository behavior
//        when(picturesRepository.saveAll(picturesList)).thenReturn(picturesList);
//
//        // Call the service method
//        UploadResponseDto uploadResponseDto = picturesService.uploadPictures(schedules.getId(), fileList, user);
//
//        // Verify the expected behavior
//        // Add assertions based on the expected behavior
//        System.out.println(file.getOriginalFilename());
//        System.out.println(uploadResponseDto.getPicturesResponseDtoList().size());
////        assertEquals(file.getOriginalFilename(), uploadResponseDto.getPicturesResponseDtoList().get(0).getPicturesName());
//    }
//
//    @Test
//    public void testGetPictures() {
//        // Mock Schedules and Pictures
//        Schedules schedules = MockSchedules();
//        Pictures picture1 = MockPicturesOne();
//        Pictures picture2 = MockPicturesTwo();
//        List<Long> picturesIdList = new ArrayList<>();
//        picturesIdList.add(picture1.getId());
//        picturesIdList.add(picture2.getId());
//        List<Pictures> picturesList = new ArrayList<>();
//        picturesList.add(picture1);
//        picturesList.add(picture2);
//        when(schedulesRepository.findById(anyLong())).thenReturn(java.util.Optional.of(schedules));
//        when(picturesRepository.findAllById(picturesIdList)).thenReturn(picturesList);
//        S3Object s3Object = amazonS3Client.getObject(bucket, picture1.getPicturesName());
//
//        // Mock Amazon S3 behavior
//        for(Pictures pictures: picturesList){
//            when(amazonS3Client.getObject(bucket, pictures.getPicturesName())).thenReturn(s3Object);
//        }
//
//        // Call the service method
//        UploadResponseDto uploadResponseDto = picturesService.getPictures(1L);
//
//        // Verify the expected behavior
//        // Add assertions based on the expected behavior
//    }
//
//    @Test
//    public void testGetPicture() {
//        // Mock Pictures
//        Pictures pictures = MockPicturesOne();
//        when(picturesRepository.findById(pictures.getId())).thenReturn(java.util.Optional.of(pictures));
//        MultipartFile file = MockFile(pictures.getPicturesName(), pictures.getPicturesName(), pictures.getPictureContentType(), pictures.getPictureSize());
//        S3Object s3Object = amazonS3Client.getObject(bucket, pictures.getPicturesName());
//
//        // Mock Amazon S3 behavior
//        when(amazonS3Client.getObject(bucket, pictures.getPicturesName())).thenReturn(s3Object);
//
//        // Call the service method
//        PicturesResponseDto picturesResponseDto = picturesService.getPicture(1L);
//
//        // Verify the expected behavior
//        // Add assertions based on the expected behavior
//        assertEquals(pictures.getPicturesName(), picturesResponseDto.getPicturesName());
//    }
//
//    @Test
//    public void testUpdatePictures() {
//        // Mock user, schedules, and Amazon S3 behavior
//        Users user = MockUsers();
//        Schedules schedules = MockSchedules();
//        Pictures pictures = MockPicturesOne();
//        MultipartFile file = MockFile(pictures.getPicturesName(), pictures.getPicturesName(), pictures.getPictureContentType(), pictures.getPictureSize());
//        when(userRepository.findByEmail(anyString())).thenReturn(java.util.Optional.of(user));
//        when(schedulesRepository.findById(anyLong())).thenReturn(java.util.Optional.of(schedules));
//        doNothing().when(amazonS3ResourceStorage).store(anyString(), any(MultipartFile.class));
//        PutObjectResult putObjectResult = mock(PutObjectResult.class);
//        when(amazonS3Client.putObject(any(PutObjectRequest.class))).thenReturn(putObjectResult);
//
//        // Mock repository behavior
//        when(picturesRepository.save(any(Pictures.class))).thenReturn(pictures);
//
//        // Call the service method
//        PicturesMessageResponseDto picturesMessageResponseDto = picturesService.updatePictures(1L, file, user);
//
//        // Verify the expected behavior
//        // Add assertions based on the expected behavior
//        assertEquals(pictures.getPicturesName(), picturesMessageResponseDto.getPicturesResponseDto().getPicturesName());
//    }
//
//    @Test
//    public void testDeletePictures() {
//        // Mock user and Pictures
//        Users user = new Users(/* initialize with test data */);
//        Pictures picture = new Pictures(/* initialize with test data */);
//        when(userRepository.findByEmail(anyString())).thenReturn(java.util.Optional.of(user));
//        when(picturesRepository.findById(anyLong())).thenReturn(java.util.Optional.of(picture));
//
//        // Mock Amazon S3 behavior
//        doNothing().when(amazonS3Client).deleteObject(anyString(), anyString());
//
//        // Call the service method
//        MessageResponseDto messageResponseDto = picturesService.deletePictures(1L, user);
//
//        // Verify the expected behavior
//        // Add assertions based on the expected behavior
//    }
//}
