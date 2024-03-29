package com.team.comma.domain.favorite.artist.controller;

import com.google.gson.Gson;
import com.team.comma.domain.artist.domain.Artist;
import com.team.comma.domain.favorite.artist.domain.FavoriteArtist;
import com.team.comma.domain.favorite.artist.dto.FavoriteArtistResponse;
import com.team.comma.domain.user.user.constant.UserRole;
import com.team.comma.domain.user.user.constant.UserType;
import com.team.comma.domain.user.user.domain.User;
import com.team.comma.domain.user.user.exception.UserException;
import com.team.comma.global.message.MessageResponse;
import com.team.comma.domain.favorite.artist.dto.FavoriteArtistRequest;
import com.team.comma.domain.favorite.artist.service.FavoriteArtistService;
import com.team.comma.global.gson.GsonUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.security.auth.login.AccountException;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.team.comma.global.constant.ResponseCodeEnum.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureRestDocs
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@WebMvcTest(FavoriteArtistController.class)
@MockBean(JpaMetamodelMappingContext.class)
@WebAppConfiguration
public class FavoriteArtistControllerTest {

    @MockBean
    FavoriteArtistService favoriteArtistService;

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
    @DisplayName("사용자 아티스트 추가 실패 _ 찾을 수 없는 사용자")
    public void addFavoriteArtistFail_notFoundUser() throws Exception {
        // given
        final String api = "/favorite/artist";
        FavoriteArtistRequest request = FavoriteArtistRequest.builder().spotifyArtistId("spotifyArtistId").build();
        doThrow(new UserException(NOT_FOUNT_USER)).when(favoriteArtistService).createFavoriteArtist("token" , "spotifyArtistId");

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders
                        .post(api)
                        .content(gson.toJson(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("accessToken" , "token")));

        // then
        resultActions.andExpect(status().isBadRequest()).andDo(
                document("favoriteArtist/addFail-notFoundUser",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestCookies(
                                cookieWithName("accessToken").description("사용자 인증에 필요한 accessToken")
                        ),
                        requestFields(
                                fieldWithPath("spotifyArtistId").description("spotify 아티스트 Id")
                        ),
                        responseFields(
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("message").description("응답 메세지"),
                                fieldWithPath("data").description("응답 데이터")
                        )
                )
        );
        final MessageResponse result = gson.fromJson(
                resultActions.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                MessageResponse.class);

        assertThat(result.getCode()).isEqualTo(SIMPLE_REQUEST_FAILURE.getCode());
        assertThat(result.getData()).isNull();
    }

    @Test
    @DisplayName("사용자 아티스트 추가 성공")
    public void addFavoriteArtistSuccess() throws Exception {
        // given
        final String api = "/favorite/artist";
        FavoriteArtistRequest request = FavoriteArtistRequest.builder().spotifyArtistId("spotifyArtistId").build();
        doReturn(MessageResponse.of(REQUEST_SUCCESS)).when(favoriteArtistService).createFavoriteArtist("token" , "spotifyArtistId");

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders
                        .post(api)
                        .content(gson.toJson(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("accessToken" , "token")));

        // then
        resultActions.andExpect(status().isCreated()).andDo(
                document("favoriteArtist/addSuccess",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestCookies(
                                cookieWithName("accessToken").description("사용자 인증에 필요한 accessToken")
                        ),
                        requestFields(
                                fieldWithPath("spotifyArtistId").description("spotify 아티스트 Id")
                        ),
                        responseFields(
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("message").description("응답 메세지"),
                                fieldWithPath("data").description("응답 데이터")
                        )
                )
        );
        final MessageResponse result = gson.fromJson(
                resultActions.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                MessageResponse.class);

        assertThat(result.getCode()).isEqualTo(REQUEST_SUCCESS.getCode());
        assertThat(result.getData()).isNull();
    }

    @Test
    @DisplayName("사용자 아티스트 삭제 실패 _ 찾을 수 없는 사용자")
    public void deleteFavoriteArtistFail_notFoundUser() throws Exception {
        // given
        final String api = "/favorite/artist/{id}";

        doThrow(new AccountException("사용자를 찾을 수 없습니다.")).when(favoriteArtistService).deleteFavoriteArtist("token" , 1L);

        // when
        final ResultActions resultActions = mockMvc.perform(
                        delete(api, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("accessToken" , "token")));

        // then
        resultActions.andExpect(status().isBadRequest()).andDo(
                document("favoriteArtist/deleteFail-notFoundUser",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestCookies(
                                cookieWithName("accessToken").description("사용자 인증에 필요한 accessToken")
                        ),
                        pathParameters(
                                parameterWithName("id").description("favorite artist Id")
                        ),
                        responseFields(
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("message").description("응답 메세지"),
                                fieldWithPath("data").description("응답 데이터")
                        )
                )
        );
        final MessageResponse result = gson.fromJson(
                resultActions.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                MessageResponse.class);

        assertThat(result.getCode()).isEqualTo(SIMPLE_REQUEST_FAILURE.getCode());
        assertThat(result.getData()).isNull();
    }

