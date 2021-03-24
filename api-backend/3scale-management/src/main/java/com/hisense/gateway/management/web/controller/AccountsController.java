package com.hisense.gateway.management.web.controller;


import com.hisense.gateway.management.service.AccountsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.hisense.gateway.library.constant.BaseConstants.URL_ACCOUNTS;

@RequestMapping(URL_ACCOUNTS)
@RestController
public class AccountsController {
    @Autowired
    AccountsService accounts;
}
