package com.mineru.hops.UserManage.Model;

/**
 * Created by rmstj on 2017-10-05.
 */

public class User {
    private String user_id;
    private String email;
    private String username;

    public User(String user_id,String email,String username){
        this.user_id = user_id;
        this.email=email;
        this.username=username;
    }
    public User(){
    }

    public String getUser_id(){
        return user_id;
    }
    public void setUser_id(String user_id){
        this.user_id=user_id;
    }
    public String getEmail(){
        return email;
    }
    public void setEmail(String email){
        this.email=email;
    }
    public String getUsername(){
        return username;
    }
    public void setUsername(String username){
        this.username=username;
    }
    @Override
    public String toString() {
        return "User{" +
                "my_id='" + user_id + '\'' +
                ", verify_email='" + email + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
