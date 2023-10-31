//package com.sparta.team2project.users;
//
//import com.sparta.team2project.commons.dto.MessageResponseDto;
//import com.sparta.team2project.commons.entity.UserRoleEnum;
//import com.sparta.team2project.commons.exceptionhandler.CustomException;
//import com.sparta.team2project.commons.exceptionhandler.ErrorCode;
//import com.sparta.team2project.commons.Util.JwtUtil;
//import com.sparta.team2project.email.EmailService;
//import com.sparta.team2project.email.ValidNumber.ValidNumber;
//import com.sparta.team2project.email.ValidNumber.ValidNumberRepository;
//import com.sparta.team2project.email.dto.ValidNumberRequestDto;
//import com.sparta.team2project.profile.ProfileRepository;
//import com.sparta.team2project.users.dto.LoginRequestDto;
//import com.sparta.team2project.users.dto.SignupRequestDto;
//import jakarta.servlet.http.HttpServletResponse;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//
//import java.lang.reflect.Field;
//import java.time.LocalTime;
//import java.time.format.DateTimeFormatter;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(SpringExtension.class)
//class UserServiceTest {
//
//    @InjectMocks
//    private UserService userService;
//    @Mock
//    private UserRepository userRepository;
//    @Mock
//    private PasswordEncoder passwordEncoder;
//    @Mock
//    private ProfileRepository profileRepository;
//    @Mock
//    private ValidNumberRepository validNumberRepository;
//    @Mock
//    private EmailService emailService;
//    @Mock
//    private JwtUtil jwtUtil;
//
//    @Value("${ADMIN_TOKEN}")
//    private String ADMIN_TOKEN;
//
//    @Nested
//    @DisplayName("회원가입")
//    class Signup {
//        @Test
//        @DisplayName("회원가입 성공")
//        void signup_success() throws IllegalAccessException, NoSuchFieldException {
//            // Given
//            SignupRequestDto requestDto = new SignupRequestDto();
//            // 어드민토큰 설정(생성자, 세터 둘다 불가능, 리플렉션을 이용한)
//            Field adminTokenField = SignupRequestDto.class.getDeclaredField("adminToken");
//            adminTokenField.setAccessible(true);
//            adminTokenField.set(requestDto, ADMIN_TOKEN);
//
//            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
//            when(userRepository.existsByNickName(anyString())).thenReturn(false);
//            UserRoleEnum role = (ADMIN_TOKEN == null || ADMIN_TOKEN.isEmpty()) ? UserRoleEnum.USER : UserRoleEnum.ADMIN;
//
//
//            // When
//            MessageResponseDto response = userService.signup(requestDto).getBody();
//
//            // Then
//            assertEquals("회원가입 완료", response.getMsg());
//            assertEquals(201, response.getStatusCode());
//        }
//
//        @Test
//        @DisplayName("회원가입 실패 - 중복 이메일")
//        void signup_fail_duplicate_email() throws NoSuchFieldException, IllegalAccessException {
//            // 가상의 이메일 주소
//            String email = "test@test.com";
//            // UserRepository에서 해당 이메일을 찾아올 때 더미 데이터 반환
//            Users dummyUser = new Users();
//
//            when(userRepository.findByEmail(email)).thenReturn(Optional.of(dummyUser));
//            // 중복 확인 메서드 호출, 예외 발생 확인
//            CustomException exception = assertThrows(CustomException.class, () -> userService.checkEmail(email));
//
//            // 예외 메시지나 코드를 확인하여 올바른 예외가 발생했는지 확인 가능
//            assertEquals(ErrorCode.DUPLICATED_EMAIL, exception.getErrorCode());
//        }
//
//        @Test
//        @DisplayName("회원가입 실패 - 중복 닉네임")
//        void signup_fail_duplicate_nickname() {
//            // 가상의 닉네임
//            String nickName = "duplicateNick";
//
//            // UserRepository에서 해당 닉네임이 이미 존재함
//            when(userRepository.existsByNickName(nickName)).thenReturn(true);
//            // 중복 확인 메서드 호출, 예외 발생 확인
//            CustomException exception = assertThrows(CustomException.class, () -> userService.checkNickName(nickName));
//            assertEquals(ErrorCode.DUPLICATED_NICKNAME, exception.getErrorCode());
//        }
//    }
//
//
//    @Nested
//    @DisplayName("이메일 인증")
//    class CheckEmail {
//        @Test
//        @DisplayName("이메일 인증 성공")
//        void checkEmail_success() {
//            // 가상의 이메일
//            String email = "test@test.com";
//
//            // UserRepository에 해당 이메일 존재 x
//            when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
//            // ValidNumberRepository에서 번호가 존재하지 않도록 설정
//            when(validNumberRepository.findByEmail(email)).thenReturn(Optional.empty());
//
//            // 메서드 호출
//            ResponseEntity<MessageResponseDto> response = userService.checkEmail(email);
//
//            // 응답 확인 - 중복 없음, 이전 번호 없음
//            assertEquals(HttpStatus.OK, response.getStatusCode());
//            assertEquals("인증번호가 발송되었습니다.", response.getBody().getMsg());
//        }
//
//        @Test
//        @DisplayName("이메일 인증 실패 - 중복 이메일")
//        void checkEmail_fail_duplicate_email() {
//            // 이메일 설정
//            String email = "duplicate@test.com";
//
//            // UserRepository에서 해당 이메일이 이미 존재함
//            when(userRepository.findByEmail(email)).thenReturn(Optional.of(new Users()));
//            // ValidNumberRepository에서 이전 번호가 존재x
//            when(validNumberRepository.findByEmail(email)).thenReturn(Optional.empty());
//
//            // 메서드 호출, 예외 발생 확인
//            CustomException exception = assertThrows(CustomException.class, () -> userService.checkEmail(email));
//            assertEquals(ErrorCode.DUPLICATED_EMAIL, exception.getErrorCode());
//        }
//
//        @Test
//        @DisplayName("이메일 인증 실패 - 잘못된 이메일 형식")
//        void checkEmail_fail_wrong_email_format() {
//            // 이메일을 null로 설정
//            String email = null;
//
//            // UserRepository와 ValidNumberRepository에서 해당 이메일을 찾지 않도록 설정
//            when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
//            when(validNumberRepository.findByEmail(email)).thenReturn(Optional.empty());
//
//            // 메서드 호출시 예외가 발생하는지 확인
//            CustomException exception = assertThrows(CustomException.class, () -> userService.checkEmail(email));
//
//            // 예외에 올바른 ErrorCode가 설정되었는지 확인
//            assertEquals(ErrorCode.EMAIL_FORMAT_WRONG, exception.getErrorCode());
//        }
//
//    }
//
//
//    @Nested
//    @DisplayName("인증번호 확인")
//    class CheckValidNumber {
//        @Test
//        @DisplayName("인증번호 확인 성공")
//        void checkValidNumber_success() throws NoSuchFieldException, IllegalAccessException {
//            // Given
//            int validNumberToCheck = 123456; // 원하는 인증번호
//            ValidNumber validNumber = new ValidNumber(validNumberToCheck, "example@example.com", 123456);
//            when(validNumberRepository.findByEmail(anyString())).thenReturn(Optional.of(validNumber));
//
//            ValidNumberRequestDto requestDto = new ValidNumberRequestDto();
//
//            // 현재 시간을 가져온다 (ValidNumber 생성 시간과 같은 값을 사용)
//            LocalTime now = LocalTime.parse("12:34:56"); // 예시 시간
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmmss");
//            long formattedNow = Long.parseLong(now.format(formatter));
//
//            Field validNumberField = ValidNumberRequestDto.class.getDeclaredField("validNumber");
//            validNumberField.setAccessible(true);
//            validNumberField.set(requestDto, validNumberToCheck);
//
//            // When
//            boolean result = userService.checkValidNumber(requestDto, "example@example.com");
//
//            // Then
//            assertTrue(result);
//        }
//
//        @Test
//        @DisplayName("인증번호 확인 실패 - 이메일로 인증번호를 찾을 수 없음")
//        void checkValidNumber_fail_invalid_token() throws NoSuchFieldException, IllegalAccessException {
//            // Given
//            int validNumberToCheck = 123456; // 올바른 인증번호
//            // validNumberRepository.findByEmail(email)가 빈 Optional을 반환하도록 설정
//            when(validNumberRepository.findByEmail(anyString())).thenReturn(Optional.empty());
//
//            ValidNumberRequestDto requestDto = new ValidNumberRequestDto();
//            // requestDto에 올바른 인증번호 설정
//            Field validNumberField = ValidNumberRequestDto.class.getDeclaredField("validNumber");
//            validNumberField.setAccessible(true);
//            validNumberField.set(requestDto, validNumberToCheck);
//
//            // When
//            // 인증번호 확인 메서드를 호출하면 예외가 발생해야 합니다.
//            CustomException exception = assertThrows(CustomException.class, () -> userService.checkValidNumber(requestDto, "example@example.com"));
//
//            // Then
//            assertEquals(ErrorCode.INVALID_VALID_TOKEN, exception.getErrorCode());
//        }
//
//        @Test
//        @DisplayName("인증번호 확인 실패 - 인증번호 유효 시간 초과")
//        void checkValidNumber_fail_expired_token() throws NoSuchFieldException, IllegalAccessException {
//            // Given
//            int validNumberToCheck = 123456; // 원하는 인증번호
//
//            ValidNumber validNumber = new ValidNumber(123456, "example@example.com", 123456);
//            when(validNumberRepository.findByEmail(anyString())).thenReturn(Optional.of(validNumber));
//
//            ValidNumberRequestDto requestDto = new ValidNumberRequestDto();
//            // requestDto에 올바르지 않은 인증번호 설정
//            Field validNumberField = ValidNumberRequestDto.class.getDeclaredField("validNumber");
//            validNumberField.setAccessible(true);
//            validNumberField.set(requestDto, validNumberToCheck);
//            // 시간을 현재 시간보다 3분 뒤로 설정 (유효 시간 초과)
//            LocalTime now = LocalTime.now().plusMinutes(3);
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmmss");
//            long formattedNow = Long.parseLong(now.format(formatter));
//
//            // Create a mock object for validNumberRepository
//            ValidNumberRepository validNumberRepository = mock(ValidNumberRepository.class);
//
//            // Make sure validNumberEmail.isPresent() returns true
//            when(validNumberRepository.findByEmail(anyString())).thenReturn(Optional.of(new ValidNumber(0, "", 0))); // 빈 Optional을 반환하도록 설정
//
//            // Try to catch the exception
//            CustomException exception = assertThrows(CustomException.class, () -> userService.checkValidNumber(requestDto, "example@example.com"));
//
//            // Verify that the expected exception was thrown
//            assertEquals(ErrorCode.VALID_TIME_OVER, exception.getErrorCode());
//        }
//
//        @Test
//        @DisplayName("인증번호 확인 실패 - 인증번호가 일치하지 않음")
//        void checkValidNumber_fail_wrong_number() throws NoSuchFieldException, IllegalAccessException {
//            // Given
//            int validNumberToCheck = 654321; // 원하는 인증번호 (올바르지 않은 번호로 설정)
//
//            ValidNumber validNumber = new ValidNumber(123456, "example@example.com", 123456);
//            when(validNumberRepository.findByEmail(anyString())).thenReturn(Optional.of(validNumber));
//
//            ValidNumberRequestDto requestDto = new ValidNumberRequestDto();
//            // requestDto에 올바르지 않은 인증번호 설정
//            Field validNumberField = ValidNumberRequestDto.class.getDeclaredField("validNumber");
//            validNumberField.setAccessible(true);
//            validNumberField.set(requestDto, validNumberToCheck);
//
//            // When
//            // 인증번호 확인 메서드를 호출하면 예외가 발생해야 합니다.
//            CustomException exception = assertThrows(CustomException.class, () -> userService.checkValidNumber(requestDto, "example@example.com"));
//
//            // Then
//            assertEquals(ErrorCode.WRONG_NUMBER, exception.getErrorCode());
//
//        }
//    }
//
//
//    @Nested
//    @DisplayName("닉네임 중복 확인")
//    class checkNickName {
//        @Test
//        @DisplayName("닉네임 중복 검사 - 중복되지 않은 닉네임")
//        void checkNickName_nonDuplicateNickname() {
//            // Given
//            String nonDuplicateNickname = "uniqueNickname";
//
//            // userRepository.existsByNickName가 false를 반환하도록 설정
//            when(userRepository.existsByNickName(nonDuplicateNickname)).thenReturn(false);
//
//            // When
//            boolean result = userService.checkNickName(nonDuplicateNickname);
//
//            // Then
//            assertTrue(result);
//        }
//
//        @Test
//        @DisplayName("닉네임 중복 확인 - 중복된 닉네임")
//        void checkNickName_duplicateNickName() {
//            // Given
//            String duplicateNickname = "existingNickname";
//
//            // userRepository.existsByNickName가 true를 반환하도록 설정
//            when(userRepository.existsByNickName(duplicateNickname)).thenReturn(true);
//
//            // When, Then
//            CustomException exception = assertThrows(CustomException.class, () -> userService.checkNickName(duplicateNickname));
//            assertEquals(ErrorCode.DUPLICATED_NICKNAME, exception.getErrorCode());
//        }
//    }
//
//
//    @Nested
//    @DisplayName("로그인")
//    class Login {
//        @Test
//        @DisplayName("로그인 성공")
//        void login_success() throws IllegalAccessException, NoSuchFieldException {
//            // Given
//            String email = "user@example.com";
//            String password = "password";
//            UserRoleEnum userRole = UserRoleEnum.USER; // UserRoleEnum 값 사용
//
//            // Create a Users object using reflection
//            Users user = new Users();
//            Field emailField = Users.class.getDeclaredField("email");
//            emailField.setAccessible(true);
//            emailField.set(user, email);
//
//            Field passwordField = Users.class.getDeclaredField("password");
//            passwordField.setAccessible(true);
//            passwordField.set(user, password);
//
//            Field roleField = Users.class.getDeclaredField("userRole");
//            roleField.setAccessible(true);
//            roleField.set(user, userRole); // UserRoleEnum 값을 설정
//
//            when(userRepository.findByEmail(email)).thenReturn(java.util.Optional.of(user));
//            when(passwordEncoder.matches(eq(password), anyString())).thenReturn(true);
//            when(jwtUtil.createToken(email, userRole)).thenReturn("fakeAccessToken");
//
//            LoginRequestDto requestDto = new LoginRequestDto();
//            Field requestEmailField = LoginRequestDto.class.getDeclaredField("email");
//            requestEmailField.setAccessible(true);
//            requestEmailField.set(requestDto, email);
//
//            Field requestPasswordField = LoginRequestDto.class.getDeclaredField("password");
//            requestPasswordField.setAccessible(true);
//            requestPasswordField.set(requestDto, password);
//
//            HttpServletResponse response = mock(HttpServletResponse.class);
//
//            // When
//            ResponseEntity<MessageResponseDto> responseEntity = userService.login(requestDto, response);
//
//            // Then
//            assertEquals(HttpStatus.OK.value(), responseEntity.getStatusCodeValue()); // HTTP 응답 코드를 확인합니다.
//            assertEquals("로그인 성공", responseEntity.getBody().getMsg());
//            verify(response, times(1)).addHeader("Authorization", "fakeAccessToken");
//        }
//    }
//
//
//    @Nested
//    @DisplayName("랜덤 닉네임 생성")
//    class createRandomNickName {
//        @Test
//        @DisplayName("랜덤 닉네임 생성 성공")
//        void createRandomNickName_success() {
//            // 랜덤 닉네임 생성 및 반환
//            String randomNickName = userService.createRandomNickName();
//
//            // 생성된 닉네임이 null이 아니고 빈 문자열이 아닌지 확인
//            assertNotNull(randomNickName);
//            assertFalse(randomNickName.isEmpty());
//        }
//        @Test
//        @DisplayName("랜덤 닉네임 생성 실패 - 중복 닉네임 발생")
//        void createRandomNickName_fail_max_attempts() {
//            // userRepository.existsByNickName() 메서드의 반환값을 설정
//            when(userRepository.existsByNickName(anyString())).thenReturn(true);
//
//            // 중복 닉네임이 생성되면 CustomException 예외가 발생해야 함
//            CustomException exception = assertThrows(CustomException.class, () -> userService.createRandomNickName());
//            assertEquals(ErrorCode.RANDOM_NICKNAME_FAIL, exception.getErrorCode());
//        }
//    }
//}