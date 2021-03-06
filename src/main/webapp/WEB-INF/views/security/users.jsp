<%@ page language="java" import="java.util.*" contentType="text/html; charset=UTF-8"%>
<%@ include file="/WEB-INF/views/includes/taglibs.jspf"%>
<!DOCTYPE HTML>
<html lang="zh_CN">
<head>
<meta charset="UTF-8">
<meta name="decorator" content="${param._decorator}" />
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<title>SDK Analytics2.0</title>
<%@ include file="/WEB-INF/views/includes/head_scripts_links.jspf"%>
</head>
<body>
	<div id="content-header">
		<div id="breadcrumb">
			<a href="#" title="用户列表" class="tip-bottom"><i class="icon-home"></i>用户列表</a>
		</div>
	</div>

	<div class="container-fluid">
		<div class="row-fluid">
			<div class="span12">
				<div class="widget-box">
					<div class="widget-title">
						<span class="icon"><i class="icon-th"></i></span>
						<h5>用户列表</h5>
						<span class="btn icon-plus-sign add-user-trigger" style="float: right; margin-top: 7px; margin-right: 8px;">添加用户</span>
					</div>
					<div class="widget-content nopadding">
						<table class="table table-bordered users-table">
							<thead>
								<tr>
									<th>ID</th>
									<th>用户名</th>
									<th>邮箱</th>
									<th>密码</th>
									<th>状态</th>
									<th>修改信息</th>
								</tr>
							</thead>
							<tbody>
								<c:forEach items="${users }" var="user">
									<tr>
										<td><c:out value="${user.id }" /></td>
										<td><c:out value="${user.username }" /></td>
										<td><c:out value="${user.email }" /></td>
										<td><c:out value="${fn:substring(user.password, 0, 4)}" />******</td>
										<td><c:choose>
												<c:when test="${user.enabled}">启用</c:when>
												<c:otherwise>禁用</c:otherwise>
											</c:choose></td>
										<td>
										<a class="query-user-trigger" href="<c:url value="/security/user/${user.id }" />">修改信息</a> 
										<a class="delete-user-trigger" href="<c:url value="/security/user/${user.id }" />">删除</a></td>
									</tr>
								</c:forEach>
							</tbody>
						</table>
					</div>
				</div>
			</div>
		</div>
	</div>
	<script type="text/javascript">
		window.onload = function() {
			$('.users-table').dataTable({
				'bJQueryUI' : true,
				'sPaginationType' : "full_numbers",
				'sDom' : '<""l>t<"F"fp>',
				'bSort' : true,
				'bRetrieve' : true,
				'aaSorting' : [ [ 2, "desc" ] ],
			});
			$('select').select2();

			$('.query-user-trigger').click(function(e) {
				e.preventDefault();
				
				var url = $(this).attr('href') + '?_decorator=none';
				window.openWindow(url, '用户信息');
			});

			$('.add-user-trigger').click(function(e) {
				e.preventDefault();
				window.openWindow('<c:url value="/security/user"/>?_decorator=none', '用户信息');
			});

			//TODO 重复代码
			$('.delete-user-trigger').click(function(e) {
				e.preventDefault(); // TODO 兼容问题，重复代码 
				
				var url = $(this).attr('href') + '?_decorator=none';
				if (!window.confirm('你去要删除该用户吗 ?')) {
					return;
				}

				$.post(url, {
					'_format' : 'json',
					'_method' : 'DELETE',
					'${_csrf.parameterName}' : '${_csrf.token}',
				}, function(data) {
					window.alert(data.success || data.error);
					window.location.reload();
				});
			});
		};
	</script>
</body>
<%@ include file="/WEB-INF/views/includes/footer.jspf"%>
<%@ include file="/WEB-INF/views/includes/foot_scripts_links.jspf"%>
</html>