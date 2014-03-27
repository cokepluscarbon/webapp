package com.demo.webapp.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.demo.webapp.domain.Authority;
import com.demo.webapp.domain.Group;
import com.demo.webapp.domain.User;

@Service
public class GroupService {
	private static final String QUERY_GROUPS_SQL = "select * from security_group";
	private static final String QUERY_GROUP_BY_ID_SQL = "select * from security_group where id = ?";
	private static final String QUERY_USERS_BY_GROUPID = "select u.* from security_user as u left join security_group_users as gu on gu.user_id = u.id left join security_group as g on g.id = gu.group_id where g.id = ?";
	private static final String QUERY_AUTHORITIES_BY_GROUPID = "select a.* from security_authority as a left join security_group_authorities as ga on ga.authority_id = a.id left join security_group as g on g.id = ga.group_id where g.id = ?";
	@Autowired
	private JdbcTemplate jdbcTemplate;

	public Object getGroups() {
		return jdbcTemplate.queryForList(QUERY_GROUPS_SQL);
	}

	public Object getGroup(long id) {
		Object[] params = new Object[] { id };

		Map<String, Object> group = jdbcTemplate.queryForMap(QUERY_GROUP_BY_ID_SQL, params);
		List<Map<String, Object>> users = jdbcTemplate.queryForList(QUERY_USERS_BY_GROUPID, params);
		List<Map<String, Object>> authorities = jdbcTemplate.queryForList(QUERY_AUTHORITIES_BY_GROUPID, params);

		group.put("users", users);
		group.put("authorities", authorities);

		return group;
	}

	public void updateGroup(Group group) {
		// TODO 名字冲突异常
		
		jdbcTemplate.update("update security_group set name = ? where id = ? ",
				new Object[] { group.getName(), group.getId() });

		jdbcTemplate.update("delete from security_group_users where group_id = ?", new Object[] { group.getId() });
		jdbcTemplate.update("delete from security_group_authorities where group_id = ?",
				new Object[] { group.getId() });

		for (User user : group.getUsers()) {
			jdbcTemplate.update("insert into security_group_users(group_id, user_id) values(?, ?)",
					new Object[] { group.getId(), user.getId() });
		}
		for (Authority authority : group.getAuthorities()) {
			jdbcTemplate.update("insert into security_group_authorities(group_id, authority_id) values(?, ?)", new Object[] {
					group.getId(), authority.getId() });
		}

	}
}
