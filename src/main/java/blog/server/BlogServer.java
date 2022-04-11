package blog.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class BlogServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(5555).addService(new BlogService()).build();

        System.out.println("Server starting ... ");

        server.start();
        server.awaitTermination();

    }
}
