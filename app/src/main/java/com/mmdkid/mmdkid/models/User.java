package com.mmdkid.mmdkid.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.mmdkid.mmdkid.App;
import com.mmdkid.mmdkid.server.Connection;
import com.mmdkid.mmdkid.server.Query;
import com.mmdkid.mmdkid.server.RESTAPIConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by LIYADONG on 2017/6/28.
 */

public class User extends Model {
    private final static String TAG = "User";
    private final static String URI_SIGNUP = "v1/signup";
    private final static String URI_SIGNUP_PHONE = "v1/signupphone";
    private final static String URI_SIGNUP_EMAIL = "v1/signupemail";
    private final static String URI_AUTO_SINGUP = "v1/auto";
    private final static String URI_RESET_PASSWORD = "resetPassword";
    private final static String URI = "v1/users";
    private final static String AUTO_CREATE_SECRET_KEY = "123456";

    public final static int ROLE_PARENT = 3;
    public final static int ROLE_STUDENT = 1;
    public final static int ROLE_TEACHER = 2;

    public final static int  GENDER_MALE  = 1;
    public final static int  GENDER_FEMALE  = 2;
    public final static int  GENDER_UNKNOWN  = 3;


    public static final String ACTION_AUTO_SIGNUP= "auto_signup";
    public static final String ACTION_SIGNUP= "signup";
    public static final String ACTION_SIGNUP_PHONE= "signupphone";
    public static final String ACTION_SIGNUP_EMAIL= "signupemail";
    public static final String ACTION_RESET_PASSWORD= "reset_password";

    public int mId;
    public String mUsername;
    public String mAvatar;
    public String mEmail;
    public String mCellphone;
    public int mFollower;
    public int mFollowing;
    public String mNickname;
    public String mRealname;
    public String mPassword;
    public String mPasswordRepeat;
    public String mSignature;
    public int mRole;
    public int mGender;
    public String mBirthday;
    public String mScenario ="default";

    public String mNewPassword; // 只用于用户更改密码

    @Override
    public void setAttributesNames() {
        this.mFieldNameMap.put("mId","id");
        this.mFieldNameMap.put("mUsername","user_name");
        this.mFieldNameMap.put("mAvatar","avatar");
        this.mFieldNameMap.put("mEmail","email");
        this.mFieldNameMap.put("mFollower","follower");
        this.mFieldNameMap.put("mFollowing","following");
        this.mFieldNameMap.put("mNickname","nick_name");
        this.mFieldNameMap.put("mRealname","real_name");
        this.mFieldNameMap.put("mPassword","password");
        this.mFieldNameMap.put("mPasswordRepeat","password_repeat");
        this.mFieldNameMap.put("mRole","role");
        this.mFieldNameMap.put("mGender","gender");
        this.mFieldNameMap.put("mScenario","scenario");
        this.mFieldNameMap.put("mBirthday","birthday");
        this.mFieldNameMap.put("mCellphone","cellphone");
        this.mFieldNameMap.put("mSignature","signature");
    }

    public static Query find(Connection connection)
    {
        Query query = new Query(connection);
        query.mModelClass = User.class;
        //connection.URL = connection.URL + "v1/users";
        Log.d(TAG,connection.URL);
        return query;
    }

    public static Query find(Context context, RESTAPIConnection.OnConnectionListener listener)
    {
        RESTAPIConnection connection = new RESTAPIConnection(context);
        connection.setListener(listener);
        Query query = new Query(connection);
        query.mModelClass = User.class;
        return query;
    }

    public static Query find(Context context, String token,RESTAPIConnection.OnConnectionListener listener)
    {
        RESTAPIConnection connection = new RESTAPIConnection(context);
        connection.setListener(listener);
        connection.ACCESS_TOKEN = token;
        Query query = new Query(connection);
        query.mModelClass = User.class;
        return query;
    }


    public static String getAutoCreateUserSecretKey(){
        return AUTO_CREATE_SECRET_KEY;
    }

