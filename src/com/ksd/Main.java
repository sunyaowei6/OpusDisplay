package com.ksd;

import com.ksd.common.tool.DatabaseUtils;
import com.ksd.service.RoleService;
import com.ksd.service.impl.RoleServiceImpl;

public class Main {
    static DatabaseUtils databaseUtils=DatabaseUtils.getDatabaseUtils();
    public static void main(String []args){
//        databaseUtils.updatePropertiesFile("root","MySQL55555@Root");
        RoleService roleService=DatabaseUtils.getDatabaseUtils().getProxy(new RoleServiceImpl());
        System.out.println(roleService==null);
        roleService.findRole(1L);
    }
}
