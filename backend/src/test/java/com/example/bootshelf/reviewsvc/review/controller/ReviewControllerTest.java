package com.example.bootshelf.reviewsvc.review.controller;

import com.example.bootshelf.common.BaseRes;
import com.example.bootshelf.common.error.ErrorCode;
import com.example.bootshelf.common.error.entityexception.ReviewException;
import com.example.bootshelf.common.error.entityexception.UserException;
import com.example.bootshelf.config.SecurityConfig;
import com.example.bootshelf.config.handler.OAuth2AuthenticationSuccessHandler;
import com.example.bootshelf.config.utils.JwtUtils;
import com.example.bootshelf.reviewsvc.review.model.request.PatchUpdateReviewReq;
import com.example.bootshelf.reviewsvc.review.model.request.PostCreateReviewReq;
import com.example.bootshelf.reviewsvc.review.model.response.GetListReviewRes;
import com.example.bootshelf.reviewsvc.review.model.response.GetMyListReviewRes;
import com.example.bootshelf.reviewsvc.review.model.response.GetMyListReviewResResult;
import com.example.bootshelf.reviewsvc.review.model.response.PostCreateReviewRes;
import com.example.bootshelf.reviewsvc.review.repository.ReviewRepository;
import com.example.bootshelf.reviewsvc.review.service.ReviewService;
import com.example.bootshelf.reviewsvc.reviewimage.service.ReviewImageService;
import com.example.bootshelf.user.controller.mock.WithCustomMockUser;
import com.example.bootshelf.user.exception.security.CustomAccessDeniedHandler;
import com.example.bootshelf.user.exception.security.CustomAuthenticationEntryPoint;
import com.example.bootshelf.user.model.entity.User;
import com.example.bootshelf.user.model.request.PatchUpdateUserReq;
import com.example.bootshelf.user.model.response.GetListUserRes;
import com.example.bootshelf.user.repository.UserRepository;
import com.example.bootshelf.user.service.UserOAuth2Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(ReviewController.class)
@ContextConfiguration(classes = {SecurityConfig.class, ReviewController.class})
@AutoConfigureMockMvc
@DisplayName("ReviewController 테스트")
public class ReviewControllerTest {


    @Autowired
    MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private ReviewImageService reviewImageService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ReviewRepository reviewRepository;

    @MockBean
    private UserOAuth2Service userOAuth2Service;

