package com.fr.swift.cloud.boot.dispatcher;

import com.fr.swift.cloud.boot.token.TokenCache;
import com.fr.swift.cloud.boot.token.TokenCacheImpl;
import com.fr.swift.cloud.util.Strings;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * This class created on 2019/4/2
 *
 * @author Lucifer
 * @description todo 验证信息
 */
@ServletComponentScan
@WebServlet(urlPatterns = "/swift", description = "swiftControllerDispatcher")
public class SwiftControllerDispatcher extends DispatcherServlet {

    private static final long serialVersionUID = -1357968706769750240L;

    private static Set<String> TRANSPARENT_URLS;

    private TokenCache tokenCache = new TokenCacheImpl();

    static {
        TRANSPARENT_URLS = new HashSet<String>();
    }

    public static void registerTransparentUrls(Set<String> urls) {
        TRANSPARENT_URLS.addAll(urls);
    }

    @Override
    protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (isNeedToken(request.getPathInfo())) {
            String token = request.getHeader("token");
            if (Strings.isEmpty(token)) {
                response.setHeader("errorCode", "Empty token!");
                response.setStatus(500);
                return;
            }
            // TODO: 2020/4/21 处理userId
            try {
                String userId = tokenCache.getUserIdByToken(token);
            } catch (Exception e) {
                response.setHeader("errorCode", e.getMessage());
                response.setStatus(500);
                return;
            }
        }
        String origin = request.getHeader("Origin");
        String accessControlRequestHeaders = request.getHeader("Access-Control-Request-Headers");
        response.setHeader("access-control-allow-credentials", String.valueOf(true));
        response.setHeader("access-control-allow-headers", accessControlRequestHeaders);
        response.setHeader("access-control-allow-origin", origin);
        response.setHeader("connection", "keep-alive");
        response.setHeader("server", "swift");
        response.setHeader("vary", "Origin,Access-Control-Request-Headers");
        response.setHeader("via", "release 1.0 swift");
        response.setHeader("x-powered-by", "Express");
        response.setStatus(HttpServletResponse.SC_OK);
        super.doDispatch(request, response);
    }

    private boolean isNeedToken(String url) {
        for (String transparentUrl : TRANSPARENT_URLS) {
            if (transparentUrl.equals(url)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doOptions(request, new HttpServletResponseWrapper(response) {
            @Override
            public void setHeader(String name, String value) {
                if ("Allow".equals(name)) {
                    value = (StringUtils.hasLength(value) ? value + ", " : "") + HttpMethod.PATCH.name();
                    name = "access-control-allow-methods";
                }
                super.setHeader(name, value);
            }
        });
    }
}
