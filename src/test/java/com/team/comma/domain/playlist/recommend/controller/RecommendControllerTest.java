package com.team.comma.domain.playlist.recommend.controller;

import static com.team.comma.global.constant.ResponseCodeEnum.RECOMMEND_ALREADY_EXIST;
import static com.team.comma.global.constant.ResponseCodeEnum.REQUEST_SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.team.comma.domain.track.track.service.TrackService;
import com.team.comma.domain.user.detail.domain.UserDetail;
import com.team.comma.global.message.MessageResponse;
import com.team.comma.domain.playlist.playlist.domain.Playlist;
import com.team.comma.domain.playlist.recommend.constant.RecommendListType;
import com.team.comma.domain.playlist.recommend.domain.Recommend;
import com.team.comma.domain.playlist.recommend.dto.RecommendRequest;
import com.team.comma.domain.playlist.recommend.dto.RecommendResponse;
import com.team.comma.domain.playlist.recommend.exception.RecommendException;
import com.team.comma.domain.playlist.recommend.service.RecommendService;
import com.team.comma.domain.track.track.domain.Track;
import com.team.comma.domain.user.user.domain.User;
import com.team.comma.global.gson.GsonUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.nio.charset.StandardCharsets;
import java.util.List;

@AutoConfigureRestDocs
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@WebMvcTest(RecommendController.class)
@MockBean(JpaMetamodelMappingContext.class)
@WebAppConfiguration
public class RecommendControllerTest {

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    RecommendService recommendService;

    @MockBean
    TrackService trackService;

    MockMvc mockMvc;
    Gson gson;

    @BeforeEach
    public void init(WebApplicationContext webApplicationContext,
                     RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(documentationConfiguration(restDocumentation))
                .build();

        gson = GsonUtil.getGsonInstance();
    }

    @Test
    void 추천_저장_성공() throws Exception {
        // given
        final String url = "/recommend";

        final User user = User.createUser();
        final Playlist playlist = Playlist.createPlaylist(1L, user);
        final Recommend recommend = Recommend.createRecommend(1L, "comment", playlist, user);
        final RecommendRequest recommendRequest = RecommendRequest.of(recommend);
        final MessageResponse messageResponse = MessageResponse.of(REQUEST_SUCCESS);
        doReturn(messageResponse).when(recommendService).createPlaylistRecommend("accessToken", recommendRequest);

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .cookie(new Cookie("accessToken", "accessToken"))
                        .content(gson.toJson(recommendRequest))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isOk()).andDo(
                document("spotify/saveRecommend",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestCookies(
                                cookieWithName("accessToken").description("사용자 access token")
                        ),
                        requestFields(
                                fieldWithPath("playlistId").description("추천 플레이리스트 id"),
                                fieldWithPath("recommendType").description("추천 대상(익명 = ANONYMOUS, 팔로잉 = FOLLOWING)"),
                                fieldWithPath("toUserEmail").description("추천 대상 이메일(익명인 경우 입력 X)"),
                                fieldWithPath("comment").description("추천 코멘트")
                        ),
                        responseFields(
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("message").description("메세지"),
                                fieldWithPath("data").description("데이터")
                        )
                )
        );
        final MessageResponse result = gson.fromJson(
                resultActions.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                MessageResponse.class);