    @Test
    @DisplayName("사용자 아티스트 삭제 성공")
    public void deleteFavoriteArtistSuccess() throws Exception {
        // given
        final String api = "/favorite/artist/{id}";

        doReturn(MessageResponse.of(REQUEST_SUCCESS)).when(favoriteArtistService).deleteFavoriteArtist("token" , 1L);

        // when
        final ResultActions resultActions = mockMvc.perform(
                        delete(api, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("accessToken" , "token")));

        // then
        resultActions.andExpect(status().isOk()).andDo(
                document("favoriteArtist/deleteSuccess",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestCookies(
                                cookieWithName("accessToken").description("사용자 인증에 필요한 accessToken")
                        ),
                        pathParameters(
                                parameterWithName("id").description("favorite artist Id")
                        ),
                        responseFields(
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("message").description("응답 메세지"),
                                fieldWithPath("data").description("응답 데이터")
                        )
                )
        );
        final MessageResponse result = gson.fromJson(
                resultActions.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                MessageResponse.class);

        assertThat(result.getCode()).isEqualTo(REQUEST_SUCCESS.getCode());
        assertThat(result.getData()).isNull();
    }

    @Test
    @DisplayName("아티스트 추가 여부 확인 _ 참")
    public void isAddedFavoriteArtist_true() throws Exception {
        // given
        final String api = "/favorite/artist/{artistName}";
        doReturn(MessageResponse.of(REQUEST_SUCCESS , true)).when(favoriteArtistService).isFavoriteArtist("token" , "artistName");

        // when
        final ResultActions resultActions = mockMvc.perform(
                RestDocumentationRequestBuilders
                        .get(api, "artistName")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("accessToken" , "token")));

        // then
        resultActions.andExpect(status().isOk()).andDo(
                document("favoriteArtist/isAddedArtist-true",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestCookies(
                                cookieWithName("accessToken").description("사용자 인증에 필요한 accessToken")
                        ),
                        pathParameters(
                                parameterWithName("artistName").description("아티스트 명")
                        ),
                        responseFields(
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("message").description("응답 메세지"),
                                fieldWithPath("data").description("응답 데이터")
                        )
                )
        );
        final MessageResponse result = gson.fromJson(
                resultActions.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                MessageResponse.class);

        assertThat(result.getCode()).isEqualTo(REQUEST_SUCCESS.getCode());
        assertThat(result.getData()).isEqualTo(true);
    }

    @Test
    @DisplayName("아티스트 추가 여부 확인 _ 거짓")
    public void isAddedFavoriteArtist_false() throws Exception {
        // given
        final String api = "/favorite/artist/{artistName}";
        doReturn(MessageResponse.of(REQUEST_SUCCESS , false)).when(favoriteArtistService).isFavoriteArtist("token" , "artistName");

        // when
        final ResultActions resultActions = mockMvc.perform(
                RestDocumentationRequestBuilders
                        .get(api, "artistName")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("accessToken" , "token")));

        // then
        resultActions.andExpect(status().isOk()).andDo(
                document("favoriteArtist/isAddedArtist-false",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestCookies(
                                cookieWithName("accessToken").description("사용자 인증에 필요한 accessToken")
                        ),
                        pathParameters(
                                parameterWithName("artistName").description("아티스트 명")
                        ),
                        responseFields(
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("message").description("응답 메세지"),
                                fieldWithPath("data").description("응답 데이터")
                        )
                )
        );
        final MessageResponse result = gson.fromJson(
                resultActions.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                MessageResponse.class);

        assertThat(result.getCode()).isEqualTo(REQUEST_SUCCESS.getCode());
        assertThat(result.getData()).isEqualTo(false);
    }

    @Test
    @DisplayName("아티스트 좋아요 리스트 조회")
    public void findAllFavoriteArtist() throws Exception {
        // given
        final String api = "/favorite/artist";
        User user = buildUser();
        Artist artist = Artist.buildArtist();
        FavoriteArtist favoriteArtist = FavoriteArtist.buildFavoriteArtist(user, artist);
        FavoriteArtistResponse favoriteArtistResponse = FavoriteArtistResponse.of(favoriteArtist);

        doReturn(MessageResponse.of(REQUEST_SUCCESS , List.of(favoriteArtistResponse))).when(favoriteArtistService).findAllFavoriteArtist("accessToken");

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders
                        .get(api)
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie("accessToken" , "accessToken")));

        // then
        resultActions.andExpect(status().isOk()).andDo(
                document("favorite/find-all-favorite-artist",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestCookies(
                                cookieWithName("accessToken").description("사용자 인증에 필요한 accessToken")
                        ),
                        responseFields(
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("message").description("응답 메세지"),
                                fieldWithPath("data").description("응답 데이터"),
                                fieldWithPath("data.[].favoriteArtistId").description("아티스트 좋아요 Id"),
                                fieldWithPath("data.[].artistResponse.spotifyArtistId").description("스포티파이 아티스트 Id"),
                                fieldWithPath("data.[].artistResponse.artistName").description("아티스트 이름"),
                                fieldWithPath("data.[].artistResponse.artistImageUrl").description("아티스트 이미지 URL")
                        )
                )
        );
        final MessageResponse result = gson.fromJson(
                resultActions.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                MessageResponse.class);

        assertThat(result.getCode()).isEqualTo(REQUEST_SUCCESS.getCode());
        assertThat(result.getMessage()).isEqualTo(REQUEST_SUCCESS.getMessage());
    }

    private User buildUser() {
        return User.builder()
                .email("userEmail")
                .password("userPassword")
                .type(UserType.GENERAL_USER)
                .role(UserRole.USER)
                .build();
    }

    public static FavoriteArtist buildFavoriteArtist(User user, Artist artist) {
        return FavoriteArtist.builder()
                .artist(artist)
                .user(user)
                .build();
    }

}
