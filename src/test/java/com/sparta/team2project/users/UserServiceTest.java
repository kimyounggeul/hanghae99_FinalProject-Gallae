package com.sparta.team2project.users;

import com.sparta.team2project.comments.repository.CommentsRepository;
import com.sparta.team2project.commons.Util.JwtUtil;
import com.sparta.team2project.commons.Util.RedisUtil;
import com.sparta.team2project.commons.dto.MessageResponseDto;
import com.sparta.team2project.commons.entity.UserRoleEnum;
import com.sparta.team2project.commons.exceptionhandler.CustomException;
import com.sparta.team2project.commons.exceptionhandler.ErrorCode;
import com.sparta.team2project.email.EmailService;
import com.sparta.team2project.email.dto.ValidNumberRequestDto;
import com.sparta.team2project.posts.repository.PostsRepository;
import com.sparta.team2project.profile.Profile;
import com.sparta.team2project.profile.ProfileRepository;
import com.sparta.team2project.replies.repository.RepliesRepository;
import com.sparta.team2project.users.dto.LoginRequestDto;
import com.sparta.team2project.users.dto.SignoutRequestDto;
import com.sparta.team2project.users.dto.SignupRequestDto;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@DataRedisTest
class UserServiceTest {
    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private PostsRepository postsRepository;
    @Mock
    private CommentsRepository commentsRepository;
    @Mock
    private RepliesRepository repliesRepository;

    @Mock
    private EmailService emailService;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private RedisUtil redisUtil;


    @Value("${ADMIN_TOKEN}")
    private String ADMIN_TOKEN;

    @Nested
    @DisplayName("회원가입")
    class Signup {
        @Test
        @DisplayName("회원가입 성공")
        void signup_success() throws IllegalAccessException, NoSuchFieldException {
            // Given
            SignupRequestDto requestDto = new SignupRequestDto();
            // 어드민토큰 설정(생성자, 세터 둘다 불가능, 리플렉션을 이용한)
            Field adminTokenField = SignupRequestDto.class.getDeclaredField("adminToken");
            adminTokenField.setAccessible(true);
            adminTokenField.set(requestDto, ADMIN_TOKEN);

            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(userRepository.existsByNickName(anyString())).thenReturn(false);
            UserRoleEnum role = (ADMIN_TOKEN == null || ADMIN_TOKEN.isEmpty()) ? UserRoleEnum.USER : UserRoleEnum.ADMIN;


            // When
            MessageResponseDto response = userService.signup(requestDto).getBody();

            // Then
            assertEquals("회원가입 완료", response.getMsg());
            assertEquals(201, response.getStatusCode());
        }

        @Test
        @DisplayName("회원가입 실패 - 중복 이메일")
        void signup_fail_duplicate_email() throws NoSuchFieldException, IllegalAccessException {
            // 가상의 이메일 주소
            String email = "test@test.com";
            // UserRepository에서 해당 이메일을 찾아올 때 더미 데이터 반환
            Users dummyUser = new Users();

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(dummyUser));
            // 중복 확인 메서드 호출, 예외 발생 확인
            CustomException exception = assertThrows(CustomException.class, () -> userService.checkEmail(email));

            // 예외 메시지나 코드를 확인하여 올바른 예외가 발생했는지 확인 가능
            assertEquals(ErrorCode.DUPLICATED_EMAIL, exception.getErrorCode());
        }

