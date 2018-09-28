package com.ksd.service.impl;

import com.ksd.common.tool.DatabaseUtils;
import com.ksd.mapper.RoleMapper;
import com.ksd.pojo.Role;
import com.ksd.service.RoleService;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Vector;

public class RoleServiceImpl implements RoleService {
    DatabaseUtils databaseUtils=DatabaseUtils.getDatabaseUtils();

    @Override
     public Role findRole(Long id) {
            RoleMapper roleMapper=databaseUtils.getMapper(RoleMapper.class);
            Role role=roleMapper.selectRole(1L);
            System.out.println(role.getId()+role.getName());
        return role;
    }
}
