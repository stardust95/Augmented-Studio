package zju.homework.augmentedstudio.Java;

/**
 * Created by stardust on 2016/11/19.
 */


public class ResponseMsg {
    private String message;

    private Object object;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}

//public class ResponseMsg<Type>{
//    private String message;
//
//    private Type object;
//
//    public String getMessage() {
//        return message;
//    }
//
//    public void setMessage(String message) {
//        this.message = message;
//    }
//
//    public Type getObject() {
//        return object;
//    }
//
//    public void setObject(Type object) {
//        this.object = object;
//    }
//}
