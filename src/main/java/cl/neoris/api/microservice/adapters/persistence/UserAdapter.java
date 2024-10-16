package cl.neoris.api.microservice.adapters.persistence;

import cl.neoris.api.microservice.adapters.http.dto.UserDTO;
import cl.neoris.api.microservice.adapters.persistence.entities.UserEntity;
import cl.neoris.api.microservice.application.ports.out.DeleteUserPort;
import cl.neoris.api.microservice.application.ports.out.GetUserPort;
import cl.neoris.api.microservice.application.ports.out.ListUsersPort;
import cl.neoris.api.microservice.application.ports.out.SaveUserPort;
import cl.neoris.api.microservice.application.ports.out.UpdateUserPort;
import cl.neoris.api.microservice.domain.exception.NotDataFoundException;
import cl.neoris.api.microservice.domain.model.UserRespondeDTO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAdapter implements SaveUserPort, ListUsersPort, GetUserPort, UpdateUserPort,
    DeleteUserPort {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  private static final String USER_NOT_FOUND = "Usuario no encontrado";

  @Override
  public UserRespondeDTO createUser(UserDTO userDTO, String token) {
    UserEntity userEntity = userMapper.toEntity(userDTO);
    userEntity.setIsActive(true);
    userEntity.setLastLogin(LocalDateTime.now());
    userEntity.setToken(token);
    userEntity.getPhones().forEach(
        phoneEntity -> phoneEntity.setUser(userEntity)
    );
    return  userMapper.toResponseDTO(userRepository.save(userEntity));
  }

  @Override
  public List<UserRespondeDTO> listUsers() {
    List<UserEntity> users = userRepository.findAll();
    if(users.isEmpty())
      throw new NotDataFoundException("Sin usuarios registrados");
    return users.stream().map( userMapper::toResponseDTO).toList();
  }

  @Override
  public Boolean deleteUser(String uuid) {
    UserEntity userEntity = userRepository.findById(UUID.fromString(uuid)).stream().findFirst()
        .orElseThrow(
            () -> new NotDataFoundException(USER_NOT_FOUND)
        );
    userEntity.setIsActive(false);
    userRepository.save(userEntity);
    return true;
  }

  @Override
  public UserRespondeDTO getUser(String uuid) {
    return userRepository.findById(UUID.fromString(uuid)).map(userMapper::toResponseDTO)
        .orElseThrow(
            () -> new NotDataFoundException(USER_NOT_FOUND)
        );
  }

  @Override
  public UserRespondeDTO updateUser(String uuid, UserDTO userDTO) {
    return userRepository.findById(UUID.fromString(uuid)).stream().findFirst().map(userEntity -> {
      UserEntity entity = userMapper.toEntity(userDTO);
      entity.setId(userEntity.getId());
      return userMapper.toResponseDTO(userRepository.save(entity));
    }).orElseThrow(
        () -> new NotDataFoundException(USER_NOT_FOUND)
    );
  }
}
