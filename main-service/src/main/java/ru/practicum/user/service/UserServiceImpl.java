package ru.practicum.user.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.user.model.UserEntity;
import ru.practicum.util.CustomPageRequest;
import ru.practicum.exception.UniquenessViolationException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.dto.NewUserRequestDto;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.QUserEntity;
import ru.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        PageRequest pageRequest = new CustomPageRequest(from, size, Sort.by(Sort.Direction.ASC, "id"));
        List<UserEntity> users;
        if (ids != null && !ids.isEmpty()) {
            BooleanExpression selectByIds = QUserEntity.userEntity.id.in(ids);
            users = userRepository.findAll(selectByIds, pageRequest).getContent();
        } else {
            users = userRepository.findAll(pageRequest).getContent();
        }
        return users
                .stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public UserDto createUser(NewUserRequestDto userRequestDto) {
        try {
            return userMapper.toUserDto(userRepository.save(userMapper.toUser(userRequestDto)));
        } catch (RuntimeException e) {
            throw new UniquenessViolationException(
                    String.format("DataIntegrityViolationException, user with email %s already exists",
                            userRequestDto.getEmail()));
        }
    }

    @Transactional
    @Override
    public void deleteUser(long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("User with id %s was not found", userId)));
        userRepository.deleteById(userId);
    }
}
