package com.team.comma.domain.user.following.service;

import com.team.comma.domain.user.following.constant.FollowingType;
import com.team.comma.domain.user.following.domain.Following;
import com.team.comma.domain.user.following.dto.FollowingCountResponse;
import com.team.comma.domain.user.following.dto.FollowingResponse;
import com.team.comma.domain.user.following.exception.FollowingException;
import com.team.comma.domain.user.following.repository.FollowingRepository;
import com.team.comma.domain.user.user.domain.User;
import com.team.comma.domain.user.user.exception.UserException;
import com.team.comma.domain.user.user.repository.UserRepository;
import com.team.comma.global.message.MessageResponse;
import com.team.comma.global.jwt.support.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountException;
import java.util.ArrayList;
import java.util.List;

import static com.team.comma.global.constant.ResponseCodeEnum.NOT_FOUNT_USER;
import static com.team.comma.global.constant.ResponseCodeEnum.REQUEST_SUCCESS;


@Service
@RequiredArgsConstructor
public class FollowingService {

    private final FollowingRepository followingRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public MessageResponse blockFollowedUser(long followingId) {
        followingRepository.blockFollowedUser(followingId);

        return MessageResponse.of(REQUEST_SUCCESS);
    }

    public MessageResponse unblockFollowedUser(long followingId) {
        followingRepository.unblockFollowedUser(followingId);

        return MessageResponse.of(REQUEST_SUCCESS);
    }

    @Transactional
    public MessageResponse addFollow(String token , long toUserId) throws AccountException {
        String fromUserEmail = jwtTokenProvider.getUserPk(token);
        if(isFollowed(toUserId , fromUserEmail)) {
            throw new FollowingException("이미 팔로우중인 사용자입니다.");
        }
        if(isBlockedUser(toUserId , fromUserEmail)) {
            throw new FollowingException("차단된 사용자입니다.");
        }

        saveFollowing(toUserId , fromUserEmail);

        return MessageResponse.of(REQUEST_SUCCESS);
    }

    public void saveFollowing(long toUserId , String fromUserEmail) throws AccountException {
        User userTo = userRepository.findById(toUserId)
                .orElseThrow(() -> new AccountException("해당 사용자를 찾을 수 없습니다."));

        User userFrom = userRepository.findUserByEmail(fromUserEmail)
                .orElseThrow(() -> new AccountException("대상 사용자를 찾을 수 없습니다."));

        Following following = Following.createFollowingToFrom(userTo , userFrom);
        followingRepository.save(following);
    }

    public MessageResponse isFollowedUser(String token , long toUserId) {
        String fromUserEmail = jwtTokenProvider.getUserPk(token);
        if(isFollowed(toUserId , fromUserEmail)) {
            return MessageResponse.of(REQUEST_SUCCESS , true);
        }

        return MessageResponse.of(REQUEST_SUCCESS , false);
    }

    public boolean isBlockedUser(long toUserId , String fromUserEmail) {
        if(followingRepository.getBlockedUser(toUserId , fromUserEmail).isPresent()) {
            return true;
        }

        return false;
    }

    public boolean isFollowed(long toUserId , String fromUserEmail) {
        if(followingRepository.getFollowedMeUserByEmail(toUserId , fromUserEmail).isPresent()) {
            return true;
        }

        return false;
    }

    public MessageResponse getFollowingUserList(String token, FollowingType followingType) {
        String userEmail = jwtTokenProvider.getUserPk(token);
        User user = userRepository.findUserByEmail(userEmail)
                .orElseThrow(() -> new UserException(NOT_FOUNT_USER));

        List<FollowingResponse> returnResponses;
        if (followingType.equals(FollowingType.FOLLOWING)) {
            returnResponses = getFollowingToUserListByFromUser(user);
        } else if (followingType.equals(FollowingType.FOLLOWED)) {
            returnResponses = getFollowingFromUserListByToUser(user);
        } else {
            List<FollowingResponse> followingResponses = getFollowingToUserListByFromUser(user);
            List<FollowingResponse> followedResponses = getFollowingFromUserListByToUser(user);

            returnResponses = getFollowingAndFollowedUserList(followingResponses, followedResponses);
        }

        return MessageResponse.of(REQUEST_SUCCESS, returnResponses);
    }

    public List<FollowingResponse> getFollowingToUserListByFromUser(User user) {
        return followingRepository.getFollowingToUserListByFromUser(user);
    }

    public List<FollowingResponse> getFollowingFromUserListByToUser(User user) {
        return followingRepository.getFollowingFromUserListByToUser(user);
    }

    public List<FollowingResponse> getFollowingAndFollowedUserList(
            List<FollowingResponse> followingResponses,
            List<FollowingResponse> followedResponses) {
        List<FollowingResponse> returnResponses = new ArrayList<>();
        for(FollowingResponse following : followingResponses){
            for(FollowingResponse followed : followedResponses){
                if(following.getUserId() == followed.getUserId()){
                    returnResponses.add(following);
                }
            }
        }
        return returnResponses;
    }

    public MessageResponse countFollowings(final String accessToken) throws AccountException {
        String userEmail = jwtTokenProvider.getUserPk(accessToken);
        User user = userRepository.findUserByEmail(userEmail)
                .orElseThrow(() -> new AccountException("사용자 정보를 찾을 수 없습니다."));

        FollowingCountResponse response = FollowingCountResponse.of(
                getFollowingToUserListByFromUser(user).size(),
                getFollowingFromUserListByToUser(user).size());

        return MessageResponse.of(REQUEST_SUCCESS, response);
    }
}
