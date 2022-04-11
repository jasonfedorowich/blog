package blog.server;

import com.mongodb.client.*;
import com.mongodb.client.result.DeleteResult;
import com.proto.blog.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.print.Doc;

import static com.mongodb.client.model.Filters.eq;


public class BlogService extends BlogServiceGrpc.BlogServiceImplBase {
    private static final String MONGO_HOST = "localhost";
    private static final String MONGO_PORT = "27017";
    private static final String MONGO_DB = "mydb";
    private static final String COLL_NAME = "blog";

    private MongoClient client;
    private MongoDatabase database;
    private MongoCollection<Document> collection;



    public BlogService() {
        client = MongoClients.create("mongodb://" + MONGO_HOST +":"+ MONGO_PORT);
        database = client.getDatabase(MONGO_DB);
        collection = database.getCollection(COLL_NAME);
    }

    private Document blogToDoc(Blog blog){
        Document document = new Document();
        document.append("title", blog.getTitle())
                .append("content", blog.getContent())
                .append("author_id", blog.getAuthorId());
        return document;

    }

    private Document blogToDoc(Blog blog, String id){
        Document document = new Document();
        document.append("title", blog.getTitle())
                .append("content", blog.getContent())
                .append("author_id", blog.getAuthorId())
                .append("_id", id);
        return document;

    }


    private Blog docToBlog(Document document){
        return Blog.newBuilder()
                .setTitle(document.getString("title"))
                .setContent(document.getString("content"))
                .setAuthorId(document.getString("author_id")).build();

    }

    private Blog docToBlog(Document document, String id){
        return Blog.newBuilder()
                .setId(id)
                .setTitle(document.getString("title"))
                .setContent(document.getString("content"))
                .setAuthorId(document.getString("author_id")).build();

    }

    @Override
    public void createBlog(CreateBlogRequest request, StreamObserver<CreateBlogResponse> responseObserver) {
        System.out.println("Received request!");
        Blog blog = request.getBlog();

        Document doc = blogToDoc(blog);

        collection.insertOne(doc);
        System.out.println("Blog inserted!");

        String id = doc.getObjectId("_id").toString();
        CreateBlogResponse response = CreateBlogResponse.newBuilder()
                .setBlog(docToBlog(doc, id)).build();
        System.out.println("Request completed!");

        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }

    @Override
    public void readBlog(ReadBlogRequest request, StreamObserver<ReadBlogResponse> responseObserver) {
        String blogId = request.getBlogId();
        Document document = collection.find(eq("_id", new ObjectId(blogId))).first();
        if(document == null){
            System.out.println("No blog found!");
            responseObserver.onError(Status.NOT_FOUND.withDescription("No Blog found")
                    .asRuntimeException());
        }else{
            System.out.println("Blog found sending response!");
            responseObserver.onNext(ReadBlogResponse.newBuilder().setBlog(docToBlog(document, blogId)).build());
            responseObserver.onCompleted();

        }
    }

    @Override
    public void updateBlog(UpdateBlogRequest request, StreamObserver<UpdateBlogResponse> responseObserver) {
        Blog blog = request.getBlog();
        String id = blog.getId();
        Document document = collection.find(eq("_id", new ObjectId(id))).first();
        if(document == null){
            System.out.println("No blog found!");
            responseObserver.onError(Status.NOT_FOUND.withDescription("No Blog found")
                    .asRuntimeException());
        }else{
            System.out.println("Blog found");
            Document newDocument = blogToDoc(blog);
            collection.replaceOne(eq("_id", document.getObjectId("_id")), newDocument);
            System.out.println("Updating blog!");

            responseObserver.onNext(UpdateBlogResponse.newBuilder().setBlog(blog).build());

            responseObserver.onCompleted();

        }
    }

    @Override
    public void deleteBlog(DeleteBlogRequest request, StreamObserver<DeleteBlogResponse> responseObserver) {
        String id = request.getId();

        DeleteResult result = collection.deleteOne(eq("_id", new ObjectId(id)));
        System.out.println("Blog deleted!");
        long count = result.getDeletedCount();
        if(count == 0){
            responseObserver.onError(Status.NOT_FOUND.withDescription("Blog not found!").asRuntimeException());
        }

        responseObserver.onNext(DeleteBlogResponse.newBuilder()
                .setCount((int) result.getDeletedCount())
                .setId(id).build());
        responseObserver.onCompleted();
    }

    @Override
    public void listBlog(ListBlogRequest request, StreamObserver<ListBlogResponse> responseObserver) {
        System.out.println("Received list blog request");
        FindIterable<Document> iterable = collection.find();
        for(Document document: iterable){
            ObjectId id = document.getObjectId("_id");
            responseObserver.onNext(ListBlogResponse.newBuilder().setBlog(docToBlog(document, id.toString())).build());
        }
        responseObserver.onCompleted();
    }
}
