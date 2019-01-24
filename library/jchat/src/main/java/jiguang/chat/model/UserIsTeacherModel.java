package jiguang.chat.model;

/**
 * Created by zhaoyuanchao on 2018/8/24.
 */

public class UserIsTeacherModel {
    private int teacher;
    private int userType;

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public int getTeacher() {
        return teacher;
    }

    public void setTeacher(int teacher) {
        this.teacher = teacher;
    }
}
