package com.lucien.spirit.core.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.MDC;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.lucien.spirit.core.constants.TokenConstants;
import com.lucien.spirit.core.constants.UserConstants;
import com.lucien.spirit.core.security.annotation.TokenValid;
import com.lucien.spirit.core.shiro.ShiroUser;
import com.lucien.spirit.core.util.HttpUtil;

/**
 * 访问拦截器，验证token、记录访问日志
 * @Filename : SecurityInterceptor.java
 * @Package : com.lucien.spirit.core.security
 * @Description : TODO
 * @author : lijunf
 * @CreateDate : 2016年2月14日
 */
public class SecurityInterceptor extends HandlerInterceptorAdapter {
    
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String servletPath = request.getServletPath();
        if (servletPath != null && servletPath.startsWith("/resources")) {
            return true;
        }
        
		// Token验证
        if (handler instanceof HandlerMethod) {
            String token = request.getParameter(TokenConstants.DEFAULT_TOKEN_NAME);
            if (token != null || ((HandlerMethod) handler).getMethodAnnotation(TokenValid.class) != null) {
                String msg = TokenHandler.validToken(request);
                if (msg != null) {
                    response.setCharacterEncoding("utf-8");
                    response.setContentType("text/html; charset=utf-8");
                    response.getWriter().append(msg);
                    response.getWriter().close();
                    return false;
                }
            }
        }

		// 设置日志MDC
		ShiroUser user = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
		if (user == null) {
		    MDC.put("userId", UserConstants.DEFAULT_USERNAME);
		} else {
		    MDC.put("userId", user.getName());
		}
		
		String ipAddr = HttpUtil.getIpAddr(request);
		MDC.put("ipAddr", ipAddr);
		
		return true;
	}

	public void afterCompletion(HttpServletRequest httpservletrequest,
			HttpServletResponse httpservletresponse, Object obj,
			Exception exception) throws Exception {
	}

}
