package com.demo.webapp.web.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.demo.webapp.domain.Authority;
import com.demo.webapp.domain.Group;
import com.demo.webapp.domain.User;
import com.demo.webapp.domain.validator.UserAdd;
import com.demo.webapp.domain.validator.UserChangePassword;
import com.demo.webapp.domain.validator.UserPersonalUpdate;
import com.demo.webapp.domain.validator.UserUpdate;
import com.demo.webapp.service.AuthorityService;
import com.demo.webapp.service.GroupService;
import com.demo.webapp.service.UserService;
import com.demo.webapp.service.exception.PasswordIncorrectException;
import com.demo.webapp.service.exception.UsernameAlreadyExistsException;

@Controller
@RequestMapping(value = { "/security" })
public class UserController {
	private static final Logger logger = LoggerFactory.getLogger(UserController.class);
	@Autowired
	private UserService userService;
	@Autowired
	private GroupService groupService;
	@Autowired
	private AuthorityService authorityService;

	@RequestMapping(value = "/user", method = { RequestMethod.GET })
	public String getUserPage(@ModelAttribute("user") User user, Model model) {
		model.addAttribute("groups", groupService.getGroups());
		model.addAttribute("authorities", authorityService.getAuthorities());

		return "security/user";
	}

	@RequestMapping(value = "/user", method = { RequestMethod.POST })
	public String addUser(@Validated(value = UserAdd.class) @ModelAttribute("user") @RequestBody User user, BindingResult bindingResult, Model model,
			HttpServletRequest request, RedirectAttributes redirectAttributes) {
		logger.debug("add User with : {}", user);
		renderUser(user, request);
		if (bindingResult.hasErrors()) {
			model.addAttribute("groups", groupService.getGroups());
			model.addAttribute("authorities", authorityService.getAuthorities());
			return null;
		}

		try {
			userService.addUser(user);
		} catch (UsernameAlreadyExistsException e) {
			model.addAttribute("groups", groupService.getGroups());
			model.addAttribute("authorities", authorityService.getAuthorities());
			model.addAttribute("error", "您添加的用户名已经存在！");
			return null;
		}

		redirectAttributes.addFlashAttribute("success", "添加用户成功！");
		return "redirect:/security/user/" + user.getId();
	}

	@RequestMapping(value = "/users", method = { RequestMethod.GET })
	public void getUsers(Model model) {
		model.addAttribute("users", userService.getUsers());
	}

	@RequestMapping(value = "/user/{id}", method = { RequestMethod.GET })
	public Object getUser(@PathVariable("id") long id, @ModelAttribute("user") User user, Model model) {
		model.addAttribute("user", userService.getUser(id));
		model.addAttribute("groups", groupService.getGroups());
		model.addAttribute("authorities", authorityService.getAuthorities());

		return "/security/user";
	}

	@RequestMapping(value = "/user/{id}", method = { RequestMethod.PUT })
	public Object updateUser(@PathVariable("id") long id, @Validated(value = UserUpdate.class) @ModelAttribute("user") User user,
			BindingResult bindingResult, HttpServletRequest request, Model model, RedirectAttributes redirect) {
		// TODO 如何绑定而不需要通过自定义的方法？
		renderUser(user, request);
		if (bindingResult.hasErrors()) {
			// TODO 重复代码？
			model.addAttribute("groups", groupService.getGroups());
			model.addAttribute("authorities", authorityService.getAuthorities());
			return "/security/user";
		}

		logger.debug("update User with : {}", user);
		userService.updateUser(user);
		redirect.addFlashAttribute("success", "更新用户信息成功！");

		return "redirect:/security/user/{id}";
	}

	@RequestMapping(value = "/user/{id}", method = RequestMethod.DELETE)
	public String deleteUser(@PathVariable("id") long id, Model model) {
		userService.deleteUser(id);
		model.addAttribute("success", "删除用户成功！");
		return "security/user";
	}

	/**
	 * 个人信息页面
	 */
	@RequestMapping(value = "/personal", method = RequestMethod.GET)
	public String getPersonalDetails(@ModelAttribute("user") User user, Model model) {
		user = userService.getUser();

		model.addAttribute("user", user);
		return "security/personal";
	}

	/**
	 * 修改个人信息页面
	 */
	@RequestMapping(value = "/personal", method = RequestMethod.PUT)
	public String updatePersonalDetails(@Validated(value = UserPersonalUpdate.class) @ModelAttribute("user") User user, BindingResult bindingResult,
			Model model, RedirectAttributes redirectAttributes) {

		logger.debug("修改个人信息 : {}", user);

		if (bindingResult.hasErrors()) {
			User dbUser = userService.getUser();
			user.setAuthorities(dbUser.getAuthorities());
			user.setGroups(dbUser.getGroups());
			return "security/personal";
		}

		userService.updatePersonalDetails(user);

		return "redirect:/security/personal?success";
	}

	/**
	 * 修改密码页面
	 */
	@RequestMapping(value = "/personal/password", method = RequestMethod.GET)
	public String getPasswrodPage(@ModelAttribute("user") User user) {
		return "/security/password";
	}

	/**
	 * 修改个人密码
	 */
	@RequestMapping(value = "/personal/password", method = RequestMethod.PUT)
	public String changePassword(@Validated(value = { UserChangePassword.class }) @ModelAttribute("user") User user, BindingResult bindingResult,
			String oldPassword, Model model, RedirectAttributes redirect) {
		if (bindingResult.hasErrors()) {
			return "/security/password";
		}

		try {
			userService.resetPassword(oldPassword, user.getPassword());
		} catch (PasswordIncorrectException e) {
			model.addAttribute("status", "error");
			model.addAttribute("message", e.getMessage());
			model.addAttribute("newPassword", user.getPassword());

			return "/security/password";
		}

		return "redirect:/security/personal/password?success";
	}

	private void renderUser(User user, HttpServletRequest request) {
		String[] groupIds = request.getParameterValues("group.id");
		String[] authorityIds = request.getParameterValues("authority.id");

		List<Group> groups = new ArrayList<Group>();
		if (groupIds != null) {
			for (String id : groupIds) {
				Group group = new Group();
				group.setId(Long.parseLong(id));
				groups.add(group);
			}
		}

		List<Authority> authorities = new ArrayList<Authority>();
		if (authorityIds != null) {
			for (String id : authorityIds) {
				Authority authority = new Authority();
				authority.setId(Long.parseLong(id));
				authorities.add(authority);
			}
		}

		user.setGroups(groups);
		user.setAuthorities(authorities);
	}

	public static Map<String, Object> SUCCESS_MESSAGE(String message) {
		Map<String, Object> rs = new HashMap<String, Object>();
		rs.put("status", true);
		rs.put("message", message);
		return rs;
	}
}
