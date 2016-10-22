package com.unfairtools;

/**
 * Created by brianroberts on 9/9/16.
 */

import com.sun.corba.se.spi.activation.Server;
import io.netty.handler.codec.http.HttpResponse;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.PostgreSQLClient;
import io.vertx.ext.sql.SQLConnection;

import java.util.Map;


public class HttpServerVerticle extends AbstractVerticle {

    private HttpServer httpServer = null;

    public void pageCountPlusOne(){
        JsonObject postgreSQLClientConfig = new JsonObject()
                .put("database", ServerInit.dbName)
                .put("username", ServerInit.dbOwner)
                .put("password", ServerInit.dbPassword);
        AsyncSQLClient postgreSQLClient = PostgreSQLClient.createShared(vertx, postgreSQLClientConfig);
        postgreSQLClient.getConnection(res -> {
            if (res.succeeded()) {
                SQLConnection connection = res.result();

                connection.query("UPDATE " + ServerInit.tableName + " SET value = value + 1;", res2 -> {
                    if (res2.succeeded()) {
                        System.out.println("i incremented");
                        connection.close();
                    } else {
                        System.out.println("failed to increment i " + res2.cause());
                        connection.close();
                    }
                });
            } else {
                System.out.println("Failed to connect to postgres" + res.cause());
            }
        });
    }

    public void returnPageCount(HttpServerRequest r){
        JsonObject postgreSQLClientConfig = new JsonObject()
                .put("database", ServerInit.dbName)
                .put("username", ServerInit.dbOwner)
                .put("password", ServerInit.dbPassword);

        AsyncSQLClient postgreSQLClient = PostgreSQLClient.createShared(vertx, postgreSQLClientConfig);
                        postgreSQLClient.getConnection(res -> {
                            if (res.succeeded()) {
                                System.out.println("Connected to postgres SUCCESS");
                                SQLConnection connection2 = res.result();
                                connection2.query("SELECT * from " + ServerInit.tableName+ ";", res2 -> {
                                    if (res2.succeeded()) {
                                        System.out.println("got value in db for 1");
                                        r.response().end("Total searches(since sep 9 2016): \n" + res2.result().getResults().get(0).getInteger(1));
                                        connection2.close();
                                    } else {
                                        System.out.println("couldn't get value for id = 1" + res2.cause());
                                        connection2.close();
                                        r.response().end("err82001");
                                    }
                                });
                            } else {
                                r.response().end("err82002");
                                System.out.println("Failed to connect to postgres" + res.cause());
                            }
                        });
    }

    @Override
    public void start(Future<Void> fut) {

        String pathToFiles = System.getProperty("user.dir");
        System.out.println("PathToFiles = " + pathToFiles);


        System.out.println("Starting server");
        httpServer = vertx
                .createHttpServer()

                .requestHandler(r -> {


                    String[] paths = r.path().split("/");

                    if(paths.length==0){
                        System.out.println("HOME SITE REQUESTED!!!");
                        r.response().sendFile(pathToFiles + ServerInit.folderName +  "/index.html");
                        pageCountPlusOne();

                    }else{
                        //So it must be /somthing, split by "/" to get the something
                        switch(paths[1]) {
                            case "dentists.html":
                                r.response().sendFile(pathToFiles + ServerInit.folderName + "/about.html");
                                break;
                            case "page_count":
                                returnPageCount(r);
                                break;
                            case "about":
                                System.out.println("ABOUT REQUESTED!!!");
                                r.response().sendFile(pathToFiles + ServerInit.folderName + r.path() + ".html");
                                break;
                            case "services":
                                System.out.println("SERVICES REQUESTED!!!");
                                r.response().sendFile(pathToFiles  + ServerInit.folderName + r.path() + ".html");
                                break;
                            case "contact":
                                System.out.println("CONTACT REQUESTED!!!");
                                r.response().sendFile(pathToFiles + ServerInit.folderName + r.path() + ".html");
                                break;
                            case "forms":
                                System.out.println("FORMS REQUESTED!!!");
                                r.response().sendFile(pathToFiles + ServerInit.folderName + r.path() + ".html");
                                break;
                            default:
                                String tag = r.headers().get("If-None-Match");
                                vertx.fileSystem().exists(pathToFiles + ServerInit.folderName + r.path(), result -> {
                                    if (result.succeeded() && result.result()) {
                                        r.response().putHeader("ETag",ServerInit.etag);
                                        if(tag!=null && tag.equals(ServerInit.etag)){
                                            System.out.println("304 for " + r.path());
                                            r.response().setStatusCode(304).end();
                                        }else{
                                            System.out.println("Requesting " + pathToFiles + ServerInit.folderName + r.path());
                                            r.response().sendFile(pathToFiles + ServerInit.folderName + r.path());
                                        }
                                    } else {
                                        System.err.println("404" + result.cause());
                                        r.response().end("Page not found\n404");
                                    }
                                });
                                break;
                        }
                    }

                }).listen(Integer.parseInt(ServerInit.port), result -> {
                    if (result.succeeded()) {
                        System.out.println("Success deploying to " + ServerInit.port);
                        fut.complete();
                    } else {
                        System.out.println("Failure deploying to " + ServerInit.port);
                        fut.fail(result.cause());
                    }

                });
    }
}

