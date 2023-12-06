package ru.practicum.user.mapper;

import org.mapstruct.Mapper;
import ru.practicum.user.dto.NewUserRequestDto;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.model.UserEntity;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserEntity toUser(NewUserRequestDto newUserRequestDto);

    UserDto toUserDto(UserEntity user);

    UserShortDto toUserShortDto(UserEntity user);
}
