package com.ksd.common.tool;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URISyntaxException;
import java.util.Properties;

public class DatabaseUtils {
    /**
     * private constructor
     */
    private DatabaseUtils() {
        try {
            mybatisConfigPath=Class.class.getResource("/database-connected.properties").toURI().getPath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    private static DatabaseUtils databaseUtils=new DatabaseUtils();
    /**
     * reference path of mybatis config
     */
    private static final String mybatisConfig="mybatis-config.xml";
    /**
     * properties key of mybatis config
     */
    private static final String D_U="username";
    private static final String D_P="password";
    /**
     * path whoch database connecet of config
     */
    private static String mybatisConfigPath;
    /**
     * default aspect that you oparater database
     */
    private DatabaseAspect databaseAspect=new DatabaseAspect();
    /**
     * only SqlSessionFactory to construct SqlSession
     */
    private SqlSessionFactory sqlSessionFactory;
    /**
     * thread safe about SqlSession
     */
    private static ThreadLocal<SqlSession> threadLocal=new ThreadLocal<SqlSession>();

    /**
     * get only object of @DatabaseUtils
     * @return
     */
    public static DatabaseUtils getDatabaseUtils(){
        return databaseUtils;
    }
    /**
     *  get proxy of obj
     * @param obj origin object
     * @param <T>
     * @return proxy
     */
    public <T> T getProxy(T obj){
        return new DatabaseProxy().bind(obj);
    }

    /**
     * get object of mapper class which  connect database to inqury
     * delete or insert ...
     * @param c
     * @param <T>
     * @return
     */
    public <T> T getMapper(Class<T> c){
        SqlSession sqlSession=threadLocal.get();
        if(sqlSession==null)return null;
        return threadLocal.get().getMapper(c);
    }

    /**
     *  encryptor database name and password
     * @param name
     * @param password
     */
    public void updatePropertiesFile(String name,String password){
        Properties p=new Properties();
        InputStream in= null;
        try {
            in = new FileInputStream(mybatisConfigPath);
            p.load(in);
            in.close();
            p.put(D_U,Encryption.encrypt_Base64(name));
            p.put(D_P,Encryption.encrypt_Base64(password));
            OutputStream out=new FileOutputStream(mybatisConfigPath,false);
            p.store(out,null);
            out.flush();
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     *  decryptor database username and pasaword
     * @return
     */
    private Properties readEncryptionProperties(){
        Properties p=new Properties();
        InputStream in=null;
        try {
            in= new FileInputStream(mybatisConfigPath);
            p.load(in);
            String username= (String) p.get(D_U);
            String password= (String) p.get(D_P);
            username=Encryption.decrypt_Base64(username);
            password=Encryption.decrypt_Base64(password);
            p.put(D_U,username);
            p.put(D_P,password);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(in!=null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return p;
    }
    private class DatabaseAspect{
        void before(){
           if(sqlSessionFactory==null){
               try {
                   Properties p=readEncryptionProperties();
                   InputStream in=Resources.getResourceAsStream(mybatisConfig);
                   sqlSessionFactory=new SqlSessionFactoryBuilder().build(in,p);
               } catch (Exception e) {
                   e.printStackTrace();
               }
           }
           SqlSession sqlSession=sqlSessionFactory.openSession();
           threadLocal.set(sqlSession);
        }
        void after(){
            SqlSession sqlSession=threadLocal.get();
            if(sqlSession!=null){
                sqlSession.close();
                threadLocal.set(null);
            }
        }
        void afterReturning(){
            SqlSession sqlSession=threadLocal.get();
            sqlSession.commit();
        }
        void afterThrowing(){
            SqlSession sqlSession=threadLocal.get();
            sqlSession.rollback();
        }

    }

    private class DatabaseProxy implements InvocationHandler {
        Object originObj;

        public <T> T bind(T obj){
            originObj=obj;
            Class c=obj.getClass();
            return (T) Proxy.newProxyInstance(c.getClassLoader(),c.getInterfaces(),this);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object retObj=null;
            boolean isException=false;
            databaseAspect.before();
            try {
                retObj=method.invoke(originObj,args);
            }catch (Exception e){
                e.printStackTrace();
                isException=false;
            }finally {
                if (isException) databaseAspect.afterThrowing();
                else databaseAspect.afterReturning();
                databaseAspect.after();
            }
            return retObj;
        }
    }

}
