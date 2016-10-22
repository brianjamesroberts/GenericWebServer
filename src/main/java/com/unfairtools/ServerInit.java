package com.unfairtools;
/**
 * Created by brianroberts on 9/9/16.
 */

import io.vertx.core.*;


public class ServerInit {

    public static String dbName = "";
    public static String dbOwner = "";
    public static String dbPassword = "";
    public static String port = "";
    public static String tableName = "";
    public static String folderName = "";

    //change this if you update the website!
    public static String etag = "69";


    public ServerInit(String name, String owner, String ownerPassword, String prt, String tblName, String fldrName){
        ServerInit.dbName = name;
        ServerInit.dbOwner = owner;
        ServerInit.dbPassword = ownerPassword;
        ServerInit.port = prt;
        ServerInit.tableName = tblName;
        folderName = fldrName;

        System.out.println("com.unfairtools.ServerInit.constructor()");
        Vertx vertx;
        vertx = Vertx.vertx();
        Handler<AsyncResult<String>> handler = new Handler<AsyncResult<String>>() {
            @Override
            public void handle(AsyncResult<String> event) {
                if(event.cause()==null){
                    System.out.println("result: Successfully deployed server");
                }else{
                    System.out.println(event.cause());
                }
            }
        };
        try {
            vertx.deployVerticle(HttpServerVerticle.class.getName(), handler);
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public static void main(String[] args){
        //0 = db name
        //1 = db owner
        //2 = db owner password for psql
        //3 = port;
        //4 = table name;
        //5 folder name
        args = new String[6];
        args[0]="db2";
        args[1]="brianroberts";
        args[2]="password1";
        args[3]="8082";
        args[4]="table1";

        //should not end with '/'
        args[5]="nancy_site_files";
        if(!(args[5].charAt(0)=='/'))
            args[5] = "/" + args[5];

        if(args.length<5){
            System.out.println("Requires args dbName, dbOwner, dbPassword, Port, tableName, folderName (where website resides, use (homedir assumed) folder1/folder2 where folder 2 holds site, folder1 is arbitrary parent.)");
        }else{
            System.out.println("Args length " + args.length);
        }
        new ServerInit(args[0], args[1] , args[2], args[3], args[4], args[5]);
    }





}


