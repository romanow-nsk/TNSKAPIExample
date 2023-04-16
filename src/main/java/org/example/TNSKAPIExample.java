package org.example;


import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import romanow.abc.core.API.APICallSynch;
import romanow.abc.core.API.RestAPI;
import romanow.abc.core.API.RestAPIBase;
import romanow.abc.core.DBRequest;
import romanow.abc.core.UniException;
import romanow.abc.core.constants.Values;
import romanow.abc.core.constants.ValuesBase;
import romanow.abc.core.entity.EntityList;
import romanow.abc.core.entity.EntityRefList;
import romanow.abc.core.entity.WorkSettings;
import romanow.abc.core.entity.baseentityes.JString;
import romanow.abc.core.entity.subjectarea.TRoute;
import romanow.abc.core.entity.subjectarea.TSegment;
import romanow.abc.core.entity.users.Account;
import romanow.abc.core.entity.users.User;
import romanow.abc.core.utils.Pair;
import romanow.abc.dataserver.APICallC;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class TNSKAPIExample {
        Gson gson = new Gson();
        BufferedReader reader;
        String clientIP="217.71.138.9";
        int clientPort=4571;
        String userPhore="9335555555";
        String userPass="112233";
        RestAPIBase service = null;
        RestAPI service2 = null;
        String debugToken=null;
        User user=null;
        boolean connected=false;
        WorkSettings workSettings=null;
        boolean localUser=false;
        private boolean isOn=false;
        //-----------------------------------------------------------------------------------------------------------------
        private Pair<RestAPIBase,String> startOneClient() throws UniException {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .readTimeout(Values.HTTPTimeOut, TimeUnit.SECONDS)
                    .connectTimeout(Values.HTTPTimeOut, TimeUnit.SECONDS)
                    .build();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://"+clientIP+":"+clientPort)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
            RestAPIBase service = (RestAPIBase)retrofit.create(RestAPIBase.class);
            localUser = clientIP.equals("localhost") || clientIP.equals("127.0.0.1");
            JString ss = new APICallC<JString>(){
                @Override
                public Call<JString> apiFun() {
                    return service.debugToken(Values.DebugTokenPass);
                }
            }.call();
            return new Pair<RestAPIBase,String>(service,ss.getValue());
        }
    private RestAPI startSecondClient() throws UniException {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(Values.HTTPTimeOut, TimeUnit.SECONDS)
                .connectTimeout(Values.HTTPTimeOut, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://"+clientIP+":"+clientPort)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
        RestAPI service = (RestAPI)retrofit.create(RestAPI.class);
        localUser = clientIP.equals("localhost") || clientIP.equals("127.0.0.1");
        return service;
        }
    public void startClient() throws UniException{
        try {
            Pair<RestAPIBase,String> res = startOneClient();
            service = res.o1;
            debugToken = res.o2;
            service2 = startSecondClient();
            } catch (UniException e) {
                throw UniException.io("Ошибка ключа отладки "+e.toString()+"\n");
                }
            }
        public void login(){
            Values.init();
            try {
                startClient();
                Account acc = new Account("",userPhore,userPass);
                user = new APICallSynch<User>() {
                    @Override
                    public Call<User> apiFun() {
                        return service.login(acc);
                        }
                    }.call();
                debugToken = user.getSessionToken();
                } catch (UniException e) {
                    System.out.println("Клиент: "+e.toString());
                    }
            }
        public EntityRefList<TSegment> getSegments() {
            try {
                EntityRefList<TSegment> list = new APICallSynch<EntityRefList<TSegment>>() {
                    @Override
                    public Call<EntityRefList<TSegment>> apiFun() {
                        return service2.getRoads(debugToken);
                        }
                    }.call();
                return list;
                } catch (UniException e) {
                    System.out.println("Ошибка чтения сегментов: " + e.toString());
                    return new EntityRefList<>();
                    }
                }

    public EntityRefList<TRoute> getRoutes() {
        try {
            ArrayList<DBRequest> list = new APICallSynch<ArrayList<DBRequest>>() {
                @Override
                public Call<ArrayList<DBRequest>> apiFun() {
                    return service.getEntityList(debugToken,"TRoute",Values.GetAllModeActual,1);
                }
            }.call();
            EntityRefList<TRoute> out = new EntityRefList<>();
            for(DBRequest request : list)
                out.add((TRoute) request.get(gson));
            return out;
        } catch (UniException e) {
            System.out.println("Ошибка чтения сегментов: " + e.toString());
            return new EntityRefList<>();
        }
    }

        public static String replace(double vv){
            return String.format("%6.3f",vv).replace(",",".");
            }

        public static void main(String ss[]){
            TNSKAPIExample example = new TNSKAPIExample();
            example.login();
            EntityRefList<TRoute> list = example.getRoutes();
            System.out.println("Маршрутов "+list.size());
            }
    }

