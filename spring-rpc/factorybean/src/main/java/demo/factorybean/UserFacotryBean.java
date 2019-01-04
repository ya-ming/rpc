package demo.factorybean;

import demo.model.User;
import org.springframework.beans.factory.FactoryBean;

public class UserFacotryBean implements FactoryBean<User> {
    private static final User user = new User();

    private String name;
    private String email;

    public User getObject() throws Exception {
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    public Class<?> getObjectType() {
        return User.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