        assertThat(result.getCode()).isEqualTo(REQUEST_SUCCESS.getCode());
        assertThat(result.getMessage()).isEqualTo(REQUEST_SUCCESS.getMessage());

    }

    @Test
    void 추천_저장_실패_사용자에게_이미_추천한_플레이리스트() throws Exception {
        // given
        final String url = "/recommend";

        final User user = User.createUser();
        final Playlist playlist = Playlist.createPlaylist(1L, user);
        final Recommend recommend = Recommend.createRecommend(1L, "comment", playlist, user);
        final RecommendRequest recommendRequest = RecommendRequest.of(recommend);
        doThrow(new RecommendException("사용자에게 이미 추천한 플레이리스트 입니다."))
                .when(recommendService).createPlaylistRecommend("accessToken", recommendRequest);

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .cookie(new Cookie("accessToken", "accessToken"))
                        .content(gson.toJson(recommendRequest))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isBadRequest()).andDo(
                document("spotify/saveRecommendFailure",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestCookies(
                                cookieWithName("accessToken").description("사용자 access token")
                        ),
                        requestFields(
                                fieldWithPath("playlistId").description("추천 플레이리스트 id"),
                                fieldWithPath("recommendType").description("추천 대상(익명 = ANONYMOUS, 팔로잉 = FOLLOWING)"),
                                fieldWithPath("toUserEmail").description("추천 대상 이메일(익명인 경우 입력 X)"),
                                fieldWithPath("comment").description("추천 코멘트")
                        ),
                        responseFields(
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("message").description("메세지"),
                                fieldWithPath("data").description("데이터")
                        )
                )
        );
        final MessageResponse result = gson.fromJson(
                resultActions.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                MessageResponse.class);

        assertThat(result.getCode()).isEqualTo(RECOMMEND_ALREADY_EXIST.getCode());
        assertThat(result.getMessage()).isEqualTo(RECOMMEND_ALREADY_EXIST.getMessage());
    }

    @Test
    void 추천_리스트_조회_성공() throws Exception {
        // given
        final String url = "/recommend/type/{type}";
        final User toUser = User.createUser("toUserEmail");
        final User fromUser = User.createUser("fromUserEmail");
        UserDetail.buildUserDetail(toUser);
        final Track track = Track.buildTrack();
        Playlist playlist = Playlist.createPlaylist(1L, fromUser);
        playlist.addPlaylistTrack(track);
        final Recommend recommend = Recommend.createRecommend(1L, "comment", playlist, toUser);

        final List<RecommendResponse> recommendList = List.of(
                RecommendResponse.of(recommend, toUser),
                RecommendResponse.of(recommend, toUser));
        final MessageResponse message = MessageResponse.of(REQUEST_SUCCESS, recommendList);
        doReturn(message).when(recommendService).findAllPlaylistRecommends("accessToken", RecommendListType.ANONYMOUS);

        final ResultActions resultActions = mockMvc.perform(
                RestDocumentationRequestBuilders.get(url, "ANONYMOUS")
                        .cookie(new Cookie("accessToken", "accessToken"))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isOk()).andDo(
                document("spotify/selectRecommendList",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestCookies(
                                cookieWithName("accessToken").description("사용자 access token")
                        ),
                        pathParameters(
                                parameterWithName("type").description("받은 추천 리스트: RECIEVED, 보낸 추천 리스트: SENDED, 익명 추천 리스트: ANONYMOUS")
                        ),
                        responseFields(
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("message").description("메세지"),
                                fieldWithPath("data").description("데이터"),
                                fieldWithPath("data.[].recommendId").description("recommend ID"),
                                fieldWithPath("data.[].comment").description("추천 코멘트"),
                                fieldWithPath("data.[].userNickname").description("사용자 닉네임"),
                                fieldWithPath("data.[].userProfileImage").description("사용자 프로필 이미지 url"),
                                fieldWithPath("data.[].playlistId").description("플레이리스트 ID"),
                                fieldWithPath("data.[].playlistTitle").description("플레이리스트 제목"),
                                fieldWithPath("data.[].repAlbumImageUrl").description("대표 이미지 URL"),
                                fieldWithPath("data.[].trackCount").description("트랙 갯수"),
                                fieldWithPath("data.[].playCount").description("추천된 플레이리스트 재생 횟수")
                        )
                )
        );

        final MessageResponse result = gson.fromJson(
                resultActions.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                MessageResponse.class);

        assertThat((List<RecommendResponse>) result.getData()).size().isEqualTo(2);
    }

    @Test
    void 추천_정보_조회_성공() throws Exception {
        // given
        final String url = "/recommend/{id}";

        final User toUser = User.createUser("toUserEmail");
        final User fromUser = User.createUser("fromUserEmail");
        UserDetail.buildUserDetail(toUser);
        final Track track = Track.buildTrack();
        Playlist playlist = Playlist.createPlaylist(1L, fromUser);
        playlist.addPlaylistTrack(track);
        final Recommend recommend = Recommend.createRecommend(1L, "comment", playlist, toUser);

        final RecommendResponse recommendResponse = RecommendResponse.of(recommend, toUser);
        final MessageResponse message = MessageResponse.of(REQUEST_SUCCESS, recommendResponse);
        doReturn(message).when(recommendService).findRecommend("accessToken", recommend.getId());

        // when
        final ResultActions resultActions = mockMvc.perform(
                RestDocumentationRequestBuilders
                        .get(url, recommend.getId())
                        .cookie(new Cookie("accessToken", "accessToken"))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isOk()).andDo(
                document("spotify/selectRecommend",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("id").description("추천 정보 id")
                        ),
                        responseFields(
                                fieldWithPath("code").description("응답 코드"),
                                fieldWithPath("message").description("메세지"),
                                fieldWithPath("data").description("데이터"),
                                fieldWithPath("data.recommendId").description("recommend ID"),
                                fieldWithPath("data.userNickname").description("사용자 닉네임"),
                                fieldWithPath("data.userProfileImage").description("사용자 프로필 이미지 url"),
                                fieldWithPath("data.comment").description("추천 코멘트"),
                                fieldWithPath("data.playlistId").description("플레이리스트 ID"),
                                fieldWithPath("data.playlistTitle").description("플레이리스트 제목"),
                                fieldWithPath("data.repAlbumImageUrl").description("대표 이미지 URL"),
                                fieldWithPath("data.trackCount").description("트랙 갯수"),
                                fieldWithPath("data.playCount").description("추천된 플레이리스트 재생 횟수")
                        )
                )
        );

        final MessageResponse result = gson.fromJson(
                resultActions.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                new TypeToken<MessageResponse<RecommendResponse>>() {}.getType()
        );

        RecommendResponse recommendResult = (RecommendResponse) result.getData();
        assertThat(recommendResult.getRecommendId()).isEqualTo(recommendResponse.getRecommendId());

    }

}
