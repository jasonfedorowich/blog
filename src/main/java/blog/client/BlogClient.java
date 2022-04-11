package blog.client;

import com.proto.blog.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class BlogClient {
    private static String testCreateBlog(ManagedChannel channel){

        BlogServiceGrpc.BlogServiceBlockingStub stub = BlogServiceGrpc.newBlockingStub(channel);
        Blog blog = Blog.newBuilder()
                .setAuthorId("jason")
                .setContent("Hello first blog")
                .setTitle("New blog!").build();

        CreateBlogResponse response = stub.createBlog(CreateBlogRequest.newBuilder().setBlog(blog).build());
        System.out.println(response.getBlog().getId());
        System.out.println(response.getBlog().toString());
        return response.getBlog().getId();
    }

    public static void testReadBlog(ManagedChannel channel, String id){
        BlogServiceGrpc.BlogServiceBlockingStub stub = BlogServiceGrpc.newBlockingStub(channel);

        ReadBlogResponse response = stub.readBlog(ReadBlogRequest.newBuilder().setBlogId(id).build());
        System.out.println(response.getBlog().getId());
        System.out.println(response.getBlog().toString());

    }

    public static void testUpdateBlog(ManagedChannel channel, String id){
        BlogServiceGrpc.BlogServiceBlockingStub stub = BlogServiceGrpc.newBlockingStub(channel);
        Blog blog = Blog.newBuilder()
                .setTitle("HEEYY!")
                .setAuthorId("Jason!")
                .setContent("This is new")
                .setId(id)
                .build();
        UpdateBlogResponse response = stub.updateBlog(UpdateBlogRequest.newBuilder().setBlog(blog).build());
        System.out.println(response.getBlog().getId());
        System.out.println(response.getBlog().toString());
    }


    private static void testDeleteBlog(ManagedChannel channel, String id){
        System.out.println("Deleting blog");
        BlogServiceGrpc.BlogServiceBlockingStub stub = BlogServiceGrpc.newBlockingStub(channel);
        DeleteBlogResponse response = stub.deleteBlog(DeleteBlogRequest.newBuilder().setId(id).build());
        System.out.println(response);
    }

    private static void testListBlog(ManagedChannel channel){
        System.out.println("Starting stream!");
        BlogServiceGrpc.BlogServiceBlockingStub stub = BlogServiceGrpc.newBlockingStub(channel);
        stub.listBlog(ListBlogRequest.newBuilder().build()).forEachRemaining(item -> System.out.println(item.getBlog().toString()));

    }

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 5555)
                .usePlaintext()
                .build();

        String id = testCreateBlog(channel);
        testReadBlog(channel, id);
        testUpdateBlog(channel, id);
        testDeleteBlog(channel, id);
        testListBlog(channel);

    }


}
