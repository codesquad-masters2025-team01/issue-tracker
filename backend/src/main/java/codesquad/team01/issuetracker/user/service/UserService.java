package codesquad.team01.issuetracker.user.service;

import java.util.List;

import org.springframework.stereotype.Service;

import codesquad.team01.issuetracker.user.dto.UserDto;
import codesquad.team01.issuetracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {

	private final UserRepository userRepository;

	public UserDto.UserFilterListResponse findUsersForFilter() {

		List<UserDto.UserFilterRow> users = userRepository.findUsersForFilter();

		return UserDto.UserFilterListResponse.builder()
			.totalCount(users.size())
			.users(users.stream().map( // UserFilterRow -> UserFilterResponse
					u -> UserDto.UserFilterResponse.builder()
						.id(u.id())
						.username(u.username())
						.profileImageUrl(u.profileImageUrl())
						.build()
				).toList()
			)
			.build();
	}
}
