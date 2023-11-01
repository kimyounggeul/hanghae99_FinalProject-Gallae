package com.sparta.team2project.users;

import com.sparta.team2project.commons.dto.MessageResponseDto;
import com.sparta.team2project.commons.entity.UserRoleEnum;
import com.sparta.team2project.commons.exceptionhandler.CustomException;
import com.sparta.team2project.commons.exceptionhandler.ErrorCode;
import com.sparta.team2project.commons.jwt.JwtUtil;
import com.sparta.team2project.email.EmailService;
import com.sparta.team2project.email.ValidNumber.ValidNumber;
import com.sparta.team2project.email.ValidNumber.ValidNumberRepository;
import com.sparta.team2project.email.dto.ValidNumberRequestDto;
import com.sparta.team2project.profile.Profile;
import com.sparta.team2project.profile.ProfileRepository;
import com.sparta.team2project.refreshToken.RefreshToken;
import com.sparta.team2project.refreshToken.RefreshTokenRepository;
import com.sparta.team2project.refreshToken.TokenDto;
import com.sparta.team2project.users.dto.CheckNickNameRequestDto;
import com.sparta.team2project.users.dto.LoginRequestDto;
import com.sparta.team2project.users.dto.SignoutRequestDto;
import com.sparta.team2project.users.dto.SignupRequestDto;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileRepository profileRepository;
    private final ValidNumberRepository validNumberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;


    // ADMIN_TOKEN
    @Value("${ADMIN_TOKEN}")
    private String ADMIN_TOKEN;

    public ResponseEntity<MessageResponseDto> signup(SignupRequestDto requestDto) {
        String email = requestDto.getEmail();
        String password = passwordEncoder.encode(requestDto.getPassword());

        // 회원 중복 확인
        Optional<Users> checkUserId = userRepository.findByEmail(email);
        if (checkUserId.isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATED_EMAIL);
        }
        // 사용자 ROLE 확인
        UserRoleEnum userRole = UserRoleEnum.USER;

        if (requestDto.getAdminToken() != null && requestDto.getAdminToken().equals(ADMIN_TOKEN)) {
            userRole = UserRoleEnum.ADMIN;
        }
        // 닉네임이 이미 있을 경우 예외처리
        if (userRepository.existsByNickName(requestDto.getNickName())) {
            throw new CustomException(ErrorCode.DUPLICATED_NICKNAME);
        }
        // 기본값 설정
        String nickName = createRandomNickName();
        String profileImg = "https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2Fb0SLv8%2FbtsyLoUxvAs%2FSKsGiOc7TzkebNvH4ZQE9K%2Fimg.png";
        // 입력값이 존재한다면 기본값 대체
        if (requestDto.getNickName() != null) {
            nickName = requestDto.getNickName();
        }
        if (requestDto.getProfileImg() != null) {
            profileImg = requestDto.getProfileImg();
        }

        // 사용자 등록
        Users users = new Users(email, nickName, password, userRole, profileImg);
        userRepository.save(users);
        // 프로필 생성
        Profile profile = new Profile(users);
        profileRepository.save(profile);

        return ResponseEntity.ok(new MessageResponseDto("회원가입 완료", HttpStatus.CREATED.value()));
    }


    // 인증번호 요청
    public ResponseEntity<MessageResponseDto> checkEmail(String email) {

        //해당 이메일로 전에 인증번호를 요청했다면 전 인증번호를 db에서 삭제함
        Optional<ValidNumber> pastNumber = validNumberRepository.findByEmail(email);
        if (pastNumber.isPresent()) {
            validNumberRepository.delete(pastNumber.get());
        }

        Optional<Users> checkUsers = userRepository.findByEmail(email);

        if (checkUsers.isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATED_EMAIL); //이메일로 인증번호를 찾을 수 없음
        }
        if (email == null) {
            throw new CustomException(ErrorCode.EMAIL_FORMAT_WRONG); // 이메일 형식이 잘못됨
        }
        // 인증한 이메일이 가입시 사용할 이메일이어야 함. -> 회원가입 기능에서 ValidNumber.email과 Users.email이 일치해야 함.


        int number = (int) (Math.random() * 899999) + 100000; // 6자리 난수 생성(인증번호)

        LocalTime now = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmmss"); // 현재시간에서 HHmmss 형식으로 시간을 가져옴
        long formatedNow = Long.parseLong(now.format(formatter));

        ValidNumber validNumber = new ValidNumber(number, email, formatedNow);
        validNumberRepository.save(validNumber);

        emailService.sendNumber(number, email);
        return ResponseEntity.ok((new MessageResponseDto("인증번호가 발송되었습니다.", HttpStatus.OK.value())));
    }


    // 인증 번호 확인하기
    public boolean checkValidNumber(ValidNumberRequestDto validNumberRequestDto, String email) {
        boolean checkNumber = true;

        LocalTime now = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HHmmss"); // 현재시간에서 HHmmss 형식으로 시간을 가져옴.
        long formatedNow = Long.parseLong(now.format(formatter));

        Optional<ValidNumber> validNumberEmail = validNumberRepository.findByEmail(email);

        if (!validNumberEmail.isPresent()) {
            throw new CustomException(ErrorCode.INVALID_VALID_TOKEN); //이메일로 인증번호를 찾을 수 없음
        }

        ValidNumber validNumber = validNumberEmail.get();
        double time = validNumber.getTime();

        if (formatedNow - time >= 300) { // 인증번호 발급 받은지 3분 초과
            throw new CustomException(ErrorCode.VALID_TIME_OVER);
        }
//        number != validNumber.getValidNumber()
        if (validNumber.getValidNumber() != validNumberRequestDto.getValidNumber()) {
            throw new CustomException(ErrorCode.WRONG_NUMBER);
        } else {
            return checkNumber; // 인증성공시 true 값이 반환됩니다.
        }
    }
    // 닉네임 중복여부 체크 메서드
    public boolean checkNickName(String nickName) {
        boolean checkNickName = true;
        if (userRepository.existsByNickName(nickName)) {
            throw new CustomException(ErrorCode.DUPLICATED_NICKNAME);
        }
        return checkNickName; // 중복이 아닐 경우 true 값이 반환됩니다.
    }


    // 회원탈퇴(연관관게 설정 필요) - 미완
    public ResponseEntity<MessageResponseDto> deleteUser(SignoutRequestDto requestDto, String email) {
        Users users = userRepository.findByEmail(email).orElseThrow(
                () -> new IllegalArgumentException("등록된 아이디가 없습니다.")
        );
        if (!passwordEncoder.matches(requestDto.getPassword(), users.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        userRepository.delete(users);
        return ResponseEntity.ok(new MessageResponseDto("회원탈퇴 완료", HttpStatus.OK.value()));
    }

    // 로그인 기능
    public ResponseEntity<MessageResponseDto> login(LoginRequestDto requestDto, HttpServletResponse response) {
        Users users = userRepository.findByEmail(requestDto.getEmail()).orElseThrow(() ->
                new CustomException(ErrorCode.ID_NOT_FOUND)); // 이메일 여부 확인
        if (!passwordEncoder.matches(requestDto.getPassword(), users.getPassword())) {
            throw new CustomException(ErrorCode.PASSWORD_NOT_MATCH); // 해당 이메일의 비밀번호가 일치하지 않음
        }
        // 로그인 성공 시 액세스 토큰 및 리프레시 토큰 생성
        String accessToken = jwtUtil.createAccessToken(users.getEmail(), users.getUserRole()); // 수정: userRole 대신 userRole 사용하도록 변경
        String refreshToken = jwtUtil.createRefreshToken(users.getEmail(), users.getUserRole());
        Optional<RefreshToken> findRefreshToken = refreshTokenRepository.findByRefreshToken(refreshToken);

        if (findRefreshToken.isPresent()) {
            RefreshToken existingToken = findRefreshToken.get();
            RefreshToken updateToken = existingToken.updateToken(refreshToken);
            refreshTokenRepository.save(updateToken);
        } else {
            RefreshToken newToken = new RefreshToken(refreshToken, requestDto.getEmail());
            refreshTokenRepository.save(newToken);
        }

        response.addHeader(JwtUtil.ACCESS_KEY, accessToken);
        // 수정: 리프레시 토큰을 다시 생성하지 않고 기존 리프레시 토큰을 사용하도록 변경
        response.addHeader(JwtUtil.REFRESH_KEY, refreshToken);

        return ResponseEntity.ok(new MessageResponseDto("로그인 성공", HttpStatus.OK.value()));
    }

    // 랜덤 닉네임 생성 메서드
    public String createRandomNickName() {
        String[] nickName =
                {"행복한", "즐거운", "평화로운", "요망진", "귀여운", "화가난", "달리는", "잠자는", "놀고있는", "하늘을나는", "여행가고싶은", "영앤리치", "목적지가없는", "바쁘다바빠!", "하고싶은말이많은", "어른스러운", "깜찍한", "엉뚱한", "소심한", "화이팅!", "두둠칫", "힘내랏"};
        String[] nickName2 =
                {"여행자", "뚜벅이", "배낭꾼", "백엔드개발자", "프론트엔드개발자", "디자이너", "캠핑족", "맛집탐방러", "힐링족", "프로그래머", "자유여행자", "휴가족", "현대사회", "예술가", "돌하루방", "편", "탈출러", "풀스택개발자", "하루와하트"};

        int maxCreateRandomNickName = nickName.length * nickName2.length; // 경우의 수
        int maxTries = 300;
        for (int tries = 0; tries < maxTries; tries++) {
            int random = (int) (Math.random() * nickName.length);
            int random2 = (int) (Math.random() * nickName2.length);

            String randomNickName = nickName[random] + " " + nickName2[random2];

            if (!userRepository.existsByNickName(randomNickName)) {
                return randomNickName;
            }
        }
        throw new CustomException(ErrorCode.RANDOM_NICKNAME_FAIL);
    }
}
