package com.team.comma.domain.user.detail.controller;

import com.google.gson.Gson;
import com.team.comma.domain.user.detail.dto.UserDetailRequest;
import com.team.comma.domain.user.detail.service.UserDetailService;
import com.team.comma.global.message.MessageResponse;
import com.team.comma.global.gson.GsonUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.security.auth.login.AccountException;
import java.nio.charset.StandardCharsets;

import static com.team.comma.global.constant.ResponseCodeEnum.REQUEST_SUCCESS;
import static com.team.comma.global.constant.ResponseCodeEnum.SIMPLE_REQUEST_FAILURE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureRestDocs
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@WebMvcTest(UserDetailController.class)
@MockBean(JpaMetamodelMappingContext.class)
@WebAppConfiguration
public class UserDetailControllerTest {

    @MockBean
    private UserDetailService userDetailService;

    MockMvc mockMvc;
    Gson gson;

    @BeforeEach
    public void init(WebApplicationContext webApplicationContext,
                     RestDocumentationContextProvider restDocumentation) {
        gson = GsonUtil.getGsonInstance();

        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .build();
    }

    @Test
    void createProfile_Fail_TokenNotFound() throws Exception {
        // given
        String api = "/user/detail";
        UserDetailRequest userDetail = UserDetailRequest.buildUserDetailCreateRequest();
        doThrow(new AccountException("로그인이 되어있지 않습니다.")).when(userDetailService)
                .createUserDetail(eq(null), any(UserDetailRequest.class));

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders
                        .post(api)
                        .content(gson.toJson(userDetail))
                        .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isBadRequest()).andDo(
                document("user-detail/create-fail-token-not-found",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("nickname").description("닉네임")
                        ),
                        responseFields(
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("message").description("메세지"),
                                fieldWithPath("data").description("데이터")
                        )
                )
        );
        final MessageResponse response = gson.fromJson(
                resultActions.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                MessageResponse.class);

        assertThat(response.getCode()).isEqualTo(SIMPLE_REQUEST_FAILURE.getCode());
        assertThat(response.getMessage()).isEqualTo("로그인이 되어있지 않습니다.");
    }

    @Test
    void createProfile_Fail_UserNotFound() throws Exception {
        // given
        String api = "/user/detail";
        UserDetailRequest userDetail = UserDetailRequest.buildUserDetailCreateRequest();
        doThrow(new AccountException("사용자를 찾을 수 없습니다."))
                .when(userDetailService).createUserDetail(anyString(), eq(userDetail));

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders
                        .post(api)
                        .cookie(new Cookie("accessToken", "token"))
                        .content(gson.toJson(userDetail))
                        .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isBadRequest()).andDo(
                document("user-detail/create-fail-user-not-found",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("nickname").description("닉네임")
                        ),
                        responseFields(
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("message").description("메세지"),
                                fieldWithPath("data").description("데이터")
                        )
                )
        );
        final MessageResponse response = gson.fromJson(
                resultActions.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                MessageResponse.class);

        assertThat(response.getCode()).isEqualTo(SIMPLE_REQUEST_FAILURE.getCode());
        assertThat(response.getMessage()).isEqualTo("사용자를 찾을 수 없습니다.");
    }

    @Test
    void createProfile_Success() throws Exception {
        // given
        String api = "/user/detail";

        UserDetailRequest userDetail = UserDetailRequest.buildUserDetailCreateRequest();
        doReturn(MessageResponse.of(REQUEST_SUCCESS))
                .when(userDetailService).createUserDetail(anyString(), any(UserDetailRequest.class));

        // when
        final ResultActions resultActions = mockMvc.perform(
                post(api)
                        .cookie(new Cookie("accessToken", "token"))
                        .content(gson.toJson(userDetail))
                        .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isCreated()).andDo(
                document("user-detail/create-success",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestCookies(
                                cookieWithName("accessToken").description("사용자 액세스 토큰")
                        ),
                        requestFields(
                                fieldWithPath("nickname").description("닉네임")
                        ),
                        responseFields(
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("message").description("메세지"),
                                fieldWithPath("data").description("데이터")
                        )
                )
        );
        final MessageResponse response = gson.fromJson(
                resultActions.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                MessageResponse.class);

        assertThat(response.getCode()).isEqualTo(REQUEST_SUCCESS.getCode());
        assertThat(response.getMessage()).isEqualTo(REQUEST_SUCCESS.getMessage());
    }

    @Test
    void modifyUserDetail_success() throws Exception {
        // given
        String api = "/user/detail";

        UserDetailRequest userDetail = UserDetailRequest.buildUserDetailModifyRequest();
        doReturn(MessageResponse.of(REQUEST_SUCCESS))
                .when(userDetailService).modifyUserDetail(anyString(), any(UserDetailRequest.class));

        // when
        final ResultActions resultActions = mockMvc.perform(
                patch(api)
                        .cookie(new Cookie("accessToken", "token"))
                        .content(gson.toJson(userDetail))
                        .contentType(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isOk()).andDo(
                document("user-detail/modify-success",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestCookies(
                                cookieWithName("accessToken").description("사용자 액세스 토큰")
                        ),
                        requestFields(
                                fieldWithPath("nickname").description("닉네임"),
                                fieldWithPath("profileImageUrl").description("프로필 이미지 url"),
                                fieldWithPath("popupAlertFlag").description("알람 설정 여부"),
                                fieldWithPath("allPublicFlag").description("전체 공개 여부"),
                                fieldWithPath("favoritePublicFlag").description("Favorite 공개 여부"),
                                fieldWithPath("calenderPublicFlag").description("Calender 공개 여부")
                        ),
                        responseFields(
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("message").description("메세지"),
                                fieldWithPath("data").description("데이터")
                        )
                )
        );
        final MessageResponse response = gson.fromJson(
                resultActions.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                MessageResponse.class);

        assertThat(response.getCode()).isEqualTo(REQUEST_SUCCESS.getCode());
        assertThat(response.getMessage()).isEqualTo(REQUEST_SUCCESS.getMessage());
    }


}