        @Test
        @DisplayName("회원가입 실패 - 중복 닉네임")
        void signup_fail_duplicate_nickname() {
            // 가상의 닉네임
            String nickName = "duplicateNick";

            // UserRepository에서 해당 닉네임이 이미 존재함
            when(userRepository.existsByNickName(nickName)).thenReturn(true);
            // 중복 확인 메서드 호출, 예외 발생 확인
            CustomException exception = assertThrows(CustomException.class, () -> userService.checkNickName(nickName));
            assertEquals(ErrorCode.DUPLICATED_NICKNAME, exception.getErrorCode());
        }
    }


    @Nested
    @DisplayName("이메일 인증")
    class CheckEmail {

        @BeforeEach
        void setUp() {
            // RedisUtil이 모의 객체(Mock)로 주입되었는지 확인합니다.
//            Assert.notNull(redisUtil, "redisUtil should not be null");
            MockitoAnnotations.initMocks(this);

        }

        @Test
        @DisplayName("이메일 인증 성공")
        void checkEmail_success() {
            // 가상의 이메일
            String email = "test@test.com";
            // 메서드 호출
            ResponseEntity<MessageResponseDto> response = userService.checkEmail(email);

            // 응답 확인 - 인증번호가 발송되었음
            verify(redisUtil).setDataExpire(eq(email), anyString(), anyLong()); // RedisUtil 메서드가 호출되었는지 확인
            verify(emailService).sendNumber(anyInt(), eq(email)); // emailService 메서드가 호출되었는지 확인
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("인증번호가 발송되었습니다.", response.getBody().getMsg());
        }

        @Test
        @DisplayName("이메일 인증 실패 - 중복 이메일")
        void checkEmail_fail_duplicate_email() {
            // 이메일 설정
            String email = "duplicate@test.com";

            // UserRepository에서 해당 이메일이 이미 존재함
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(new Users()));

            // 메서드 호출, 예외 발생 확인
            CustomException exception = assertThrows(CustomException.class, () -> userService.checkEmail(email));
            assertEquals(ErrorCode.DUPLICATED_EMAIL, exception.getErrorCode());
        }
    }


    @Nested
    @DisplayName("인증번호 확인")
    class CheckValidNumber {
        @Test
        @DisplayName("인증번호 확인 성공")
        void checkValidNumber_success() throws NoSuchFieldException, IllegalAccessException {
            // Given
            String email = "example@example.com";
            int validNumberToCheck = 123456; // 원하는 인증번호

            // Redis에 저장된 인증번호 설정
            when(redisUtil.getData(eq(email))).thenReturn(String.valueOf(validNumberToCheck));

            ValidNumberRequestDto requestDto = new ValidNumberRequestDto();
            Field validNumberField = ValidNumberRequestDto.class.getDeclaredField("validNumber");
            validNumberField.setAccessible(true);
            validNumberField.set(requestDto, validNumberToCheck);

            // When
            boolean result = userService.checkValidNumber(requestDto, email);

            // Then
            assertTrue(result);
        }
    }

    @Test
    @DisplayName("인증번호 확인 실패 - 이메일로 인증번호를 찾을 수 없음")
    void checkValidNumber_fail_invalid_token() throws NoSuchFieldException, IllegalAccessException {
        // Given
        String email = "example@example.com";
        int validNumberToCheck = 123456; // 올바른 인증번호

        // Redis에 저장된 인증번호가 없는 상태를 설정
        when(redisUtil.getData(eq(email))).thenReturn(null);

        ValidNumberRequestDto requestDto = new ValidNumberRequestDto();
        Field validNumberField = ValidNumberRequestDto.class.getDeclaredField("validNumber");
        validNumberField.setAccessible(true);
        validNumberField.set(requestDto, validNumberToCheck);

        // When
        CustomException exception = assertThrows(CustomException.class, () -> userService.checkValidNumber(requestDto, email));

        // Then
        assertEquals(ErrorCode.WRONG_NUMBER, exception.getErrorCode());
    }

    @Test
    @DisplayName("인증번호 확인 실패 - 인증번호가 일치하지 않음")
    void checkValidNumber_fail_wrong_number() throws NoSuchFieldException, IllegalAccessException {
        // Given
        String email = "example@example.com";
        int validNumberToCheck = 654321; // 올바르지 않은 인증번호

        // Redis에 저장된 인증번호를 설정
        when(redisUtil.getData(eq(email))).thenReturn("123456"); // 올바른 인증번호로 설정

        ValidNumberRequestDto requestDto = new ValidNumberRequestDto();
        Field validNumberField = ValidNumberRequestDto.class.getDeclaredField("validNumber");
        validNumberField.setAccessible(true);
        validNumberField.set(requestDto, validNumberToCheck);
        // When
        CustomException exception = assertThrows(CustomException.class, () -> userService.checkValidNumber(requestDto, email));

        // Then
        assertEquals(ErrorCode.WRONG_NUMBER, exception.getErrorCode());
    }

    @Nested
    @DisplayName("닉네임 중복 확인")
    class checkNickName {
        @Test
        @DisplayName("닉네임 중복 검사 - 중복되지 않은 닉네임")
        void checkNickName_nonDuplicateNickname() {
            // Given
            String nonDuplicateNickname = "uniqueNickname";
            // userRepository.existsByNickName가 false를 반환하도록 설정
            when(userRepository.existsByNickName(nonDuplicateNickname)).thenReturn(false);

            // When
            boolean result = userService.checkNickName(nonDuplicateNickname);

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("닉네임 중복 확인 - 중복된 닉네임")
        void checkNickName_duplicateNickName() {
            // Given
            String duplicateNickname = "existingNickname";
            // userRepository.existsByNickName가 true를 반환하도록 설정
            when(userRepository.existsByNickName(duplicateNickname)).thenReturn(true);

            // When, Then
            CustomException exception = assertThrows(CustomException.class, () -> userService.checkNickName(duplicateNickname));
            assertEquals(ErrorCode.DUPLICATED_NICKNAME, exception.getErrorCode());
        }
    }


    @Nested
    @DisplayName("로그인")
    class Login {
        @Test
        @DisplayName("로그인 성공")
        void login_success() throws IllegalAccessException, NoSuchFieldException {
            // Given
            String email = "user@example.com";
            String password = "password";
            UserRoleEnum userRole = UserRoleEnum.USER;

            Users user = new Users();
            Field emailField = Users.class.getDeclaredField("email");
            emailField.setAccessible(true);
            emailField.set(user, email);

            Field passwordField = Users.class.getDeclaredField("password");
            passwordField.setAccessible(true);
            passwordField.set(user, password);

            Field roleField = Users.class.getDeclaredField("userRole");
            roleField.setAccessible(true);
            roleField.set(user, userRole);

            Field accessTimeField = JwtUtil.class.getDeclaredField("ACCESS_TOKEN_TIME");
            accessTimeField.setAccessible(true);
            accessTimeField.set(jwtUtil, 3600000L);

            when(userRepository.findByEmail(email)).thenReturn(java.util.Optional.of(user));
            when(passwordEncoder.matches(eq(password), anyString())).thenReturn(true);
            when(jwtUtil.createAccessToken(email, userRole)).thenReturn("fakeAccessToken"); // 업데이트된 메소드명 사용

            LoginRequestDto requestDto = new LoginRequestDto();
            Field requestEmailField = LoginRequestDto.class.getDeclaredField("email");
            requestEmailField.setAccessible(true);
            requestEmailField.set(requestDto, email);

            Field requestPasswordField = LoginRequestDto.class.getDeclaredField("password");
            requestPasswordField.setAccessible(true);
            requestPasswordField.set(requestDto, password);

            HttpServletResponse response = mock(HttpServletResponse.class);

            // When
            ResponseEntity<MessageResponseDto> responseEntity = userService.login(requestDto, response);

            // Then
            assertEquals(HttpStatus.OK.value(), responseEntity.getStatusCodeValue());
            assertEquals("로그인 성공", responseEntity.getBody().getMsg());
            verify(response, times(1)).addHeader("Authorization", "fakeAccessToken");
        }

        @Test
        @DisplayName("로그인 실패 - 아이디 찾을 수 없음")
        void login_fail_email_not_found() throws NoSuchFieldException, IllegalAccessException {
            // Given
            String email = "user@example.com";
            String password = "password";
            Users user = new Users();


            LoginRequestDto requestDto = new LoginRequestDto();
            Field emailField = Users.class.getDeclaredField("email");
            emailField.setAccessible(true);
            emailField.set(user, email);

            Field requestPasswordField = LoginRequestDto.class.getDeclaredField("password");
            requestPasswordField.setAccessible(true);
            requestPasswordField.set(requestDto, password);

            when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

            // When and Then
            CustomException exception = assertThrows(CustomException.class, () -> userService.login(requestDto, null));
            assertEquals(ErrorCode.ID_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("로그인 실패 - 비밀번호 불일치")
        void login_fail_password_not_match() throws NoSuchFieldException, IllegalAccessException {
            // Given
            String email = "user@example.com";
            String password = "password";

            Users user = new Users();
            setPrivateField(user, "email", email);
            setPrivateField(user, "password", "incorrect_password");

            when(userRepository.findByEmail(email)).thenReturn(java.util.Optional.of(user));
            when(passwordEncoder.matches(password, user.getPassword())).thenReturn(false);

            LoginRequestDto requestDto = new LoginRequestDto();
            setPrivateField(requestDto, "email", email);
            setPrivateField(requestDto, "password", password);

            // When and Then
            CustomException exception = assertThrows(CustomException.class, () -> userService.login(requestDto, null));
            assertEquals(ErrorCode.PASSWORD_NOT_MATCH, exception.getErrorCode());
        }

        private void setPrivateField(Object object, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        }
    }

    @Nested
    @DisplayName("회원 탈퇴")
    class deleteUser {
        @Test
        @DisplayName("회원탈퇴 성공")
        void deleteUser_success() throws NoSuchFieldException, IllegalAccessException {
            // Given
            String email = "user@example.com";

            SignoutRequestDto requestDto = new SignoutRequestDto();
            Field emailFieldInDto = SignoutRequestDto.class.getDeclaredField("email");
            emailFieldInDto.setAccessible(true);
            emailFieldInDto.set(requestDto, email);

            Field passwordFieldInDto = SignoutRequestDto.class.getDeclaredField("password");
            passwordFieldInDto.setAccessible(true);
            passwordFieldInDto.set(requestDto, "password"); // Set a valid password

            // Create a Users object using reflection
            Users users = new Users();
            Field emailFieldInUsers = Users.class.getDeclaredField("email");
            emailFieldInUsers.setAccessible(true);
            emailFieldInUsers.set(users, email);

            Field passwordFieldInUsers = Users.class.getDeclaredField("password");
            passwordFieldInUsers.setAccessible(true);
            passwordFieldInUsers.set(users, "password");

            Profile userProfile = new Profile(); // 사용자의 프로필

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(users));
            when(passwordEncoder.matches(requestDto.getPassword(), users.getPassword())).thenReturn(true);
            when(profileRepository.findByUsers_Email(email)).thenReturn(Optional.of(userProfile));
            when(commentsRepository.findByEmail(email)).thenReturn(new ArrayList<>()); // 사용자의 게시물이 없음
            when(repliesRepository.findByEmail(email)).thenReturn(new ArrayList<>()); // 사용자의 게시물이 없음
            when(postsRepository.findByUsers(users)).thenReturn(new ArrayList<>()); // 사용자의 게시물이 없음

            // When
            ResponseEntity<MessageResponseDto> result = userService.deleteUser(requestDto, email);

            // Then
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("회원탈퇴 완료", result.getBody().getMsg());
            verify(userRepository, times(1)).delete(users);
            verify(redisUtil, times(1)).deleteRefreshToken(email);
        }

        @Test
        @DisplayName("회원탈퇴 - 사용자 없음")
        void deleteUser_userNotFound() throws NoSuchFieldException, IllegalAccessException {
            // Given
            String email = "user@example.com";

            SignoutRequestDto requestDto = new SignoutRequestDto();
            Field emailFieldInDto = SignoutRequestDto.class.getDeclaredField("email");
            emailFieldInDto.setAccessible(true);
            emailFieldInDto.set(requestDto, email);

            Field passwordFieldInDto = SignoutRequestDto.class.getDeclaredField("password");
            passwordFieldInDto.setAccessible(true);
            passwordFieldInDto.set(requestDto, "password"); // Set a valid password

            when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

            // When and Then
            assertThrows(CustomException.class, () -> userService.deleteUser(requestDto, email));
        }

        @Test
        @DisplayName("회원탈퇴 - 비밀번호 불일치")
        void deleteUser_passwordMismatch() throws NoSuchFieldException, IllegalAccessException {
            // Given
            String email = "user@example.com";
            SignoutRequestDto requestDto = new SignoutRequestDto();
            Field emailFieldInDto = SignoutRequestDto.class.getDeclaredField("email");
            emailFieldInDto.setAccessible(true);
            emailFieldInDto.set(requestDto, email);

            Field passwordFieldInDto = SignoutRequestDto.class.getDeclaredField("password");
            passwordFieldInDto.setAccessible(true);
            passwordFieldInDto.set(requestDto, "wrongPassword"); // Set a valid password

            Users users = new Users();
            Field emailFieldInUsers = Users.class.getDeclaredField("email");
            emailFieldInUsers.setAccessible(true);
            emailFieldInUsers.set(users, email);

            Field passwordFieldInUsers = Users.class.getDeclaredField("password");
            passwordFieldInUsers.setAccessible(true);
            passwordFieldInUsers.set(users, "correctPassword");

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(users));
            when(passwordEncoder.matches(requestDto.getPassword(), users.getPassword())).thenReturn(false);

            // When and Then
            assertThrows(CustomException.class, () -> userService.deleteUser(requestDto, email));
        }

        @Test
        @DisplayName("회원탈퇴 - 이메일 불일치")
        void deleteUser_emailMismatch() throws NoSuchFieldException, IllegalAccessException {
            // Given
            String correctEmail = "correctUser@example.com";
            String wrongEmail = "wrongUser@example.com";
            SignoutRequestDto requestDto = new SignoutRequestDto();
            Field emailFieldInDto = SignoutRequestDto.class.getDeclaredField("email");
            emailFieldInDto.setAccessible(true);
            emailFieldInDto.set(requestDto, wrongEmail);

            Field passwordFieldInDto = SignoutRequestDto.class.getDeclaredField("password");
            passwordFieldInDto.setAccessible(true);
            passwordFieldInDto.set(requestDto, "password"); // Set a valid password

            // Create a Users object using reflection
            Users users = new Users();
            Field emailFieldInUsers = Users.class.getDeclaredField("email");
            emailFieldInUsers.setAccessible(true);
            emailFieldInUsers.set(users, correctEmail);

            Field passwordFieldInUsers = Users.class.getDeclaredField("password");
            passwordFieldInUsers.setAccessible(true);
            passwordFieldInUsers.set(users, "password");

            when(userRepository.findByEmail(correctEmail)).thenReturn(Optional.of(users));

            // When and Then
            assertThrows(CustomException.class, () -> userService.deleteUser(requestDto, wrongEmail));
        }

        @Test
        @DisplayName("회원탈퇴 - 프로필 없음")
        void deleteUser_profileNotFound() throws NoSuchFieldException, IllegalAccessException {
            // Given
            String email = "user@example.com";
            SignoutRequestDto requestDto = new SignoutRequestDto();
            Field emailFieldInDto = SignoutRequestDto.class.getDeclaredField("email");
            emailFieldInDto.setAccessible(true);
            emailFieldInDto.set(requestDto, email);

            Field passwordFieldInDto = SignoutRequestDto.class.getDeclaredField("password");
            passwordFieldInDto.setAccessible(true);
            passwordFieldInDto.set(requestDto, "password"); // Set a valid password

            // Create a Users object using reflection
            Users users = new Users();
            Field emailFieldInUsers = Users.class.getDeclaredField("email");
            emailFieldInUsers.setAccessible(true);
            emailFieldInUsers.set(users, email);

            Field passwordFieldInUsers = Users.class.getDeclaredField("password");
            passwordFieldInUsers.setAccessible(true);
            passwordFieldInUsers.set(users, "password");

            when(userRepository.findByEmail(email)).thenReturn(Optional.of(users));
            when(passwordEncoder.matches(requestDto.getPassword(), users.getPassword())).thenReturn(true);
            when(profileRepository.findByUsers_Email(email)).thenReturn(Optional.empty());

            // When and Then
            assertThrows(CustomException.class, () -> userService.deleteUser(requestDto, email));
        }
    }

    @Nested
    @DisplayName("랜덤 닉네임 생성")
    class createRandomNickName {
        @Test
        @DisplayName("랜덤 닉네임 생성 성공")
        void createRandomNickName_success() {
            // 랜덤 닉네임 생성 및 반환
            String randomNickName = userService.createRandomNickName();

            // 생성된 닉네임이 null이 아니고 빈 문자열이 아닌지 확인
            assertNotNull(randomNickName);
            assertFalse(randomNickName.isEmpty());
        }

        @Test
        @DisplayName("랜덤 닉네임 생성 실패 - 중복 닉네임 발생")
        void createRandomNickName_fail_max_attempts() {
            // userRepository.existsByNickName() 메서드의 반환값을 설정
            when(userRepository.existsByNickName(anyString())).thenReturn(true);

            // 중복 닉네임이 생성되면 CustomException 예외가 발생해야 함
            CustomException exception = assertThrows(CustomException.class, () -> userService.createRandomNickName());
            assertEquals(ErrorCode.RANDOM_NICKNAME_FAIL, exception.getErrorCode());
        }
    }
}

