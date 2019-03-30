package com.fr.swift.boot.controller;

import com.fr.swift.boot.controller.result.ResultMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * This class created on 2019/3/19
 *
 * @author Lucifer
 * @description
 */
@RestController()
@RequestMapping("/api/user")
public class UserController {

    private String account = "swift";
    private String passward = "swift";

    @ResponseBody
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResultMap login(HttpServletResponse response, HttpServletRequest request,
                           @RequestBody() Map<String, String> map) {
        ResultMap resultMap = new ResultMap();
        String account = map.get("account");
        String passward = map.get("password");
        if (!account.equals(this.account)) {
            resultMap.setStatusCode(1);
        }
        if (!passward.equals(this.passward)) {
            resultMap.setStatusCode(2);
        }
        resultMap.setData(account);
        return resultMap;
    }
}
