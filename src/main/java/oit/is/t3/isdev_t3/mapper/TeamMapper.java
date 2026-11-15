package oit.is.t3.isdev_t3.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TeamMapper {

  @Delete("DELETE FROM team_preferences WHERE room_id = #{roomId} AND username = #{username}")
  void deletePreference(@Param("roomId") long roomId, @Param("username") String username);

  @Insert("INSERT INTO team_preferences (room_id, username, preferred_username) "
      + "VALUES (#{roomId}, #{username}, #{preferredUsername})")
  void insertPreference(@Param("roomId") long roomId, @Param("username") String username,
      @Param("preferredUsername") String preferredUsername);

  @Select("SELECT username, preferred_username AS preferredUsername "
      + "FROM team_preferences WHERE room_id = #{roomId}")
  List<PreferenceRow> selectPreferencesByRoom(long roomId);

  @Delete("DELETE FROM team_members WHERE room_id = #{roomId}")
  void deleteMembersByRoom(long roomId);

  @Delete("DELETE FROM teams WHERE room_id = #{roomId}")
  void deleteTeamsByRoom(long roomId);

  @Insert("INSERT INTO teams (room_id, team_number) VALUES (#{roomId}, #{teamNumber})")
  void insertTeam(@Param("roomId") long roomId, @Param("teamNumber") int teamNumber);

  @Insert("INSERT INTO team_members (room_id, team_number, username) "
      + "VALUES (#{roomId}, #{teamNumber}, #{username})")
  void insertMember(@Param("roomId") long roomId, @Param("teamNumber") int teamNumber,
      @Param("username") String username);

  @Select("SELECT team_number AS teamNumber, username "
      + "FROM team_members WHERE room_id = #{roomId} ORDER BY team_number")
  List<TeamMemberRow> selectMembersByRoom(long roomId);

  record PreferenceRow(String username, String preferredUsername) {
  }

  record TeamMemberRow(int teamNumber, String username) {
  }
}
