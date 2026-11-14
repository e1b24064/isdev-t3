package oit.is.t3.isdev_t3.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

  @Select("SELECT username, password, role FROM users WHERE username = #{username}")
  User findByUsername(String username);

  record User(String username, String password, String role) {
  }
}