    @MockBean
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @MockBean
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @DisplayName("후기글 생성 성공")
    @WithCustomMockUser
    @Test
    void reviewScrapController_create_success() throws Exception {
        PostCreateReviewRes mockResponse = PostCreateReviewRes.builder()
                .reviewIdx(1)
                .reviewCategoryIdx(1)
                .reviewTitle("한화 후기")
                .courseName("백엔드")
                .reviewContent("부트캠프")
                .courseEvaluation(5)
                .build();

        BaseRes baseRes = BaseRes.builder()
                .isSuccess(true)
                .message("후기글 등록 성공")
                .result(mockResponse)
                .build();

        ObjectMapper mapper = new ObjectMapper();

        PostCreateReviewReq request = PostCreateReviewReq.builder()
                .reviewCategoryIdx(1)
                .reviewTitle("한화 후기")
                .reviewContent("부트캠프")
                .courseName("백엔드")
                .courseEvaluation(5)
                .build();

        String content = mapper.writeValueAsString(request);

        final String fileName = "testImage1";
        final String contentType = "png";

        MockMultipartFile multipartFile = setMockMultipartFile(fileName, contentType);

        given(reviewService.createReview(any(User.class), any(PostCreateReviewReq.class), any()))
                .willReturn(baseRes);

        mvc.perform(multipart("/review/create")
                        .file(new MockMultipartFile("review", "", "application/json", content.getBytes(StandardCharsets.UTF_8)))
                        .file(multipartFile)
                        .contentType(MULTIPART_FORM_DATA)
                        .accept(APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.isSuccess").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("후기글 등록 성공"))
                .andDo(print());
    }

    @DisplayName("후기글 생성 실패")
    @WithCustomMockUser
    @Test
    void reviewController_create_failed() throws Exception {

        // given
        ObjectMapper mapper = new ObjectMapper();

        PostCreateReviewReq request = PostCreateReviewReq.builder()
                .reviewCategoryIdx(1)
                .reviewTitle("한화 후기")
                .reviewContent("부트캠프")
                .courseName("백엔드")
                .courseEvaluation(5)
                .build();

        String content = mapper.writeValueAsString(request);

        final String fileName = "testImage1";
        final String contentType = "png";

        MockMultipartFile multipartFile = setMockMultipartFile(fileName, contentType);

        given(reviewService.createReview(any(User.class), any(PostCreateReviewReq.class), any()))
                .willThrow(new ReviewException(ErrorCode.DUPLICATE_REVIEW_TITLE, "Review Title [ %s ] is duplicated."));

        // when & then
        mvc.perform(multipart("/review/create")
                        .file(new MockMultipartFile("review", "", "application/json", content.getBytes(StandardCharsets.UTF_8)))
                        .file(multipartFile)
                        .contentType(MULTIPART_FORM_DATA)
                        .accept(APPLICATION_JSON)
                        .characterEncoding("UTF-8"))

                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("ACCOUNT-001"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Request processing failed; nested exception is com.example.bootshelf.common.error.entityexception.ReviewException: Review Title [ %s ] is duplicated."))
                .andDo(print());
    }


    @DisplayName("인증회원 작성 후기글 목록 조회 성공")
    @WithCustomMockUser
    @Test
    void reviewController_myList_success() throws Exception {

        GetMyListReviewRes mockResponse = GetMyListReviewRes.builder()
                .idx(1)
                .reviewCategoryIdx(1)
                .title("후기")
                .content("좋아요")
                .courseName("백엔드")
                .courseEvaluation(5)
                .viewCnt(5)
                .upCnt(5)
                .scrapCnt(5)
                .commentCnt(5)
                .type("review")
                .boardType("write")
                .updatedAt("2024/03/16 13:50:31")
                .build();

        BaseRes baseRes = BaseRes.builder()
                .isSuccess(true)
                .message("인증회원 본인 후기글 목록 조회 요청 성공")
                .result(mockResponse)
                .build();
        // given
        ObjectMapper mapper = new ObjectMapper();

        given(reviewService.myList(any(User.class), any(Pageable.class), any(Integer.class), any(Integer.class)))
                .willReturn(baseRes);

        // when & then
        mvc.perform(get("/review/mylist/1/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.isSuccess").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("인증회원 본인 후기글 목록 조회 요청 성공"))
                .andDo(print());
    }

    @DisplayName("후기글 수정 성공")
    @WithCustomMockUser
    @Test
    void reviewController_update_success() throws Exception {

        BaseRes baseRes = BaseRes.builder()
                .isSuccess(true)
                .message("후기글 수정 성공")
                .result("요청 성공")
                .build();
        // given
        ObjectMapper mapper = new ObjectMapper();

        PatchUpdateReviewReq request = PatchUpdateReviewReq.builder()
                .reviewIdx(2)
                .reviewTitle("부트캠프 후기")
                .reviewContent("좋아욥")
                .courseEvaluation(5)
                .build();

        String content = mapper.writeValueAsString(request);

        final String fileName = "testImage1";
        final String contentType = "png";

        MockMultipartFile multipartFile = setMockMultipartFile(fileName, contentType);

        given(reviewService.updateReview(any(User.class), any(PatchUpdateReviewReq.class), any(MockMultipartFile.class)))
                .willReturn(baseRes);

        // when & then
        mvc.perform(multipart(HttpMethod.PATCH, "/review/update")
                        .file(new MockMultipartFile("review", "", "application/json", content.getBytes(StandardCharsets.UTF_8)))
                        .file(multipartFile)
                        .contentType(MULTIPART_FORM_DATA)
                        .accept(APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.isSuccess").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("후기글 수정 성공"))
                .andDo(print());
    }

    @DisplayName("후기글 삭제 성공")
    @WithCustomMockUser
    @Test
    void reviewController_cancel_success() throws Exception {

        BaseRes baseRes = BaseRes.builder()
                .isSuccess(true)
                .message("후기글 삭제 성공")
                .result("요청 성공")
                .build();

        // given
        given(reviewService.deleteReview(any(User.class), any(Integer.class)))
                .willReturn(baseRes);

        // when & then

        mvc.perform(delete("/review/delete/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.isSuccess").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("후기글 삭제 성공"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result").value("요청 성공"))
                .andDo(print());
    }

    @DisplayName("후기글 삭제 실패")
    @WithCustomMockUser
    @Test
    void reviewController_deleteReview_failed() throws Exception {

        // given
        given(reviewService.deleteReview(any(User.class), any(Integer.class)))
                .willThrow(new ReviewException(ErrorCode.REVIEW_NOT_EXISTS, "Review Idx [ %s ] is not exists."));

        // when & then

        mvc.perform(delete("/review/delete/1")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value("ACCOUNT-001"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Request processing failed; nested exception is com.example.bootshelf.common.error.entityexception.ReviewException: Review Idx [ %s ] is not exists."))
                .andDo(print());
    }



    private MockMultipartFile setMockMultipartFile(String fileName, String contentType) {
        return new MockMultipartFile("reviewImage", fileName + "." + contentType, contentType, "<<data>>".getBytes());

    }
}