    public static JSONObject getRequest(Query query)  {
        RESTAPIConnection connection = (RESTAPIConnection) query.getConnection();
        connection.REQUEST_METHOD = Request.Method.GET;
        connection.URL = connection.URL + URI+ "?"
                    + RESTAPIConnection.ACCESS_TOKEN_NAME + "="
                    + connection.ACCESS_TOKEN;
        Map<String,String> parameters = query.getmParameters();
        if (parameters!=null){
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
               connection.URL = connection.URL + "&" +  entry.getKey() +"=" + entry.getValue();
            }
        }
        return null;
    }

    public static ArrayList<User> populateModels(JSONObject response){
        Log.d(TAG,"Get response to populate the user model."+response.toString());
        ArrayList<User> arrayList = new ArrayList<User>();
        if(response.has("items")){
            // response is mutiple results.
            try {
                JSONArray items = response.getJSONArray("items");
                JSONObject meta = response.getJSONObject("_meta");
                int perPage = meta.getInt("perPage");
                int currentPage = meta.getInt("currentPage");
                int totalCount = meta.getInt("totalCount");
                int currentPageTotal;
                if(perPage*currentPage < totalCount){
                    currentPageTotal = perPage;
                }else{
                    currentPageTotal = perPage - (perPage*currentPage - totalCount);
                }
                for(int i = 0; i < currentPageTotal; i++){
                    User user = populateModel(items.getJSONObject(i));
                    if(user!=null) arrayList.add(user);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            // response is a single result
            User user = populateModel(response);
            if(user!=null) arrayList.add(user);
            /*User user = new User();
            try {
                user.mUsername = response.getString("user_name");
                user.mId = response.getInt("id");
                if(response.has("email"))  user.mEmail = response.getString("email");
                user.mAvatar = response.getString("avatar");
                user.mNickname = response.getString("nick_name");
                arrayList.add(user);
            } catch (JSONException e) {
                e.printStackTrace();
            }*/
        }
        return arrayList;
    }

    /*public static ArrayList<User> populateModel(JSONObject response) {
        Log.d(TAG,"Get response to populate the user model."+response.toString());
        ArrayList<User> arrayList = new ArrayList<User>();
        User user = new User();
        try {
            user.mUsername = response.getString("user_name");
            user.mId = response.getInt("id");
            user.mEmail = response.getString("email");
            user.mAvatar = response.getString("avatar");
            arrayList.add(user);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return arrayList;
    }*/

    public static User populateModel(JSONObject response) {
        Log.d(TAG,"Get response to populate the user model."+response.toString());
        try {
            User user = new User();
            if(response.has("user_name")) user.mUsername = response.getString("user_name");
            if(response.has("id")) user.mId = response.getInt("id");
            if(response.has("email")) user.mEmail = response.getString("email");
            if(response.has("avatar")) user.mAvatar = response.getString("avatar");
            if(response.has("nick_name") && !response.isNull("nick_name")) user.mNickname = response.getString("nick_name");
            if(response.has("real_name")) user.mRealname = response.getString("real_name");
            if(response.has("follower")) user.mFollower = response.getInt("follower");
            if(response.has("following")) user.mFollowing = response.getInt("following");
            if(response.has("cellphone") && !response.isNull("cellphone")) user.mCellphone = response.getString("cellphone");
            if(response.has("role")) user.mRole = response.getInt("role");
            if(response.has("signature") && !response.isNull("signature")) user.mSignature = response.getString("signature");
            return user;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void saveToLocal(Context context){
        SharedPreferences settings = context.getSharedPreferences(App.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("user_name",this.mUsername);
        editor.putString("nick_name",this.mNickname);
        editor.putString("real_name",this.mRealname);
        editor.putString("avatar",this.mAvatar);
        editor.putString("email",this.mEmail);
        editor.putString("cellphone",this.mCellphone);
        editor.putString("signature",this.mSignature);
        editor.putInt("user_id",this.mId);
        editor.putInt("following",this.mFollowing);
        editor.putInt("follower",this.mFollower);
        editor.putInt("role",this.mRole);
        editor.commit();
    }

    public static User loadFromLocal(Context context){
        User user = new User();
        SharedPreferences settings = context.getSharedPreferences(App.PREFS_NAME,Context.MODE_PRIVATE);
        //if((user.mId=settings.getInt("user_id",-1))==-1) return null;
        //if(TextUtils.isEmpty(user.mUsername=settings.getString("user_name",""))) return null;
        user.mId=settings.getInt("user_id",-1);
        user.mUsername=settings.getString("user_name","");
        user.mRole=settings.getInt("role",ROLE_PARENT);
        user.mFollower=settings.getInt("follower",0);
        user.mFollowing=settings.getInt("following",0);
        user.mNickname=settings.getString("nick_name","");
        user.mRealname=settings.getString("real_name","");
        user.mAvatar=settings.getString("avatar","");
        user.mCellphone=settings.getString("cellphone","");
        user.mEmail=settings.getString("email","");
        user.mSignature=settings.getString("signature","");
        if (user.mUsername.isEmpty() && user.mCellphone.isEmpty() && user.mEmail.isEmpty()) return null;
        return user;
    }

    @Override
    public JSONObject getRequest(String action, Connection connection){
        JSONObject request = new JSONObject();
        switch (action){
            case RESTAPIConnection.ACTION_CREATE:
                ((RESTAPIConnection) connection).setRequestMethod(Request.Method.POST);
                connection.URL = connection.URL + URI_SIGNUP;

                try {
                    request.put("username",mUsername);
                    request.put("email",mEmail);
                    request.put("password",mPassword);
                    request.put("password_repeat",mPasswordRepeat);
                    request.put("role",mRole);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return request;
            case User.ACTION_AUTO_SIGNUP:
                ((RESTAPIConnection) connection).setRequestMethod(Request.Method.POST);
                connection.URL = connection.URL + URI_AUTO_SINGUP;
                try {
                    request.put("nick_name",mNickname);
                    request.put("avatar",mAvatar);
                    request.put("secret",AUTO_CREATE_SECRET_KEY);
                    request.put("role",mRole);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return request;

        }
        return null;
    }

    public static void signup(RESTAPIConnection connection,String username,String email,String password,String password_repeat,int role){
        User user = new User();
        user.mUsername = username;
        user.mEmail = email;
        user.mPassword = password;
        user.mPasswordRepeat = password_repeat;
        user.mRole = role;
        connection.create(user);
    }

   /* public static void autoSignup(LoginActivity.ShareUserInfo userInfo,Context context,RESTAPIConnection.OnConnectionListener listener){
        RESTAPIConnection connection = new RESTAPIConnection(context);
        connection.setListener(listener);
        User user = new User();
        user.mAvatar = userInfo.iconurl;
        user.mNickname = userInfo.name;
        user.mRole = ROLE_PARENT;
        connection.action(ACTION_AUTO_SIGNUP,user);
    }*/


    public String getDisplayName(){
        if( mNickname.equals("null") || mNickname.equals("")){
            return mUsername;
        }else{
            return mNickname;
        }
    }

    @Override
    public JSONObject getJsonRequest(String action,Connection connection) {
        JSONObject request = new JSONObject();
        switch (action) {
            case User.ACTION_SIGNUP:
                ((RESTAPIConnection) connection).setRequestMethod(Request.Method.POST);
                connection.URL = connection.URL + URI_SIGNUP;
                try {
                    request.put("username", mUsername);
                    request.put("email", mEmail);
                    request.put("password", mPassword);
                    request.put("password_repeat", mPasswordRepeat);
                    request.put("role", mRole);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return request;
            case User.ACTION_AUTO_SIGNUP:
                ((RESTAPIConnection) connection).setRequestMethod(Request.Method.POST);
                connection.URL = connection.URL + URI_AUTO_SINGUP;
                try {
                    request.put("nick_name", mNickname);
                    request.put("avatar", mAvatar);
                    request.put("secret", AUTO_CREATE_SECRET_KEY);
                    request.put("password", mPassword);
                    request.put("role", mRole);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return request;
            case User.ACTION_SIGNUP_PHONE:
                ((RESTAPIConnection) connection).setRequestMethod(Request.Method.POST);
                connection.URL = connection.URL + URI_SIGNUP_PHONE;
                try {
                    request.put("cellphone",mCellphone);
                    request.put("password",mPassword);
                    request.put("role",mRole);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return request;
            case User.ACTION_SIGNUP_EMAIL:
                ((RESTAPIConnection) connection).setRequestMethod(Request.Method.POST);
                connection.URL = connection.URL + URI_SIGNUP_EMAIL;
                try {
                    request.put("email",mEmail);
                    request.put("password",mPassword);
                    request.put("role",mRole);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return request;
            case User.ACTION_CREATE:
                ((RESTAPIConnection) connection).setRequestMethod(Request.Method.POST);
                connection.URL = connection.URL + URI;
                request = this.toJsonObject();
                request.remove("id");
                Log.d(TAG,"User json object is :" + request.toString());
                return  request;
            case User.ACTION_UPDATE:
                ((RESTAPIConnection) connection).setRequestMethod(Request.Method.PATCH);
                if(mId!=0){
                    connection.URL = connection.URL + URI + "/"+ mId;
                    Log.d(TAG,"User update url is " + connection.URL);
                }else {
                    Log.d(TAG,"User for updating, but there is no user id.");
                    return null;
                }
                request = this.toJsonObject();
                request.remove("id");
                Log.d(TAG,"User json object is :" + request.toString());
                return  request;
            case User.ACTION_RESET_PASSWORD:
                ((RESTAPIConnection) connection).setRequestMethod(Request.Method.POST);
                connection.URL = connection.URL + URI + "/" + mId + "/" + URI_RESET_PASSWORD;
                try {
                    request.put("password",mPassword);
                    request.put("newPassword",mNewPassword);
                    request.put("confirmPassword",mPasswordRepeat);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d(TAG,"User reset password json object is :" + request.toString());
                return  request;

        }
        return null;
    }

    public static String getGenderName(int gender){
        switch (gender){
            case GENDER_FEMALE:
                return "女";
            case GENDER_MALE:
                return "男";
            default:
                return "未知";
        }
    }

    /**
     * 服务器返回的json数据有可能含有字符串"null"
     */
    public String getIdentity(){
        if (mUsername!=null && !mUsername.isEmpty()&& !mUsername.equalsIgnoreCase("null")) return mUsername;
        if (mCellphone!=null && !mCellphone.isEmpty() && !mCellphone.equalsIgnoreCase("null")) return mCellphone;
        if (mEmail!=null && !mEmail.isEmpty()&& !mEmail.equalsIgnoreCase("null")) return mEmail;
        return null;
    }


    /*@Override
    public void save(String action ,Context context, RESTAPIConnection.OnConnectionListener listener) {
        RESTAPIConnection connection = new RESTAPIConnection(context);
        connection.setListener(listener);
        connection.excute(this.getJsonRequest(action,connection),User.class);
    }*/

    public String getUrl(){
        return "http://www.mmdkid.cn/index.php?r=user/show-me&theme=app&id="+this.mId;
    }
}
